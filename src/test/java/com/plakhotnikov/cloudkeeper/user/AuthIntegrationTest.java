package com.plakhotnikov.cloudkeeper.user;

import com.plakhotnikov.cloudkeeper.user.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertTrue;


@Testcontainers
@SpringBootTest
@ActiveProfiles({"auth", "web", "test"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Basic RDB and Redis integration test")
@DirtiesContext
public class AuthIntegrationTest {

    @Container
    protected static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withUsername("test")
            .withPassword("testpass");

    @Container
    protected static final GenericContainer<?> redis = new GenericContainer<>("redis:latest")
            .withExposedPorts(6379)
            .withCommand("redis-server --requirepass testpass");


    @Autowired
    private UserRepository userRepository;


    @Test
    @Order(1)
    @DisplayName("Test data was loaded into relative database")
    public void testUserDataWasLoaded() {
        assertTrue(userRepository.findByUsername("user").isPresent());
    }


    @DynamicPropertySource
    protected static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.name", postgres::getDatabaseName);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }


    @DynamicPropertySource
    protected static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("spring.data.redis.password", () -> "testpass");
    }
}
