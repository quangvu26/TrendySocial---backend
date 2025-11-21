package com.example.trendy_chat.controller;

import com.example.trendy_chat.entity.FriendRequest;
import com.example.trendy_chat.entity.User;
import com.example.trendy_chat.repository.UserRepository;
import com.example.trendy_chat.service.FriendService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/trendy/friends")
public class FriendController {
    private final FriendService friendService;
    private final UserRepository userRepository;

    public FriendController(FriendService friendService, UserRepository userRepository) {
        this.friendService = friendService;
        this.userRepository = userRepository;
    }

    @PostMapping("/request")
    public ResponseEntity<?> sendRequest(@RequestBody Map<String, String> body) {
        // Accept both "from/to" and "fromUserId/toUserId" for compatibility
        String from = body.get("from") != null ? body.get("from") : body.get("fromUserId");
        String to = body.get("to") != null ? body.get("to") : body.get("toUserId");

        if (from == null || from.trim().isEmpty() || to == null || to.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("fromUserId/toUserId không được để trống");
        }

        try {
            FriendRequest fr = friendService.sendRequest(from, to);
            // Return friend status after sending request
            String status = friendService.getFriendStatus(from, to);
            return ResponseEntity.ok(Map.of(
                "message", "Đã gửi lời mời kết bạn",
                "status", status,
                "friendRequest", fr
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/incoming")
    public ResponseEntity<?> incoming(@RequestParam String userId) {
        List<FriendRequest> list = friendService.incomingRequests(userId);
        // map with sender info
        var data = list.stream().map(fr -> Map.of(
                "maYeuCau", fr.getMaYeuCau(),
                "from", fr.getMaNguoiGui(),
                "ngayGui", fr.getNgayGui()
        )).collect(Collectors.toList());
        return ResponseEntity.ok(data);
    }

    @GetMapping("/outgoing")
    public ResponseEntity<?> outgoing(@RequestParam String userId) {
        List<FriendRequest> list = friendService.outgoingRequests(userId);
        var data = list.stream().map(fr -> Map.of(
                "maYeuCau", fr.getMaYeuCau(),
                "to", fr.getMaNguoiNhan(),
                "ngayGui", fr.getNgayGui()
        )).collect(Collectors.toList());
        return ResponseEntity.ok(data);
    }

    @GetMapping("/relations")
    public ResponseEntity<?> relations(@RequestParam String userId) {
        try {
            List<FriendRequest> list = friendService.allRelations(userId);
            var data = list.stream().map(fr -> Map.of(
                "maYeuCau", fr.getMaYeuCau(),
                "maNguoiGui", fr.getMaNguoiGui(),
                "maNguoiNhan", fr.getMaNguoiNhan(),
                "trangThai", fr.getTrangThai(),
                "ngayGui", fr.getNgayGui(),
                "other", fr.getMaNguoiGui().equals(userId) ? fr.getMaNguoiNhan() : fr.getMaNguoiGui()
            )).collect(Collectors.toList());
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

    @PostMapping("/{maYeuCau}/accept")
    public ResponseEntity<?> accept(@PathVariable String maYeuCau) {
        try {
            FriendRequest fr = friendService.findById(maYeuCau)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu"));
            
            // Check if already accepted
            if ("XAC_NHAN".equals(fr.getTrangThai())) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Yêu cầu này đã được chấp nhận"
                ));
            }
            
            // Accept and sync all related requests
            FriendRequest result = friendService.acceptAndSync(maYeuCau);
            
            return ResponseEntity.ok(Map.of(
                "message", "Đã chấp nhận lời mời kết bạn",
                "status", "accepted",
                "friendRequest", result
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{maYeuCau}/reject")
    public ResponseEntity<?> reject(@PathVariable String maYeuCau) {
        FriendRequest fr = friendService.reject(maYeuCau);
        return ResponseEntity.ok(Map.of(
            "message", "Đã từ chối lời mời kết bạn",
            "status", "rejected"
        ));
    }

    /**
     * Get friend status between two users
     * Returns: "none", "pending_from_me", "pending_from_them", "accepted"
     */
    @GetMapping("/status")
    public ResponseEntity<?> checkFriendStatus(@RequestParam String userId1, @RequestParam String userId2) {
        try {
            System.out.println("📥 Getting friend status between: " + userId1 + " <-> " + userId2);
            
            String status = friendService.getFriendStatus(userId1, userId2);
            
            HashMap<String, Object> response = new HashMap<>();
            response.put("userId1", userId1);
            response.put("userId2", userId2);
            response.put("status", status);
            
            System.out.println("✅ Friend status: " + status);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ Error getting friend status: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    public ResponseEntity<?> unfriend(@RequestParam String userId1, @RequestParam String userId2) {
        try {
            System.out.println("🔄 Unfriend request: " + userId1 + " <-> " + userId2);
            
            friendService.deleteFriendRelation(userId1, userId2);
            
            System.out.println("✅ Unfriended successfully");
            return ResponseEntity.ok(Map.of(
                "message", "Đã hủy kết bạn thành công",
                "status", "none"
            ));
        } catch (RuntimeException e) {
            System.err.println("❌ Unfriend error: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/group/{groupId}/add-members")
    public ResponseEntity<?> getFriendStatus(
            @RequestParam String userId1,
            @RequestParam String userId2) {
        try {
            String status = friendService.getFriendStatus(userId1, userId2);
            return ResponseEntity.ok(Map.of(
                "status", status,
                "areFriends", "accepted".equals(status)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Check if two users are friends
     */
    @GetMapping("/check")
    public ResponseEntity<?> checkFriends(
            @RequestParam String userId1,
            @RequestParam String userId2) {
        try {
            boolean areFriends = friendService.areFriends(userId1, userId2);
            return ResponseEntity.ok(Map.of("areFriends", areFriends));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/cleanup")
    public ResponseEntity<?> cleanupDuplicates() {
        try {
            System.out.println("🔧 Starting friend relations cleanup...");
            
            // Step 1: Fix inconsistent relations
            int fixed = friendService.fixInconsistentRelations();
            
            // Step 2: Cleanup duplicates
            int deleted = friendService.cleanupDuplicateRelations();
            
            return ResponseEntity.ok(Map.of(
                "message", "Cleanup completed",
                "fixed", fixed,
                "deleted", deleted
            ));
        } catch (Exception e) {
            System.err.println("❌ Cleanup failed: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> getFriendsList(@RequestParam String userId) {
        try {
            // Get all accepted friend relationships
            List<FriendRequest> acceptedFriends = friendService.allRelations(userId).stream()
                    .filter(fr -> "XAC_NHAN".equals(fr.getTrangThai()))
                    .toList();

            // Map to user info
            var friendsList = acceptedFriends.stream().map(fr -> {
                String friendId = fr.getMaNguoiGui().equals(userId) ? 
                    fr.getMaNguoiNhan() : fr.getMaNguoiGui();
                
                // Get friend user info
                User friend = userRepository.findById(friendId).orElse(null);
                if (friend == null) {
                    return Map.of(
                        "id", friendId,
                        "name", friendId,
                        "isFriend", true
                    );
                }
                
                return Map.of(
                    "id", friend.getId(),
                    "name", friend.getTen(),
                    "email", friend.getEmail(),
                    "avatar", friend.getAvatar() != null ? friend.getAvatar() : "",
                    "gioiTinh", friend.getGioiTinh(),
                    "isFriend", true
                );
            }).collect(Collectors.toList());

            return ResponseEntity.ok(friendsList);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingRequests(@RequestParam String userId) {
        try {
            // Get pending friend requests where current user is receiver
            List<FriendRequest> pendingRequests = friendService.incomingRequests(userId);

            // Map with sender info
            var data = pendingRequests.stream().map(fr -> {
                // Try to get sender user info
                User sender = userRepository.findById(fr.getMaNguoiGui()).orElse(null);
                
                return Map.of(
                    "maYeuCau", fr.getMaYeuCau(),
                    "from", fr.getMaNguoiGui(),
                    "maNguoiGui", fr.getMaNguoiGui(),
                    "maNguoiNhan", fr.getMaNguoiNhan(),
                    "ngayGui", fr.getNgayGui(),
                    "trangThai", fr.getTrangThai(),
                    "senderName", sender != null ? sender.getTen() : fr.getMaNguoiGui()
                );
            }).collect(Collectors.toList());

            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

    /**
     * Count friends with XAC_NHAN status
     * Only counts one direction to avoid duplication
     */
    @GetMapping("/count/{userId}")
    public ResponseEntity<?> countFriends(@PathVariable String userId) {
        try {
            // Get all relationships for this user
            List<FriendRequest> allRelations = friendService.allRelations(userId);
            
            // Collect unique friend IDs from XAC_NHAN relationships
            Set<String> uniqueFriends = new HashSet<>();
            
            for (FriendRequest relation : allRelations) {
                if (!"XAC_NHAN".equals(relation.getTrangThai())) continue;
                
                if (relation.getMaNguoiGui().equals(userId)) {
                    uniqueFriends.add(relation.getMaNguoiNhan());
                } else if (relation.getMaNguoiNhan().equals(userId)) {
                    uniqueFriends.add(relation.getMaNguoiGui());
                }
            }
            
            long friendsCount = uniqueFriends.size();
            System.out.println("✅ Friends count for " + userId + ": " + friendsCount);
            return ResponseEntity.ok(Map.of("friendsCount", friendsCount));
        } catch (Exception e) {
            System.err.println("❌ Error counting friends: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
