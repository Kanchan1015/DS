package com.logsystem.controller;

import com.logsystem.model.LogEntry;
import com.logsystem.service.LogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
        if (message == null || message.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(logService.createLog(message));
    }

    @GetMapping("/data")
    public ResponseEntity<List<LogEntry>> getTestData() {
        return ResponseEntity.ok(logService.getAllLogs());
    }
} 