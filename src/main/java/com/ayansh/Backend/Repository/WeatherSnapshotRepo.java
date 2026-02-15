package com.ayansh.Backend.Repository;

import com.ayansh.Backend.Model.WeatherSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeatherSnapshotRepo extends JpaRepository<WeatherSnapshot, Long> {

}
