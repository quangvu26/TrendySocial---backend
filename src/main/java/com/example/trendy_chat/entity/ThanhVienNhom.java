package com.example.trendy_chat.entity;

import com.example.trendy_chat.dto.ThanhVienNhomId;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "thanh_vien_nhom")
@Data
@IdClass(ThanhVienNhomId.class)
public class ThanhVienNhom {

    @Id
    @Column(name = "ma_nhom")
    private String maNhom;

    @Id
    @Column(name = "id_user")
    private String idUser;

    @Column(name = "vai_tro")
    private String vaiTro;

    @Column(name = "ngay_tham_gia")
    private LocalDateTime ngayThamGia;
}

