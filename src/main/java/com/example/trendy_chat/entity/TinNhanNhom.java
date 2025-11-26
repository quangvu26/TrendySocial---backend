package com.example.trendy_chat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "chatting_group")
public class TinNhanNhom {
    @Id
    @Column(name = "ma_tin_nhan")
    private UUID maTinNhan;

    @Column(name = "ma_nhom", length = 100, nullable = false)
    private String maNhom;

    @Column(name = "ma_nguoi_gui", length = 100, nullable = false)
    private String maNguoiGui;

    @Column(name = "noi_dung", columnDefinition = "TEXT")
    private String noiDung;

    @Column(name = "tep_dinh_kem", columnDefinition = "TEXT")
    private String tepDinhKem;

    @Column(name = "ngay_gui")
    private LocalDateTime ngayGui;

    @Column(name = "ghim")
    private Boolean ghim = false;

    @Column(name = "reply_to_id")
    private UUID replyToId;

    @Column(name = "reply_to_content", columnDefinition = "TEXT")
    private String replyToContent;

    @Column(name = "reply_to_sender", length = 100)
    private String replyToSender;

    @Column(name = "attachments", columnDefinition = "TEXT")
    private String attachments; // JSON array of attachment URLs

    // Getters and Setters
    public UUID getReplyToId() {
        return replyToId;
    }

    public void setReplyToId(UUID replyToId) {
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
