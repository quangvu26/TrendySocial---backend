package com.example.trendy_chat.service;

import com.example.trendy_chat.entity.FriendRequest;
import com.example.trendy_chat.entity.User;
import com.example.trendy_chat.repository.FriendRequestRepository;
import com.example.trendy_chat.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@Service
public class FriendService {
    private final FriendRequestRepository friendRepo;
    private final UserRepository userRepo;

    public FriendService(FriendRequestRepository friendRepo, UserRepository userRepo) {
        this.friendRepo = friendRepo;
        this.userRepo = userRepo;
    }

    public Optional<FriendRequest> findById(String id) {
        return friendRepo.findById(id);
    }

    public FriendRequest sendRequest(String from, String to) {
        // Validate inputs
        if (from == null || from.trim().isEmpty()) {
            throw new RuntimeException("maNguoiGui không được để trống");
        }
        if (to == null || to.trim().isEmpty()) {
            throw new RuntimeException("maNguoiNhan không được để trống");
        }
        if (from.equals(to)) {
            throw new RuntimeException("Không thể gửi lời mời cho chính mình");
        }

        // Check users exist
        User fromUser = userRepo.findById(from).orElse(null);
        User toUser = userRepo.findById(to).orElse(null);
        if (fromUser == null || toUser == null) {
            throw new RuntimeException("Người dùng không tồn tại");
        }

        // Check if already friends
        if (areFriends(from, to)) {
            throw new RuntimeException("Bạn đã là bạn bè của người này");
        }

        // avoid duplicate pending request
        if (friendRepo.existsByMaNguoiGuiAndMaNguoiNhan(from, to)) {
            throw new RuntimeException("Yêu cầu đã tồn tại");
        }

        FriendRequest f = new FriendRequest();
        f.setMaYeuCau(UUID.randomUUID().toString());
        f.setMaNguoiGui(from);
        f.setMaNguoiNhan(to);
        f.setTrangThai("CHO_DUYET");
        f.setNgayGui(LocalDateTime.now());
        return friendRepo.save(f);
    }

    public List<FriendRequest> incomingRequests(String userId) {
        return friendRepo.findByMaNguoiNhanAndTrangThai(userId, "CHO_DUYET");
    }

    public List<FriendRequest> outgoingRequests(String userId) {
        return friendRepo.findByMaNguoiGuiAndTrangThai(userId, "CHO_DUYET");
    }

    public List<FriendRequest> allRelations(String userId) {
        return friendRepo.findByMaNguoiGuiOrMaNguoiNhan(userId, userId);
    }


    public int cleanupDuplicateRelations() {
        List<FriendRequest> duplicates = friendRepo.findDuplicateBidirectionalRelations();

        if (duplicates.isEmpty()) {
            System.out.println("✅ No duplicate relations found");
            return 0;
        }

        System.out.println("⚠️ Found " + duplicates.size() + " duplicate relations. Cleaning up...");

        int deleted = 0;
        Set<String> processed = new HashSet<>();

        for (FriendRequest fr : duplicates) {
            String key = fr.getMaNguoiGui() + "-" + fr.getMaNguoiNhan();
            String keyReverse = fr.getMaNguoiNhan() + "-" + fr.getMaNguoiGui();

            // Skip if already processed this pair
            if (processed.contains(key) || processed.contains(keyReverse)) {
                continue;
            }

            // Delete the relation (will delete both directions if using deleteAllBidirectional)
            friendRepo.delete(fr);
            deleted++;
            processed.add(key);

            System.out.println("  🗑️ Deleted duplicate: " + key);
        }

        System.out.println("✅ Cleanup complete. Deleted " + deleted + " relations");
        return deleted;
    }


    public int fixInconsistentRelations() {
        System.out.println("🔍 Checking for inconsistent relations...");

        List<FriendRequest> allRelations = friendRepo.findAll();
        Map<String, List<FriendRequest>> pairMap = new HashMap<>();

        // Group relations by pair (ignore direction)
        for (FriendRequest fr : allRelations) {
            String pair = fr.getMaNguoiGui().compareTo(fr.getMaNguoiNhan()) < 0
                    ? fr.getMaNguoiGui() + "-" + fr.getMaNguoiNhan()
                    : fr.getMaNguoiNhan() + "-" + fr.getMaNguoiGui();

            pairMap.computeIfAbsent(pair, k -> new ArrayList<>()).add(fr);
        }

        int fixed = 0;

        // Fix inconsistencies
        for (Map.Entry<String, List<FriendRequest>> entry : pairMap.entrySet()) {
            List<FriendRequest> relations = entry.getValue();

            if (relations.size() == 2) {
                String status1 = relations.get(0).getTrangThai();
                String status2 = relations.get(1).getTrangThai();

                // If one is XAC_NHAN and other is not, make both XAC_NHAN
                if (status1.equals("XAC_NHAN") && !status2.equals("XAC_NHAN")) {
                    relations.get(1).setTrangThai("XAC_NHAN");
                    friendRepo.save(relations.get(1));
                    fixed++;
                    System.out.println("  ✅ Fixed: " + entry.getKey() + " -> both XAC_NHAN");
                } else if (status2.equals("XAC_NHAN") && !status1.equals("XAC_NHAN")) {
                    relations.get(0).setTrangThai("XAC_NHAN");
                    friendRepo.save(relations.get(0));
                    fixed++;
                    System.out.println("  ✅ Fixed: " + entry.getKey() + " -> both XAC_NHAN");
                }
            }
        }

        System.out.println("✅ Fixed " + fixed + " inconsistent relations");
        return fixed;
    }

    public FriendRequest accept(String maYeuCau) {
        FriendRequest fr = friendRepo.findById(maYeuCau).orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu"));
        fr.setTrangThai("XAC_NHAN");
        return friendRepo.save(fr);
    }

    public FriendRequest reject(String maYeuCau) {
        FriendRequest fr = friendRepo.findById(maYeuCau).orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu"));
        fr.setTrangThai("TU_CHOI");
        return friendRepo.save(fr);
    }


    public void deleteFriendRelation(String userId1, String userId2) {
        if (userId1 == null || userId2 == null) {
            throw new RuntimeException("User IDs không được để trống");
        }

        // Find all relations both directions
        var relations = friendRepo.findByMaNguoiGuiAndMaNguoiNhanOrMaNguoiGuiAndMaNguoiNhan(
                userId1, userId2, userId2, userId1
        ).stream()
                .filter(fr ->
                        (fr.getMaNguoiGui().equals(userId1) && fr.getMaNguoiNhan().equals(userId2)) ||
                                (fr.getMaNguoiGui().equals(userId2) && fr.getMaNguoiNhan().equals(userId1))
                )
                .toList();

        System.out.println("🗑️ Deleting " + relations.size() + " relations between " + userId1 + " and " + userId2);

        // Delete all (both directions)
        if (!relations.isEmpty()) {
            friendRepo.deleteAll(relations);
            System.out.println("✅ Deleted " + relations.size() + " relations");
        }
    }


    public String getFriendStatus(String userId1, String userId2) {
        if (userId1 == null || userId2 == null) {
            return "none";
        }

        // Get all relations between these two users (both directions)
        var relations = friendRepo.findByMaNguoiGuiAndMaNguoiNhanOrMaNguoiGuiAndMaNguoiNhan(
                userId1, userId2, userId2, userId1
        );

        if (relations == null || relations.isEmpty()) {
            return "none";
        }

        // Check if any is XAC_NHAN (accepted friendship)
        boolean hasAccepted = relations.stream()
                .anyMatch(r -> "XAC_NHAN".equals(r.getTrangThai()));

        if (hasAccepted) {
            return "accepted";
        }

        // Check if pending from userId1 perspective
        boolean pendingFromUser1 = relations.stream()
                .anyMatch(r ->
                        r.getMaNguoiGui() != null && r.getMaNguoiNhan() != null &&
                                r.getMaNguoiGui().equals(userId1) &&
                                r.getMaNguoiNhan().equals(userId2) &&
                                "CHO_DUYET".equals(r.getTrangThai())
                );

        // Check if pending from userId2 perspective
        boolean pendingFromUser2 = relations.stream()
                .anyMatch(r ->
                        r.getMaNguoiGui() != null && r.getMaNguoiNhan() != null &&
                                r.getMaNguoiGui().equals(userId2) &&
                                r.getMaNguoiNhan().equals(userId1) &&
                                "CHO_DUYET".equals(r.getTrangThai())
                );

        if (pendingFromUser1) {
            return "pending_from_me";
        }
        if (pendingFromUser2) {
            return "pending_from_them";
        }

        return "none";
    }


    public boolean areFriends(String userId1, String userId2) {
        if (userId1 == null || userId2 == null) {
            return false;
        }

        var relation = friendRepo.findByMaNguoiGuiAndMaNguoiNhanOrMaNguoiGuiAndMaNguoiNhan(
                userId1, userId2, userId2, userId1
        ).stream()
                .filter(fr -> "XAC_NHAN".equals(fr.getTrangThai()))
                .filter(fr ->
                        (fr.getMaNguoiGui().equals(userId1) && fr.getMaNguoiNhan().equals(userId2)) ||
                                (fr.getMaNguoiGui().equals(userId2) && fr.getMaNguoiNhan().equals(userId1))
                )
                .findFirst();

        return relation.isPresent();
    }


    public FriendRequest acceptAndSync(String maYeuCau) {
        FriendRequest fr = friendRepo.findById(maYeuCau)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy yêu cầu"));

        fr.setTrangThai("XAC_NHAN");
        FriendRequest saved = friendRepo.save(fr);
        return fr;
    }


    public long countFriends(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return 0;
        }

        // Count relationships where user is maNguoiGui
        long asGui = friendRepo.countByMaNguoiGuiAndTrangThai(userId, "XAC_NHAN");
        
        // Count relationships where user is maNguoiNhan
        long asNhan = friendRepo.countByMaNguoiNhanAndTrangThai(userId, "XAC_NHAN");
        
        // Total unique friends
        long totalFriends = asGui + asNhan;
        
        System.out.println("✅ Friends count for " + userId + ": " + totalFriends + " (gui:" + asGui + " nhan:" + asNhan + ")");
        return totalFriends;
    }
}
