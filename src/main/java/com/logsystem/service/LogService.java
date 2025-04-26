package com.logsystem.service;

import com.logsystem.model.LogEntry;
import com.logsystem.model.LogLevel;
import com.logsystem.repository.LogEntryRepository;
import com.logsystem.service.CounterService;
import com.logsystem.service.LeaderElectionService;
import com.logsystem.service.TimeSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class LogService {

    private final LogEntryRepository logEntryRepository;
    private final LeaderElectionService leaderElectionService;
    private final TimeSyncService timeSyncService;
    private final CounterService counterService;

    @Autowired
    public LogService(LogEntryRepository logEntryRepository,
                      LeaderElectionService leaderElectionService,
                      TimeSyncService timeSyncService,
                      CounterService counterService) {
        this.logEntryRepository = logEntryRepository;
        this.leaderElectionService = leaderElectionService;
        this.timeSyncService = timeSyncService;
        this.counterService = counterService;
    }

    public boolean existsByMessageAndLevel(String message, LogLevel level) {
        return logEntryRepository.existsByMessageAndLevel(message, level);
    }

    public LogEntry createLog(String message, LogLevel level) {
        if (!leaderElectionService.isLeader()) {
            throw new IllegalStateException("Only the leader can write logs.");
        }

        if (existsByMessageAndLevel(message, level)) {
            throw new IllegalArgumentException("Log with the same message and level already exists.");
        }

        int nextIndex = counterService.getNextSequence("logIndex");
        LogEntry logEntry = new LogEntry(nextIndex, message, level);
        Instant normalizedTimestamp = timeSyncService.getSynchronizedTimestamp();
        logEntry.setTimestamp(normalizedTimestamp);

        int maxRetries = 3;
        int attempt = 0;
        long retryDelayMs = 100;

        while (attempt < maxRetries) {
            try {
                return logEntryRepository.save(logEntry);
            } catch (DataAccessException e) {
                attempt++;
                if (attempt >= maxRetries) {
                    throw new RuntimeException("Failed to write log after " + maxRetries + " attempts", e);
                }
                try {
                    Thread.sleep(retryDelayMs);
                    retryDelayMs *= 2;
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted while saving log", ie);
                }
            }
        }

        throw new RuntimeException("Unexpected error during log save retry");
    }

    public Page<LogEntry> getAllLogs(Pageable pageable) {
        return logEntryRepository.findAll(pageable);
    }

    public Page<LogEntry> getLogsByLevel(LogLevel level, Pageable pageable) {
        return logEntryRepository.findByLevel(level, pageable);
    }

    public Page<LogEntry> getFilteredLogs(LogLevel level, String keyword, Pageable pageable) {
        if (level != null && keyword != null) {
            return logEntryRepository.findByLevelAndMessageContainingIgnoreCase(level, keyword, pageable);
        } else if (level != null) {
            return logEntryRepository.findByLevel(level, pageable);
        } else if (keyword != null) {
            return logEntryRepository.findByMessageContainingIgnoreCase(keyword, pageable);
        }
    
        return Page.empty(pageable);
    }

    public List<LogEntry> simulateOutOfOrderLogs() {
        return logEntryRepository.findAll(Sort.by(Sort.Direction.DESC, "timestamp"));
    }

    public List<LogEntry> getSortedLogs() {
        return logEntryRepository.findAll(Sort.by(Sort.Direction.ASC, "timestamp"));
    }
}