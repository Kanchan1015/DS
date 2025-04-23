package com.logsystem.service;

import com.logsystem.model.LogEntry;
import com.logsystem.model.LogLevel;
import com.logsystem.repository.LogEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class LogReplicationService {

    private final LogEntryRepository logEntryRepository;

    // Track the latest log index each node has received
    private final Map<String, Integer> nodeLogIndices = new HashMap<>();

    @Autowired
    public LogReplicationService(LogEntryRepository logEntryRepository) {
        this.logEntryRepository = logEntryRepository;
    }

    // Save new log entry (used by leader)
    public void appendLog(String message, LogLevel level) {
        // Automatically assign index as (max index + 1)
        Integer maxIndex = logEntryRepository.findAll().stream()
                .map(LogEntry::getIndex)
                .max(Integer::compareTo)
                .orElse(-1);

        LogEntry logEntry = new LogEntry(maxIndex + 1, message, level);
        logEntryRepository.save(logEntry);
    }

    // Save replicated log from another node
    public void replicateLog(LogEntry logEntry) {
        logEntryRepository.save(logEntry);
    }

    // Fetch missing logs for a rejoining node
    public List<LogEntry> getMissingLogs(String rejoiningNodeId) {
        int lastSeenIndex = nodeLogIndices.getOrDefault(rejoiningNodeId, -1);

        return logEntryRepository.findAll().stream()
                .filter(log -> log.getIndex() > lastSeenIndex)
                .sorted(Comparator.comparingInt(LogEntry::getIndex))
                .collect(Collectors.toList());
    }

    // Update the last known index for a node after successful replication
    public void updateNodeLogIndex(String nodeId, int lastIndex) {
        nodeLogIndices.put(nodeId, lastIndex);
    }
}