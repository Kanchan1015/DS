package com.logsystem.service;

import com.logsystem.model.LogEntry;
import com.logsystem.model.LogLevel;
import com.logsystem.repository.LogEntryRepository;  // Import Correct Repository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogReplicationService {
    
    private final LogEntryRepository logEntryRepository;

    @Autowired
    public LogReplicationService(LogEntryRepository logEntryRepository) {
        this.logEntryRepository = logEntryRepository;
    }

    // Updated method to save logs into the database
    public void appendLog(String message, LogLevel level) {
        LogEntry logEntry = new LogEntry(0, message, level); // Provide index explicitly
        logEntryRepository.save(logEntry);
    }

    public void replicateLog(LogEntry logEntry) {
        logEntryRepository.save(logEntry); // Save log entry
    }

    // NEW METHOD: Fetch missing logs for rejoining node
    public List<String> getMissingLogs(String rejoiningNodeId) {
        // Implement logic to get logs that the rejoining node is missing
        // For now, return a placeholder list of logs
        return List.of("Log1", "Log2", "Log3");
    }
}