package com.example.trendy_chat.repository;

import com.example.trendy_chat.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {
    List<Comment> findByIdPostOrderByNgayBinhLuanDesc(String idPost);
    List<Comment> findByIdPostAndMaTraLoiIsNullOrderByNgayBinhLuanDesc(String idPost);
    List<Comment> findByIdPostAndMaTraLoiIsNullAndAnBinhLuanFalseOrderByNgayBinhLuanDesc(String idPost);
    List<Comment> findByIdPost(String idPost);
    List<Comment> findByMaTraLoi(String maTraLoi);
    List<Comment> findByMaTraLoiAndAnBinhLuanFalse(String maTraLoi);
    List<Comment> findByIdUser(String idUser);
    long countByIdPost(String idPost);
}
