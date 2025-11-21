package com.example.trendy_chat.repository;

import com.example.trendy_chat.entity.TinNhanReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TinNhanReactionRepository extends JpaRepository<TinNhanReaction, String> {
    List<TinNhanReaction> findByMaTinNhan(String maTinNhan);
    
    Optional<TinNhanReaction> findByMaTinNhanAndIdUser(String maTinNhan, String idUser);
    
    void deleteByMaTinNhanAndIdUser(String maTinNhan, String idUser);
}