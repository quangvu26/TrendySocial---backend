package com.example.trendy_chat.repository;

import com.example.trendy_chat.entity.TinNhanNhom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChattingGroupRepository extends JpaRepository<TinNhanNhom, UUID> {
    List<TinNhanNhom> findByMaNhomOrderByNgayGuiAsc(String maNhom);
    Page<TinNhanNhom> findByMaNhomOrderByNgayGuiAsc(String maNhom, Pageable pageable);

    java.util.Optional<TinNhanNhom> findTopByMaNhomOrderByNgayGuiDesc(String maNhom);
    
    List<TinNhanNhom> findByMaNhomAndGhimTrueOrderByNgayGuiDesc(String maNhom);
}
