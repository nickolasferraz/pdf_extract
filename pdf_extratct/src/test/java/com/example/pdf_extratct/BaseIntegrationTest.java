package com.example.pdf_extratct;

import com.example.pdf_extratct.uploadfiles.storage.service.StorageService;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.example.pdf_extratct.security.jwt.JwtUtil;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
public abstract class BaseIntegrationTest {

    @Container
    protected static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0.4");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @MockBean
    protected StorageService storageService;

    @MockBean
    protected StringRedisTemplate stringRedisTemplate;

    @MockBean
    protected RedisConnectionFactory redisConnectionFactory;

    @MockBean
    protected JwtUtil jwtUtil;

    protected String generateTestJwt(String userId, String email) {
        String token = "mocked-jwt-token";
        org.mockito.Mockito.when(jwtUtil.generateToken(userId, email)).thenReturn(token);
        org.mockito.Mockito.when(jwtUtil.validateToken(token)).thenReturn(true);
        org.mockito.Mockito.when(jwtUtil.getUserIdFromToken(token)).thenReturn(userId);
        org.mockito.Mockito.when(jwtUtil.getEmailFromToken(token)).thenReturn(email);
        return token;
    }
}
