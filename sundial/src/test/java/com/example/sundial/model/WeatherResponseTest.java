package com.example.sundial.model;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class WeatherResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ── plain POJO behaviour ───────────────────────────────────

    @Test
    void gettersAndSetters_workCorrectly() {
        WeatherResponse response = new WeatherResponse();
        response.setName("Berlin");

        WeatherResponse.Clouds clouds = new WeatherResponse.Clouds();
        clouds.setAll(75);
        response.setClouds(clouds);

        WeatherResponse.Main main = new WeatherResponse.Main();
        main.setTemp(21.5);
        response.setMain(main);

        assertThat(response.getName()).isEqualTo("Berlin");
        assertThat(response.getClouds().getAll()).isEqualTo(75);
        assertThat(response.getMain().getTemp()).isEqualTo(21.5);
    }

    @Test
    void nestedWeather_gettersAndSetters() {
        WeatherResponse.Weather weather = new WeatherResponse.Weather();
        weather.setId(800);
        weather.setDesc("clear sky");

        assertThat(weather.getId()).isEqualTo(800);
        assertThat(weather.getdesc()).isEqualTo("clear sky");
    }

    // ── deserialization from an OpenWeatherMap-shaped JSON ─────
    // This mirrors exactly what the WireMock stubs return, so it
    // proves the DTO maps correctly from the mocked API response.

    @Test
    void deserializesFromApiJson() throws Exception {
        String json = """
                {
                  "name": "Berlin",
                  "weather": [
                    { "id": 800, "desc": "clear sky" }
                  ],
                  "main": { "temp": 21.5 },
                  "clouds": { "all": 5 }
                }
                """;

        WeatherResponse response = objectMapper.readValue(json, WeatherResponse.class);

        assertThat(response.getName()).isEqualTo("Berlin");
        assertThat(response.getClouds().getAll()).isEqualTo(5);
        assertThat(response.getMain().getTemp()).isEqualTo(21.5);
        assertThat(response.getWeather()).hasSize(1);
        assertThat(response.getWeather().get(0).getId()).isEqualTo(800);
    }

    @Test
    void deserializesMinimalJson_withOnlyClouds() {
        String json = "{\"clouds\":{\"all\":80}}";

        WeatherResponse response;
        try {
            response = objectMapper.readValue(json, WeatherResponse.class);
        } catch (Exception e) {
            throw new AssertionError("Should deserialize minimal JSON", e);
        }

        assertThat(response.getClouds().getAll()).isEqualTo(80);
        assertThat(response.getName()).isNull();
    }
}