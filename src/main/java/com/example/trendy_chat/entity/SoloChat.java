package com.example.trendy_chat.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "solo_chat")
@Data
public class SoloChat {
    @Id
    @Column(name = "ma_nhom_solo", length = 100)
    private String idSoloChat;
    
    @Column(name = "id_user_1", length = 100)
    private String id_user_1;
    
    @Column(name = "id_user_2", length = 100)
    private String id_user_2;
    
    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;
    
    @PrePersist
    public void prePersist() {
        if (this.idSoloChat == null) {
            this.idSoloChat = UUID.randomUUID().toString();
        }
        if (this.ngayTao == null) {
            this.ngayTao = LocalDateTime.now();
        }
    }
}