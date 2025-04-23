package com.logsystem.service;

import com.logsystem.model.ClusterNode;
import com.logsystem.model.LogEntry; // Assuming LogEntry is the model for log entries
import com.logsystem.model.LogLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Random;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors; // For list conversion

@Service
public class LeaderElectionService implements ApplicationListener<ContextRefreshedEvent> {

    private final NodeRegistryService nodeRegistryService;
    private final LogReplicationService logReplicationService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String nodeId;
    private volatile String currentLeader;
    private AtomicBoolean isLeader = new AtomicBoolean(false);
    private Random random = new Random();
    private long lastHeartbeatReceivedTime = 0; // for failure detection

    @Autowired
    public LeaderElectionService(NodeRegistryService nodeRegistryService, LogReplicationService logReplicationService) {
        this.nodeRegistryService = nodeRegistryService;
        this.logReplicationService = logReplicationService;
        this.nodeId = "Node-" + random.nextInt(1000);
        nodeRegistryService.registerNode(new ClusterNode(nodeId, "localhost", 8081));
    }

    public String getCurrentLeader() {
        return currentLeader;
    }

    public boolean isLeader() {
        return isLeader.get();
    }

    public void startElection() {
        System.out.println(nodeId + " is starting leader election...");
        currentLeader = nodeId;
        isLeader.set(true);
        nodeRegistryService.setLeader(nodeId);
        System.out.println("New Leader Elected: " + currentLeader);
    }

    // Detect leader failure by checking last heartbeat time
    @Scheduled(fixedRate = 5000)
    public void leaderHeartbeat() {
        if (isLeader.get()) {
            System.out.println(nodeId + " (Leader) is alive...");
            lastHeartbeatReceivedTime = System.currentTimeMillis();
        } else {
            System.out.println(nodeId + " is waiting for leader...");
            if (System.currentTimeMillis() - lastHeartbeatReceivedTime > 10000) { // Leader failure detected
                System.out.println("Leader failed, starting re-election process...");
                startElection();
            }
        }
    }

    // NEW METHOD: Leader appends and replicates logs
    public void appendLogEntry(String message, LogLevel level) {
        if (isLeader.get()) {
            logReplicationService.appendLog(message, level);
            replicateLogToFollowers(message, level);
        } else {
            System.out.println("Not the leader! Cannot accept logs.");
        }
    }

    // NEW METHOD: Send log updates to followers
    private void replicateLogToFollowers(String message, LogLevel level) {
        for (ClusterNode node : nodeRegistryService.getAllNodes()) {
            if (!node.getNodeId().equals(nodeId)) {
                try {
                    String followerUrl = "http://" + node.getHost() + ":" + node.getPort() + "/api/logs/replicate";
                    restTemplate.postForObject(followerUrl, message + "|" + level, String.class);
                    System.out.println("Replicated log to: " + node.getNodeId());
                } catch (Exception e) {
                    System.out.println("Failed to replicate log to: " + node.getNodeId());
                }
            }
        }
    }

    // NEW METHOD: Handle Log Recovery for Rejoining Nodes
    public void handleLogRecovery(String rejoiningNodeId) {
        if (isLeader.get()) {
            List<LogEntry> missingLogs = logReplicationService.getMissingLogs(rejoiningNodeId);
            // Convert List<LogEntry> to List<String> if necessary
            List<String> logMessages = missingLogs.stream()
                                                  .map(log -> log.getMessage()) // Assuming LogEntry has getMessage() method
                                                  .collect(Collectors.toList());

            // Send missing logs to the rejoining node
            String followerUrl = "http://" + nodeRegistryService.getNodeById(rejoiningNodeId).getHost()
                                 + ":" + nodeRegistryService.getNodeById(rejoiningNodeId).getPort() + "/api/logs/recovery";
            for (String log : logMessages) {
                restTemplate.postForObject(followerUrl, log, String.class);
            }
            System.out.println("Sent missing logs to " + rejoiningNodeId);
        } else {
            System.out.println("Cannot recover logs, as " + nodeId + " is not the leader.");
        }
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        startElection();
    }
}