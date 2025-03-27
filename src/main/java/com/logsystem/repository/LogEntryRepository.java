package com.logsystem.repository;

import com.logsystem.model.LogEntry;
import com.logsystem.model.LogLevel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LogEntryRepository extends MongoRepository<LogEntry, String> {
    List<LogEntry> findByLevel(LogLevel level);
} 