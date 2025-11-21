package com.example.trendy_chat.dto;

import com.example.trendy_chat.entity.SoloChat;
import com.example.trendy_chat.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SoloChatDTO {
    private String maNhomSolo;      // Primary key of solo_chat
    private String maNguoiGui;      // Sender ID (for mapping)
    private String maNguoiNhan;     // Receiver ID (for mapping)
    private String otherUserId;     // ID of the other user in conversation
    private String otherUserName;
    private String avatar;
    private Boolean gender;
    private String lastMessage;
    private String ngayGui;         // Time of last message

    public static SoloChatDTO fromEntity(SoloChat sc, String currentUserId, User otherUser) {
        String otherUserId = sc.getId_user_1().equals(currentUserId) ? 
            sc.getId_user_2() : sc.getId_user_1();
        
        return SoloChatDTO.builder()
            .maNhomSolo(sc.getIdSoloChat())
            .maNguoiGui(sc.getId_user_1())
            .maNguoiNhan(sc.getId_user_2())
            .otherUserId(otherUserId)
            .otherUserName(otherUser != null ? otherUser.getTen() : otherUserId)
            .avatar(otherUser != null ? otherUser.getAvatar() : null)
            .gender(otherUser != null ? otherUser.getGioiTinh() : null)
            .lastMessage("")
            .build();
    }
}
