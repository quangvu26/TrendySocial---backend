package com.example.trendy_chat.service;

import com.example.trendy_chat.entity.Comment;
import com.example.trendy_chat.entity.CommentLike;
import com.example.trendy_chat.repository.CommentRepository;
import com.example.trendy_chat.repository.CommentLikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CommentService {
    
    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private CommentLikeRepository commentLikeRepository;
    

    @Transactional
    public void deleteCommentWithReplies(String commentId) {
        List<Comment> replies = commentRepository.findByMaTraLoi(commentId);
        for (Comment reply : replies) {
            deleteCommentWithReplies(reply.getMaBinhLuan());
        }
        deleteCommentLikesOnly(commentId);
        commentRepository.deleteById(commentId);
        System.out.println("✅ Deleted comment: " + commentId);
    }
    

    private void deleteCommentLikesOnly(String commentId) {
        List<CommentLike> likes = commentLikeRepository.findByMaBinhLuan(commentId);
        commentLikeRepository.deleteAll(likes);
    }
    

    @Transactional
    public void likeComment(String commentId, String userId) {
        CommentLike.CommentLikeId id = new CommentLike.CommentLikeId(commentId, userId);
        
        if (!commentLikeRepository.existsById(id)) {
            CommentLike like = new CommentLike();
            like.setMaBinhLuan(commentId);
            like.setIdUser(userId);
            like.setNgayThich(LocalDateTime.now());
            commentLikeRepository.save(like);
            System.out.println("✅ Comment liked by user: " + userId + " for comment: " + commentId);
        }
    }
    

    @Transactional
    public void unlikeComment(String commentId, String userId) {
        CommentLike.CommentLikeId id = new CommentLike.CommentLikeId(commentId, userId);
        commentLikeRepository.deleteById(id);
        System.out.println("✅ Comment unliked by user: " + userId + " for comment: " + commentId);
    }
    

    public boolean isCommentLikedByUser(String commentId, String userId) {
        return commentLikeRepository.existsByMaBinhLuanAndIdUser(commentId, userId);
    }
    

    public long getCommentLikeCount(String commentId) {
        return commentLikeRepository.countByMaBinhLuan(commentId);
    }
    

    @Transactional
    public Comment createComment(String postId, String userId, String content, String replyToId) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content cannot be empty");
        }
        
        Comment comment = new Comment();
        comment.setMaBinhLuan(UUID.randomUUID().toString());
        comment.setIdPost(postId);
        comment.setIdUser(userId);
        comment.setNoiDung(content);
        comment.setNgayBinhLuan(LocalDateTime.now());
        
        if (replyToId != null && !replyToId.isEmpty()) {
            comment.setMaTraLoi(replyToId);
        }
        
        return commentRepository.save(comment);
    }


    public List<Comment> getParentComments(String postId) {
        return commentRepository.findByIdPostAndMaTraLoiIsNullOrderByNgayBinhLuanDesc(postId);
    }


    public List<Comment> getReplies(String commentId) {
        return commentRepository.findByMaTraLoi(commentId);
    }
}
