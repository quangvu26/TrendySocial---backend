package com.example.trendy_chat.repository;

import com.example.trendy_chat.entity.TinNhanReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TinNhanReactionRepository extends JpaRepository<TinNhanReaction, UUID> {
    List<TinNhanReaction> findByMaTinNhan(UUID maTinNhan);
    
    Optional<TinNhanReaction> findByMaTinNhanAndIdUser(UUID maTinNhan, String idUser);
    
    void deleteByMaTinNhanAndIdUser(UUID maTinNhan, String idUser);
}