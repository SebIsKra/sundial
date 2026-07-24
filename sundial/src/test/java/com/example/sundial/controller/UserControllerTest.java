package com.example.sundial.controller;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.example.sundial.model.Cafe;
import com.example.sundial.model.User;
import com.example.sundial.repository.CafeLocationRepository;
import com.example.sundial.service.UserService;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CafeLocationRepository cafeLocationRepository;

    private User testUser;
    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "password123");
        testUser.setId(1L);

        session = new MockHttpSession();
        session.setAttribute("loggedInUser", testUser);
    }

    @Test
    void showLogin_shouldReturnIndexView() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(view().name("index"));
    }

    @Test
    void login_shouldRedirectToLocation_whenCredentialsAreCorrect() throws Exception {
        when(userService.login("testuser", "password123"))
            .thenReturn(testUser);

        mockMvc.perform(post("/")
            .param("username", "testuser")
            .param("password", "password123"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/location"));
    }

    @Test
    void login_shouldReturnIndexWithError_whenCredentialsAreWrong() throws Exception {
        when(userService.login("testuser", "wrongpassword"))
            .thenReturn(null);

        mockMvc.perform(post("/")
            .param("username", "testuser")
            .param("password", "wrongpassword"))
            .andExpect(status().isOk())
            .andExpect(view().name("index"))
            .andExpect(model().attributeExists("error"));
    }

    @Test
    void login_shouldStoreUserInSession_whenCredentialsAreCorrect() throws Exception {
        when(userService.login("testuser", "password123"))
            .thenReturn(testUser);

        mockMvc.perform(post("/")
            .param("username", "testuser")
            .param("password", "password123"))
            .andExpect(request().sessionAttribute("loggedInUser", testUser));
    }

    @Test
    void showRegister_shouldReturnRegisterView() throws Exception {
        mockMvc.perform(get("/register"))
            .andExpect(status().isOk())
            .andExpect(view().name("register"));
    }

    @Test
    void register_shouldRedirectToLogin_whenRegistrationSucceeds() throws Exception {
        when(userService.register("newuser", "password123"))
            .thenReturn(true);

        mockMvc.perform(post("/register")
            .param("username", "newuser")
            .param("password", "password123"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"));
    }

    @Test
    void registerShouldReturnErrorWhenUsernameIsTaken() throws Exception {
        when(userService.register("existinguser", "password123"))
            .thenReturn(false);

        mockMvc.perform(post("/register")
            .param("username", "existinguser")
            .param("password", "password123"))
            .andExpect(status().isOk())
            .andExpect(view().name("register"))
            .andExpect(model().attributeExists("error"));
    }

    @Test
    void favoritesShouldRedirectToLoginWhenNotLoggedIn() throws Exception {
        mockMvc.perform(get("/favorites"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"));
    }

    @Test
    void favorites_shouldReturnFavoritesPage_whenLoggedIn() throws Exception {
        when(cafeLocationRepository.findByUserAndFavouriteTrue(testUser))
            .thenReturn(List.of());

        mockMvc.perform(get("/favorites").session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("favorites"));
    }

    @Test
    void favorites_shouldAddUsernameToUser_whenLoggedIn() throws Exception {
        when(cafeLocationRepository.findByUserAndFavouriteTrue(testUser))
            .thenReturn(List.of());

        mockMvc.perform(get("/favorites").session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("username", "testuser"));
    }

    @Test
    void favorites_shouldAddFavouritesToUser_whenLoggedIn() throws Exception {
        Cafe cafe1 = new Cafe("Café Einstein", 52.5, 13.4);
        cafe1.setFavourite(true);
        Cafe cafe2 = new Cafe("Bonanza Coffee", 52.6, 13.5);
        cafe2.setFavourite(true);

        when(cafeLocationRepository.findByUserAndFavouriteTrue(testUser))
            .thenReturn(List.of(cafe1, cafe2));

        mockMvc.perform(get("/favorites").session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("favourites", List.of(cafe1, cafe2)));
    }
}