package com.ayansh.Backend.Repository;

import com.ayansh.Backend.Model.GovernmentScheme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GovernmentSchemeRepo extends JpaRepository<GovernmentScheme,Long> {
    List<GovernmentScheme> findByIsActiveTrue();
    Optional<GovernmentScheme> findBySchemeNameIgnoreCase(String schemeName);
}
