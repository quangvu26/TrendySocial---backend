package com.example.trendy_chat.repository;

import com.example.trendy_chat.entity.TinNhanCaNhan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TinNhanCaNhanRepository extends JpaRepository<TinNhanCaNhan, UUID> {
    List<TinNhanCaNhan> findByMaNhomSoloOrderByNgayGuiAsc(UUID maNhomSolo);

    List<TinNhanCaNhan> findByMaNhomSoloAndGhimTrueOrderByNgayGuiDesc(UUID maNhomSolo);
    Optional<TinNhanCaNhan> findTopByMaNhomSoloOrderByNgayGuiDesc(UUID maNhomSolo);
}
