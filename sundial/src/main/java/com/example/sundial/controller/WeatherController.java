package com.example.sundial.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.example.sundial.model.CafeLocation;
import com.example.sundial.model.WeatherResponse;
import com.example.sundial.repository.CafeLocationRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class WeatherController {

    @Value("${weather.api.key}")
    private String apiKey;

    private final CafeLocationRepository cafeLocationRepository;

    public WeatherController(CafeLocationRepository cafeLocationRepository) {
        this.cafeLocationRepository = cafeLocationRepository;
    }


    @GetMapping("/weather")
    public String getWeather(@RequestParam String lat,
                             @RequestParam String lon,
                             HttpSession session,
                             Model model) {
        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/";
        }

        String url = "https://api.openweathermap.org/data/2.5/weather"
                   + "?lat=" + lat
                   + "&lon=" + lon
                   + "&units=metric&lang=en&appid=739a05de68ee3b3295f1de09756a422f";

        RestTemplate restTemplate = new RestTemplate();
        WeatherResponse response = restTemplate.getForObject(url, WeatherResponse.class);

        model.addAttribute("weather", response);
        model.addAttribute("lat", lat);
        model.addAttribute("lon", lon);

        return "weather";
    }


    @GetMapping("/weather-data")
    @ResponseBody
    public Map<String, Object> getWeatherData(@RequestParam String lat,
                                               @RequestParam String lon,
                                               HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) {
            return Map.of("error", "not logged in");
        }

        String url = "https://api.openweathermap.org/data/2.5/weather"
                   + "?lat=" + lat
                   + "&lon=" + lon
                   + "&units=metric&lang=en&appid=";

        RestTemplate restTemplate = new RestTemplate();
        WeatherResponse response = restTemplate.getForObject(url, WeatherResponse.class);

        int clouds = response.getClouds().getAll();

        Map<String, Object> result = new HashMap<>();
        result.put("clouds", clouds);
        result.put("lat", lat);
        result.put("lon", lon);
        return result;
    }

    @PostMapping("/cafes/check")
    @ResponseBody
    public List<CafeLocation> checkCafes(@RequestBody List<CafeLocation> cafes,
                                          HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) {
            return List.of();
        }

        List<CafeLocation> result = new ArrayList<>();

        for (CafeLocation cafe : cafes) {
            String url = "https://api.openweathermap.org/data/2.5/weather"
                       + "?lat=" + cafe.getLatitude()
                       + "&lon=" + cafe.getLongitude()
                       + "&units=metric&appid=";

            try {
                RestTemplate restTemplate = new RestTemplate();
                WeatherResponse weather = restTemplate.getForObject(url, WeatherResponse.class);
                int clouds = weather.getClouds().getAll();

                cafe.setCloudiness(clouds);
                cafe.setSunny(clouds < 15);
            } catch (Exception e) {
                cafe.setCloudiness(-1);
                cafe.setSunny(false);
            }

            result.add(cafe);
        }

        
        for (CafeLocation cafe : result) {
            if (!cafeLocationRepository.existsByNameAndLatitudeAndLongitude(
                    cafe.getName(), cafe.getLatitude(), cafe.getLongitude())) {
                cafeLocationRepository.save(cafe);
            }
        }


        return result;
    }

    @GetMapping("/location")
    public String getLocation(HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) return "redirect:/";
    return "location";  
    }


}
