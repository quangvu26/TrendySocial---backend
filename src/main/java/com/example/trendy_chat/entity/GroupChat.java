package com.example.trendy_chat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "group_chat")
@Data
public class GroupChat {

    @Id
    @Column(name = "ma_nhom")
    private String maNhom;

    @Column(name = "ten_nhom", nullable = false)
    private String tenNhom;

    @Column(name = "anh_nhom")
    private String anhNhom;

    @Column(name = "mo_ta")
    private String moTa;

    @Column(name = "nguoi_tao")
    private String nguoiTao;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;
}

