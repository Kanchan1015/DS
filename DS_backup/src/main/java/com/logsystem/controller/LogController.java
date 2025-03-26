package com.logsystem.controller;

import com.logsystem.model.LogEntry;
import com.logsystem.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/logs")
@CrossOrigin(origins = "*")
public class LogController {
    private final LogService logService;

    @Autowired
    public LogController(LogService logService) {
        this.logService = logService;
    }

    @PostMapping
    public ResponseEntity<LogEntry> createLog(@RequestBody Map<String, String> request) {
        String message = request.get("message");
        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(logService.createLog(message));
    }

    @GetMapping
    public ResponseEntity<List<LogEntry>> getAllLogs() {
        return ResponseEntity.ok(logService.getAllLogs());
    }
} 