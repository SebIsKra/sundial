package com.example.weatherApi.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.weatherApi.model.CafeLocation;

@Repository
public interface CafeLocationRepository extends JpaRepository<CafeLocation, Long> {
    List<CafeLocation> findBySunnyTrue();
    boolean existsByNameAndLatitudeAndLongitude(String name, double latitude, double longitude);
}


