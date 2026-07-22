package com.example.sundial.model;

public class SavedLocation {

    private Long id;
    private double latitude;
    private double longitude;
    private boolean favourite;

    public SavedLocation(Long id, double latitude, double longitude, boolean favourite) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.favourite = favourite;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public boolean isFavourite() { return favourite; }
    public void setFavourite(boolean favourite) { this.favourite = favourite; }
}
