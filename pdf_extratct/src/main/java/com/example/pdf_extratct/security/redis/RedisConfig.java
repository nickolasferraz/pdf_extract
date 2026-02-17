package com.example.pdf_extratct.security.redis;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String,Integer>redisTemplate(RedisConnectionFactory factory){
        RedisTemplate<String,Integer> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new GenericToStringSerializer<>(Integer.class));

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    public CommandLineRunner commandLineRunner(RedisTemplate<String, Integer> redisTemplate) {
        return args -> {
            redisTemplate.opsForValue()
                    .set("test", 123, Duration.ofSeconds(7));

            System.out.println("Valor salvo no Redis!");
        };
    }


}
