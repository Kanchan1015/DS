package com.logsystem.service;

import com.logsystem.model.ClusterNode;
import com.logsystem.model.LogEntry;
import com.logsystem.model.LogLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
public class LeaderElectionService implements ApplicationListener<ContextRefreshedEvent> {

    private final NodeRegistryService nodeRegistryService;
    private final LogReplicationService logReplicationService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String nodeId;
    private volatile String currentLeader;
    private AtomicBoolean isLeader = new AtomicBoolean(false);
    private long lastHeartbeatReceivedTime = 0;

    @Autowired
    public LeaderElectionService(NodeRegistryService nodeRegistryService, LogReplicationService logReplicationService) {
        this.nodeRegistryService = nodeRegistryService;
        this.logReplicationService = logReplicationService;
        this.nodeId = "Node-" + nodeRegistryService.getAllNodes().size();
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

    @Scheduled(fixedRate = 5000)
    public void leaderHeartbeat() {
        if (isLeader.get()) {
            System.out.println(nodeId + " (Leader) is alive...");
            lastHeartbeatReceivedTime = System.currentTimeMillis();
        } else {
            System.out.println(nodeId + " is waiting for leader...");
            if (System.currentTimeMillis() - lastHeartbeatReceivedTime > 10000) {
                System.out.println("Leader failed, starting re-election process...");
                startElection();
            }
        }
    }

    // Append log and replicate it to the quorum
    public void appendLogEntry(String message, LogLevel level) {
        if (isLeader.get()) {
            logReplicationService.appendLog(message, level);
            replicateLogToQuorum(message, level);
        } else {
            System.out.println("Not the leader! Cannot accept logs.");
        }
    }

    // Replicate the log to a quorum of followers (majority of nodes)
    private void replicateLogToQuorum(String message, LogLevel level) {
        List<ClusterNode> nodes = nodeRegistryService.getAllNodes().stream()
                .filter(node -> !node.getNodeId().equals(nodeId)) // exclude leader
                .collect(Collectors.toList());

        // Send log to a majority of nodes
        int quorumSize = (nodes.size() / 2) + 1; // Majority of nodes
        int successfulReplications = 0;

        for (ClusterNode node : nodes) {
            try {
                String followerUrl = "http://" + node.getHost() + ":" + node.getPort() + "/api/logs/replicate";
                restTemplate.postForObject(followerUrl, message + "|" + level, String.class);
                successfulReplications++;
                if (successfulReplications >= quorumSize) {
                    System.out.println("Quorum reached for log replication.");
                    return; // Stop if quorum is achieved
                }
            } catch (Exception e) {
                System.out.println("Failed to replicate log to: " + node.getNodeId());
            }
        }
    }

    public void handleLogRecovery(String rejoiningNodeId) {
        if (isLeader.get()) {
            List<LogEntry> missingLogs = logReplicationService.getMissingLogs(rejoiningNodeId);
            List<String> logMessages = missingLogs.stream()
                                                  .map(LogEntry::getMessage)
                                                  .collect(Collectors.toList());

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