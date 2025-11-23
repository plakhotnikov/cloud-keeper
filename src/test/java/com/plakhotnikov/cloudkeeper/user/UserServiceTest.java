package com.plakhotnikov.cloudkeeper.user;

import com.plakhotnikov.cloudkeeper.user.model.Role;
import com.plakhotnikov.cloudkeeper.user.model.UserDto;
import com.plakhotnikov.cloudkeeper.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;


@Testcontainers
@SpringBootTest
@ActiveProfiles({"auth", "test"})
public class UserServiceTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withUsername("test")
            .withPassword("testpass");

    @Autowired
    private UserService userService;


    @Test
    @DisplayName("Register new user")
    public void registerNewUser() {
        UserDto user = new UserDto("user2", "pass", "pass", Role.USER);
        assertDoesNotThrow(() -> userService.register(user));
        assertTrue(userService.findByUsername("user2").isPresent());
    }

    @Test
    @DisplayName("Register new user with existing username")
    public void registerNewUser_withExistingUsername() {
        UserDto user = new UserDto("user", "pass", "pass", Role.USER);
        assertThrows(DataIntegrityViolationException.class, () -> userService.register(user));
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.name", postgres::getDatabaseName);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

}
