package com.example.trendy_chat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "comment_like")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(CommentLike.CommentLikeId.class)
public class CommentLike {
    
    @Id
    @Column(name = "ma_binh_luan")
    private UUID maBinhLuan;
    
    @Id
    @Column(name = "id_user", length = 100)
    private String idUser;
    
    @Column(name = "ngay_thich")
    private LocalDateTime ngayThich;
    
    @PrePersist
    public void prePersist() {
        if (this.ngayThich == null) {
            this.ngayThich = LocalDateTime.now();
        }
    }
    
    // Composite key class
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentLikeId implements Serializable {
        private UUID maBinhLuan;
        private String idUser;
    }
}
