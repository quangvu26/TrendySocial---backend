package com.example.trendy_chat.dto;

import com.example.trendy_chat.entity.GroupChat;
import lombok.Data;

@Data
public class GroupChatDTO {
    private String maNhom;
    private String tenNhom;
    private String nguoiTao;
    private String anhNhom;  // Match database column name
    private String lastMessage;
    private String ngayGui;  // Time of last message

    public static GroupChatDTO fromEntity(GroupChat g) {
        GroupChatDTO dto = new GroupChatDTO();
        dto.setMaNhom(g.getMaNhom());
        dto.setTenNhom(g.getTenNhom());
        dto.setNguoiTao(g.getNguoiTao());
        dto.setAnhNhom(g.getAnhNhom());  // Use database field
        // TODO: lastMessage could be populated from message repository
        return dto;
    }
}