package com.example.pdf_extratct.security.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
@Profile("!test")
public class MongoTestConfig {

    @Bean
    CommandLineRunner testMongoConnection(MongoTemplate mongoTemplate) {
        return args -> {
            try {
                mongoTemplate.getDb().runCommand(new org.bson.Document("ping", 1));
                System.out.println("✅ Conectado ao MongoDB com sucesso!");
            } catch (Exception e) {
                System.out.println("❌ Erro ao conectar no MongoDB");
                e.printStackTrace();
            }
        };
    }
}
