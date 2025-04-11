package com.logsystem.test;

import com.logsystem.model.LogLevel;
import com.logsystem.service.LeaderElectionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RaftTest {

    @Autowired
    private LeaderElectionService leaderElectionService;

    @Test
    public void stressTestLogIngestion() {
        if (!leaderElectionService.isLeader()) {
            System.out.println("This node is not the leader. Skipping stress test.");
            return;
        }

        System.out.println("Starting Raft stress test on leader...");

        for (int i = 1; i <= 100; i++) {
            try {
                String message = "Stress Log #" + i;
                leaderElectionService.appendLogEntry(message, LogLevel.INFO);
                System.out.println("Appended: " + message);
                Thread.sleep(50); // slight delay to simulate realistic load
            } catch (Exception e) {
                System.err.println("Error during stress log " + i + ": " + e.getMessage());
            }
        }

        System.out.println("Raft stress test completed.");
    }
}