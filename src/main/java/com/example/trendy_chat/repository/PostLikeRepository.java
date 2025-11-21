package com.example.trendy_chat.repository;

import com.example.trendy_chat.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, PostLike.PostLikeId> {
    List<PostLike> findByIdPost(String idPost);
    List<PostLike> findByIdUser(String idUser);
    boolean existsByIdPostAndIdUser(String idPost, String idUser);
}
