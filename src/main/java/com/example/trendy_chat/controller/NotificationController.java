package com.example.trendy_chat.controller;

import com.example.trendy_chat.entity.Notification;
import com.example.trendy_chat.repository.NotificationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/trendy/notification")
@CrossOrigin(origins = "*")
public class NotificationController {
    private final NotificationRepository notificationRepo;

    public NotificationController(NotificationRepository notificationRepo) {
        this.notificationRepo = notificationRepo;
    }

    @PostMapping
    public ResponseEntity<?> createNotification(@RequestBody Notification notification) {
        if (notification.getIdThongBao() == null) {
            notification.setIdThongBao(UUID.randomUUID().toString());
        }
        if (notification.getNgayTao() == null) {
            notification.setNgayTao(LocalDateTime.now());
        }
        if (notification.getTrangThai() == null) {
            notification.setTrangThai(false);
        }
        
        Notification saved = notificationRepo.save(notification);
        System.out.println("✅ Notification created: " + saved.getIdThongBao());
        System.out.println("   Type: " + saved.getType());
        System.out.println("   For: " + saved.getIdNguoiNhan());
        System.out.println("   From: " + saved.getMaNguoiGui());
        return ResponseEntity.ok(saved);
    }

    @Transactional
    @GetMapping("/list")
    public ResponseEntity<?> getNotifications(
            @RequestParam String userId,
            @RequestParam(defaultValue = "20") int limit) {
        System.out.println("Loading notifications for userId: " + userId + " (limit: " + limit + ")");
        
        LocalDateTime twoDaysAgo = LocalDateTime.now().minusDays(2);
        try {
            long deletedCount = notificationRepo.deleteByIdNguoiNhanAndNgayTaoBefore(userId, twoDaysAgo);
            if (deletedCount > 0) {
                System.out.println("Auto-deleted " + deletedCount + " old notifications");
            }
        } catch (Exception e) {
            System.err.println("Failed to auto-delete old notifications: " + e.getMessage());
        }
        
        List<Notification> notifications = notificationRepo.findByIdNguoiNhanOrderByNgayTaoDesc(userId);
        
        if (notifications.size() > limit) {
            notifications = notifications.subList(0, limit);
        }
        
        System.out.println("Loaded " + notifications.size() + " notifications for user: " + userId);
        return ResponseEntity.ok(notifications);
    }

    @Transactional
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable String id) {
        try {
            Notification notification = notificationRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Notification not found"));
            notification.setTrangThai(true);
            notificationRepo.save(notification);
            System.out.println("Notification marked as read: " + id);
            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            System.err.println("Error marking notification as read: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Transactional
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable String id) {
        try {
            notificationRepo.deleteById(id);
            System.out.println("Notification deleted: " + id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã xóa thông báo"));
        } catch (Exception e) {
            System.err.println("Error deleting notification: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(@RequestParam String userId) {
        long count = notificationRepo.countByIdNguoiNhanAndTrangThai(userId, false);
        System.out.println("Unread notifications for " + userId + ": " + count);
        return ResponseEntity.ok(Map.of("count", count));
    }
}