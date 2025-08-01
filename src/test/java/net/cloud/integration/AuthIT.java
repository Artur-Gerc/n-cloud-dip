package net.cloud.integration;

import net.cloud.dto.AuthenticationResponse;
import net.cloud.repository.UserRepository;
import net.cloud.security.JwtBlackList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureWebTestClient
public class AuthIT {

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtBlackList jwtBlackList;

    private final String requestBody = """
            {
              "login": "user",
              "password": "password"
            }
            """;

    @Container
    private static final PostgreSQLContainer<?> POSTGRE_SQL_CONTAINER = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("test-db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    private static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRE_SQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.password", POSTGRE_SQL_CONTAINER::getPassword);
        registry.add("spring.datasource.username", POSTGRE_SQL_CONTAINER::getUsername);
        registry.add("spring.liquibase.enabled", () -> "true");
        registry.add("spring.liquibase.change-log", () -> "classpath:/db/changelog/db.changelog-master.yaml");
    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void signUpTest() {
        webClient.post().uri("/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.auth-token").exists()
                .jsonPath("$.auth-token").isNotEmpty();
    }

    @Test
    void loginTest() {
        webClient.post().uri("/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange();

        webClient.post().uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.auth-token").exists()
                .jsonPath("$.auth-token").isNotEmpty();
    }

    @Test
        void logoutTest() {
        webClient.post().uri("/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange();

        String authToken = webClient.post().uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthenticationResponse.class)
                .returnResult()
                .getResponseBody()
                .getAuthToken();

        webClient.post().uri("/logout")
                .header("auth-token", "Bearer " + authToken)
                .exchange()
                .expectStatus().isOk();

        int blackListSize = jwtBlackList.getBlacklist().size();
        Assertions.assertEquals(1, blackListSize);
    }

    @Test
    void incorrectPasswordTest() {
        final String requestBody = """
            {
              "login": "user",
              "password": "password1"
            }
            """;

        webClient.post().uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").exists()
                .jsonPath("$.message").isEqualTo("Username or password is incorrect");
    }
}
