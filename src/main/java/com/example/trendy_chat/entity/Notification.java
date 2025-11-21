package com.example.trendy_chat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @Column(name = "id_thong_bao")
    private String idThongBao;
    
    @Column(name = "id_nguoi_nhan", nullable = false)
    private String idNguoiNhan;
    
    @Column(name = "ma_nguoi_gui")
    private String maNguoiGui;
    
    @Column(name = "sender_name")
    private String senderName;
    
    @Column(name = "loai_thong_bao", nullable = false)
    private String type;
    
    @Column(name = "post_id")
    private String postId;
    
    @Column(name = "noi_dung")
    private String noiDung;
    
    @Column(name = "lien_ket")
    private String lienKet;
    
    @Column(name = "trang_thai", nullable = false)
    private Boolean trangThai = false;
    
    // Aliases for compatibility
    public void setLoaiThongBao(String type) { this.type = type; }
    public String getLoaiThongBao() { return this.type; }
    public void setNoiDung(String content) { this.noiDung = content; }
    public String getNoiDung() { return this.noiDung; }
    public String getContent() { return this.noiDung; }
    public void setContent(String content) { this.noiDung = content; }
    
    @Column(name = "ngay_tao", nullable = false)
    private LocalDateTime ngayTao;
    
    @PrePersist
    public void prePersist() {
        if (this.ngayTao == null) {
            this.ngayTao = LocalDateTime.now();
        }
        if (this.trangThai == null) {
            this.trangThai = false;
        }
    }
}