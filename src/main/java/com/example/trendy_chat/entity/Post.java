package com.example.trendy_chat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

import java.time.LocalDateTime;

@Entity
@Table(name = "post")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    
    @Id
    @Column(name = "id_post")
    private UUID idPost;
    
    @Column(name = "id_user", length = 100, nullable = false)
    private String idUser;
    
    @Column(name = "noi_dung", columnDefinition = "TEXT")
    private String noiDung;
    
    @Column(name = "duong_dan_anh", columnDefinition = "TEXT")
    private String duongDanAnh;
    
    @Column(name = "che_do_rieng_tu", length = 20)
    private String cheDoRiengTu;
    
    @Column(name = "ngay_dang")
    private LocalDateTime ngayDang;
    
    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;
    
    @Column(name = "likes_count", columnDefinition = "INT DEFAULT 0")
    private Integer likesCount = 0;
    
    @Column(name = "comments_count", columnDefinition = "INT DEFAULT 0")
    private Integer commentsCount = 0;
    
    @Column(name = "views_count", columnDefinition = "INT DEFAULT 0")
    private Integer viewsCount = 0;
    
    @PrePersist
    public void prePersist() {
        if (this.ngayDang == null) {
            this.ngayDang = LocalDateTime.now();
        }
        if (this.likesCount == null) {
            this.likesCount = 0;
        }
        if (this.commentsCount == null) {
            this.commentsCount = 0;
        }
        if (this.viewsCount == null) {
            this.viewsCount = 0;
        }
    }
}