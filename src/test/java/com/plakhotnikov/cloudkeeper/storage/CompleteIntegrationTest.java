package com.plakhotnikov.cloudkeeper.storage;

import com.plakhotnikov.cloudkeeper.storage.repository.MinioRepository;
import com.plakhotnikov.cloudkeeper.user.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertTrue;


@Testcontainers
@SpringBootTest
@ActiveProfiles({"dev", "test"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Basic integration test")
@DirtiesContext
public class CompleteIntegrationTest {

    @Container
    protected static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withUsername("test")
            .withPassword("testpass");

    @Container
    protected static final MinIOContainer minio = new MinIOContainer("minio/minio:latest")
            .withUserName("test")
            .withPassword("testpass")
            .withExposedPorts(9000, 9001);

    @Container
    protected static final GenericContainer<?> redis = new GenericContainer<>("redis:latest")
            .withExposedPorts(6379)
            .withCommand("redis-server --requirepass testpass");


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MinioRepository minioRepository;


    @Test
    @Order(1)
    @DisplayName("Default MinIO bucket was created")
    public void defaultBucketWasCreated() {
        assertTrue(minioRepository.isDefaultBucketExist());
    }

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
    protected static void minioProperties(DynamicPropertyRegistry registry) {
        registry.add("minio.client.endpoint", () -> "http:/localhost:" + minio.getMappedPort(9000));
        registry.add("minio.client.user", () -> "test");
        registry.add("minio.client.password", () -> "testpass");
    }

    @DynamicPropertySource
    protected static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
        registry.add("spring.data.redis.password", () -> "testpass");
    }
}
