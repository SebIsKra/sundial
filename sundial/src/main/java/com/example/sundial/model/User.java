package com.example.sundial.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CafeLocation> cafeLocations = new ArrayList<>();

    public User() {}

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public Long getId(){ 
        return id; 
    }

    public void setId(Long id){ 
        this.id = id; 
    }

    public String getUsername(){ 
        return username; 
    }

    public void setUsername(String username){ 
        this.username = username; 
    }

    public String getPassword(){ 
        return password; 
    }

    public void setPassword(String password){ 
        this.password = password; 
    }

    public List<CafeLocation> getCafeLocations(){
        return cafeLocations; 
    }

    public void setCafeLocations(List<CafeLocation> cafeLocations) {
        this.cafeLocations = cafeLocations;
    }
}






