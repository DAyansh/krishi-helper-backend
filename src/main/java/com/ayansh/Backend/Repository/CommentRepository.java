package com.ayansh.Backend.Repository;

import com.ayansh.Backend.Model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    long countByPost_Id(Long postId);
}

