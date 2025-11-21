package com.example.trendy_chat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "friends")
@Data
public class FriendRequest {
    @Id
    @Column(name = "ma_yeu_cau")
    private String maYeuCau;

    @Column(name = "ma_nguoi_gui")
    private String maNguoiGui;

    @Column(name = "ma_nguoi_nhan")
    private String maNguoiNhan;

    @Column(name = "trang_thai")
    private String trangThai; // CHO_DUYET, XAC_NHAN, TU_CHOI

    @Column(name = "ngay_gui")
    private LocalDateTime ngayGui;
}
