package com.example.trendy_chat.repository;

import com.example.trendy_chat.entity.SavePost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SavePostRepository extends JpaRepository<SavePost, SavePost.SavePostId> {
    List<SavePost> findByIdUserOrderByNgayLuuDesc(String idUser);
    List<SavePost> findByIdPost(UUID idPost);
    boolean existsByIdPostAndIdUser(UUID idPost, String idUser);
}
