package com.example.trendy_chat.repository;

import com.example.trendy_chat.entity.SharePost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SharePostRepository extends JpaRepository<SharePost, UUID> {
    List<SharePost> findByIdUserOrderByNgayChiaSeDesc(String idUser);
    List<SharePost> findByIdPost(UUID idPost);
}
