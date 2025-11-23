package com.plakhotnikov.cloudkeeper.user;

import com.plakhotnikov.cloudkeeper.security.SecurityConfig;
import com.plakhotnikov.cloudkeeper.user.service.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Testing mock authentication requests")
public class AuthControllerTest extends AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private SecurityConfig securityConfig;


    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("Testing registration requests")
    class RegistrationTest {
        @Test
        @Order(1)
        @WithAnonymousUser
        @DisplayName("GET registration page for anonymous user -> 200 ok")
        public void getRegistrationPage_withAnonymousUser() throws Exception {
            mockMvc.perform(get(securityConfig.getRegisterUrl()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/registration"))
                    .andExpect(model().attributeDoesNotExist("errorMessage"));
        }

        @Test
        @Order(2)
        @WithUserDetails()
        @DisplayName("GET registration page for authenticated user -> 302 redirect to home page")
        public void getRegistrationPage_withAuthenticatedUser() throws Exception {
            mockMvc.perform(get(securityConfig.getRegisterUrl()))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @Order(3)
        @WithAnonymousUser
        @DisplayName("Registering new user -> 302 redirect to login page and record added into database")
        public void registerUser() throws Exception {
            mockMvc.perform(post(securityConfig.getRegisterUrl())
                            .param("username", "test")
                            .param("password", "test")
                            .param("passwordConfirmation", "test")
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl(securityConfig.getLoginUrl()));

            Assertions.assertTrue(userService.findByUsername("test").isPresent());
        }

        @Test
        @Order(4)
        @WithAnonymousUser
        @DisplayName("Registering new user with existing username -> 200 ok and showing error message at registration page")
        public void registerUser_withExistingUsername() throws Exception {
            mockMvc.perform(post(securityConfig.getRegisterUrl())
                            .param("username", "user")
                            .param("password", "test")
                            .param("passwordConfirmation", "test")
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/registration"))
                    .andExpect(model().attributeExists("errorMessage"));
        }

        @Test
        @Order(5)
        @WithAnonymousUser
        @DisplayName("Registering new user and passwords do not match -> 200 ok and showing error message at registration page")
        public void registerUser_withDifferentPasswords() throws Exception {
            mockMvc.perform(post(securityConfig.getRegisterUrl())
                            .param("username", "user")
                            .param("password", "test")
                            .param("passwordConfirmation", "tset")
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/registration"))
                    .andExpect(model().attributeExists("errorMessage"));
        }


    }

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("Testing login requests")
    class LoginTest {
        @Test
        @Order(1)
        @WithAnonymousUser
        @DisplayName("GET login page for anonymous user -> 200 ok")
        public void getLoginPage_withAnonymousUser() throws Exception {
            mockMvc.perform(get(securityConfig.getLoginUrl()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(view().name("auth/login"));
        }

        @Test
        @Order(2)
        @WithUserDetails()
        @DisplayName("GET login page for authenticated user -> 302 redirect to home page")
        public void getLoginPage_withAuthenticatedUser() throws Exception {
            mockMvc.perform(get(securityConfig.getLoginUrl()))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }


        @Test
        @Order(3)
        @WithAnonymousUser
        @DisplayName("Login existing user -> 200 ok")
        public void loginUser() throws Exception {
            mockMvc.perform(formLogin(securityConfig.getLoginUrl())
                            .user("user")
                            .password("1234"))
                    .andDo(print())
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/"));
        }

        @Test
        @Order(4)
        @WithAnonymousUser
        @DisplayName("Login non-existing user -> 302 redirect login page with error param")
        public void loginNonExistentUser() throws Exception {
            mockMvc.perform(formLogin(securityConfig.getLoginUrl())
                            .user("user1")
                            .password("test"))
                    .andDo(print())
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("/**/%s?error".formatted(securityConfig.getLoginUrl())));
        }

        @Test
        @Order(5)
        @WithAnonymousUser
        @DisplayName("Login existing user with wrong password -> 302 redirect login page with error param")
        public void loginUser_withInvalidCredentials() throws Exception {
            mockMvc.perform(formLogin(securityConfig.getLoginUrl())
                            .user("user")
                            .password("test"))
                    .andDo(print())
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("/**/%s?error".formatted(securityConfig.getLoginUrl())));
        }


    }


    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("Testing logout requests")
    class LogoutTest {
        @Test
        @Order(1)
        @WithAnonymousUser
        @DisplayName("GET logout for anonymous user -> 302 redirect to login page")
        public void getLogout_withAnonymousUser() throws Exception {
            mockMvc.perform(get(securityConfig.getLogoutUrl()))
                    .andDo(print())
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("**%s".formatted(securityConfig.getWelcomeUrl())));
        }

        @Test
        @Order(2)
        @WithUserDetails()
        @DisplayName("GET logout for authenticated user -> 302 redirect to error (no such resource found)")
        public void getLogout_withAuthenticatedUser() throws Exception {
            mockMvc.perform(get(securityConfig.getLogoutUrl()))
                    .andDo(print())
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/error"));
        }

        @Test
        @Order(3)
        @WithUserDetails()
        @DisplayName("Logout existing user -> 302 redirect to login page")
        public void logoutUser() throws Exception {
            mockMvc.perform(logout(securityConfig.getLogoutUrl()))
                    .andDo(print())
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("/**%s".formatted(securityConfig.getWelcomeUrl())));
        }

        @Test
        @Order(4)
        @DisplayName("Logout anonymous user -> 302 redirect to login page")
        public void logoutAnonymousUser() throws Exception {
            mockMvc.perform(post(securityConfig.getLogoutUrl()))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }


    }

}
