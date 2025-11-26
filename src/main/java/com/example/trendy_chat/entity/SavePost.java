package com.example.trendy_chat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "save_post")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(SavePost.SavePostId.class)
public class SavePost {
    
    @Id
    @Column(name = "id_post")
    private UUID idPost;
    
    @Id
    @Column(name = "id_user", length = 100)
    private String idUser;
    
    @Column(name = "ngay_luu")
    private LocalDateTime ngayLuu;
    
    @PrePersist
    public void prePersist() {
        if (this.ngayLuu == null) {
            this.ngayLuu = LocalDateTime.now();
        }
    }
    
    // Composite key class
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SavePostId implements Serializable {
        private UUID idPost;
        private String idUser;
    }
}
