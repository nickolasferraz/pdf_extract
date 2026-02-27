package com.example.pdf_extratct.security.redis;

import com.example.pdf_extratct.Payment.dto.CreditPackgesRequestDTO;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;
import tools.jackson.databind.json.JsonMapper;

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

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {

        ObjectMapper mapper = JsonMapper.builder()// Adiciona o suporte a datas
                .build();

        // 2. Passamos o mapper para o Serializer
        GenericJacksonJsonRedisSerializer serializer = new GenericJacksonJsonRedisSerializer(mapper);

                RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(serializer)
                        );

                return RedisCacheManager
                        .builder(redisConnectionFactory)
                        .cacheDefaults(cacheConfig).build();
    }

}
