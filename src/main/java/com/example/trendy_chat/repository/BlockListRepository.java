package com.example.trendy_chat.repository;

import com.example.trendy_chat.entity.BlockList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface BlockListRepository extends JpaRepository<BlockList, UUID> {
    boolean existsByMaNguoiChanAndMaNguoiBiChan(String blockerId, String blockedId);
    
    List<BlockList> findByMaNguoiChan(String blockerId);
    
    List<BlockList> findByMaNguoiBiChan(String blockedId);
    
    @Modifying
    @Transactional
    void deleteByMaNguoiChanAndMaNguoiBiChan(String blockerId, String blockedId);
}