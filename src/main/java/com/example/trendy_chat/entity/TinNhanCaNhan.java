package com.example.trendy_chat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tin_nhan_ca_nhan")
public class TinNhanCaNhan {
    @Id
    @Column(name = "ma_tin_nhan", length = 100)
    private String maTinNhan;

    // ...existing code...

    public String getId() {
        return maTinNhan;
    }

    public void setId(String id) {
        this.maTinNhan = id;
    }

    @Column(name = "ma_nguoi_gui", length = 100, nullable = false)
    private String maNguoiGui;

    @Column(name = "ma_nguoi_nhan", length = 100, nullable = false)
    private String maNguoiNhan;

    @Column(name = "noi_dung", columnDefinition = "NVARCHAR(MAX)")
    private String noiDung;

    @Column(name = "tep_dinh_kem", columnDefinition = "NVARCHAR(MAX)")
    private String tepDinhKem;
    @Column(name = "ma_nhom_solo")
    private String maNhomSolo;
    @Column(name = "ngay_gui")
    private LocalDateTime ngayGui;

    @Column(name = "da_doc")
    private Boolean daDoc = false;

    @Column(name = "ma_tin_nhan_tra_loi")
    private String maTinNhanTraLoi;

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

    public String getMaTinNhanTraLoi() {
        return maTinNhanTraLoi;
    }

    public void setMaTinNhanTraLoi(String maTinNhanTraLoi) {
        this.maTinNhanTraLoi = maTinNhanTraLoi;
    }

    public Boolean getGhim() {
        return ghim;
    }

    public void setGhim(Boolean ghim) {
        this.ghim = ghim;
    }

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
}
