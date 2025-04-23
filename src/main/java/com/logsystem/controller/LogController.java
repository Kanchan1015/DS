package com.logsystem.controller;

import com.logsystem.model.LogEntry;
import com.logsystem.model.LogLevel;
import com.logsystem.service.LogService;


import com.logsystem.service.CounterService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/logs")
@CrossOrigin(origins = "*")
public class LogController {
    private static final Logger logger = LoggerFactory.getLogger(LogController.class);
    private final LogService logService;

    @Autowired
    public LogController(LogService logService) {
        this.logService = logService;
    }

    @PostMapping
    public ResponseEntity<?> createLog(@RequestBody Map<String, String> request) {
        try {
            String message = request.get("message");
            String levelStr = request.get("level");

            if (message == null || message.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Message cannot be empty");
            }

            LogLevel level;
            try {
                level = levelStr != null ? LogLevel.valueOf(levelStr.toUpperCase()) : LogLevel.INFO;
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Invalid log level. Valid levels are: INFO, WARNING, ERROR, DEBUG");
            }

            if (logService.existsByMessageAndLevel(message, level)) {
                return ResponseEntity.badRequest().body("Log with the same message and level already exists.");
            }

            LogEntry logEntry = logService.createLog(message, level);
            logger.info("Created log entry: {}", logEntry);
            return ResponseEntity.ok(logEntry);
        } catch (Exception e) {
            logger.error("Error creating log entry", e);
            return ResponseEntity.internalServerError().body("Error creating log entry: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllLogs(Pageable pageable) {
        try {
            Page<LogEntry> logs = logService.getAllLogs(pageable);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            logger.error("Error retrieving logs", e);
            return ResponseEntity.internalServerError().body("Error retrieving logs: " + e.getMessage());
        }
    }

    @GetMapping("/level/{level}")
    public ResponseEntity<?> getLogsByLevel(@PathVariable String level, Pageable pageable) {
        try {
            LogLevel logLevel = LogLevel.valueOf(level.toUpperCase());
            Page<LogEntry> logs = logService.getLogsByLevel(logLevel, pageable);
            return ResponseEntity.ok(logs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid log level. Valid levels are: INFO, WARNING, ERROR, DEBUG");
        } catch (Exception e) {
            logger.error("Error retrieving logs by level", e);
            return ResponseEntity.internalServerError().body("Error retrieving logs: " + e.getMessage());
        }
    }

   
    @PostMapping("/simulate")
    public ResponseEntity<?> simulateLogs() {
        try {
            List<LogEntry> simulated = logService.simulateOutOfOrderLogs();
            return ResponseEntity.ok(simulated);
        } catch (Exception e) {
            logger.error("Error simulating logs", e);
            return ResponseEntity.internalServerError().body("Error simulating logs: " + e.getMessage());
        }
    }

    
    @GetMapping("/sorted")
    public ResponseEntity<?> getSortedLogs() {
        try {
            List<LogEntry> logs = logService.getSortedLogs();
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            logger.error("Error retrieving sorted logs", e);
            return ResponseEntity.internalServerError().body("Error retrieving logs: " + e.getMessage());
        }
    }

    @RequestMapping("/counter")
    public class CounterController {

        private final CounterService counterService;

        @Autowired
        public CounterController(CounterService counterService) {
            this.counterService = counterService;
        }

        @GetMapping("/next")
        public ResponseEntity<Integer> getNextSequence(@RequestParam String counterName) {
            int nextSequence = counterService.getNextSequence(counterName);
            return ResponseEntity.ok(nextSequence);
        }
    }

    @PostMapping("/recovery")
    public ResponseEntity<?> recoverLogs(@RequestBody String log) { 
    try {
        // Parse and save the recovered log
        String[] parts = log.split("\\|");
        String message = parts[0];
        LogLevel level = LogLevel.valueOf(parts[1].toUpperCase());
        logService.createLog(message, level); // or directly save it if trusted
        return ResponseEntity.ok("Log recovered successfully.");
    } catch (Exception e) {
        logger.error("Error recovering log", e);
        return ResponseEntity.badRequest().body("Invalid log format or internal error.");
    }
}
}