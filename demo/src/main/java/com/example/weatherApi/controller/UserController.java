package com.example.weatherApi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.weatherApi.model.User;
import com.example.weatherApi.service.UserService;

import jakarta.servlet.http.HttpSession;


@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("/")
    public String showLogin() {
        return "index";
    }

    @PostMapping("/")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        User user = userService.login(username, password);
        if (user == null) {
            model.addAttribute("error", "Invalid username or password.");
            return "index";
        }
        session.setAttribute("loggedInUser", username);
        return "redirect:/location";
    }

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        String username = (String) session.getAttribute("loggedInUser");
        if (username == null) return "redirect:/";

        model.addAttribute("username", username);
        return "profile";
    }

    @PostMapping("/register")
public String register(@RequestParam String username,
                       @RequestParam String password,
                       Model model) {
    boolean success = userService.register(username, password);
    if (!success) {
        model.addAttribute("error", "Username already taken.");
        return "register";
    }
    return "redirect:/";
}

@GetMapping("/register")
public String showRegister() {
    return "register";
}

}

//Hashmap für User benutzen 
//User eigenschaften gegenspeichern, Sessions mit User-Objekt 
//Welcher User klickt auf welche bewertung -> normalerweise mit sternebewertung -> User mit Id 1 hat 3 sterne geklickt persistent -> Spring bean um zu welcher User eingeloggt ist -> simuliert spring 