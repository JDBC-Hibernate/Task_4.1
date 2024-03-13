package com.itm.space.backendresources.controller;

import com.itm.space.backendresources.BaseIntegrationTest;
import com.itm.space.backendresources.api.request.UserRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static net.minidev.json.JSONValue.toJSONString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@WithMockUser(username = "robisho", password = "user", roles = "MODERATOR")
class UserControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mvc;
    private final String TEST_USER_ID = "ffb2600d-6367-410a-8a8c-3f526ca85d86";
    private final String TEST_USERNAME = "robisho";
    private final String TEST_EMAIL = "test_user@test.com";

    @Container
    private final static PostgreSQLContainer<?> pgContainer =
            new PostgreSQLContainer<>("postgres:13.5");

    @DynamicPropertySource
    static void pgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> pgContainer.getJdbcUrl());
        registry.add("spring.datasource.username", () -> pgContainer.getUsername());
        registry.add("spring.datasource.password", () -> pgContainer.getPassword());
    }

    @Test
    void testCreateUser() throws Exception {
        UserRequest userRequest = new UserRequest(
                "johndoe",
                "johndoe@mail.com",
                "user",
                "John",
                "Doe"
        );
        String userString = toJSONString(userRequest);
        mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userString))
                .andDo(print()) //статический элемент из MockMvcResultHandlers
                .andExpect(status().isOk());

    }

    @Test
    void testGetUserById() throws Exception {
        String urlTemplate = "/api/users/{id}";
        RequestBuilder request = get(urlTemplate, TEST_USER_ID);
        String responseString = mvc.perform(request)
                .andExpect(status().isOk())
                .andReturn()
                        .getResponse()
                        .getContentAsString();
        assertNotNull(responseString);
        assertThat(responseString).contains(TEST_EMAIL);
        assertThat(responseString).contains(TEST_USERNAME);
        System.out.println("testController > testGetUserById > " + responseString);
    }

    @Test
    void testHello() throws Exception {
        String urlTemplate = "/api/users/hello";
        RequestBuilder request = get(urlTemplate);
        String responseString = mvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertNotNull(responseString);
        assertEquals(responseString, TEST_USERNAME);
        System.out.println("testController > testHello > " + responseString);
    }

}