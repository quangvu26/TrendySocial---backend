package com.example.trendy_chat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chatting_group")
public class TinNhanNhom {
    @Id
    @Column(name = "ma_tin_nhan", length = 100)
    private String maTinNhan;

    @Column(name = "ma_nhom", length = 100, nullable = false)
    private String maNhom;

    @Column(name = "ma_nguoi_gui", length = 100, nullable = false)
    private String maNguoiGui;

    @Column(name = "noi_dung", columnDefinition = "NVARCHAR(MAX)")
    private String noiDung;

    @Column(name = "tep_dinh_kem", columnDefinition = "NVARCHAR(MAX)")
    private String tepDinhKem;

    @Column(name = "ngay_gui")
    private LocalDateTime ngayGui;

    @Column(name = "ghim")
    private Boolean ghim = false;

    @Column(name = "reply_to_id", length = 100)
    private String replyToId;

    @Column(name = "reply_to_content", columnDefinition = "NVARCHAR(MAX)")
    private String replyToContent;

    @Column(name = "reply_to_sender", length = 100)
    private String replyToSender;

    @Column(name = "attachments", columnDefinition = "NVARCHAR(MAX)")
    private String attachments; // JSON array of attachment URLs

    // Getters and Setters
    public String getReplyToId() {
        return replyToId;
    }

    public void setReplyToId(String replyToId) {
        this.replyToId = replyToId;
    }

    public String getReplyToContent() {
        return replyToContent;
    }

    public void setReplyToContent(String replyToContent) {
        this.replyToContent = replyToContent;
    }

    public String getReplyToSender() {
        return replyToSender;
    }

    public void setReplyToSender(String replyToSender) {
        this.replyToSender = replyToSender;
    }

    public Boolean getGhim() {
        return ghim;
    }

    public void setGhim(Boolean ghim) {
        this.ghim = ghim;
    }
}
