package com.example.sundial.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.example.sundial.model.Cafe;
import com.example.sundial.model.User;

@DataJpaTest
class CafeLocationRepositoryTest {

    @Autowired
    private CafeLocationRepository cafeLocationRepository;

    @Autowired
    private UserRepository userRepository;

    private User alice;
    private User bob;

    @BeforeEach
    void setUp() {
        alice = userRepository.save(new User("alice", "pw"));
        bob = userRepository.save(new User("bob", "pw"));

        // alice: two cafés, one favourite
        Cafe sunny = new Cafe("Sunny Spot", 52.0, 13.0);
        sunny.setUser(alice);
        sunny.setFavourite(true);
        sunny.setAddress("Main Street 1");

        Cafe shady = new Cafe("Shady Spot", 48.0, 11.0);
        shady.setUser(alice);
        shady.setFavourite(false);

        // bob: one café (to prove per-user scoping)
        Cafe bobsCafe = new Cafe("Bobs Cafe", 40.0, 10.0);
        bobsCafe.setUser(bob);
        bobsCafe.setFavourite(true);

        cafeLocationRepository.save(sunny);
        cafeLocationRepository.save(shady);
        cafeLocationRepository.save(bobsCafe);
    }

    // ── existsByNameAndLatitudeAndLongitudeAndUser ─────────────

    @Test
    void existsByNameAndLatLonAndUser_true_whenMatch() {
        boolean exists = cafeLocationRepository
                .existsByNameAndLatitudeAndLongitudeAndUser("Sunny Spot", 52.0, 13.0, alice);
        assertThat(exists).isTrue();
    }

    @Test
    void existsByNameAndLatLonAndUser_false_whenDifferentUser() {
        // same café coordinates/name, but queried for the wrong user
        boolean exists = cafeLocationRepository
                .existsByNameAndLatitudeAndLongitudeAndUser("Sunny Spot", 52.0, 13.0, bob);
        assertThat(exists).isFalse();
    }

    @Test
    void existsByNameAndLatLonAndUser_false_whenNoMatch() {
        boolean exists = cafeLocationRepository
                .existsByNameAndLatitudeAndLongitudeAndUser("Nowhere", 0.0, 0.0, alice);
        assertThat(exists).isFalse();
    }

    // ── findByNameAndUser ──────────────────────────────────────

    @Test
    void findByNameAndUser_returnsCafe_whenMatch() {
        Optional<Cafe> result = cafeLocationRepository.findByNameAndUser("Sunny Spot", alice);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Sunny Spot");
        assertThat(result.get().getUser().getId()).isEqualTo(alice.getId());
    }

    @Test
    void findByNameAndUser_empty_whenWrongUser() {
        Optional<Cafe> result = cafeLocationRepository.findByNameAndUser("Sunny Spot", bob);
        assertThat(result).isEmpty();
    }

    // ── findByNameAndLatitudeAndLongitudeAndUser ───────────────

    @Test
    void findByNameLatLonAndUser_returnsCafe_whenMatch() {
        Optional<Cafe> result = cafeLocationRepository
                .findByNameAndLatitudeAndLongitudeAndUser("Shady Spot", 48.0, 11.0, alice);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Shady Spot");
    }

    @Test
    void findByNameLatLonAndUser_empty_whenCoordsDiffer() {
        Optional<Cafe> result = cafeLocationRepository
                .findByNameAndLatitudeAndLongitudeAndUser("Shady Spot", 99.0, 99.0, alice);
        assertThat(result).isEmpty();
    }

    // ── findByUser ─────────────────────────────────────────────

    @Test
    void findByUser_returnsOnlyThatUsersCafes() {
        List<Cafe> aliceCafes = cafeLocationRepository.findByUser(alice);
        List<Cafe> bobCafes = cafeLocationRepository.findByUser(bob);

        assertThat(aliceCafes).hasSize(2)
                .extracting(Cafe::getName)
                .containsExactlyInAnyOrder("Sunny Spot", "Shady Spot");
        assertThat(bobCafes).hasSize(1)
                .extracting(Cafe::getName)
                .containsExactly("Bobs Cafe");
    }

    // ── findByUserAndFavouriteTrue ─────────────────────────────

    @Test
    void findByUserAndFavouriteTrue_returnsOnlyFavourites() {
        List<Cafe> favourites = cafeLocationRepository.findByUserAndFavouriteTrue(alice);

        assertThat(favourites).hasSize(1);
        assertThat(favourites.get(0).getName()).isEqualTo("Sunny Spot");
        assertThat(favourites.get(0).isFavourite()).isTrue();
    }

    @Test
    void findByUserAndFavouriteTrue_empty_whenNoFavourites() {
        // give bob's only café a non-favourite state by adding a fresh user with none
        User carol = userRepository.save(new User("carol", "pw"));
        Cafe plain = new Cafe("Plain Cafe", 1.0, 1.0);
        plain.setUser(carol);
        plain.setFavourite(false);
        cafeLocationRepository.save(plain);

        List<Cafe> favourites = cafeLocationRepository.findByUserAndFavouriteTrue(carol);
        assertThat(favourites).isEmpty();
    }
}