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
import org.springframework.http.HttpHeaders;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import({TestcontainersConfiguration.class, TestJacksonConfig.class})
public abstract class AbstractIntegrationTest {

    protected static final String USER_ID_HEADER = "X-USER-ID";
    protected static final String USER_ROLE_HEADER = "X-USER-ROLE";
    protected static final String USER_EMAIL_HEADER = "X-USER-EMAIL";
    protected static final String USERNAME_HEADER = "X-USER-NAME";

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

    protected HttpHeaders adminHeaders() {
        return buildHeaders(1L, "admin@example.com", Role.ADMIN);
    }

    protected HttpHeaders userHeaders(Long userId, String email) {
        return buildHeaders(userId, email, Role.USER);
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

    private HttpHeaders buildHeaders(Long userId, String email, Role role) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(USER_ID_HEADER, String.valueOf(userId));
        headers.add(USER_ROLE_HEADER, role.name());
        headers.add(USER_EMAIL_HEADER, email);
        headers.add(USERNAME_HEADER, email);
        return headers;
    }
}
