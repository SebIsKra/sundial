package com.example.sundial.controller;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.sundial.model.Cafe;
import com.example.sundial.model.User;
import com.example.sundial.repository.CafeLocationRepository;
import com.github.tomakehurst.wiremock.WireMockServer;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import jakarta.servlet.http.HttpSession;

@ExtendWith(MockitoExtension.class)
class WeatherControllerTest {

    private WireMockServer wireMock;

    @Mock
    private CafeLocationRepository cafeLocationRepository;

    @Mock
    private HttpSession session;

    private WeatherController controller;
    private User user;

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(0); // 0 = random free port
        wireMock.start();

        controller = new WeatherController(cafeLocationRepository);
        controller.setBaseUrl("http://localhost:" + wireMock.port()); // Option B hook

        user = new User("alice", "pw");
        user.setId(1L);
        when(session.getAttribute("loggedInUser")).thenReturn(user);
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
    }

    // ── helpers ────────────────────────────────────────────────

    private void stubWeatherWithClouds(int clouds) {
        wireMock.stubFor(get(urlPathEqualTo("/data/2.5/weather"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"name\":\"Test\",\"clouds\":{\"all\":" + clouds + "}}")));
    }

    private void stubWeatherServerError() {
        wireMock.stubFor(get(urlPathEqualTo("/data/2.5/weather"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\":\"boom\"}")));
    }

    // ── happy path: sunny ──────────────────────────────────────

    @Test
    void checkCafes_marksSunny_whenCloudsBelowThreshold() {
        stubWeatherWithClouds(5); // < 15 -> sunny
        when(cafeLocationRepository.findByNameAndLatitudeAndLongitudeAndUser(
                any(), any(Double.class), any(Double.class), any()))
                .thenReturn(Optional.empty());

        Cafe cafe = new Cafe("Sunny Spot", 52.0, 13.0);
        List<Cafe> result = controller.checkCafes(List.of(cafe), session);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCloudiness()).isEqualTo(5);
        assertThat(result.get(0).isSunny()).isTrue();
        // new café -> saved
        verify(cafeLocationRepository).save(any(Cafe.class));
    }

    // ── happy path: cloudy ─────────────────────────────────────

    @Test
    void checkCafes_marksNotSunny_whenCloudsAtOrAboveThreshold() {
        stubWeatherWithClouds(80); // >= 15 -> not sunny
        when(cafeLocationRepository.findByNameAndLatitudeAndLongitudeAndUser(
                any(), any(Double.class), any(Double.class), any()))
                .thenReturn(Optional.empty());

        Cafe cafe = new Cafe("Cloudy Spot", 48.0, 11.0);
        List<Cafe> result = controller.checkCafes(List.of(cafe), session);

        assertThat(result.get(0).getCloudiness()).isEqualTo(80);
        assertThat(result.get(0).isSunny()).isFalse();
    }

    // ── failure branch: API error -> cloudiness -1, not sunny ──

    @Test
    void checkCafes_setsUnknown_whenApiFails() {
        stubWeatherServerError(); // 500 -> RestTemplate throws -> catch block
        when(cafeLocationRepository.findByNameAndLatitudeAndLongitudeAndUser(
                any(), any(Double.class), any(Double.class), any()))
                .thenReturn(Optional.empty());

        Cafe cafe = new Cafe("Broken Spot", 40.0, 10.0);
        List<Cafe> result = controller.checkCafes(List.of(cafe), session);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCloudiness()).isEqualTo(-1); // -1 = unknown
        assertThat(result.get(0).isSunny()).isFalse();
    }

    // ── existing café gets updated, not re-created ─────────────

    @Test
    void checkCafes_updatesExisting_whenSunnyChanged() {
        stubWeatherWithClouds(5); // -> sunny = true

        Cafe existing = new Cafe("Sunny Spot", 52.0, 13.0);
        existing.setId(99L);
        existing.setUser(user);
        existing.setSunny(false);      // differs from the new value -> triggers update
        existing.setFavourite(true);

        when(cafeLocationRepository.findByNameAndLatitudeAndLongitudeAndUser(
                any(), any(Double.class), any(Double.class), any()))
                .thenReturn(Optional.of(existing));

        Cafe incoming = new Cafe("Sunny Spot", 52.0, 13.0);
        List<Cafe> result = controller.checkCafes(List.of(incoming), session);

        // existing record was updated and saved
        assertThat(existing.isSunny()).isTrue();
        verify(cafeLocationRepository).save(existing);

        // returned café carries the DB id + favourite flag
        assertThat(result.get(0).getId()).isEqualTo(99L);
        assertThat(result.get(0).isFavourite()).isTrue();
    }

    // ── existing café unchanged -> no save ─────────────────────

    @Test
    void checkCafes_doesNotSave_whenExistingUnchanged() {
        stubWeatherWithClouds(5); // -> sunny = true

        Cafe existing = new Cafe("Sunny Spot", 52.0, 13.0);
        existing.setId(99L);
        existing.setUser(user);
        existing.setSunny(true);       // same as new value
        existing.setFavourite(false);
        // address & description both null on both sides -> nothing changed

        when(cafeLocationRepository.findByNameAndLatitudeAndLongitudeAndUser(
                any(), any(Double.class), any(Double.class), any()))
                .thenReturn(Optional.of(existing));

        Cafe incoming = new Cafe("Sunny Spot", 52.0, 13.0);
        controller.checkCafes(List.of(incoming), session);

        verify(cafeLocationRepository, never()).save(any(Cafe.class));
    }

    // ── not logged in -> empty list, no API call, no save ──────

    @Test
    void checkCafes_returnsEmpty_whenNotLoggedIn() {
        when(session.getAttribute("loggedInUser")).thenReturn(null);

        Cafe cafe = new Cafe("Whatever", 1.0, 1.0);
        List<Cafe> result = controller.checkCafes(List.of(cafe), session);

        assertThat(result).isEmpty();
        verify(cafeLocationRepository, never()).save(any(Cafe.class));
    }
}