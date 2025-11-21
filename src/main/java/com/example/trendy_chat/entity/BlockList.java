package com.example.trendy_chat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "block_list")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlockList {
    
    @Id
    @Column(name = "ma_chan", length = 100)
    private String maChan;
    
    @Column(name = "ma_nguoi_chan", length = 100, nullable = false)
    private String maNguoiChan;
    
    @Column(name = "ma_nguoi_bi_chan", length = 100, nullable = false)
    private String maNguoiBiChan;
    
    @Column(name = "ngay_chan")
    private LocalDateTime ngayChan;
    
    @PrePersist
    public void prePersist() {
        if (this.ngayChan == null) {
            this.ngayChan = LocalDateTime.now();
        }
    }
}