package com.logsystem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoAuditing
@EnableMongoRepositories(basePackages = "com.logsystem.repository")
public class MongoConfig {
    // Basic MongoDB configuration
    // We'll add more configuration as needed
} 