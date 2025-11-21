package com.example.trendy_chat.controller;

import com.example.trendy_chat.dto.PrivateMessage;
import com.example.trendy_chat.dto.GroupMessage;
import com.example.trendy_chat.entity.TinNhanCaNhan;
import com.example.trendy_chat.entity.TinNhanNhom;
import com.example.trendy_chat.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatMessageController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Handle private messages via WebSocket
    @MessageMapping("/chat/private/{toUserId}")
    public void handlePrivateMessage(
            @DestinationVariable String toUserId,
            @Payload PrivateMessage request,
            SimpMessageHeaderAccessor headerAccessor
    ) throws Exception {
        try {
            String fromUserId = request.getMaNguoiGui();
            if (fromUserId == null || fromUserId.trim().isEmpty()) {
                throw new RuntimeException("Sender ID is required");
            }

            request.setMaNguoiNhan(toUserId);
            
            // Save and broadcast
            TinNhanCaNhan message = chatService.savePrivateMessage(request);
            
            System.out.println(" Private message sent from " + fromUserId + " to " + toUserId);
            System.out.println("   Message ID: " + message.getMaTinNhan());
            System.out.println("   Reply To: " + message.getReplyToId());
            System.out.println("   MaNhomSolo: " + message.getMaNhomSolo());
            
            // Broadcast to both users via maNhomSolo
            String topic = "/topic/chat/private/" + message.getMaNhomSolo();
            messagingTemplate.convertAndSend(topic, message);
            System.out.println(" Broadcasted to: " + topic);
        } catch (Exception e) {
            System.err.println(" Error handling private message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // Handle group messages via WebSocket
    @MessageMapping("/chat/group/{groupId}")
    public void handleGroupMessage(
            @DestinationVariable String groupId,
            @Payload GroupMessage request,
            SimpMessageHeaderAccessor headerAccessor
    ) throws Exception {
        try {
            String fromUserId = request.getMaNguoiGui();
            if (fromUserId == null || fromUserId.trim().isEmpty()) {
                throw new RuntimeException("Sender ID is required");
            }

            request.setMaNhom(groupId);
            
            // Save and broadcast
            TinNhanNhom message = chatService.saveGroupMessage(request);
            
            System.out.println("Group message sent from " + fromUserId + " to group " + groupId);
        } catch (Exception e) {
            System.err.println("Error handling group message: " + e.getMessage());
            throw e;
        }
    }
}
