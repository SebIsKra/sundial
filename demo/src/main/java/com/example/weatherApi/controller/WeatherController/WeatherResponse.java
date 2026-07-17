package com.example.weatherApi.controller.WeatherController;

import java.util.List;

import com.example.weatherApi.controller.WeatherController.WeatherResponse.Clouds;

public class WeatherResponse {

    private String name;
    private List<Weather> weather;
    private Main main;
    private Clouds clouds;
    

    public Clouds getClouds() { return this.clouds; }
    public void setClouds(Clouds clouds) { this.clouds = clouds; }

    public String getName(){
        return this.name;
    }

    public void setName(String name){
        this.name = name;
    }

    public List<Weather> getWeather(){
        return this.weather;
    }

    public void setWeather(List<Weather> weather){
        this.weather = weather;
    }

    public Main getMain(){
        return this.main;
    }

    public void setMain(Main main){
        this.main = main;
    }

    public static class Weather{
        private int id;
        private String description;

        public int getId(){
            return this.id;
        }

        public void setId(int newId){
            this.id = newId;
        }

        public String getdesc(){
            return this.description;
        }

        public void setDesc(String newDesc){
            this.description = newDesc;
        }
    }

    public static class Clouds {
    private int all;
    public int getAll() { return this.all; }
    public void setAll(int all) { this.all = all; }
    }

    public static class Main{
        
        private double temp;
        private int clouds;

        public double getTemp(){
            return this.temp;
        }

        public void setTemp(double newTemp){
            temp = newTemp;
        }
    }

}
