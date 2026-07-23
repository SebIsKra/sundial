package com.example.sundial.controller;

import java.util.List;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

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

    @Test
    void showLogin_returnsIndex() throws Exception {
        mockMvc.perform(get("/"))
               .andExpect(status().isOk())
               .andExpect(view().name("index"));
    }

    @Test
    void login_redirectsToLocation_onSuccess() throws Exception {
        when(userService.login("alice", "pw")).thenReturn(new User("alice", "pw"));
        mockMvc.perform(post("/").param("username", "alice").param("password", "pw"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/location"));
    }

    @Test
    void login_returnsIndexWithError_onFailure() throws Exception {
        when(userService.login(anyString(), anyString())).thenReturn(null);
        mockMvc.perform(post("/").param("username", "alice").param("password", "wrong"))
               .andExpect(status().isOk())
               .andExpect(view().name("index"))
               .andExpect(model().attributeExists("error"));
    }

    @Test
    void register_redirectsToLogin_onSuccess() throws Exception {
        when(userService.register("bob", "pw")).thenReturn(true);
        mockMvc.perform(post("/register").param("username", "bob").param("password", "pw"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/"));
    }

    @Test
    void register_returnsRegisterWithError_whenTaken() throws Exception {
        when(userService.register(anyString(), anyString())).thenReturn(false);
        mockMvc.perform(post("/register").param("username", "bob").param("password", "pw"))
               .andExpect(status().isOk())
               .andExpect(view().name("register"))
               .andExpect(model().attributeExists("error"));
    }

    @Test
    void favorites_redirectsToLogin_whenNoSessionUser() throws Exception {
        mockMvc.perform(get("/favorites"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/"));
    }

    @Test
    void favorites_returnsFavoritesView_whenLoggedIn() throws Exception {
        User user = new User("alice", "pw");
        MockHttpSession httpSession = new MockHttpSession();
        httpSession.setAttribute("loggedInUser", user);
        when(cafeLocationRepository.findByUserAndFavouriteTrue(user)).thenReturn(List.of());

        mockMvc.perform(get("/favorites").session(httpSession))
               .andExpect(status().isOk())
               .andExpect(view().name("favorites"))
               .andExpect(model().attributeExists("username", "favourites"));
    }
}