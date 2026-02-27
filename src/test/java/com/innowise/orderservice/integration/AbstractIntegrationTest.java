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
import java.time.Instant;
import java.util.Date;
import java.util.List;
import javax.sql.DataSource;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, TestJacksonConfig.class})
public abstract class AbstractIntegrationTest {

    protected static final String AUTH_HEADER = "Authorization";

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

    @Value("${jwt.secret}")
    private String jwtSecret;

    protected JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("userservice.base-url", WIRE_MOCK_SERVER::baseUrl);
    }

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        WIRE_MOCK_SERVER.resetAll();
        jdbcTemplate.update("DELETE FROM order_items");
        jdbcTemplate.update("DELETE FROM orders");
        jdbcTemplate.update("DELETE FROM items");
    }

    protected String adminAuthHeader() {
        return "Bearer " + buildToken(1L, "admin@example.com", Role.ADMIN);
    }

    protected String userAuthHeader(Long userId, String email) {
        return "Bearer " + buildToken(userId, email, Role.USER);
    }

    protected void stubUserByEmail(String email) {
        UserInfoResponse response = UserInfoResponse.builder()
                .id(1)
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
                        .id(1)
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

    private String buildToken(Long userId, String email, Role role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(email)
                .claim("email", email)
                .claim("userId", userId)
                .claim("role", role.name())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(3600)))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}
