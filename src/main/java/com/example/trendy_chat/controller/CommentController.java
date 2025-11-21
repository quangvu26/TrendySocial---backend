package com.example.trendy_chat.controller;

import com.example.trendy_chat.entity.Comment;
import com.example.trendy_chat.entity.CommentLike;
import com.example.trendy_chat.entity.Post;
import com.example.trendy_chat.repository.CommentRepository;
import com.example.trendy_chat.repository.CommentLikeRepository;
import com.example.trendy_chat.repository.PostRepository;
import com.example.trendy_chat.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/trendy/posts")
@CrossOrigin(origins = "*")
public class CommentController {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private CommentService commentService;

    // Get comments for a post (only parent comments, excluding hidden)
    @GetMapping("/{postId}/comments")
    public ResponseEntity<?> getComments(@PathVariable String postId) {
        try {
            List<Comment> comments = commentRepository.findByIdPostAndMaTraLoiIsNullAndAnBinhLuanFalseOrderByNgayBinhLuanDesc(postId);
            
            // Map comments with user info and like counts, including replies
            List<Map<String, Object>> response = comments.stream().map(comment -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", comment.getMaBinhLuan());
                dto.put("idComment", comment.getMaBinhLuan());
                dto.put("authorName", comment.getIdUser());
                dto.put("authorId", comment.getIdUser());
                dto.put("content", comment.getNoiDung());
                dto.put("createdAt", comment.getNgayBinhLuan());
                dto.put("likes", commentLikeRepository.countByMaBinhLuan(comment.getMaBinhLuan()));
                dto.put("maTraLoi", comment.getMaTraLoi());
                
                // Load replies for this comment
                List<Comment> replies = commentRepository.findByMaTraLoiAndAnBinhLuanFalse(comment.getMaBinhLuan());
                List<Map<String, Object>> repliesDtos = replies.stream().map(reply -> {
                    Map<String, Object> replyDto = new HashMap<>();
                    replyDto.put("id", reply.getMaBinhLuan());
                    replyDto.put("idComment", reply.getMaBinhLuan());
                    replyDto.put("authorName", reply.getIdUser());
                    replyDto.put("authorId", reply.getIdUser());
                    replyDto.put("content", reply.getNoiDung());
                    replyDto.put("createdAt", reply.getNgayBinhLuan());
                    replyDto.put("likes", commentLikeRepository.countByMaBinhLuan(reply.getMaBinhLuan()));
                    replyDto.put("maTraLoi", reply.getMaTraLoi());
                    return replyDto;
                }).collect(Collectors.toList());
                
                dto.put("replies", repliesDtos);
                return dto;
            }).collect(Collectors.toList());
            
            System.out.println(" Loaded " + response.size() + " parent comments for post: " + postId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error loading comments: " + e.getMessage());
        }
    }

    // Add comment to post
    @PostMapping("/{postId}/comment")
    public ResponseEntity<?> addComment(
            @PathVariable String postId,
            @RequestBody Map<String, String> request,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = extractUserIdFromToken(token);
            if (userId == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            String content = request.get("content");
            String replyToId = request.get("replyToId");  // Parent comment ID for replies
            String replyToContent = request.get("replyToContent");  // Parent comment content
            String replyToSender = request.get("replyToSender");    // Parent comment author
            
            if (content == null || content.trim().isEmpty()) {
                return ResponseEntity.status(400).body("Comment content cannot be empty");
            }

            // Verify post exists
            Optional<Post> postOpt = postRepository.findById(postId);
            if (postOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Post not found");
            }

            Comment comment = new Comment();
            comment.setMaBinhLuan(UUID.randomUUID().toString());
            comment.setIdPost(postId);
            comment.setIdUser(userId);
            comment.setNoiDung(content);
            comment.setNgayBinhLuan(LocalDateTime.now());
            
            // Set parent comment if this is a reply
            if (replyToId != null && !replyToId.isEmpty()) {
                comment.setMaTraLoi(replyToId);
                System.out.println(" Saving reply comment:");
                System.out.println("   - ReplyToId: " + replyToId);
                System.out.println("   - ReplyToSender: " + replyToSender);
                System.out.println("   - ReplyToContent: " + replyToContent);
                System.out.println("   - Reply content: " + content);
            }

            Comment savedComment = commentRepository.save(comment);
            
            // Update commentsCount in post (only for top-level comments)
            if (replyToId == null || replyToId.isEmpty()) {
                Post post = postOpt.get();
                post.setCommentsCount((post.getCommentsCount() != null ? post.getCommentsCount() : 0) + 1);
                postRepository.save(post);
                System.out.println("✅ Comments count updated: " + post.getCommentsCount());
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", savedComment.getMaBinhLuan());
            response.put("idComment", savedComment.getMaBinhLuan());
            response.put("authorName", userId);
            response.put("authorId", userId);
            response.put("content", savedComment.getNoiDung());
            response.put("createdAt", savedComment.getNgayBinhLuan());
            response.put("likes", 0);
            response.put("maTraLoi", savedComment.getMaTraLoi());
            
            System.out.println("✅ Comment added: " + savedComment.getMaBinhLuan());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error adding comment: " + e.getMessage());
        }
    }

    // Get replies for a comment (excluding hidden)
    @GetMapping("/{postId}/comment/{commentId}/replies")
    public ResponseEntity<?> getCommentReplies(@PathVariable String commentId) {
        try {
            List<Comment> replies = commentRepository.findByMaTraLoiAndAnBinhLuanFalse(commentId);
            
            List<Map<String, Object>> response = replies.stream().map(reply -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", reply.getMaBinhLuan());
                dto.put("idComment", reply.getMaBinhLuan());
                dto.put("authorName", reply.getIdUser());
                dto.put("authorId", reply.getIdUser());
                dto.put("content", reply.getNoiDung());
                dto.put("createdAt", reply.getNgayBinhLuan());
                dto.put("likes", commentLikeRepository.countByMaBinhLuan(reply.getMaBinhLuan()));
                dto.put("maTraLoi", reply.getMaTraLoi());
                return dto;
            }).collect(Collectors.toList());
            
            System.out.println("✅ Loaded " + response.size() + " replies for comment: " + commentId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error loading replies: " + e.getMessage());
        }
    }

    // ...existing methods...
    @PostMapping("/{postId}/comment/{commentId}/like")
    public ResponseEntity<?> likeComment(
            @PathVariable String postId,
            @PathVariable String commentId,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = extractUserIdFromToken(token);
            if (userId == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            // Verify comment exists
            if (!commentRepository.existsById(commentId)) {
                return ResponseEntity.status(404).body("Comment not found");
            }

            commentService.likeComment(commentId, userId);
            return ResponseEntity.ok("Comment liked");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error liking comment: " + e.getMessage());
        }
    }

    // Unlike comment
    @DeleteMapping("/{postId}/comment/{commentId}/like")
    public ResponseEntity<?> unlikeComment(
            @PathVariable String postId,
            @PathVariable String commentId,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = extractUserIdFromToken(token);
            if (userId == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            commentService.unlikeComment(commentId, userId);
            return ResponseEntity.ok("Comment unliked");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error unliking comment: " + e.getMessage());
        }
    }

    // Check if comment is liked by user
    @GetMapping("/{postId}/comment/{commentId}/liked")
    public ResponseEntity<?> isCommentLiked(
            @PathVariable String postId,
            @PathVariable String commentId,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = extractUserIdFromToken(token);
            if (userId == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            boolean liked = commentService.isCommentLikedByUser(commentId, userId);
            Map<String, Object> response = new HashMap<>();
            response.put("liked", liked);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error checking like status");
        }
    }

    // Get comment likes count
    @GetMapping("/{postId}/comment/{commentId}/likes")
    public ResponseEntity<?> getCommentLikesCount(
            @PathVariable String postId,
            @PathVariable String commentId) {
        try {
            long likesCount = commentLikeRepository.countByMaBinhLuan(commentId);
            Map<String, Object> response = new HashMap<>();
            response.put("likesCount", likesCount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error getting likes count");
        }
    }

    // Delete comment
    @DeleteMapping("/{postId}/comment/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable String postId,
            @PathVariable String commentId,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = extractUserIdFromToken(token);
            if (userId == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            Optional<Comment> commentOpt = commentRepository.findById(commentId);
            if (commentOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Comment not found");
            }

            Comment comment = commentOpt.get();
            
            // Check if user is comment author
            if (!comment.getIdUser().equals(userId)) {
                // Check if user is post owner
                Optional<Post> postOpt = postRepository.findById(postId);
                if (postOpt.isEmpty() || !postOpt.get().getIdUser().equals(userId)) {
                    return ResponseEntity.status(403).body("Forbidden - only comment author or post owner can delete");
                }
            }

            // Check if this is a top-level comment (no parent)
            boolean isTopLevel = comment.getMaTraLoi() == null || comment.getMaTraLoi().isEmpty();
            
            // Delete comment with all its replies and likes
            commentService.deleteCommentWithReplies(commentId);
            
            // Update commentsCount in post (only for top-level comments)
            if (isTopLevel) {
                Optional<Post> postOpt = postRepository.findById(postId);
                if (postOpt.isPresent()) {
                    Post post = postOpt.get();
                    post.setCommentsCount(Math.max(0, (post.getCommentsCount() != null ? post.getCommentsCount() : 0) - 1));
                    postRepository.save(post);
                    System.out.println("✅ Comments count updated: " + post.getCommentsCount());
                }
            }
            
            System.out.println("✅ Comment deleted: " + commentId);
            return ResponseEntity.ok("Comment deleted");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error deleting comment: " + e.getMessage());
        }
    }

    // Hide comment (mark as hidden, only user can see it)
    @PostMapping("/{postId}/comment/{commentId}/hide")
    public ResponseEntity<?> hideComment(
            @PathVariable String postId,
            @PathVariable String commentId,
            @RequestHeader("Authorization") String token) {
        try {
            String userId = extractUserIdFromToken(token);
            if (userId == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            Optional<Comment> commentOpt = commentRepository.findById(commentId);
            if (commentOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Comment not found");
            }

            Comment comment = commentOpt.get();
            
            // Mark as hidden
            comment.setAnBinhLuan(true);
            commentRepository.save(comment);
            
            System.out.println("✅ Comment hidden: " + commentId);
            return ResponseEntity.ok("Comment hidden");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error hiding comment: " + e.getMessage());
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
}
