package com.plakhotnikov.cloudkeeper.storage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Testing mock requests on home page url")
public class HomeControllerTest extends CompleteIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Order(1)
    @WithAnonymousUser
    @DisplayName("GET home page for anonymous user -> 302 redirect to login")
    public void getHomePage_withAnonymousUser() throws Exception {
        mockMvc.perform(get("/")).andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/welcome"));
    }

    @Test
    @Order(2)
    @WithUserDetails()
    @DisplayName("GET home page for authenticated user -> 200 ok")
    public void getHomePage_withAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/")).andDo(print())
                .andExpect(view().name("storage/home"));
    }


}
