package com.ayansh.Backend.Repository;

import com.ayansh.Backend.Model.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface PostImageRepository extends JpaRepository<PostImage, Long> {}

