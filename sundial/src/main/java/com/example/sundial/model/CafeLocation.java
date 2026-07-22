package com.example.sundial.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "cafe_locations")
@JsonIgnoreProperties(ignoreUnknown = true)
public class CafeLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private double latitude;
    private double longitude;
    private int cloudiness;
    private boolean sunny;
    private String description;
    private Boolean favourite = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"cafeLocations", "password"})
    private User user;

    public CafeLocation() {}

    public CafeLocation(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public int getCloudiness() { return cloudiness; }
    public void setCloudiness(int cloudiness) { this.cloudiness = cloudiness; }

    public boolean isSunny() { return sunny; }
    public void setSunny(boolean sunny) { this.sunny = sunny; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean isFavourite() { return favourite; }
    public void setFavourite(Boolean favourite) { this.favourite = favourite; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}