package com.example.trendy_chat.repository;

import com.example.trendy_chat.entity.SavePost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavePostRepository extends JpaRepository<SavePost, SavePost.SavePostId> {
    List<SavePost> findByIdUserOrderByNgayLuuDesc(String idUser);
    List<SavePost> findByIdPost(String idPost);
    boolean existsByIdPostAndIdUser(String idPost, String idUser);
}
