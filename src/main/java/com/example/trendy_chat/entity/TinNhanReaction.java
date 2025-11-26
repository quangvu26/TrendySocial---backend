package com.example.trendy_chat.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tin_nhan_reaction")
public class TinNhanReaction {
    
    @Id
    @Column(name = "id")
    private UUID id;
    
    @Column(name = "ma_tin_nhan", nullable = false)
    private UUID maTinNhan;
    
    @Column(name = "id_user", nullable = false)
    private String idUser;
    
    @Column(name = "loai_reaction", nullable = false, length = 20)
    private String loaiReaction;
    
    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;
    
    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (ngayTao == null) {
            ngayTao = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getMaTinNhan() {
        return maTinNhan;
    }

    public void setMaTinNhan(UUID maTinNhan) {
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