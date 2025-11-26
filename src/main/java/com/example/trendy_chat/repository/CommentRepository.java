package com.example.trendy_chat.repository;

import com.example.trendy_chat.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findByIdPostOrderByNgayBinhLuanDesc(UUID idPost);
    List<Comment> findByIdPostAndMaTraLoiIsNullOrderByNgayBinhLuanDesc(UUID idPost);
    List<Comment> findByIdPostAndMaTraLoiIsNullAndAnBinhLuanFalseOrderByNgayBinhLuanDesc(UUID idPost);
    List<Comment> findByIdPost(UUID idPost);
    List<Comment> findByMaTraLoi(UUID maTraLoi);
    List<Comment> findByMaTraLoiAndAnBinhLuanFalse(UUID maTraLoi);
    List<Comment> findByIdUser(String idUser);
    long countByIdPost(UUID idPost);
}
