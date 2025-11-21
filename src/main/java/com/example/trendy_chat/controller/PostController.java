package com.example.trendy_chat.controller;

import com.example.trendy_chat.dto.PostUpdateRequest;
import com.example.trendy_chat.entity.*;
import com.example.trendy_chat.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/trendy/posts")
@CrossOrigin(origins = "*")
public class PostController {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private SavePostRepository savePostRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private SharePostRepository sharePostRepository;

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    private static final String UPLOAD_DIR = "uploads/posts";

    // Create post
    @PostMapping("/create")
    public ResponseEntity<?> createPost(
            @RequestParam String noiDung,
            @RequestParam(required = false) String privacy,
            @RequestParam(required = false) MultipartFile file,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = extractUserIdFromToken(token);
            if (userId == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            System.out.println("✅ Creating post for user: " + userId);
            System.out.println("📝 Privacy: " + privacy);
            System.out.println("📎 File: " + (file != null ? file.getOriginalFilename() : "null"));

            Post post = new Post();
            post.setIdPost(UUID.randomUUID().toString());
            post.setIdUser(userId);
            post.setNoiDung(noiDung);
            String privacyToSave = privacy;
            if ("CONG_KHAI".equalsIgnoreCase(privacy)) privacyToSave = "CONG_KHAI";
            else if ("BAN_BE".equalsIgnoreCase(privacy)) privacyToSave = "BAN_BE";
            else if ("RIENG_TU".equalsIgnoreCase(privacy)) privacyToSave = "RIENG_TU";
            else privacyToSave = "CONG_KHAI";
            post.setCheDoRiengTu(privacyToSave);
            post.setNgayDang(LocalDateTime.now());

            // Handle file upload
            if (file != null && !file.isEmpty()) {
                String fileName = post.getIdPost() + "_" + System.currentTimeMillis() + getFileExtension(file.getOriginalFilename());
                Path uploadPath = Paths.get(UPLOAD_DIR);
                Files.createDirectories(uploadPath);
                Files.write(uploadPath.resolve(fileName), file.getBytes());
                String filePath = "/uploads/posts/" + fileName;
                post.setDuongDanAnh(filePath);
                System.out.println("✅ File saved: " + filePath);
            }

            Post savedPost = postRepository.save(post);
            System.out.println("✅ Post saved: " + savedPost.getIdPost());
            System.out.println("✅ Image path: " + savedPost.getDuongDanAnh());
            
            return ResponseEntity.ok(savedPost);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error creating post: " + e.getMessage());
        }
    }

    // Get posts by userId
    @GetMapping
    public ResponseEntity<?> getPostsByUser(
            @RequestParam String userId,
            @RequestParam(required = false) String viewerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<Post> posts = postRepository.findByIdUserOrderByNgayDangDesc(userId);
            System.out.println("✅ Loaded " + posts.size() + " posts for user: " + userId + ", viewerId: " + viewerId);
            
            if (posts.isEmpty()) {
                System.out.println("⚠️ No posts found for user: " + userId);
                return ResponseEntity.ok(new ArrayList<>());
            }
            
            // Filter posts based on privacy and viewer
            boolean isSameUser = userId.equals(viewerId);
            List<Map<String, Object>> response = new ArrayList<>();
            
            for (Post post : posts) {
                String privacy = post.getCheDoRiengTu();
                
                // Privacy filter logic:
                // - Owner can always see their own posts (RIENG_TU, BAN_BE, CONG_KHAI)
                // - Others can only see CONG_KHAI (public)
                if (!isSameUser && "RIENG_TU".equalsIgnoreCase(privacy)) {
                    System.out.println("🔒 Skipping private post " + post.getIdPost());
                    continue; // Skip private posts for non-owners
                }
                if (!isSameUser && "BAN_BE".equalsIgnoreCase(privacy)) {
                    System.out.println("👥 Skipping friend-only post " + post.getIdPost());
                    continue; // Skip friend-only posts (TODO: check friendship)
                }
                
                Map<String, Object> dto = new HashMap<>();
                dto.put("idPost", post.getIdPost());
                dto.put("idUser", post.getIdUser());
                dto.put("noiDung", post.getNoiDung());
                dto.put("duongDanAnh", post.getDuongDanAnh());
                
                // Map privacy to frontend format
                String privacyDisplay = privacy;
                if ("public".equalsIgnoreCase(privacy)) privacyDisplay = "CONG_KHAI";
                else if ("friends".equalsIgnoreCase(privacy)) privacyDisplay = "BAN_BE";
                else if ("private".equalsIgnoreCase(privacy)) privacyDisplay = "RIENG_TU";
                else privacyDisplay = "CONG_KHAI";
                
                dto.put("cheDoRiengTu", privacyDisplay);
                dto.put("ngayDang", post.getNgayDang());
                
                // Count likes from DB
                long likesCount = postLikeRepository.findByIdPost(post.getIdPost()).size();
                dto.put("likesCount", likesCount);
                dto.put("commentsCount", post.getCommentsCount() != null ? post.getCommentsCount() : 0);
                dto.put("viewsCount", post.getViewsCount() != null ? post.getViewsCount() : 0);
                
                System.out.println("📊 Post " + post.getIdPost() + " likes from DB: " + likesCount);
                
                response.add(dto);
            }
            
            System.out.println("✅ Response size after filtering: " + response.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("❌ Error: " + e.getMessage());
            return ResponseEntity.status(500).body("Error loading posts: " + e.getMessage());
        }
    }

    // Get total likes received by user (sum of all likes on user's posts)
    // MUST be before /{postId} to avoid greedy path matching
    @GetMapping("/user/{userId}/total-likes")
    public ResponseEntity<?> getTotalLikesReceivedByUser(@PathVariable String userId) {
        try {
            // Get all posts by this user
            List<Post> userPosts = postRepository.findByIdUserOrderByNgayDangDesc(userId);
            
            // Sum all likes for these posts
            long totalLikes = 0;
            for (Post post : userPosts) {
                totalLikes += postLikeRepository.findByIdPost(post.getIdPost()).size();
            }
            
            System.out.println("✅ Total likes for user " + userId + ": " + totalLikes);
            return ResponseEntity.ok(Map.of("totalLikes", totalLikes));
        } catch (Exception e) {
            System.err.println("❌ Error getting total likes: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // Get post by ID
    @GetMapping("/{postId}")
    public ResponseEntity<?> getPostById(@PathVariable String postId) {
        try {
            Optional<Post> post = postRepository.findById(postId);
            if (post.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Post p = post.get();
            Map<String, Object> response = new HashMap<>();
            response.put("idPost", p.getIdPost());
            response.put("idUser", p.getIdUser());
            response.put("noiDung", p.getNoiDung());
            response.put("duongDanAnh", p.getDuongDanAnh());
            // Map privacy
            String privacy = p.getCheDoRiengTu();
            if ("public".equalsIgnoreCase(privacy)) privacy = "CONG_KHAI";
            else if ("friends".equalsIgnoreCase(privacy)) privacy = "BAN_BE";
            else if ("private".equalsIgnoreCase(privacy)) privacy = "RIENG_TU";
            response.put("cheDoRiengTu", privacy);
            response.put("ngayDang", p.getNgayDang());
            // Count likes from DB
            long likesCount = postLikeRepository.findByIdPost(postId).size();
            response.put("likesCount", likesCount);
            System.out.println("📊 Post " + postId + " likes from DB: " + likesCount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error loading post");
        }
    }

    // Like post
    @PostMapping("/{postId}/like")
    public ResponseEntity<?> likePost(
            @PathVariable String postId,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = extractUserIdFromToken(token);
            if (userId == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            PostLike.PostLikeId id = new PostLike.PostLikeId(postId, userId);
            int likesCount = 0;
            
            if (!postLikeRepository.existsById(id)) {
                PostLike like = new PostLike();
                like.setIdPost(postId);
                like.setIdUser(userId);
                like.setNgayThich(LocalDateTime.now());
                postLikeRepository.save(like);
                
                // Count total likes
                likesCount = postLikeRepository.findByIdPost(postId).size();
                
                // Update Post.likesCount in database
                Optional<Post> postOpt = postRepository.findById(postId);
                if (postOpt.isPresent()) {
                    Post post = postOpt.get();
                    post.setLikesCount(likesCount);
                    postRepository.save(post);
                    System.out.println("✅ Post liked, updated DB count: " + likesCount);
                }
                
                // Send notification to post owner
                if (postOpt.isPresent()) {
                    Post post = postOpt.get();
                    if (!post.getIdUser().equals(userId)) {
                        try {
                            var userOpt = userRepository.findById(userId);
                            String userName = userId;
                            if (userOpt.isPresent()) {
                                var user = userOpt.get();
                                userName = user.getTen() != null ? user.getTen() : userId;
                            }
                            System.out.println("📢 Should send notification: " + userName + " liked post " + postId);
                        } catch (Exception e) {
                            System.err.println("Failed to send like notification: " + e.getMessage());
                        }
                    }
                }
            } else {
                likesCount = postLikeRepository.findByIdPost(postId).size();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("liked", true);
            response.put("likesCount", likesCount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error liking post");
        }
    }

    // Unlike post
    @DeleteMapping("/{postId}/like")
    public ResponseEntity<?> unlikePost(
            @PathVariable String postId,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = extractUserIdFromToken(token);
            if (userId == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            PostLike.PostLikeId id = new PostLike.PostLikeId(postId, userId);
            if (postLikeRepository.existsById(id)) {
                postLikeRepository.deleteById(id);
                
                // Count remaining likes
                int likesCount = postLikeRepository.findByIdPost(postId).size();
                
                // Update Post.likesCount in database
                Optional<Post> postOpt = postRepository.findById(postId);
                if (postOpt.isPresent()) {
                    Post post = postOpt.get();
                    post.setLikesCount(likesCount);
                    postRepository.save(post);
                    System.out.println("✅ Post unliked, updated DB count: " + likesCount);
                }
            }
            return ResponseEntity.ok("Post unliked");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error unliking post");
        }
    }

    // Save post
    @PostMapping("/{postId}/save")
    public ResponseEntity<?> savePost(
            @PathVariable String postId,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = extractUserIdFromToken(token);
            if (userId == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            SavePost.SavePostId id = new SavePost.SavePostId(postId, userId);
            if (!savePostRepository.existsById(id)) {
                SavePost save = new SavePost();
                save.setIdPost(postId);
                save.setIdUser(userId);
                save.setNgayLuu(LocalDateTime.now());
                savePostRepository.save(save);
            }
            return ResponseEntity.ok("Post saved");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error saving post");
        }
    }

    // Unsave post
    @DeleteMapping("/{postId}/save")
    public ResponseEntity<?> unsavePost(
            @PathVariable String postId,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = extractUserIdFromToken(token);
            if (userId == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            SavePost.SavePostId id = new SavePost.SavePostId(postId, userId);
            savePostRepository.deleteById(id);
            return ResponseEntity.ok("Post unsaved");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error unsaving post");
        }
    }

    // Share post
    @PostMapping("/{postId}/share")
    public ResponseEntity<?> sharePost(
            @PathVariable String postId,
            @RequestParam(required = false) String ghiChu,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = extractUserIdFromToken(token);
            if (userId == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            SharePost share = new SharePost();
            share.setMaChiaSe(UUID.randomUUID().toString());
            share.setIdPost(postId);
            share.setIdUser(userId);
            share.setGhiChu(ghiChu);
            share.setNgayChiaSe(LocalDateTime.now());

            SharePost savedShare = sharePostRepository.save(share);
            return ResponseEntity.ok(savedShare);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error sharing post");
        }
    }

    // Get saved posts
    @GetMapping("/saved")
    public ResponseEntity<?> getSavedPosts(
            @RequestParam String userId) {
        try {
            List<SavePost> savedPosts = savePostRepository.findByIdUserOrderByNgayLuuDesc(userId);
            List<Post> posts = savedPosts.stream()
                    .map(sp -> postRepository.findById(sp.getIdPost()).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error loading saved posts");
        }
    }

    // Get shared posts
    @GetMapping("/shared")
    public ResponseEntity<?> getSharedPosts(
            @RequestParam String userId) {
        try {
            List<SharePost> sharedPosts = sharePostRepository.findByIdUserOrderByNgayChiaSeDesc(userId);
            List<Map<String, Object>> result = new ArrayList<>();

            for (SharePost share : sharedPosts) {
                Optional<Post> post = postRepository.findById(share.getIdPost());
                if (post.isPresent()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("post", post.get());
                    item.put("share", share);
                    result.add(item);
                }
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error loading shared posts");
        }
    }

    // Delete post (permanent delete)
    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(
            @PathVariable String postId,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = extractUserIdFromToken(token);
            if (userId == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            Optional<Post> post = postRepository.findById(postId);
            if (post.isEmpty() || !post.get().getIdUser().equals(userId)) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            // Delete related data first (cascade delete)
            // 1. Delete all likes for this post
            postLikeRepository.deleteAll(postLikeRepository.findByIdPost(postId));
            System.out.println("✅ Deleted all likes for post: " + postId);
            
            // 2. Delete all saves for this post
            savePostRepository.deleteAll(savePostRepository.findByIdPost(postId));
            System.out.println("✅ Deleted all saves for post: " + postId);
            
            // 3. Delete all shares for this post
            sharePostRepository.deleteAll(sharePostRepository.findByIdPost(postId));
            System.out.println("✅ Deleted all shares for post: " + postId);
            
            // Delete comments first (cascade delete)
            List<Comment> comments = commentRepository.findByIdPost(postId);
            for (Comment comment : comments) {
                // Delete comment likes first
                commentLikeRepository.deleteAll(
                    commentLikeRepository.findByMaBinhLuan(comment.getMaBinhLuan())
                );
            }
            commentRepository.deleteAll(comments);
            System.out.println("✅ Deleted all comments for post: " + postId);

            // Finally delete the post
            postRepository.deleteById(postId);
            System.out.println("✅ Post deleted permanently: " + postId);

            return ResponseEntity.ok("Post deleted");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error deleting post: " + e.getMessage());
        }
    }

    // Check if post is liked by user
    @GetMapping("/{postId}/liked")
    public ResponseEntity<?> isPostLiked(
            @PathVariable String postId,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = extractUserIdFromToken(token);
            if (userId == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            boolean liked = postLikeRepository.existsByIdPostAndIdUser(postId, userId);
            Map<String, Object> response = new HashMap<>();
            response.put("liked", liked);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error checking like status");
        }
    }

    // Check if post is saved by user
    @GetMapping("/{postId}/saved")
    public ResponseEntity<?> isPostSaved(
            @PathVariable String postId,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = extractUserIdFromToken(token);
            if (userId == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            boolean saved = savePostRepository.existsByIdPostAndIdUser(postId, userId);
            Map<String, Object> response = new HashMap<>();
            response.put("saved", saved);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error checking save status");
        }
    }

    // Get likes count
    @GetMapping("/{postId}/likes-count")
    public ResponseEntity<?> getLikesCount(@PathVariable String postId) {
        try {
            long count = postLikeRepository.findByIdPost(postId).size();
            Map<String, Object> response = new HashMap<>();
            response.put("count", count);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error getting likes count");
        }
    }

    // Count view when user opens post
    @PostMapping("/{postId}/view")
    public ResponseEntity<?> countPostView(
            @PathVariable String postId,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = extractUserIdFromToken(token);
            if (userId == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            Optional<Post> postOpt = postRepository.findById(postId);
            if (postOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Post not found");
            }

            Post post = postOpt.get();
            // Increment view counter in DB
            post.setViewsCount((post.getViewsCount() != null ? post.getViewsCount() : 0) + 1);
            postRepository.save(post);

            System.out.println("✅ View counted for post: " + postId);
            System.out.println("📊 Total views: " + post.getViewsCount());

            Map<String, Object> response = new HashMap<>();
            response.put("viewsCount", post.getViewsCount());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error counting view: " + e.getMessage());
        }
    }

    // Update post
    @PutMapping("/{postId}")
    public ResponseEntity<?> updatePost(
            @PathVariable String postId,
            @RequestBody PostUpdateRequest request,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = extractUserIdFromToken(token);
            if (userId == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            Optional<Post> postOpt = postRepository.findById(postId);
            if (postOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Post post = postOpt.get();
            if (!post.getIdUser().equals(userId)) {
                return ResponseEntity.status(403).body("Forbidden");
            }

            // Update fields
            if (request.getNoiDung() != null) {
                post.setNoiDung(request.getNoiDung());
            }
            if (request.getCheDoRiengTu() != null) {
                post.setCheDoRiengTu(request.getCheDoRiengTu());
            }

            Post updatedPost = postRepository.save(post);
            return ResponseEntity.ok(updatedPost);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error updating post: " + e.getMessage());
        }
    }


    private String extractUserIdFromToken(String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            String[] parts = token.split("\\.");
            if (parts.length != 3) return null;

            String payload = new String(Base64.getDecoder().decode(parts[1]));
            // Parse JSON manually or use a library
            if (payload.contains("\"sub\":")) {
                int start = payload.indexOf("\"sub\":\"") + 7;
                int end = payload.indexOf("\"", start);
                return payload.substring(start, end);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return "." + fileName.substring(fileName.lastIndexOf(".") + 1);
        }
        return "";
    }
}
