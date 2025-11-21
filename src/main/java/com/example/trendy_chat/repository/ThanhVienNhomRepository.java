package com.example.trendy_chat.repository;

import com.example.trendy_chat.dto.ThanhVienNhomId;
import com.example.trendy_chat.entity.ThanhVienNhom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ThanhVienNhomRepository extends JpaRepository<ThanhVienNhom, ThanhVienNhomId> {
    boolean existsByMaNhomAndIdUser(String maNhom,String idUser);
    List<ThanhVienNhom> findByMaNhom(String maNhom);
    java.util.Optional<ThanhVienNhom> findByMaNhomAndIdUser(String maNhom, String idUser);
}
