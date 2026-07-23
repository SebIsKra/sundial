package com.example.sundial.repository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.example.sundial.model.User;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.save(new User("alice", "pw123"));
    }

    // ── findByUsername ─────────────────────────────────────────

    @Test
    void findByUsername_returnsUser_whenExists() {
        Optional<User> result = userRepository.findByUsername("alice");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("alice");
        assertThat(result.get().getPassword()).isEqualTo("pw123");
        assertThat(result.get().getId()).isNotNull(); // id generated on persist
    }

    @Test
    void findByUsername_returnsEmpty_whenUnknown() {
        Optional<User> result = userRepository.findByUsername("ghost");

        assertThat(result).isEmpty();
    }

    // ── existsByUsername ───────────────────────────────────────

    @Test
    void existsByUsername_returnsTrue_whenExists() {
        assertThat(userRepository.existsByUsername("alice")).isTrue();
    }

    @Test
    void existsByUsername_returnsFalse_whenUnknown() {
        assertThat(userRepository.existsByUsername("ghost")).isFalse();
    }

    // ── round-trip: save then load ─────────────────────────────

    @Test
    void save_persistsAndAssignsId() {
        User saved = userRepository.save(new User("bob", "secret"));

        assertThat(saved.getId()).isNotNull();
        assertThat(userRepository.findByUsername("bob")).isPresent();
    }
}