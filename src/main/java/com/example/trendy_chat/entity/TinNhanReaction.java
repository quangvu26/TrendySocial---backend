package com.example.trendy_chat.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tin_nhan_reaction")
public class TinNhanReaction {
    
    @Id
    @Column(name = "id")
    private String id;
    
    @Column(name = "ma_tin_nhan", nullable = false)
    private String maTinNhan;
    
    @Column(name = "id_user", nullable = false)
    private String idUser;
    
    @Column(name = "loai_reaction", nullable = false, length = 20)
    private String loaiReaction;
    
    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;
    
    @PrePersist
    public void prePersist() {
        if (id == null || id.isEmpty()) {
            id = java.util.UUID.randomUUID().toString();
        }
        if (ngayTao == null) {
            ngayTao = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMaTinNhan() {
        return maTinNhan;
    }

    public void setMaTinNhan(String maTinNhan) {
        this.maTinNhan = maTinNhan;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getLoaiReaction() {
        return loaiReaction;
    }

    public void setLoaiReaction(String loaiReaction) {
        this.loaiReaction = loaiReaction;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }
}