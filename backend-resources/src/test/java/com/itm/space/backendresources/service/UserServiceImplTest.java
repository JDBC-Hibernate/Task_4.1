package com.itm.space.backendresources.service;

import com.itm.space.backendresources.BaseIntegrationTest;
import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.api.response.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@SpringBootTest
class UserServiceImplTest extends BaseIntegrationTest {

    @Autowired
    private UserService userService;

    private final String TEST_USER_ID = "ffb2600d-6367-410a-8a8c-3f526ca85d86";
    private final String TEST_USERNAME = "robisho";
    private final String TEST_EMAIL = "test_user@test.com";

    @Container
    private static final PostgreSQLContainer<?> pgContainer =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:13.5"))
                    .withDatabaseName("keycloak_db")
                    .withUsername("my_admin")
                    .withPassword("my_password");

    @Container
    private static final GenericContainer<?> kcContainer =
            new GenericContainer<>("quay.io/keycloak/keycloak:legacy")
                    .withEnv("KEYCLOAK_USER", "admin")
                    .withEnv("KEYCLOAK_PASSWORD", "admin");

    @DynamicPropertySource
    static void pgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> pgContainer.getJdbcUrl());
        registry.add("spring.datasource.username", () -> pgContainer.getUsername());
        registry.add("spring.datasource.password", () -> pgContainer.getPassword());

    }

    @Test
    void testCreateUser() {
        UserRequest userRequest = new UserRequest(
                "test_service",
                "test_service@mail.com",
                "user",
                "test",
                "service"
        );
        UserResponse createdUser = userService.createUser(userRequest);
        System.out.println("testService > createdUser > " + createdUser.toString());
        assertThat(createdUser).isNotNull();
        assertEquals(createdUser.getEmail(), userRequest.getEmail());

    }

    @Test
    void testGetUserById() {
        UUID userId = UUID.fromString(TEST_USER_ID);
        UserResponse testUser = userService.getUserById(userId);
        assertEquals(TEST_USERNAME, testUser.getFirstName());
        assertEquals(TEST_EMAIL, testUser.getEmail());
        System.out.println("testService > testGetUserById > " + testUser.toString());
    }
}