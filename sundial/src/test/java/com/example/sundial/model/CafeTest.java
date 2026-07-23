package com.example.sundial.model;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class CafeTest {

    @Test
    void constructor_setsNameLatLon() {
        Cafe cafe = new Cafe("Sunny Spot", 52.0, 13.0);

        assertThat(cafe.getName()).isEqualTo("Sunny Spot");
        assertThat(cafe.getLatitude()).isEqualTo(52.0);
        assertThat(cafe.getLongitude()).isEqualTo(13.0);
    }

    @Test
    void noArgsConstructor_createsCafeWithDefaults() {
        Cafe cafe = new Cafe();

        assertThat(cafe.getId()).isNull();
        assertThat(cafe.getName()).isNull();
        assertThat(cafe.isFavourite()).isFalse(); // field initialised to false
        assertThat(cafe.isSunny()).isFalse();
        assertThat(cafe.getCloudiness()).isEqualTo(0);
    }

    @Test
    void gettersAndSetters_workCorrectly() {
        Cafe cafe = new Cafe();
        User user = new User("alice", "pw");

        cafe.setId(5L);
        cafe.setName("Cafe Central");
        cafe.setLatitude(48.5);
        cafe.setLongitude(11.2);
        cafe.setCloudiness(42);
        cafe.setSunny(true);
        cafe.setDescription("Cosy corner");
        cafe.setAddress("Main Street 1");
        cafe.setFavourite(true);
        cafe.setUser(user);

        assertThat(cafe.getId()).isEqualTo(5L);
        assertThat(cafe.getName()).isEqualTo("Cafe Central");
        assertThat(cafe.getLatitude()).isEqualTo(48.5);
        assertThat(cafe.getLongitude()).isEqualTo(11.2);
        assertThat(cafe.getCloudiness()).isEqualTo(42);
        assertThat(cafe.isSunny()).isTrue();
        assertThat(cafe.getDescription()).isEqualTo("Cosy corner");
        assertThat(cafe.getAddress()).isEqualTo("Main Street 1");
        assertThat(cafe.isFavourite()).isTrue();
        assertThat(cafe.getUser()).isSameAs(user);
    }
}