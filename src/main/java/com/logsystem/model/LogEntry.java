package com.logsystem.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Document(collection = "logs")
public class LogEntry {

    @Id
    private String id;
    private int index; // Added for Raft log tracking
    private String message;
    private Instant timestamp;
    private LogLevel level;

    // Default constructor for MongoDB
    public LogEntry() {
        this.timestamp = Instant.now();
        this.level = LogLevel.INFO; // Default level as INFO
    }

    // Constructor with parameters
    public LogEntry(int index, String message, LogLevel level) {
        this.index = index;
        this.message = message;
        this.timestamp = Instant.now(); // Capture current timestamp
        this.level = level;
    }

    // Getters and Setters
    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

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

    @Override
    public String toString() {
        return "LogEntry{" +
                "id='" + id + '\'' +
                ", index=" + index +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                ", level=" + level +
                '}';
    }
}