package com.logsystem.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document(collection = "logs")
public class LogEntry {
    @Id
    private String id;
    private String message;
    private Instant timestamp;  // Changed to Instant
    private LogLevel level;

    public LogEntry() {
        this.timestamp = Instant.now();  // Set timestamp to UTC using Instant
        this.level = LogLevel.INFO;  // Default level
    }

    public LogEntry(String message, LogLevel level) {
        this.message = message;
        this.timestamp = Instant.now();  // Set timestamp to UTC using Instant
        this.level = level;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public LogLevel getLevel() {
        return level;
    }

    public void setLevel(LogLevel level) {
        this.level = level;
    }
}