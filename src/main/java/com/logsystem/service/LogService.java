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
    private final CounterService counterService; // NEW

    @Autowired
    public LogService(LogEntryRepository logEntryRepository,
                      LeaderElectionService leaderElectionService,
                      TimeSyncService timeSyncService,
                      CounterService counterService) { // Injected CounterService
        this.logEntryRepository = logEntryRepository;
        this.leaderElectionService = leaderElectionService;
        this.timeSyncService = timeSyncService;
        this.counterService = counterService;
    }

    // Deduplication check
    public boolean existsByMessageAndLevel(String message, LogLevel level) {
        return logEntryRepository.existsByMessageAndLevel(message, level);
    }

    // 🔄 Updated to assign a unique, increasing index to each log
    public LogEntry createLog(String message, LogLevel level) {
        if (!leaderElectionService.isLeader()) {
            throw new IllegalStateException("Only the leader can write logs.");
        }

        if (existsByMessageAndLevel(message, level)) {
            throw new IllegalArgumentException("Log with the same message and level already exists.");
        }

        int nextIndex = counterService.getNextSequence("logIndex"); // Get the next available index
        LogEntry logEntry = new LogEntry(nextIndex, message, level);

        Instant normalizedTimestamp = timeSyncService.getSynchronizedTimestamp();
        logEntry.setTimestamp(normalizedTimestamp);

        return logEntryRepository.save(logEntry);
    }

    // Basic log queries
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
        return logEntryRepository.findAll(pageable);
    }

    public List<LogEntry> getLogsSortedByTimestamp() {
        return logEntryRepository.findAll(Sort.by(Sort.Direction.ASC, "timestamp"));
    }

    public List<LogEntry> simulateOutOfOrderLogs() {
        List<LogEntry> logs = new ArrayList<>();

        LogEntry oldLog = new LogEntry(0, "Old log", LogLevel.INFO);
        oldLog.setTimestamp(Instant.now().minusSeconds(300));
        logs.add(logEntryRepository.save(oldLog));

        LogEntry nowLog = new LogEntry(0, "Current log", LogLevel.INFO);
        nowLog.setTimestamp(Instant.now());
        logs.add(logEntryRepository.save(nowLog));

        LogEntry futureLog = new LogEntry(0, "Future log", LogLevel.INFO);
        futureLog.setTimestamp(Instant.now().plusSeconds(120));
        logs.add(logEntryRepository.save(futureLog));

        return logs;
    }

    public List<LogEntry> getSortedLogs() {
        return getLogsSortedByTimestamp();
    }

    // 🔄 Log Recovery: Return logs that the rejoining node missed
    public List<LogEntry> recoverLogsForRejoiningNode(int lastAppliedIndex) {
        List<LogEntry> allLogs = logEntryRepository.findAll(Sort.by(Sort.Direction.ASC, "index"));
        List<LogEntry> missingLogs = new ArrayList<>();

        for (LogEntry log : allLogs) {
            if (log.getIndex() > lastAppliedIndex) {
                missingLogs.add(log);
            }
        }

        return missingLogs;
    }
}