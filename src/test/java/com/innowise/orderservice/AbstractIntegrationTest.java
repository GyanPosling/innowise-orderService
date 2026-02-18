// package com.innowise.orderservice;

// import io.jsonwebtoken.Jwts;
// import io.jsonwebtoken.security.Keys;
// import java.nio.charset.StandardCharsets;
// import java.util.Date;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.test.context.ActiveProfiles;
// import org.springframework.test.context.DynamicPropertyRegistry;
// import org.springframework.test.context.DynamicPropertySource;
// import org.springframework.transaction.annotation.Transactional;
// import org.testcontainers.containers.PostgreSQLContainer;
// import org.testcontainers.utility.DockerImageName;

// @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

// @ActiveProfiles("test")
// @Transactional
// public abstract class AbstractIntegrationTest {

//     @Value("${jwt.secret}")
//     private String jwtSecret;

//     @Value("${jwt.expiration}")
//     private Long jwtExpiration;

//     @SuppressWarnings("resource")
//     static final PostgreSQLContainer<?> POSTGRES =
//             new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
//                     .withReuse(true);

//     static {
//         POSTGRES.start();
//     }

//     @DynamicPropertySource
//     static void configureProperties(DynamicPropertyRegistry registry) {
//         registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
//         registry.add("spring.datasource.username", POSTGRES::getUsername);
//         registry.add("spring.datasource.password", POSTGRES::getPassword);
//         registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
//     }

//     protected String generateTestToken(Long userId, String email, String role) {
//         byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
//         return "Bearer " + Jwts.builder()
//                 .setSubject(email)
//                 .claim("userId", userId)
//                 .claim("role", role)
//                 .setIssuedAt(new Date())
//                 .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
//                 .signWith(Keys.hmacShaKeyFor(keyBytes))
//                 .compact();
//     }
// }
