package com.example.sundial.service;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.sundial.model.User;
import com.example.sundial.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void register_savesUser_whenUsernameFree() {
        when(userRepository.existsByUsername("alice")).thenReturn(false);

        boolean result = userService.register("alice", "pw");

        assertThat(result).isTrue();
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_fails_whenUsernameTaken() {
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        boolean result = userService.register("alice", "pw");

        assertThat(result).isFalse();
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_returnsUser_whenPasswordMatches() {
        User user = new User("alice", "pw");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        User result = userService.login("alice", "pw");

        assertThat(result).isSameAs(user);
    }

    @Test
    void login_returnsNull_whenPasswordWrong() {
        User user = new User("alice", "pw");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        User result = userService.login("alice", "wrong");

        assertThat(result).isNull();
    }

    @Test
    void login_returnsNull_whenUserUnknown() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        User result = userService.login("ghost", "pw");

        assertThat(result).isNull();
    }
}