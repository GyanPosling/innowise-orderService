package com.innowise.orderservice.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.innowise.orderservice.TestcontainersConfiguration;
import com.innowise.orderservice.config.TestJacksonConfig;
import com.innowise.orderservice.model.dto.response.UserInfoResponse;
import com.innowise.orderservice.model.entity.Role;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
@Import({TestJacksonConfig.class, TestcontainersConfiguration.class})
public abstract class AbstractIntegrationTest {

    private static final String USER_ID_HEADER = "X-USER-ID";
    private static final String USER_ROLE_HEADER = "X-USER-ROLE";
    private static final String USER_EMAIL_HEADER = "X-USER-EMAIL";
    private static final String USERNAME_HEADER = "X-USER-NAME";
    private static final String TS_HEADER = "X-TS";
    private static final String SIGN_HEADER = "X-SIGN";
    private static final String SIGN_ALGORITHM = "HmacSHA256";

    private static final WireMockServer WIRE_MOCK_SERVER =
            new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());

    static {
        WIRE_MOCK_SERVER.start();
    }

    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected DataSource dataSource;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Value("${gateway.internal-signing-secret}")
    private String internalSecret;

    protected JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("userservice.base-url", WIRE_MOCK_SERVER::baseUrl);
        registry.add(
                "spring.kafka.bootstrap-servers",
                () -> TestcontainersConfiguration.getKafkaContainer().getBootstrapServers()
        );
    }

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        WIRE_MOCK_SERVER.resetAll();
        jdbcTemplate.update("DELETE FROM order_items");
        jdbcTemplate.update("DELETE FROM order_payment_events");
        jdbcTemplate.update("DELETE FROM orders");
        jdbcTemplate.update("DELETE FROM items");
    }

    protected RequestPostProcessor adminAuthHeader() {
        return gatewayAuth(UUID.fromString("00000000-0000-0000-0000-000000000001"), "admin@example.com", Role.ADMIN);
    }

    protected RequestPostProcessor userAuthHeader(UUID userId, String email) {
        return gatewayAuth(userId, email, Role.USER);
    }

    protected void stubUserByEmail(String email) {
        UserInfoResponse response = UserInfoResponse.builder()
                .id(UUID.fromString("00000000-0000-0000-0000-000000000101"))
                .email(email)
                .name("Test")
                .surname("User")
                .active(true)
                .build();
        try {
            WIRE_MOCK_SERVER.stubFor(get(urlPathEqualTo("/api/users/by-email"))
                    .withQueryParam("email", equalTo(email))
                    .willReturn(okJson(objectMapper.writeValueAsString(response))));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize user response", ex);
        }
    }

    protected void stubUsersByEmails(List<String> emails) {
        List<UserInfoResponse> responses = emails.stream()
                .map(email -> UserInfoResponse.builder()
                        .id(UUID.fromString("00000000-0000-0000-0000-000000000101"))
                        .email(email)
                        .name("Test")
                        .surname("User")
                        .active(true)
                        .build())
                .toList();
        try {
            WIRE_MOCK_SERVER.stubFor(post(urlPathEqualTo("/api/users/batch"))
                    .withRequestBody(equalToJson(objectMapper.writeValueAsString(emails)))
                    .willReturn(okJson(objectMapper.writeValueAsString(responses))));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize users response", ex);
        }
    }

    private RequestPostProcessor gatewayAuth(UUID userId, String email, Role role) {
        return request -> {
            String userIdValue = userId.toString();
            String roleValue = role.name();
            String username = email;
            String ts = String.valueOf(Instant.now().getEpochSecond());
            String payload = String.join(
                    "|",
                    emptyIfNull(request.getMethod()),
                    emptyIfNull(request.getRequestURI()),
                    userIdValue,
                    roleValue,
                    email,
                    username,
                    ts
            );

            request.addHeader(USER_ID_HEADER, userIdValue);
            request.addHeader(USER_ROLE_HEADER, roleValue);
            request.addHeader(USER_EMAIL_HEADER, email);
            request.addHeader(USERNAME_HEADER, username);
            request.addHeader(TS_HEADER, ts);
            request.addHeader(SIGN_HEADER, sign(payload));
            return request;
        };
    }

    private String emptyIfNull(String value) {
        return value == null ? "" : value;
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance(SIGN_ALGORITHM);
            mac.init(new SecretKeySpec(internalSecret.getBytes(StandardCharsets.UTF_8), SIGN_ALGORITHM));
            return Base64.getEncoder().encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            throw new IllegalStateException("Failed to sign test gateway headers", ex);
        }
    }
}
