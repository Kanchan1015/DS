package com.logsystem.service;

import com.logsystem.model.LogEntry;
import com.logsystem.repository.LogEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class LogService {
    private final LogEntryRepository logEntryRepository;

    @Autowired
    public LogService(LogEntryRepository logEntryRepository) {
        this.logEntryRepository = logEntryRepository;
    }

    public LogEntry createLog(String message) {
        LogEntry logEntry = new LogEntry(message);
        return logEntryRepository.save(logEntry);
    }

    public List<LogEntry> getAllLogs() {
        return logEntryRepository.findAll();
    }
} 