package com.logsystem.controller;

import com.logsystem.model.LogEntry;
import com.logsystem.model.LogLevel;
import com.logsystem.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.*;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class TestDataController {
    private final LogService logService;

    @Autowired
    public TestDataController(LogService logService) {
        this.logService = logService;
    }

    @PostMapping("/data")
    public ResponseEntity<LogEntry> addTestData(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        String levelStr = request.get("level");
        
        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        LogLevel level = levelStr != null ? LogLevel.valueOf(levelStr.toUpperCase()) : LogLevel.INFO;
        return ResponseEntity.ok(logService.createLog(message, level));
    }

    // Updated method with filtering, sorting, and pagination
    @GetMapping("/data")
    public ResponseEntity<Page<LogEntry>> getTestData(
        @RequestParam(required = false) String level, 
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "timestamp") String sortBy,
        @RequestParam(defaultValue = "desc") String order
    ) {
        LogLevel logLevel = level != null ? LogLevel.valueOf(level.toUpperCase()) : null;
        Sort.Direction sortDirection = order.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        return ResponseEntity.ok(logService.getFilteredLogs(logLevel, keyword, pageable));
    }
}