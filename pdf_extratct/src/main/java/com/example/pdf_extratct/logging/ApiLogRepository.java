package com.example.pdf_extratct.logging;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiLogRepository extends MongoRepository<ApiLogDocument, String> {
}