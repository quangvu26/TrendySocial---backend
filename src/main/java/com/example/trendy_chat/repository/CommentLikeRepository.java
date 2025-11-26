package com.example.trendy_chat.repository;

import com.example.trendy_chat.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, CommentLike.CommentLikeId> {
    List<CommentLike> findByMaBinhLuan(UUID maBinhLuan);
    List<CommentLike> findByIdUser(String idUser);
    long countByMaBinhLuan(UUID maBinhLuan);
    boolean existsByMaBinhLuanAndIdUser(UUID maBinhLuan, String idUser);
}
