package com.logsystem.repository;

import com.logsystem.model.Counter;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CounterRepository extends MongoRepository<Counter, String> {
    // Custom query methods if needed
}