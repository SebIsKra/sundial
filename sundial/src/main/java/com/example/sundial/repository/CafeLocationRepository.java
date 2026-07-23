package com.example.sundial.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.sundial.model.CafeLocation;
import com.example.sundial.model.User;

@Repository
public interface CafeLocationRepository extends JpaRepository<CafeLocation, Long> {
    boolean existsByNameAndLatitudeAndLongitudeAndUser(
        String name, double latitude, double longitude, User user);
    Optional<CafeLocation> findByNameAndUser(String name, User user);
    Optional<CafeLocation> findByNameAndLatitudeAndLongitudeAndUser(
        String name,
        double latitude,
        double longitude,
        User user
    );
    List<CafeLocation> findByUser(User user);
    List<CafeLocation> findByUserAndFavouriteTrue(User user);
    Optional<CafeLocation> findByOverpassIdAndUser(
        Long overpassId,
        User user
    );
}