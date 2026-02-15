package com.ayansh.Backend.Repository;

import com.ayansh.Backend.Model.SoilTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SoilTestRepo extends JpaRepository<SoilTest,Long> {
}
