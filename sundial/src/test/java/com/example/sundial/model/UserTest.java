package com.example.sundial.model;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void constructor_setsUsernameAndPassword() {
        User user = new User("alice", "pw");

        assertThat(user.getUsername()).isEqualTo("alice");
        assertThat(user.getPassword()).isEqualTo("pw");
    }

    @Test
    void noArgsConstructor_createsEmptyUser() {
        User user = new User();

        assertThat(user.getUsername()).isNull();
        assertThat(user.getPassword()).isNull();
        assertThat(user.getId()).isNull();
    }

    @Test
    void gettersAndSetters_workCorrectly() {
        User user = new User();

        user.setId(1L);
        user.setUsername("bob");
        user.setPassword("secret");

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getUsername()).isEqualTo("bob");
        assertThat(user.getPassword()).isEqualTo("secret");
    }

    @Test
    void cafeLocations_defaultsToEmptyListAndIsSettable() {
        User user = new User();
        assertThat(user.getCafeLocations()).isEmpty();

        Cafe cafe = new Cafe("Spot", 1.0, 2.0);
        user.setCafeLocations(List.of(cafe));

        assertThat(user.getCafeLocations()).containsExactly(cafe);
    }
}
