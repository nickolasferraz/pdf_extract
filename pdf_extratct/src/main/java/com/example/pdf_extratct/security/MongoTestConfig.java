package com.example.pdf_extratct.security;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
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