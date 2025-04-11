package com.logsystem.repository;

import com.logsystem.model.LogEntry;
import com.logsystem.model.LogLevel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface LogEntryRepository extends MongoRepository<LogEntry, String> {

    // Custom query method to check for duplicate logs based on message and level
    boolean existsByMessageAndLevel(String message, LogLevel level);

    // Additional methods for fetching logs
    Page<LogEntry> findByLevel(LogLevel level, Pageable pageable);
    Page<LogEntry> findByMessageContainingIgnoreCase(String keyword, Pageable pageable);
    Page<LogEntry> findByLevelAndMessageContainingIgnoreCase(LogLevel level, String keyword, Pageable pageable);
}