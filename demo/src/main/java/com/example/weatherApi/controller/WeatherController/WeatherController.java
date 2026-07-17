package com.example.weatherApi.controller.WeatherController;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;


@Controller
public class WeatherController {


    @GetMapping("/")
    public String getIndex() {
        return "index";
    }

    @GetMapping("/weather")
    public String getCloudyness(@RequestParam("lat") String lat, @RequestParam("lon") String lon, Model model) {
        
        String url = "https://api.openweathermap.org/data/2.5/weather"
           + "?lat=" + lat
           + "&lon=" + lon
           + "&units=metric&lang=en&appid=716304b7bbb202347872a34cb167918d"; //API key is hardcoded and needs to be replaced with a secure method of storing API keys.
        //Here you need to use the longitude and altitude you get in the WeatherResponse class

        RestTemplate restTemplate = new RestTemplate();
        WeatherResponse weatherResponse = restTemplate.getForObject(url, WeatherResponse.class);
        if(weatherResponse != null) {
            model.addAttribute("lat", lat);
            model.addAttribute("lon", lon);
            model.addAttribute("temperature", weatherResponse.getMain().getTemp());
            model.addAttribute("Cloudyness", weatherResponse.getClouds().getAll());
            model.addAttribute("weatherDescription", weatherResponse.getWeather().get(0).getdesc());
        } else {
            model.addAttribute("error", "Failed to retrieve weather data. Sorry :(");
        }
        return "weather";
    }
    
    
}
