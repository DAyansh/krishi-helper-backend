package com.ayansh.Backend.Repository;

import com.ayansh.Backend.Model.Comment;
import com.ayansh.Backend.Model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;



@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    long countByPost_Id(Long postId);

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.post = :post")
    void deleteCommentByPost(@Param("post") Post post);
}

