package net.cloud.integration;

import net.cloud.dto.AuthenticationResponse;
import net.cloud.dto.ResourceResponseDto;
import net.cloud.repository.UserRepository;
import net.cloud.service.MinioService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.web.reactive.function.BodyInserters;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // ← Позволяет использовать @BeforeAll на нестатических методах
public class ResourceIT {

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MinioService minioService;


    private final String requestBody = """
            {
              "login": "user",
              "password": "password"
            }
            """;

    private String token;

    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> POSTGRE_SQL_CONTAINER = new PostgreSQLContainer<>("postgres:17")
            .withDatabaseName("test-db")
            .withUsername("test")
            .withPassword("test");

    @Container
    private static final GenericContainer<?> MINIO_CONTAINER = new GenericContainer<>("minio/minio")
            .withExposedPorts(9000)
            .withEnv("MINIO_ROOT_USER", "admin")
            .withEnv("MINIO_ROOT_PASSWORD", "secret123456")
            .withCommand("server", "/data");

    static {
        MINIO_CONTAINER.start();
    }

    @DynamicPropertySource
    private static void setProperties(DynamicPropertyRegistry registry) {

        registry.add("spring.liquibase.enabled", () -> "true");
        registry.add("spring.liquibase.change-log", () -> "classpath:/db/changelog/db.changelog-master.yaml");
        registry.add("minio.endpoint", () -> "http://localhost:" + MINIO_CONTAINER.getMappedPort(9000));
        registry.add("minio.access-key", () -> "admin");
        registry.add("minio.secret-key", () -> "secret123456");
        registry.add("minio.bucket", () -> "user-files");

    }

    @BeforeAll
    void regTestUser() {
        webClient.post().uri("/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectBody(AuthenticationResponse.class)
                .returnResult().getResponseBody()
                .getAuthToken();
    }

    @BeforeEach
    void login() {
        token = webClient.post().uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectBody(AuthenticationResponse.class)
                .returnResult().getResponseBody()
                .getAuthToken();
    }

    @Test
    void testUpload() throws IOException {

        File file = File.createTempFile("test-upload-", ".txt");
        file.deleteOnExit(); // автоматически удалится

        // Создаём файл и записываем в него хотя бы один байт, чтобы он точно был доступен
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("test content"); // ← Добавляем содержимое
            writer.flush();
        }

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new FileSystemResource(file));

        webClient.post().uri("/file?filename=test-file.txt")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header("auth-token", "Bearer " + token)
                .body(BodyInserters.fromValue(builder.build()))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.filename").isEqualTo("test-file.txt");
    }

    @Test
    void deleteFile() throws IOException {

        File file = File.createTempFile("test-upload-", ".txt");
        file.deleteOnExit();

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new FileSystemResource(file));

        webClient.post().uri("/file?filename=test-file.txt")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header("auth-token", "Bearer " + token)
                .body(BodyInserters.fromValue(builder.build()))
                .exchange();

        webClient.delete().uri("/file?filename=test-file.txt")
                .header("auth-token", "Bearer " + token)
                .exchange().expectStatus().isOk();

        webClient.delete().uri("/file?filename=test-file.txt")
                .header("auth-token", "Bearer " + token)
                .exchange().expectStatus().isBadRequest();
    }

    @Test
    void downloadFile() throws IOException {
        File file = File.createTempFile("test-upload-", ".txt");
        file.deleteOnExit(); // автоматически удалится


        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new FileSystemResource(file));

        webClient.post().uri("/file?filename=test-file.txt")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header("auth-token", "Bearer " + token)
                .body(BodyInserters.fromValue(builder.build()))
                .exchange();

        webClient.get().uri("/file?filename=test-file.txt")
                .header("auth-token", "Bearer " + token)
                .exchange().expectStatus().isOk();

        webClient.get().uri("/file?filename=bad-file.txt")
                .header("auth-token", "Bearer " + token)
                .exchange().expectStatus().isBadRequest();
    }

    @Test
    void renameFile() throws IOException {
        File file = File.createTempFile("test-upload-", ".txt");
        file.deleteOnExit(); // автоматически удалится

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", new FileSystemResource(file));

        final String newFileName = """
                    {
                        "name":"new-name.txt"
                    }
                """;

        webClient.post().uri("/file?filename=test-file.txt")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header("auth-token", "Bearer " + token)
                .body(BodyInserters.fromValue(builder.build()))
                .exchange();

        webClient.put().uri("/file?filename=test-file.txt")
                .contentType(MediaType.APPLICATION_JSON)
                .header("auth-token", "Bearer " + token)
                .bodyValue(newFileName)
                .exchange().expectStatus().isOk();

        webClient.put().uri("/file?filename=test-file.txt")
                .contentType(MediaType.APPLICATION_JSON)
                .header("auth-token", "Bearer " + token)
                .bodyValue(newFileName)
                .exchange().expectStatus().isBadRequest();
    }

    @Test
    void getFiles() throws IOException {

        File file1 = File.createTempFile("test-upload-", ".txt");
        file1.deleteOnExit(); // автоматически удалится

        MultipartBodyBuilder builder1 = new MultipartBodyBuilder();
        builder1.part("file", new FileSystemResource(file1));

        File file2 = File.createTempFile("test-upload-", ".txt");
        file2.deleteOnExit(); // автоматически удалится

        MultipartBodyBuilder builder2 = new MultipartBodyBuilder();
        builder2.part("file", new FileSystemResource(file2));

        webClient.post().uri("/file?filename=test-file.txt")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header("auth-token", "Bearer " + token)
                .body(BodyInserters.fromValue(builder1.build()))
                .exchange();

        webClient.post().uri("/file?filename=test-file.txt")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header("auth-token", "Bearer " + token)
                .body(BodyInserters.fromValue(builder2.build()))
                .exchange();

        webClient.get().uri("/list?limit=2")
                .header("auth-token", "Bearer " + token)
                .exchange().expectStatus().isOk()
                .expectBodyList(ResourceResponseDto.class).hasSize(2);

        webClient.get().uri("/list?limit=1")
                .header("auth-token", "Bearer " + token)
                .exchange().expectStatus().isOk()
                .expectBodyList(ResourceResponseDto.class).hasSize(1);
    }
}
