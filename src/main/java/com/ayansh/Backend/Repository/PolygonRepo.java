package com.ayansh.Backend.Repository;

import com.ayansh.Backend.Model.Polygon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PolygonRepo extends JpaRepository<Polygon, Long> {

    Polygon findByAgroPolygonId(String agroPolygonId);
}
