package com.logsystem.service;

import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TimeSyncService {

    public Instant getSynchronizedTimestamp() {
        // Simulate synchronized system timestamp
        return Instant.now();
    }

    public Instant normalizeTimestamp(Instant incomingTimestamp) {
        return incomingTimestamp != null ? incomingTimestamp : getSynchronizedTimestamp();
    }
}