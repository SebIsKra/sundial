package com.example.weatherApi.controller.WeatherController;

import com.example.weatherApi.model.User;
import com.example.weatherApi.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.example.weatherApi.model.SavedLocation;
import com.example.weatherApi.service.UserService;


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

        List<SavedLocation> locations = userService.getSavedLocations(username);
        model.addAttribute("username", username);
        model.addAttribute("locations", locations);
        return "profile";
    }
}
