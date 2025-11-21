package com.example.trendy_chat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "comment")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    
    @Id
    @Column(name = "ma_binh_luan", length = 100)
    private String maBinhLuan;
    
    @Column(name = "id_post", length = 100, nullable = false)
    private String idPost;
    
    @Column(name = "id_user", length = 100, nullable = false)
    private String idUser;
    
    @Column(name = "noi_dung", columnDefinition = "NVARCHAR(MAX)", nullable = false)
    private String noiDung;
    
    @Column(name = "ngay_binh_luan")
    private LocalDateTime ngayBinhLuan;
    
    @Column(name = "ma_tra_loi", length = 100)
    private String maTraLoi;  // ID of parent comment for replies
    
    @Column(name = "an_binh_luan")
    private Boolean anBinhLuan = false;  // Hide comment (only user can see)
    
    @PrePersist
    public void prePersist() {
        if (this.ngayBinhLuan == null) {
            this.ngayBinhLuan = LocalDateTime.now();
        }
        if (this.anBinhLuan == null) {
            this.anBinhLuan = false;
        }
    }
}