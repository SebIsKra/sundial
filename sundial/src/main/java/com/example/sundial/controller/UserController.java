package com.example.sundial.controller;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.sundial.model.Cafe;
import com.example.sundial.model.User;
import com.example.sundial.repository.CafeLocationRepository;
import com.example.sundial.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class UserController {

    private final UserService userService;
    private final CafeLocationRepository cafeLocationRepository;

    public UserController(UserService userService, CafeLocationRepository cafeLocationRepository) {
        this.userService = userService;
        this.cafeLocationRepository = cafeLocationRepository;
    }

    //Index is login-page
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
        session.setAttribute("loggedInUser", user); // ← full User object
        return "redirect:/location";
    }


    //Register-page when you click on register button on login-page
    @GetMapping("/register")
    public String showRegister() {
        return "register";
    }

    //Post request that saves user-data and returns to login-page
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

    //Redirects to favorites page
    @GetMapping("/favorites")
    public String favorites(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/";
        }

        List<Cafe> favourites = cafeLocationRepository.findByUserAndFavouriteTrue(user);

        model.addAttribute("username", user.getUsername());
        model.addAttribute("favourites", favourites);

        return "favorites";
    }
}