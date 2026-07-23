package com.example.sundial.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SavedLocationTest {

    @Test
    void constructor_setsAllFields() {
        SavedLocation location = new SavedLocation(1L, 52.0, 13.0, true);

        assertThat(location.getId()).isEqualTo(1L);
        assertThat(location.getLatitude()).isEqualTo(52.0);
        assertThat(location.getLongitude()).isEqualTo(13.0);
        assertThat(location.isFavourite()).isTrue();
    }

    @Test
    void gettersAndSetters_workCorrectly() {
        SavedLocation location = new SavedLocation(1L, 0.0, 0.0, false);

        location.setId(99L);
        location.setLatitude(48.5);
        location.setLongitude(11.2);
        location.setFavourite(true);

        assertThat(location.getId()).isEqualTo(99L);
        assertThat(location.getLatitude()).isEqualTo(48.5);
        assertThat(location.getLongitude()).isEqualTo(11.2);
        assertThat(location.isFavourite()).isTrue();
    }

    @Test
    void favourite_canBeToggledFalse() {
        SavedLocation location = new SavedLocation(1L, 1.0, 1.0, true);

        location.setFavourite(false);

        assertThat(location.isFavourite()).isFalse();
    }
}
