package com.example.pdf_extratct;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class PdfExtratctApplicationTests {

	@Container
	@ServiceConnection
	static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.2");

	@MockBean
	private StringRedisTemplate redisTemplate;

	@MockBean
	private RedisConnectionFactory redisConnectionFactory;

	@Test
	void contextLoads() {
	}

}
