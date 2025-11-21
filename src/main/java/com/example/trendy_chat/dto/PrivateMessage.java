package com.example.trendy_chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrivateMessage {
    private String maNguoiGui;
    private String maNguoiNhan;
    private String noiDung;
    private String tepDinhKem;
    private String type;
    private String replyToId;
    private String replyToContent;
    private String replyToSender;
    private String attachments;
}
