package com.example.weatherApi.model;

import jakarta.persistence.*;

@Entity
@Table(name = "cafe_locations")
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
}
