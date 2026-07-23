package com.example.sundial.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.sundial.model.Cafe;
import com.example.sundial.model.User;

@Repository
public interface CafeLocationRepository extends JpaRepository<Cafe, Long> {
    boolean existsByNameAndLatitudeAndLongitudeAndUser(
        String name, double latitude, double longitude, User user);
    Optional<Cafe> findByNameAndUser(String name, User user);
    Optional<Cafe> findByNameAndLatitudeAndLongitudeAndUser(
        String name,
        double latitude,
        double longitude,
        User user
    );
    List<Cafe> findByUser(User user);
    List<Cafe> findByUserAndFavouriteTrue(User user);
}