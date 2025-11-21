package com.example.trendy_chat.repository;

import com.example.trendy_chat.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, CommentLike.CommentLikeId> {
    List<CommentLike> findByMaBinhLuan(String maBinhLuan);
    List<CommentLike> findByIdUser(String idUser);
    long countByMaBinhLuan(String maBinhLuan);
    boolean existsByMaBinhLuanAndIdUser(String maBinhLuan, String idUser);
}
