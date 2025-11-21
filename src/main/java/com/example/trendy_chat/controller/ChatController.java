package com.example.trendy_chat.controller;

import com.example.trendy_chat.dto.SoloChatDTO;
import com.example.trendy_chat.dto.GroupChatDTO;
import com.example.trendy_chat.dto.PrivateMessage;
import com.example.trendy_chat.dto.GroupMessage;
import com.example.trendy_chat.entity.*;
import com.example.trendy_chat.repository.*;
import com.example.trendy_chat.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/trendy/chat")
public class ChatController {
    
    @Autowired
    private SoloChatRepository soloChatRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private GroupChatRepository groupChatRepository;
    
    @Autowired
    private ThanhVienNhomRepository thanhVienNhomRepository;
    
    @Autowired
    private ChatService chatService;
    
    @Autowired
    private TinNhanReactionRepository reactionRepo;

    
    @Autowired
    private TinNhanCaNhanRepository privateRepo;

    @Autowired
    private ChattingGroupRepository groupRepo;

    @Autowired
    private NotificationRepository notificationRepo;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
  
    @GetMapping("/solo")
    public ResponseEntity<?> getSoloChats(@RequestParam String userId) {
        try {
            List<SoloChat> soloChats = soloChatRepository.findByUserId(userId);
            
            // Convert to DTO with other user info and latest message
            List<SoloChatDTO> dtos = soloChats.stream().map(sc -> {
                String otherUserId = sc.getId_user_1().equals(userId) ? 
                    sc.getId_user_2() : sc.getId_user_1();
                
                try {
                    User otherUser = userRepository.findById(otherUserId)
                        .orElse(null);
                    SoloChatDTO dto = SoloChatDTO.fromEntity(sc, userId, otherUser);
                    
                    // Get latest message
                    try {
                        var latestMsg = privateRepo
                            .findTopByMaNhomSoloOrderByNgayGuiDesc(sc.getIdSoloChat());
                        if (latestMsg.isPresent()) {
                            dto.setLastMessage(latestMsg.get().getNoiDung());
                            dto.setNgayGui(latestMsg.get().getNgayGui() != null ? 
                                latestMsg.get().getNgayGui().toString() : "");
                        }
                    } catch (Exception e) {
                        System.out.println("Failed to get last message for solo chat: " + e.getMessage());
                    }
                    
                    return dto;
                } catch (Exception e) {
                    return SoloChatDTO.fromEntity(sc, userId, null);
                }
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Get or create solo chat between 2 users
     * POST /trendy/chat/solo?userId1=...&userId2=...
     */
    @PostMapping("/solo")
    public ResponseEntity<?> getOrCreateSoloChat(@RequestParam String userId1, @RequestParam String userId2) {
        try {
            // Check if chat exists
            var existing = soloChatRepository.findBetweenUsers(userId1, userId2);
            
            SoloChat soloChat;
            if (existing.isPresent()) {
                soloChat = existing.get();
            } else {
                // Create new solo chat
                soloChat = new SoloChat();
                soloChat.setId_user_1(userId1);
                soloChat.setId_user_2(userId2);
                soloChat = soloChatRepository.save(soloChat);
            }
            
            // Return DTO
            String otherUserId = soloChat.getId_user_1().equals(userId1) ? 
                soloChat.getId_user_2() : soloChat.getId_user_1();
            User otherUser = userRepository.findById(otherUserId).orElse(null);
            
            return ResponseEntity.ok(SoloChatDTO.fromEntity(soloChat, userId1, otherUser));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Get all group chats for a user
     * GET /trendy/chat/group?userId=...
     */
    @GetMapping("/group")
    public ResponseEntity<?> getGroupChats(@RequestParam String userId) {
        try {
            // Get all groups where user is a member
            List<GroupChat> groups = groupChatRepository.findAll().stream()
                .filter(g -> thanhVienNhomRepository.existsByMaNhomAndIdUser(g.getMaNhom(), userId))
                .collect(Collectors.toList());
            
            // Convert to DTO with latest message
            List<GroupChatDTO> dtos = groups.stream()
                .map(g -> {
                    GroupChatDTO dto = GroupChatDTO.fromEntity(g);
                    // Get latest message
                    try {
                        var latestMsg = groupRepo
                            .findTopByMaNhomOrderByNgayGuiDesc(g.getMaNhom());
                        if (latestMsg.isPresent()) {
                            dto.setLastMessage(latestMsg.get().getNoiDung());
                            dto.setNgayGui(latestMsg.get().getNgayGui() != null ? 
                                latestMsg.get().getNgayGui().toString() : "");
                        }
                    } catch (Exception e) {
                        System.out.println("Failed to get last message: " + e.getMessage());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Create new group chat
     * POST /trendy/chat/group/create?tenNhom=...&userId=...&memberIds=id1,id2,id3
     */
    @PostMapping("/group/create")
    public ResponseEntity<?> createGroup(@RequestParam(required = true) String tenNhom,
                                         @RequestParam(required = true) String userId,
                                         @RequestParam(required = false) String moTa,
                                         @RequestParam(required = false) String memberIds) {
        try {
            // Validate inputs
            if (tenNhom == null || tenNhom.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Tên nhóm không được để trống"));
            }
            if (userId == null || userId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "User ID không được để trống"));
            }
            
            // Check user exists
            User creator = userRepository.findById(userId).orElse(null);
            if (creator == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Người dùng không tồn tại"));
            }

            // Create group
            GroupChat g = new GroupChat();
            g.setMaNhom(UUID.randomUUID().toString());
            g.setTenNhom(tenNhom.trim());
            g.setMoTa(moTa != null ? moTa.trim() : null);
            g.setNguoiTao(userId);
            g.setNgayTao(LocalDateTime.now());

            GroupChat savedGroup = groupChatRepository.save(g);
            System.out.println("✅ Group created: " + savedGroup.getMaNhom());

            // Add creator as ADMIN member
            ThanhVienNhom creator_member = new ThanhVienNhom();
            creator_member.setMaNhom(savedGroup.getMaNhom());
            creator_member.setIdUser(userId);
            creator_member.setVaiTro("ADMIN");
            creator_member.setNgayThamGia(LocalDateTime.now());
            thanhVienNhomRepository.save(creator_member);
            System.out.println("✅ Creator added as ADMIN: " + userId);

            // Parse and add other members if provided (comma-separated)
            int addedMemberCount = 0;
            if (memberIds != null && !memberIds.trim().isEmpty()) {
                String[] memberIdArray = memberIds.split(",");
                for (String memberId : memberIdArray) {
                    memberId = memberId.trim();
                    if (memberId.isEmpty() || memberId.equals(userId)) continue; // Skip creator
                    
                    // Check if member exists
                    User member = userRepository.findById(memberId).orElse(null);
                    if (member == null) {
                        System.out.println("⚠️ Member not found, skipping: " + memberId);
                        continue;
                    }

                    ThanhVienNhom m = new ThanhVienNhom();
                    m.setMaNhom(savedGroup.getMaNhom());
                    m.setIdUser(memberId);
                    m.setVaiTro("THANH_VIEN");
                    m.setNgayThamGia(LocalDateTime.now());
                    thanhVienNhomRepository.save(m);
                    addedMemberCount++;
                    System.out.println("✅ Member added: " + memberId);
                    // Create notification for added member
                    try {
                        Notification n = new Notification();
                        n.setIdThongBao(UUID.randomUUID().toString());
                        n.setIdNguoiNhan(memberId);
                        n.setType("THEM_VAO_NHOM");
                        n.setNoiDung(userId + " đã thêm bạn vào nhóm " + g.getTenNhom());
                        n.setLienKet("/group/" + g.getMaNhom());
                        n.setTrangThai(false);
                        n.setNgayTao(LocalDateTime.now());
                        notificationRepo.save(n);
                        try { messagingTemplate.convertAndSend("/topic/notification." + memberId, n);} catch(Exception ig) {}
                    } catch (Exception ignore) {}
                }
            }
            // Create a notification for the creator (optional summary)
            try {
                Notification n = new Notification();
                n.setIdThongBao(UUID.randomUUID().toString());
                n.setIdNguoiNhan(userId);
                n.setType("TAO_NHOM");
                n.setNoiDung("Bạn đã tạo nhóm " + g.getTenNhom());
                n.setLienKet("/group/" + g.getMaNhom());
                n.setTrangThai(false);
                n.setNgayTao(LocalDateTime.now());
                notificationRepo.save(n);
                try { messagingTemplate.convertAndSend("/topic/notification." + userId, n);} catch(Exception ig) {}
            } catch (Exception ignore) {}

            // Return group info with member count
            Map<String, Object> response = new HashMap<>();
            response.put("maNhom", savedGroup.getMaNhom());
            response.put("tenNhom", savedGroup.getTenNhom());
            response.put("moTa", savedGroup.getMoTa());
            response.put("nguoiTao", savedGroup.getNguoiTao());
            response.put("ngayTao", savedGroup.getNgayTao());
            response.put("memberCount", addedMemberCount + 1); // +1 for creator

            System.out.println("✅ Group creation complete. Total members: " + (addedMemberCount + 1));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Lỗi tạo nhóm: " + e.getMessage()));
        }
    }

    /**
     * Add member to group
     * POST /trendy/chat/group/{maNhom}/add
     */
    @PostMapping("/group/{maNhom}/add")
    public ResponseEntity<?> addMember(@PathVariable String maNhom,
                                       @RequestParam String idUser) {
        try {
            // Validate inputs
            if (maNhom == null || maNhom.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Mã nhóm không được để trống");
            }
            if (idUser == null || idUser.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("ID người dùng không được để trống");
            }

            // Check if group exists
            GroupChat group = groupChatRepository.findById(maNhom).orElse(null);
            if (group == null) {
                return ResponseEntity.badRequest().body("Nhóm không tồn tại");
            }

            // Check if user exists
            User user = userRepository.findById(idUser).orElse(null);
            if (user == null) {
                return ResponseEntity.badRequest().body("Người dùng không tồn tại");
            }

            // Check if already a member
            if (thanhVienNhomRepository.existsByMaNhomAndIdUser(maNhom, idUser)) {
                return ResponseEntity.badRequest().body("User đã trong nhóm");
            }

            ThanhVienNhom m = new ThanhVienNhom();
            m.setMaNhom(maNhom);
            m.setIdUser(idUser);
            m.setVaiTro("THANH_VIEN");
            m.setNgayThamGia(LocalDateTime.now());

            thanhVienNhomRepository.save(m);
            // Create notification for added member
            try {
                Notification n = new Notification();
                n.setIdThongBao(UUID.randomUUID().toString());
                n.setIdNguoiNhan(idUser);
                n.setType("THEM_VAO_NHOM");
                n.setNoiDung(group.getNguoiTao() + " đã thêm bạn vào nhóm " + group.getTenNhom());
                n.setLienKet("/group/" + group.getMaNhom());
                n.setTrangThai(false);
                n.setNgayTao(LocalDateTime.now());
                notificationRepo.save(n);
                try { messagingTemplate.convertAndSend("/topic/notification." + idUser, n);} catch(Exception ig) {}
            } catch (Exception ignore) {}
            return ResponseEntity.ok(Map.of("message", "Đã thêm user vào nhóm"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi thêm thành viên: " + e.getMessage());
        }
    }

    /**
     * Add multiple members to group
     * POST /trendy/chat/group/{maNhom}/add-members
     */
    @PostMapping("/group/{maNhom}/add-members")
    public ResponseEntity<?> addGroupMembers(
        @PathVariable String maNhom,
        @RequestBody Map<String, Object> body) {
      try {
        @SuppressWarnings("unchecked")
        List<String> memberIds = (List<String>) body.get("memberIds");

        if (memberIds == null || memberIds.isEmpty()) {
          return ResponseEntity.badRequest().body("No members to add");
        }

        GroupChat group = groupChatRepository.findById(maNhom)
            .orElseThrow(() -> new RuntimeException("Group not found"));

        int addedCount = 0;
        for (String memberId : memberIds) {
          // Check if already member
          if (thanhVienNhomRepository.existsByMaNhomAndIdUser(maNhom, memberId)) {
            continue;
          }

          ThanhVienNhom member = new ThanhVienNhom();
          member.setMaNhom(maNhom);
          member.setIdUser(memberId);
          member.setVaiTro("THANH_VIEN");
          member.setNgayThamGia(LocalDateTime.now());

          thanhVienNhomRepository.save(member);
          addedCount++;

          // Notify new member
          try {
            Notification n = new Notification();
            n.setIdThongBao(UUID.randomUUID().toString());
            n.setIdNguoiNhan(memberId);
            n.setType("THEM_VAO_NHOM");
            n.setNoiDung("Bạn đã được thêm vào nhóm " + group.getTenNhom());
            n.setLienKet("/group/" + maNhom);
            n.setTrangThai(false);
            n.setNgayTao(LocalDateTime.now());
            notificationRepo.save(n);
            messagingTemplate.convertAndSend("/topic/notification." + memberId, n);
          } catch (Exception ignore) {}
        }

        return ResponseEntity.ok(Map.of(
            "message", "Added " + addedCount + " members",
            "count", addedCount
        ));
      } catch (Exception e) {
        return ResponseEntity.status(500).body("Error: " + e.getMessage());
      }
    }
    
    /**
     * Send private message via REST
     * POST /trendy/chat/private/send
     */
    @PostMapping("/private/send")
    public ResponseEntity<?> sendPrivate(@RequestBody PrivateMessage dto) {
        try {
            TinNhanCaNhan message = chatService.savePrivateMessage(dto);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    /**
     * Get private chat history
     * GET /trendy/chat/private/history?userA=...&userB=...
     */
    @DeleteMapping("/message/{messageId}")
    public ResponseEntity<?> deleteMessage(
        @PathVariable String messageId,
        @RequestParam String userId) {
      try {
        System.out.println("🗑️ Deleting message: " + messageId);
        
        Optional<TinNhanCaNhan> msgOpt = privateRepo.findById(messageId);
        if (msgOpt.isPresent()) {
          TinNhanCaNhan msg = msgOpt.get();
          if (!msg.getMaNguoiGui().equals(userId)) {
            return ResponseEntity.status(403).body("Cannot delete message");
          }
          privateRepo.deleteById(messageId);
        }
        
        return ResponseEntity.ok(Map.of("message", "Message deleted"));
      } catch (Exception e) {
        return ResponseEntity.status(500).body("Error: " + e.getMessage());
      }
    }

    // ...existing code...

    @PostMapping("/message/{messageId}/hide")
    public ResponseEntity<?> hideMessage(@PathVariable String messageId, @RequestParam String userId) {
        try {
            System.out.println("👁️ Hide message: " + messageId + " for user: " + userId);
            
            // Mark message as deleted (hidden) for the user
            Optional<TinNhanCaNhan> msgOpt = privateRepo.findById(messageId);
            if (msgOpt.isPresent()) {
                TinNhanCaNhan msg = msgOpt.get();
                privateRepo.save(msg);
            }
            
            System.out.println("✅ Message hidden");
            return ResponseEntity.ok(Map.of("success", true, "message", "Message hidden"));
        } catch (Exception e) {
            System.err.println("❌ Hide error: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Hide message helper - đã bị xóa, dùng hideGroupMessagePermanent thay thế

    @PostMapping("/message/react")
    public ResponseEntity<?> getPrivateHistory(@RequestParam String userA, @RequestParam String userB) {
        try {
            List<TinNhanCaNhan> history = chatService.getPrivateHistory(userA, userB);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/private/history")
    public ResponseEntity<?> getPrivateHistoryGet(@RequestParam String userA, @RequestParam String userB) {
        try {
            List<TinNhanCaNhan> history = chatService.getPrivateHistory(userA, userB);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    /**
     * Send group message via REST
     * POST /trendy/chat/group/{maNhom}/send
     */
    @PostMapping("/group/{maNhom}/send")
    public ResponseEntity<?> sendGroup(@PathVariable String maNhom, @RequestBody GroupMessage dto) {
        try {
            dto.setMaNhom(maNhom);
            TinNhanNhom message = chatService.saveGroupMessage(dto);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    /**
     * Get group chat history
     * GET /trendy/chat/group/{maNhom}/history
     */
    @GetMapping("/group/{maNhom}/history")
    public ResponseEntity<?> getGroupHistory(@PathVariable String maNhom) {
        try {
            List<TinNhanNhom> history = chatService.getGroupHistory(maNhom);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @Transactional
    @PostMapping("/private/pin/{messageId}")
    public ResponseEntity<?> pinPrivateMessage(@PathVariable String messageId) {
        try {
            System.out.println("📌 Pin private message: " + messageId);
            
            // Tìm bằng maTinNhan
            Optional<TinNhanCaNhan> msgOpt = privateRepo.findById(messageId);
            if (!msgOpt.isPresent()) {
                System.out.println("❌ Message not found by ID: " + messageId);
                return ResponseEntity.status(404).body(Map.of("error", "Tin nhắn không tồn tại"));
            }
            
            TinNhanCaNhan message = msgOpt.get();
            System.out.println("   - Found message: " + message.getMaTinNhan());
            
            message.setGhim(true);
            TinNhanCaNhan saved = privateRepo.save(message);
            System.out.println("✅ Private message pinned: " + saved.getGhim());
            
            return ResponseEntity.ok(Map.of(
                "success", true, 
                "message", "Đã ghim tin nhắn",
                "messageId", messageId,
                "pinned", true
            ));
        } catch (Exception e) {
            System.err.println("❌ Pin error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @Transactional
    @PostMapping("/private/unpin/{messageId}")
    public ResponseEntity<?> unpinPrivateMessage(@PathVariable String messageId) {
        try {
            System.out.println("📌 Unpin private message: " + messageId);
            
            // Tìm bằng maTinNhan
            Optional<TinNhanCaNhan> msgOpt = privateRepo.findById(messageId);
            if (!msgOpt.isPresent()) {
                System.out.println("❌ Message not found by ID: " + messageId);
                return ResponseEntity.status(404).body(Map.of("error", "Tin nhắn không tồn tại"));
            }
            
            TinNhanCaNhan message = msgOpt.get();
            System.out.println("   - Found message: " + message.getMaTinNhan());
            
            message.setGhim(false);
            TinNhanCaNhan saved = privateRepo.save(message);
            System.out.println("✅ Private message unpinned: " + saved.getGhim());
            
            return ResponseEntity.ok(Map.of(
                "success", true, 
                "message", "Đã bỏ ghim tin nhắn",
                "messageId", messageId,
                "pinned", false
            ));
        } catch (Exception e) {
            System.err.println("❌ Unpin error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @Transactional
    @PostMapping("/group/react")
    public ResponseEntity<?> reactToGroupMessage(@RequestBody Map<String, String> body) {
        try {
            String messageId = body.get("messageId");
            String reactionName = body.get("reaction");
            String userId = body.get("userId");
            
            System.out.println("🎯 Group React: messageId=" + messageId + ", userId=" + userId + ", reaction=" + reactionName);
            
            // Check if user already reacted to this message
            var existingReaction = reactionRepo.findByMaTinNhanAndIdUser(messageId, userId);
            
            if (existingReaction.isPresent()) {
                // User already reacted - remove the reaction (toggle off)
                reactionRepo.deleteByMaTinNhanAndIdUser(messageId, userId);
                System.out.println("✅ Group reaction removed (toggled off)");
                
                return ResponseEntity.ok(Map.of(
                    "messageId", messageId,
                    "userId", userId,
                    "success", true,
                    "status", "removed",
                    "action", "delete"
                ));
            } else {
                // First time reacting - add new reaction
                TinNhanReaction reaction = new TinNhanReaction();
                reaction.setId(UUID.randomUUID().toString());
                reaction.setMaTinNhan(messageId);
                reaction.setIdUser(userId);
                reaction.setLoaiReaction(reactionName);
                reaction.setNgayTao(LocalDateTime.now());
                reactionRepo.save(reaction);
                System.out.println("✅ Group reaction saved to DB");
                
                return ResponseEntity.ok(Map.of(
                    "messageId", messageId,
                    "reaction", reactionName,
                    "userId", userId,
                    "success", true,
                    "status", "saved",
                    "action", "add"
                ));
            }
        } catch (Exception e) {
            System.err.println("❌ Group reaction error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * React to private message - POST /trendy/chat/private/react
     * Supports add and remove reactions
     */
    @Transactional
    @PostMapping("/private/react")
    public ResponseEntity<?> reactToPrivateMessage(@RequestBody Map<String, String> body) {
        try {
            String messageId = body.get("messageId");
            String reactionName = body.get("reaction");
            String userId = body.get("userId");
            
            System.out.println("🎯 Private React: messageId=" + messageId + ", userId=" + userId + ", reaction=" + reactionName);
            
            // Check if user already reacted to this message
            var existingReaction = reactionRepo.findByMaTinNhanAndIdUser(messageId, userId);
            
            if (existingReaction.isPresent()) {
                // User already reacted - remove the reaction (toggle off)
                TinNhanReaction existing = existingReaction.get();
                reactionRepo.deleteByMaTinNhanAndIdUser(messageId, userId);
                System.out.println("✅ Reaction removed (toggled off)");
                
                return ResponseEntity.ok(Map.of(
                    "messageId", messageId,
                    "userId", userId,
                    "success", true,
                    "status", "removed",
                    "action", "delete"
                ));
            } else {
                // First time reacting - add new reaction
                TinNhanReaction reaction = new TinNhanReaction();
                reaction.setId(UUID.randomUUID().toString());
                reaction.setMaTinNhan(messageId);
                reaction.setIdUser(userId);
                reaction.setLoaiReaction(reactionName);
                reaction.setNgayTao(LocalDateTime.now());
                reactionRepo.save(reaction);
                System.out.println("✅ Reaction saved to DB");
                
                return ResponseEntity.ok(Map.of(
                    "messageId", messageId,
                    "reaction", reactionName,
                    "userId", userId,
                    "success", true,
                    "status", "saved",
                    "action", "add"
                ));
            }
        } catch (Exception e) {
            System.err.println("❌ Private reaction error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get pinned messages for solo chat
     * GET /trendy/chat/private/pinned?maNhomSolo=...
     */
    @GetMapping("/private/pinned")
    public ResponseEntity<?> getPinnedPrivateMessages(@RequestParam String maNhomSolo) {
        try {
            List<TinNhanCaNhan> pinnedMsgs = privateRepo.findByMaNhomSoloAndGhimTrueOrderByNgayGuiDesc(maNhomSolo);
            System.out.println("📌 Found " + pinnedMsgs.size() + " pinned messages");
            return ResponseEntity.ok(pinnedMsgs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get pinned messages for group chat
     * GET /trendy/chat/group/pinned?maNhom=...
     */
    @GetMapping("/group/pinned")
    public ResponseEntity<?> getPinnedGroupMessages(@RequestParam String maNhom) {
        try {
            List<TinNhanNhom> pinnedMsgs = groupRepo.findByMaNhomAndGhimTrueOrderByNgayGuiDesc(maNhom);
            System.out.println("📌 Found " + pinnedMsgs.size() + " pinned group messages");
            return ResponseEntity.ok(pinnedMsgs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ...existing code...
    public ResponseEntity<?> getMessageReactions(@PathVariable String messageId) {
        try {
            List<TinNhanReaction> reactions = reactionRepo.findByMaTinNhan(messageId);
            
            // Group by reaction type
            Map<String, List<String>> grouped = new HashMap<>();
            for (TinNhanReaction r : reactions) {
                grouped.computeIfAbsent(r.getLoaiReaction(), k -> new ArrayList<>())
                    .add(r.getIdUser());
            }
            
            System.out.println("📊 Got " + reactions.size() + " reactions for message: " + messageId);
            return ResponseEntity.ok(grouped);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get group reactions - GET /trendy/chat/group/reactions/{messageId}
     */
    @GetMapping("/group/reactions/{messageId}")
    public ResponseEntity<?> getGroupMessageReactions(@PathVariable String messageId) {
        try {
            List<TinNhanReaction> reactions = reactionRepo.findByMaTinNhan(messageId);
            
            Map<String, List<String>> grouped = new HashMap<>();
            for (TinNhanReaction r : reactions) {
                grouped.computeIfAbsent(r.getLoaiReaction(), k -> new ArrayList<>())
                    .add(r.getIdUser());
            }
            
            return ResponseEntity.ok(grouped);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Reply to a private message
     * POST /trendy/chat/private/reply
     */
    @PostMapping("/private/reply")
    public ResponseEntity<?> replyToPrivateMessage(@RequestBody Map<String, Object> body) {
        try {
            String messageId = (String) body.get("messageId");
            String replyContent = (String) body.get("content");
            String fromUserId = (String) body.get("fromUserId");
            String toUserId = (String) body.get("toUserId");
            
            // Get original message to get replyToSender name
            TinNhanCaNhan originalMsg = privateRepo.findById(messageId).orElse(null);
            if (originalMsg == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Tin nhắn gốc không tồn tại"));
            }

            // Save reply message with reference to original message
            PrivateMessage dto = new PrivateMessage();
            dto.setMaNguoiGui(fromUserId);
            dto.setMaNguoiNhan(toUserId);
            dto.setNoiDung(replyContent);
            dto.setReplyToId(messageId);
            dto.setReplyToContent(originalMsg.getNoiDung());
            dto.setReplyToSender(originalMsg.getMaNguoiGui());
            
            TinNhanCaNhan message = chatService.savePrivateMessage(dto);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error: " + e.getMessage()));
        }
    }

    /**
     * Reply to a group message
     * POST /trendy/chat/group/{maNhom}/reply
     */
    @PostMapping("/group/{maNhom}/reply")
    public ResponseEntity<?> replyToGroupMessage(@PathVariable String maNhom, @RequestBody Map<String, Object> body) {
        try {
            String messageId = (String) body.get("messageId");
            String replyContent = (String) body.get("content");
            String fromUserId = (String) body.get("fromUserId");
            
            // Get original message info (assuming we have a group message repo)
            // For now, we'll save with basic info
            GroupMessage dto = new GroupMessage();
            dto.setMaNhom(maNhom);
            dto.setMaNguoiGui(fromUserId);
            dto.setNoiDung(replyContent);
            dto.setReplyToId(messageId);
            dto.setReplyToContent((String) body.get("replyToContent"));
            dto.setReplyToSender((String) body.get("replyToSender"));
            
            TinNhanNhom message = chatService.saveGroupMessage(dto);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Error: " + e.getMessage()));
        }
    }

    @PostMapping("/group/pin/{messageId}")
    public ResponseEntity<?> pinGroupMessage(@PathVariable String messageId) {
        try {
            System.out.println("📌 Pin group message: " + messageId);
            Optional<?> msgOpt = groupRepo.findById(messageId);
            
            if (!msgOpt.isPresent()) {
                System.out.println("❌ Message not found: " + messageId);
                return ResponseEntity.badRequest().body(Map.of("error", "Message not found"));
            }
            
            TinNhanNhom message = (TinNhanNhom) msgOpt.get();
            message.setGhim(true);
            groupRepo.save(message);
            System.out.println("✅ Group message pinned");
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã ghim tin nhắn"));
        } catch (Exception e) {
            System.err.println("❌ Pin error: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/group/unpin/{messageId}")
    public ResponseEntity<?> unpinGroupMessage(@PathVariable String messageId) {
        try {
            System.out.println("📌 Unpin group message: " + messageId);
            Optional<?> msgOpt = groupRepo.findById(messageId);
            
            if (!msgOpt.isPresent()) {
                System.out.println("❌ Message not found: " + messageId);
                return ResponseEntity.badRequest().body(Map.of("error", "Message not found"));
            }
            
            TinNhanNhom message = (TinNhanNhom) msgOpt.get();
            message.setGhim(false);
            groupRepo.save(message);
            System.out.println("✅ Group message unpinned");
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã bỏ ghim tin nhắn"));
        } catch (Exception e) {
            System.err.println("❌ Unpin error: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete group message (only owner) - DELETE /trendy/chat/group-message/{messageId}
     */
    @DeleteMapping("/group-message/{messageId}")
    public ResponseEntity<?> deleteGroupMessageOwner(@PathVariable String messageId, @RequestParam String userId) {
        try {
            System.out.println("🗑️ Deleting group message: " + messageId);
            Optional<?> msgOpt = groupRepo.findById(messageId);
            if (msgOpt.isPresent()) {
                TinNhanNhom groupMsg = (TinNhanNhom) msgOpt.get();
                if (!groupMsg.getMaNguoiGui().equals(userId)) {
                    return ResponseEntity.status(403).body(Map.of("error", "Cannot delete"));
                }
                groupRepo.deleteById(messageId);
                System.out.println("✅ Group message deleted");
            }
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    @PostMapping("/group-message/{messageId}/hide")
    public ResponseEntity<?> hideGroupMessagePermanent(@PathVariable String messageId, @RequestParam String userId) {
        try {
            System.out.println("Hiding group message: " + messageId);
            Optional<?> msgOpt = groupRepo.findById(messageId);
            if (msgOpt.isPresent()) {
                TinNhanNhom groupMsg = (TinNhanNhom) msgOpt.get();
                groupRepo.save(groupMsg);
                System.out.println("Group message hidden");
            }
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    @DeleteMapping("/private/message/{messageId}")
    public ResponseEntity<?> deletePrivateMessage(@PathVariable String messageId, @RequestParam String userId) {
        try {
            System.out.println("🗑DELETE Private message: " + messageId + " by user: " + userId);
            Optional<TinNhanCaNhan> msgOpt = privateRepo.findById(messageId);
            
            if (!msgOpt.isPresent()) {
                System.out.println("Message not found: " + messageId);
                return ResponseEntity.status(404).body(Map.of("error", "Message not found"));
            }
            
            TinNhanCaNhan msg = msgOpt.get();
            if (!msg.getMaNguoiGui().equals(userId)) {
                System.out.println("Not message owner. Sender: " + msg.getMaNguoiGui() + ", Requester: " + userId);
                return ResponseEntity.status(403).body(Map.of("error", "Cannot delete - not message owner"));
            }
            
            // Hard delete
            privateRepo.deleteById(messageId);
            System.out.println(" Private message deleted permanently");
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã xóa tin nhắn vĩnh viễn"));
        } catch (Exception e) {
            System.err.println(" Delete error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Delete group message (owner only) - HARD DELETE
     * DELETE /trendy/chat/group/message/{messageId}?userId=...
     */
    @DeleteMapping("/group/message/{messageId}")
    public ResponseEntity<?> deleteGroupMessage(@PathVariable String messageId, @RequestParam String userId) {
        try {
            System.out.println(" DELETE Group message: " + messageId + " by user: " + userId);
            Optional<?> msgOpt = groupRepo.findById(messageId);
            
            if (!msgOpt.isPresent()) {
                System.out.println(" Message not found: " + messageId);
                return ResponseEntity.status(404).body(Map.of("error", "Message not found"));
            }
            
            TinNhanNhom msg = (TinNhanNhom) msgOpt.get();
            if (!msg.getMaNguoiGui().equals(userId)) {
                System.out.println(" Not message owner. Sender: " + msg.getMaNguoiGui() + ", Requester: " + userId);
                return ResponseEntity.status(403).body(Map.of("error", "Cannot delete - not message owner"));
            }
            
            // Hard delete
            groupRepo.deleteById(messageId);
            System.out.println(" Group message deleted permanently");
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã xóa tin nhắn vĩnh viễn"));
        } catch (Exception e) {
            System.err.println(" Delete error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    @DeleteMapping("/group/delete/{maNhom}")
    public ResponseEntity<?> deleteGroup(@PathVariable String maNhom, @RequestParam String userId) {
        try {
            if (maNhom == null || maNhom.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error","Mã nhóm không hợp lệ"));
            }
            GroupChat group = groupChatRepository.findById(maNhom).orElse(null);
            if (group == null) {
                return ResponseEntity.badRequest().body(Map.of("error","Nhóm không tồn tại"));
            }
            if (!group.getNguoiTao().equals(userId)) {
                return ResponseEntity.status(403).body(Map.of("error","Bạn không có quyền xóa nhóm này"));
            }
            // Delete messages
            try { groupRepo.deleteAll(groupRepo.findByMaNhomOrderByNgayGuiAsc(maNhom)); } catch(Exception ignore) {}
            // Delete members
            try { thanhVienNhomRepository.findByMaNhom(maNhom).forEach(thanhVienNhomRepository::delete); } catch(Exception ignore) {}
            // Delete group
            groupChatRepository.delete(group);
            // Broadcast deletion notification to creator (optional)
            try {
                Notification n = new Notification();
                n.setIdThongBao(UUID.randomUUID().toString());
                n.setIdNguoiNhan(userId);
                n.setType("XOA_NHOM");
                n.setNoiDung("Bạn đã xóa nhóm " + group.getTenNhom());
                n.setLienKet("/group/" + maNhom);
                n.setTrangThai(false);
                n.setNgayTao(LocalDateTime.now());
                notificationRepo.save(n);
                messagingTemplate.convertAndSend("/topic/notification." + userId, n);
            } catch(Exception ignore) {}
            return ResponseEntity.ok(Map.of("message","Đã xóa nhóm thành công"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error","Lỗi xóa nhóm: " + e.getMessage()));
        }
    }

    /**
     * Upload file for chat
     * POST /trendy/chat/upload
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadChatFile(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam String userId) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "File không được để trống"));
            }

            // Validate file size (max 50MB)
            long maxFileSize = 50 * 1024 * 1024;
            if (file.getSize() > maxFileSize) {
                return ResponseEntity.badRequest().body(Map.of("error", "File quá lớn, tối đa 50MB"));
            }

            // Create uploads directory if not exists
            java.nio.file.Path uploadDir = java.nio.file.Paths.get("uploads/chat");
            java.nio.file.Files.createDirectories(uploadDir);

            // Generate unique filename
            String originalFileName = file.getOriginalFilename();
            String fileExtension = originalFileName != null ? 
                originalFileName.substring(originalFileName.lastIndexOf(".")) : "";
            String uniqueFileName = userId + "_" + System.currentTimeMillis() + fileExtension;

            // Save file
            java.nio.file.Path filePath = uploadDir.resolve(uniqueFileName);
            java.nio.file.Files.write(filePath, file.getBytes());

            // Return file URL
            String fileUrl = "/uploads/chat/" + uniqueFileName;
            
            System.out.println(" File uploaded: " + uniqueFileName);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "fileUrl", fileUrl,
                "fileName", originalFileName,
                "fileSize", file.getSize()
            ));
        } catch (Exception e) {
            System.err.println(" Upload error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Lỗi upload file: " + e.getMessage()));
        }
    }
    @GetMapping("/group/{maNhom}/members")
    public ResponseEntity<?> getGroupMembers(@PathVariable String maNhom) {
        try {
            if (maNhom == null || maNhom.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Mã nhóm không hợp lệ"));
            }

            GroupChat group = groupChatRepository.findById(maNhom).orElse(null);
            if (group == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Nhóm không tồn tại"));
            }

            // Get all members
            List<ThanhVienNhom> members = thanhVienNhomRepository.findByMaNhom(maNhom);

            // Map to DTO with user info
            List<Map<String, Object>> memberList = members.stream().map(m -> {
                Map<String, Object> memberMap = new HashMap<>();
                memberMap.put("idUser", m.getIdUser());
                memberMap.put("vaiTro", m.getVaiTro());
                memberMap.put("ngayThamGia", m.getNgayThamGia());

                // Get user info
                try {
                    User user = userRepository.findById(m.getIdUser()).orElse(null);
                    if (user != null) {
                        memberMap.put("ten", user.getTen());
                        memberMap.put("avatar", user.getAvatar());
                        memberMap.put("email", user.getEmail());
                    }
                } catch (Exception e) {
                    System.err.println("Error loading user info: " + e.getMessage());
                }

                return memberMap;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(memberList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Lỗi tải thành viên nhóm: " + e.getMessage()));
        }
    }

    @GetMapping("/group/{maNhom}/info")
    public ResponseEntity<?> getGroupInfo(@PathVariable String maNhom) {
        try {
            if (maNhom == null || maNhom.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Mã nhóm không hợp lệ"));
            }

            GroupChat group = groupChatRepository.findById(maNhom).orElse(null);
            if (group == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Nhóm không tồn tại"));
            }

            // Get member count
            List<ThanhVienNhom> members = thanhVienNhomRepository.findByMaNhom(maNhom);

            Map<String, Object> info = new HashMap<>();
            info.put("maNhom", group.getMaNhom());
            info.put("tenNhom", group.getTenNhom());
            info.put("moTa", group.getMoTa());
            info.put("anhNhom", group.getAnhNhom());
            info.put("nguoiTao", group.getNguoiTao());
            info.put("ngayTao", group.getNgayTao());
            info.put("memberCount", members.size());

            return ResponseEntity.ok(info);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Lỗi tải thông tin nhóm: " + e.getMessage()));
        }
    }

    /**
     * Leave a group
     * DELETE /trendy/chat/group/{maNhom}/leave
     */
    @DeleteMapping("/group/{maNhom}/leave")
    public ResponseEntity<?> leaveGroup(@PathVariable String maNhom, @RequestParam String userId) {
        try {
            if (maNhom == null || maNhom.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Mã nhóm không hợp lệ"));
            }
            if (userId == null || userId.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "User ID không hợp lệ"));
            }

            GroupChat group = groupChatRepository.findById(maNhom).orElse(null);
            if (group == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Nhóm không tồn tại"));
            }

            // Check if user is a member
            if (!thanhVienNhomRepository.existsByMaNhomAndIdUser(maNhom, userId)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Bạn không phải thành viên của nhóm này"));
            }

            // Remove user from group
            var member = thanhVienNhomRepository.findByMaNhomAndIdUser(maNhom, userId);
            if (member.isPresent()) {
                thanhVienNhomRepository.delete(member.get());
                System.out.println(" User " + userId + " left group " + maNhom);
            }

            // Create notification for group (optional)
            try {
                // Get user info for notification
                User user = userRepository.findById(userId).orElse(null);
                String userName = user != null ? user.getTen() : userId;
                
                Notification n = new Notification();
                n.setIdThongBao(UUID.randomUUID().toString());
                n.setIdNguoiNhan(group.getNguoiTao()); // Notify group creator
                n.setType("THANH_VIEN_ROI_NHOM");
                n.setNoiDung(userName + " đã rời khỏi nhóm " + group.getTenNhom());
                n.setLienKet("/group/" + maNhom);
                n.setTrangThai(false);
                n.setNgayTao(LocalDateTime.now());
                notificationRepo.save(n);
                try { messagingTemplate.convertAndSend("/topic/notification." + group.getNguoiTao(), n); } catch(Exception ig) {}
            } catch (Exception ignore) {}

            return ResponseEntity.ok(Map.of("message", "Đã rời khỏi nhóm thành công"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Lỗi rời khỏi nhóm: " + e.getMessage()));
        }
    }
}
