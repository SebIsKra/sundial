package com.example.sundial.service;

import org.springframework.stereotype.Service;

import com.example.sundial.model.User;
import com.example.sundial.repository.UserRepository;


@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean register(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            return false;
        }
        userRepository.save(new User(username, password));
        return true;
    }

    public User login(String username, String password) {
        return userRepository.findByUsername(username)
                .filter(user -> user.getPassword().equals(password))
                .orElse(null);
    }
}
