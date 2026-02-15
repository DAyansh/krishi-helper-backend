package com.ayansh.Backend.Repository;

import com.ayansh.Backend.Model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByRegionOrderByCreatedAtDesc(String region, Pageable pageable);
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
