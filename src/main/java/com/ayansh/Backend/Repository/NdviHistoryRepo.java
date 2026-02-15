package com.ayansh.Backend.Repository;

import com.ayansh.Backend.Model.NdviHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NdviHistoryRepo extends JpaRepository<NdviHistory, Long> {
        List<NdviHistory> findByAgroPolygonIdOrderByDateAsc(String agroPolygonId);
    }

