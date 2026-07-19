package com.example.sundial.model;

public class SavedLocation {

    private Long id;
    private double latitude;
    private double longitude;
    private int rating;

    public SavedLocation(Long id, double latitude, double longitude, int rating) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rating = rating;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
}
