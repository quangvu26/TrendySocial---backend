package com.example.trendy_chat.repository;

import com.example.trendy_chat.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {
    List<Notification> findByIdNguoiNhanOrderByNgayTaoDesc(String idNguoiNhan);
    
    long countByIdNguoiNhanAndTrangThai(String idNguoiNhan, Boolean trangThai);
    
    // Auto-delete notifications older than X days
    long deleteByIdNguoiNhanAndNgayTaoBefore(String idNguoiNhan, LocalDateTime dateTime);
}