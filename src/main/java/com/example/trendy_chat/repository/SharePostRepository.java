package com.example.trendy_chat.repository;

import com.example.trendy_chat.entity.SharePost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SharePostRepository extends JpaRepository<SharePost, String> {
    List<SharePost> findByIdUserOrderByNgayChiaSeDesc(String idUser);
    List<SharePost> findByIdPost(String idPost);
}
