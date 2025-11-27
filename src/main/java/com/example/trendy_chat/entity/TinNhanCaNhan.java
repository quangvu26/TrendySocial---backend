package com.example.trendy_chat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tin_nhan_ca_nhan")
public class TinNhanCaNhan {
    @Id
    @Column(name = "ma_tin_nhan")
    private UUID maTinNhan;

    // ...existing code...

    public UUID getId() {
        return maTinNhan;
    }

    public void setId(UUID id) {
        this.maTinNhan = id;
    }

    @Column(name = "ma_nguoi_gui", length = 100, nullable = false)
    private String maNguoiGui;

    @Column(name = "ma_nguoi_nhan", length = 100, nullable = false)
    private String maNguoiNhan;

    @Column(name = "noi_dung", columnDefinition = "TEXT")
    private String noiDung;

    @Column(name = "tep_dinh_kem", columnDefinition = "TEXT")
    private String tepDinhKem;
    @Column(name = "ma_nhom_solo", columnDefinition = "UUID")
    private UUID maNhomSolo;
    @Column(name = "ngay_gui")
    private LocalDateTime ngayGui;

    @Column(name = "da_doc")
    private Boolean daDoc = false;

    @Column(name = "ma_tin_nhan_tra_loi")
    private UUID maTinNhanTraLoi;

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

    public UUID getMaTinNhanTraLoi() {
        return maTinNhanTraLoi;
    }

    public void setMaTinNhanTraLoi(UUID maTinNhanTraLoi) {
        this.maTinNhanTraLoi = maTinNhanTraLoi;
    }

    public Boolean getGhim() {
        return ghim;
    }

    public void setGhim(Boolean ghim) {
        this.ghim = ghim;
    }

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
}
