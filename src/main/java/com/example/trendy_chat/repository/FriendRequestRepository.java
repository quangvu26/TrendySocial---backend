package com.example.trendy_chat.repository;

import com.example.trendy_chat.entity.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, String> {
    // Basic queries
    boolean existsByMaNguoiGuiAndMaNguoiNhan(String from, String to);
    
    List<FriendRequest> findByMaNguoiNhanAndTrangThai(String maNguoiNhan, String trangThai);
    
    List<FriendRequest> findByMaNguoiGuiAndTrangThai(String maNguoiGui, String trangThai);
    
    List<FriendRequest> findByMaNguoiGuiOrMaNguoiNhan(String a, String b);
    
    // Count friends - both directions
    long countByMaNguoiGuiAndTrangThai(String maNguoiGui, String trangThai);
    long countByMaNguoiNhanAndTrangThai(String maNguoiNhan, String trangThai);
    
    /**
     * Find friendship between two users (bidirectional, any status)
     */
    List<FriendRequest> findByMaNguoiGuiAndMaNguoiNhanOrMaNguoiGuiAndMaNguoiNhan(
        String gui1, String nhan1, String gui2, String nhan2);
    
    /**
     * Find all duplicate bidirectional relations
     * Dùng để cleanup nếu có 2 XAC_NHAN relations giữa cùng 2 người
     */
    @Query(value = "SELECT * FROM friends f1 WHERE EXISTS (" +
           "SELECT 1 FROM friends f2 " +
           "WHERE f2.ma_nguoi_gui = f1.ma_nguoi_nhan " +
           "AND f2.ma_nguoi_nhan = f1.ma_nguoi_gui " +
           "AND f2.trang_thai = 'XAC_NHAN' " +
           "AND f1.trang_thai = 'XAC_NHAN' " +
           "AND f1.ma_yeu_cau != f2.ma_yeu_cau" +
           ")", nativeQuery = true)
    List<FriendRequest> findDuplicateBidirectionalRelations();
}
