package com.example.weatherApi.service;

import com.example.weatherApi.model.SavedLocation;
import com.example.weatherApi.model.User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserService {

    // Stand-in for the database — your teammate replaces this later
    private final Map<String, User> userStore = new HashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);
    private final AtomicLong locationIdCounter = new AtomicLong(1);

    public boolean register(String username, String password) {
        if (userStore.containsKey(username)) {
            return false; // username already taken
        }
        User user = new User(idCounter.getAndIncrement(), username, password);
        userStore.put(username, user);
        return true;
    }

    public User login(String username, String password) {
        User user = userStore.get(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    public boolean saveLocation(String username, double lat, double lon, int rating) {
        User user = userStore.get(username);
        if (user == null) return false;

        SavedLocation location = new SavedLocation(
            locationIdCounter.getAndIncrement(), lat, lon, rating
        );
        user.getSavedLocations().add(location);
        return true;
    }

    public List<SavedLocation> getSavedLocations(String username) {
        User user = userStore.get(username);
        if (user == null) return List.of();
        return user.getSavedLocations();
    }

    public UserService() {
        userStore.put("test", new User(idCounter.getAndIncrement(), "test", "test123"));
    }
}

