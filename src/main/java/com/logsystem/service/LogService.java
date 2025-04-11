package com.logsystem.service;

import com.logsystem.model.LogEntry;
import com.logsystem.model.LogLevel;
import com.logsystem.repository.LogEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class LogService {
    private final LogEntryRepository logEntryRepository;
    private final LeaderElectionService leaderElectionService;
    private final TimeSyncService timeSyncService;

    @Autowired
    public LogService(LogEntryRepository logEntryRepository,
                      LeaderElectionService leaderElectionService,
                      TimeSyncService timeSyncService) {
        this.logEntryRepository = logEntryRepository;
        this.leaderElectionService = leaderElectionService;
        this.timeSyncService = timeSyncService;
    }

    // Method to check for log duplication based on message and level
    public boolean existsByMessageAndLevel(String message, LogLevel level) {
        return logEntryRepository.existsByMessageAndLevel(message, level);
    }

    // Create log method with deduplication check and timestamp correction
    public LogEntry createLog(String message, LogLevel level) {
        if (!leaderElectionService.isLeader()) {
            throw new IllegalStateException("Only the leader can write logs.");
        }

        if (existsByMessageAndLevel(message, level)) {
            throw new IllegalArgumentException("Log with the same message and level already exists.");
        }

        LogEntry logEntry = new LogEntry(0, message, level);

        // Normalize timestamp
        Instant normalizedTimestamp = timeSyncService.getSynchronizedTimestamp();
        logEntry.setTimestamp(normalizedTimestamp);

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

    // Filtering logs by level and message keyword with pagination
    public Page<LogEntry> getFilteredLogs(LogLevel level, String keyword, Pageable pageable) {
        if (level != null && keyword != null) {
            return logEntryRepository.findByLevelAndM