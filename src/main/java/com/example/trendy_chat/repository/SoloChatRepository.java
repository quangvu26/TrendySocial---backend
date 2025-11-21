package com.example.trendy_chat.repository;

import com.example.trendy_chat.entity.SoloChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SoloChatRepository extends JpaRepository<SoloChat, String> {

    @Query("SELECT sc FROM SoloChat sc WHERE sc.id_user_1 = :userId OR sc.id_user_2 = :userId ORDER BY sc.idSoloChat DESC")
    List<SoloChat> findByUserId(@Param("userId") String userId);
    
    @Query("SELECT CASE WHEN COUNT(sc) > 0 THEN true ELSE false END FROM SoloChat sc " +
            "WHERE (sc.id_user_1 = :userA AND sc.id_user_2 = :userB) " +
            "OR (sc.id_user_1 = :userB AND sc.id_user_2 = :userA)")
    boolean existsBetweenUsers(@Param("userA") String userA, @Param("userB") String userB);
    
    @Query("SELECT sc FROM SoloChat sc " +
            "WHERE (sc.id_user_1 = :userA AND sc.id_user_2 = :userB) " +
            "OR (sc.id_user_1 = :userB AND sc.id_user_2 = :userA)")
    Optional<SoloChat> findBetweenUsers(@Param("userA") String userA, @Param("userB") String userB);
}