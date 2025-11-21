package com.example.trendy_chat.service;


import com.example.trendy_chat.dto.GroupMessage;
import com.example.trendy_chat.dto.PrivateMessage;
import com.example.trendy_chat.entity.*;
import com.example.trendy_chat.repository.SoloChatRepository;
import com.example.trendy_chat.repository.GroupChatRepository;
import com.example.trendy_chat.repository.ChattingGroupRepository;
import com.example.trendy_chat.repository.ThanhVienNhomRepository;
import com.example.trendy_chat.repository.TinNhanCaNhanRepository;
import com.example.trendy_chat.repository.BlockListRepository;
import com.example.trendy_chat.repository.NotificationRepository;
import com.example.trendy_chat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

@Service
public class ChatService {
    @Autowired
    private ThanhVienNhomRepository memberRepo;
    private final TinNhanCaNhanRepository privateRepo;
    private final ChattingGroupRepository groupRepo;
    private final SimpMessagingTemplate messagingTemplate;
    @Autowired
    private SoloChatRepository soloChatRepository;
    @Autowired
    private GroupChatRepository groupChatRepository;
    @Autowired
    private BlockListRepository blockRepo;
    @Autowired
    private NotificationRepository notificationRepo;
    @Autowired
    private UserRepository userRepo;

    public ChatService(TinNhanCaNhanRepository privateRepo,
                       ChattingGroupRepository groupRepo,
                       SimpMessagingTemplate messagingTemplate) {
        this.privateRepo = privateRepo;
        this.groupRepo = groupRepo;
        this.messagingTemplate = messagingTemplate;
    }

    // Methods to list chats
    public List<SoloChat> getSoloChats(String userId) {
        return soloChatRepository.findByUserId(userId);
    }

    public List<GroupChat> getGroupChats(String userId) {
        // Get all groups where user is a member
        List<ThanhVienNhom> members = memberRepo.findByMaNhom(userId);
        // Wait, this is wrong - findByMaNhom expects maNhom, not userId
        // Need to get all members where idUser = userId, then get their groups
        
        // Better approach: query all groups and filter by member
        List<GroupChat> allGroups = groupChatRepository.findAll();
        return allGroups.stream()
                .filter(g -> memberRepo.existsByMaNhomAndIdUser(g.getMaNhom(), userId))
                .toList();
    }

    // Private chat
    public TinNhanCaNhan savePrivateMessage(PrivateMessage dto) {
        // Validate inputs first
        if (dto.getMaNguoiGui() == null || dto.getMaNguoiGui().trim().isEmpty()) {
            throw new RuntimeException("maNguoiGui không được để trống");
        }
        if (dto.getMaNguoiNhan() == null || dto.getMaNguoiNhan().trim().isEmpty()) {
            throw new RuntimeException("maNguoiNhan không được để trống");
        }
        
        // Check if blocked (both directions)
        boolean isBlocked = blockRepo.existsByMaNguoiChanAndMaNguoiBiChan(
            dto.getMaNguoiNhan(), dto.getMaNguoiGui()
        ) || blockRepo.existsByMaNguoiChanAndMaNguoiBiChan(
            dto.getMaNguoiGui(), dto.getMaNguoiNhan()
        );
        
        if (isBlocked) {
            throw new RuntimeException("Không thể gửi tin nhắn cho người dùng này");
        }
        
        try {
            // Auto-create or get solo chat
            String user1 = dto.getMaNguoiGui();
            String user2 = dto.getMaNguoiNhan();
            
            // Ensure consistent ordering for lookup
            SoloChat soloChat = soloChatRepository
                .findBetweenUsers(user1, user2)
                .orElseGet(() -> {
                    SoloChat newSolo = new SoloChat();
                    newSolo.setId_user_1(user1);
                    newSolo.setId_user_2(user2);
                    return soloChatRepository.save(newSolo);
                });
            
            // Create message
            TinNhanCaNhan m = new TinNhanCaNhan();
            m.setMaTinNhan(UUID.randomUUID().toString());
            m.setMaNguoiGui(dto.getMaNguoiGui());
            m.setMaNguoiNhan(dto.getMaNguoiNhan());
            m.setMaNhomSolo(soloChat.getIdSoloChat()); // Link to solo_chat
            m.setNoiDung(dto.getNoiDung());
            m.setTepDinhKem(dto.getTepDinhKem());
            m.setNgayGui(LocalDateTime.now());
            m.setDaDoc(false);
            
            // Save reply information if present
            if (dto.getReplyToId() != null && !dto.getReplyToId().isEmpty()) {
                m.setReplyToId(dto.getReplyToId());
                m.setReplyToContent(dto.getReplyToContent());
                m.setReplyToSender(dto.getReplyToSender());
                System.out.println("✅ Private Reply saved: messageId=" + m.getMaTinNhan() + 
                    ", replyToId=" + dto.getReplyToId());
            }

            if (dto.getAttachments() != null && !dto.getAttachments().isEmpty()) {
                m.setAttachments(dto.getAttachments());
                System.out.println(" Attachments saved: " + dto.getAttachments());
            }

            TinNhanCaNhan saved = privateRepo.save(m);

            String conversationTopic = "/topic/chat/private/" + soloChat.getIdSoloChat();

            Map<String, Object> response = new HashMap<>();
            response.put("maTinNhan", saved.getMaTinNhan());
            response.put("maNguoiGui", saved.getMaNguoiGui());
            response.put("maNguoiNhan", saved.getMaNguoiNhan());
            response.put("noiDung", saved.getNoiDung());
            response.put("ngayGui", saved.getNgayGui());
            response.put("maNhomSolo", saved.getMaNhomSolo());
            response.put("replyToId", saved.getReplyToId());
            response.put("replyToContent", saved.getReplyToContent());
            response.put("replyToSender", saved.getReplyToSender());
            response.put("attachments", saved.getAttachments());
            
            messagingTemplate.convertAndSend(conversationTopic, response);
            
            System.out.println("✅ Message broadcast to: " + conversationTopic);
            
            // Create notification for receiver
            try {
                User sender = userRepo.findById(dto.getMaNguoiGui()).orElse(null);
                String senderName = sender != null ? sender.getTen() : dto.getMaNguoiGui();
                
                Notification notification = new Notification();
                notification.setIdThongBao(UUID.randomUUID().toString());
                notification.setIdNguoiNhan(dto.getMaNguoiNhan());
                notification.setLoaiThongBao("TIN_NHAN");
                notification.setNoiDung(senderName + " mới gửi tin nhắn cho bạn");
                notification.setLienKet("/chat/" + dto.getMaNguoiGui());
                notification.setTrangThai(false);
                notification.setNgayTao(LocalDateTime.now());
                
                notificationRepo.save(notification);
                System.out.println("✅ Notification created for user: " + dto.getMaNguoiNhan());
                
                // Broadcast notification to the receiver over WebSocket
                try {
                    messagingTemplate.convertAndSend("/topic/notification." + dto.getMaNguoiNhan(), notification);
                    System.out.println("📢 Notification pushed to /topic/notification." + dto.getMaNguoiNhan());
                } catch (Exception wsEx) {
                    System.err.println("Failed to push WS notification: " + wsEx.getMessage());
                }
            } catch (Exception e) {
                System.err.println("Failed to create notification: " + e.getMessage());
            }
            
            return saved;
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Người dùng không tồn tại. Mã người dùng: " + 
                (dto.getMaNguoiGui().length() > 20 ? dto.getMaNguoiGui().substring(0, 20) : dto.getMaNguoiGui()));
        }
    }

    public List<TinNhanCaNhan> getPrivateHistory(String userA, String userB) {
        // Get solo chat first
        var soloChat = soloChatRepository.findBetweenUsers(userA, userB);
        
        if (soloChat.isEmpty()) {
            return List.of(); // No chat history if solo chat doesn't exist
        }
        
        String maNhomSolo = soloChat.get().getIdSoloChat();
        
        // Get all messages from this solo chat, sorted by date
        return privateRepo.findByMaNhomSoloOrderByNgayGuiAsc(maNhomSolo);
    }

    public List<TinNhanNhom> getGroupHistory(String maNhom) {
        return groupRepo.findByMaNhomOrderByNgayGuiAsc(maNhom);
    }
    public TinNhanNhom saveGroupMessage(GroupMessage dto) {

        boolean isMember = memberRepo.existsByMaNhomAndIdUser(dto.getMaNhom(), dto.getMaNguoiGui());
        if (!isMember) {
            throw new RuntimeException("User không thuộc nhóm!");
        }

        TinNhanNhom g = new TinNhanNhom();
        g.setMaTinNhan(UUID.randomUUID().toString());
        g.setMaNhom(dto.getMaNhom());
        g.setMaNguoiGui(dto.getMaNguoiGui());
        g.setNoiDung(dto.getNoiDung());
        g.setTepDinhKem(dto.getTepDinhKem());
        g.setNgayGui(LocalDateTime.now());

        // Save reply information if present
        if (dto.getReplyToId() != null && !dto.getReplyToId().isEmpty()) {
            g.setReplyToId(dto.getReplyToId());
            g.setReplyToContent(dto.getReplyToContent());
            g.setReplyToSender(dto.getReplyToSender());
            System.out.println("✅ Group Reply saved: messageId=" + g.getMaTinNhan() + 
                ", replyToId=" + dto.getReplyToId());
        }

        // Save attachments if present
        if (dto.getAttachments() != null && !dto.getAttachments().isEmpty()) {
            g.setAttachments(dto.getAttachments());
            System.out.println("✅ Group Attachments saved: " + dto.getAttachments());
        }

        TinNhanNhom saved = groupRepo.save(g);

        messagingTemplate.convertAndSend("/topic/group." + dto.getMaNhom(), saved);

        return saved;
    }
}
