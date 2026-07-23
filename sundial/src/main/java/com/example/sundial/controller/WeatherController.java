package com.example.sundial.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.example.sundial.model.Cafe;
import com.example.sundial.model.User;
import com.example.sundial.model.WeatherResponse;
import com.example.sundial.repository.CafeLocationRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class WeatherController {

    private String baseUrl = "https://api.openweathermap.org";
    private String apiKey="96ae788f4a1c22ba0b688caff214cd92";

    private final CafeLocationRepository cafeLocationRepository;

    public WeatherController(CafeLocationRepository cafeLocationRepository) {
        this.cafeLocationRepository = cafeLocationRepository;
    }
    
    // add to WeatherController — minimal, test-only hook

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;

    }

    // ── Session helper ────────────────────────────────────────

    private User getLoggedInUser(HttpSession session) {
        return (User) session.getAttribute("loggedInUser");
    }

    // ── Location page ─────────────────────────────────────────

    @GetMapping("/location")
    public String getLocation(HttpSession session) {
        if (getLoggedInUser(session) == null) return "redirect:/";
        return "location";
    }

    // ── Weather page ──────────────────────────────────────────

    @GetMapping("/weather")
    public String getWeather(@RequestParam String lat,
                             @RequestParam String lon,
                             HttpSession session,
                             Model model) {
        if (getLoggedInUser(session) == null) return "redirect:/";

        String url = baseUrl + "/data/2.5/weather"
                   + "?lat=" + lat
                   + "&lon=" + lon
                   + "&units=metric&lang=en&appid=" + apiKey;

        RestTemplate restTemplate = new RestTemplate();
        WeatherResponse response = restTemplate.getForObject(url, WeatherResponse.class);

        model.addAttribute("weather", response);
        model.addAttribute("lat", lat);
        model.addAttribute("lon", lon);

        return "weather";
    }

    // ── Weather data as JSON ──────────────────────────────────

    @GetMapping("/weather-data")
    @ResponseBody
    public Map<String, Object> getWeatherData(@RequestParam String lat,
                                               @RequestParam String lon,
                                               HttpSession session) {
        if (getLoggedInUser(session) == null) {
            return Map.of("error", "not logged in");
        }

        String url = baseUrl + "/data/2.5/weather"
                   + "?lat=" + lat
                   + "&lon=" + lon
                   + "&units=metric&lang=en&appid=" + apiKey;

        RestTemplate restTemplate = new RestTemplate();
        WeatherResponse response = restTemplate.getForObject(url, WeatherResponse.class);

        int clouds = response.getClouds().getAll();

        Map<String, Object> result = new HashMap<>();
        result.put("clouds", clouds);
        result.put("lat", lat);
        result.put("lon", lon);
        return result;
    }

    // ── Café check + save ─────────────────────────────────────

    @PostMapping("/cafes/check")
    @ResponseBody
    public List<Cafe> checkCafes(@RequestBody List<Cafe> cafes,
                                          HttpSession session) {
        User user = getLoggedInUser(session);
        if (user == null) return List.of();

        System.out.println(">>> /cafes/check called with "
            + cafes.size() + " cafés for user: " + user.getUsername());

        List<Cafe> result = new ArrayList<>();

        for (Cafe cafe : cafes) {
            String url = baseUrl + "/data/2.5/weather"
                       + "?lat=" + cafe.getLatitude()
                       + "&lon=" + cafe.getLongitude()
                       + "&units=metric&appid=" + apiKey;

            try {
                RestTemplate restTemplate = new RestTemplate();
                WeatherResponse weather = restTemplate.getForObject(url, WeatherResponse.class);

                if (weather != null && weather.getClouds() != null) {
                    int clouds = weather.getClouds().getAll();
                    cafe.setCloudiness(clouds);
                    cafe.setSunny(clouds < 15);
                } else {
                    cafe.setCloudiness(0);
                    cafe.setSunny(false);
                }
            } catch (Exception e) {
                System.out.println(">>> Weather call failed for: "
                    + cafe.getName() + " - " + e.getMessage());
                cafe.setCloudiness(-1);  // -1 means unknown
                cafe.setSunny(false);
            }

            result.add(cafe);
        }

       // Save per user — create new or update changed data
        for (Cafe cafe : result) {

    cafeLocationRepository.findByNameAndLatitudeAndLongitudeAndUser(
            cafe.getName(),
            cafe.getLatitude(),
            cafe.getLongitude(),
            user
    ).ifPresentOrElse(existing -> {

        boolean changed = false;

        if (!java.util.Objects.equals(existing.getAddress(), cafe.getAddress())) {
            existing.setAddress(cafe.getAddress());
            changed = true;
        }

        if (!java.util.Objects.equals(existing.getDescription(), cafe.getDescription())) {
            existing.setDescription(cafe.getDescription());
            changed = true;
        }

        if (existing.isSunny() != cafe.isSunny()) {
            existing.setSunny(cafe.isSunny());
            changed = true;
        }

        if (changed) {
            cafeLocationRepository.save(existing);
        }

        // return the database version
        cafe.setId(existing.getId());
        cafe.setFavourite(existing.isFavourite());

    }, () -> {

        cafe.setUser(user);
        cafe.setFavourite(false);

        cafeLocationRepository.save(cafe);

    });
}




        System.out.println(">>> Save complete for user: " + user.getUsername());

        return result;
    }

    // ── Toggle favourite ──────────────────────────────────────

    @PostMapping("/cafes/favourite")
    @ResponseBody
    public ResponseEntity<Void> toggleFavourite(@RequestBody Cafe cafe,
                                                HttpSession session) {
        User user = getLoggedInUser(session);

        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        cafeLocationRepository.findByNameAndUser(cafe.getName(), user)
            .ifPresent(existing -> {
                existing.setFavourite(cafe.isFavourite());
                cafeLocationRepository.save(existing);
            });

        return ResponseEntity.ok().build();
    }


    @PostMapping("/cafes/unfavourite")
    public String unfavourite(@RequestParam Long cafeId,
                            HttpSession session) {
        User user = getLoggedInUser(session);
        if (user == null) return "redirect:/";

        cafeLocationRepository.findById(cafeId).ifPresent(cafe -> {
            if (cafe.getUser().getId().equals(user.getId())) {
                cafe.setFavourite(false);
                cafeLocationRepository.save(cafe);
            }
        });

        return "redirect:/favorites";
    }
}