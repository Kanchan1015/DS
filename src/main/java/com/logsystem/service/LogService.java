package com.logsystem.service;

import com.logsystem.model.LogEntry;
import com.logsystem.model.LogLevel;
import com.logsystem.repository.LogEntryRepository;
import com.logsystem.service.LeaderElectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
public class LogService {
    private final LogEntryRepository logEntryRepository;
    private final LeaderElectionService leaderElectionService;

    @Autowired
    public LogService(LogEntryRepository logEntryRepository, LeaderElectionService leaderElectionService) {
        this.logEntryRepository = logEntryRepository;
        this.leaderElectionService = leaderElectionService;
    }

    public LogEntry createLog(String message, LogLevel level) {
        if (!leaderElectionService.isLeader()) {
            throw new IllegalStateException("Only the leader can write logs.");
        }
        LogEntry logEntry = new LogEntry(0,message, level);
        return logEntryRepository.save(logEntry);
    }

    // Fetch all logs with pagination
    public Page<LogEntry> getAllLogs(Pageable pageable) {
        return logEntryRepository.findAll(pageable);
    }

    // Fetch logs by level with pagination
    public Page<LogEntry> getLogsByLevel(LogLevel level, Pageable pageable) {
        return logEntryRepository.findByLevel(level, pageable);
    }

    // New method for filtering logs by level, message keyword & sorting
    public Page<LogEntry> getFilteredLogs(LogLevel level, String keyword, Pageable pageable) {
        if (level != null && keyword != null) {
            return logEntryRepository.findByLevelAndMessageContainingIgnoreCase(level, keyword, pageable);
        } else if (level != null) {
            return logEntryRepository.findByLevel(level, pageable);
        } else if (keyword != null) {
            return logEntryRepository.findByMessageContainingIgnoreCase(keyword, pageable);
        }
        return logEntryRepository.findAll(pageable);
    }
}