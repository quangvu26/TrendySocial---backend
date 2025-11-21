package com.example.trendy_chat.repository;

import com.example.trendy_chat.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, String> {
    List<Post> findByIdUserOrderByNgayDangDesc(String idUser);
    List<Post> findAllByOrderByNgayDangDesc();
}
