package com.example.trendy_chat.repository;

import com.example.trendy_chat.entity.TinNhanCaNhan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TinNhanCaNhanRepository extends JpaRepository<TinNhanCaNhan, String> {
    List<TinNhanCaNhan> findByMaNhomSoloOrderByNgayGuiAsc(String maNhomSolo);
    
    List<TinNhanCaNhan> findByMaNhomSoloAndGhimTrueOrderByNgayGuiDesc(String maNhomSolo);
    Optional<TinNhanCaNhan> findTopByMaNhomSoloOrderByNgayGuiDesc(String maNhomSolo);
}
