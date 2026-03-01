package com.ayansh.Backend.Repository;

import com.ayansh.Backend.Model.SoilTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SoilTestRepo extends JpaRepository<SoilTest,Long> {
    List<SoilTest> findByUserIdOrderByCreatedAtDesc(Long userId);
}
