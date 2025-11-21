package com.example.trendy_chat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "post_like")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(PostLike.PostLikeId.class)
public class PostLike {
    
    @Id
    @Column(name = "id_post", length = 100)
    private String idPost;
    
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
    public static class PostLikeId implements Serializable {
        private String idPost;
        private String idUser;
    }
}