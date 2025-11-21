package com.example.trendy_chat.dto;

import lombok.Data;

@Data
public class GroupMessage {
    private String maNhom;
    private String maNguoiGui;
    private String noiDung;
    private String tepDinhKem;
    private String type;
    private String replyToId;
    private String replyToContent;
    private String replyToSender;
    private String attachments;
}
