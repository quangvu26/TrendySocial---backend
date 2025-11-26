package com.example.trendy_chat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

import java.time.LocalDateTime;

@Entity
@Table(name = "share_post")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SharePost {
    
    @Id
    @Column(name = "ma_chia_se")
    private UUID maChiaSe;
    
    @Column(name = "id_post", nullable = false)
    private UUID idPost;
    
    @Column(name = "id_user", length = 100, nullable = false)
    private String idUser;
    
    @Column(name = "ghi_chu")
    private String ghiChu;
    
    @Column(name = "ngay_chia_se")
    private LocalDateTime ngayChiaSe;
    
    @PrePersist
    public void prePersist() {
        if (this.ngayChiaSe == null) {
            this.ngayChiaSe = LocalDateTime.now();
        }
    }
}
