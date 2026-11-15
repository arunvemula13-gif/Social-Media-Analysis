package com.spedo.analytics.repository;

import com.spedo.analytics.entity.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    List<PostComment> findByPostIdOrderByCommentedAtDesc(Long postId);

    long countByPostIdAndSentiment(Long postId, String sentiment);

    void deleteByPostId(Long postId);
}
