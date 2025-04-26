package com.logsystem.service;

import com.logsystem.model.ClusterNode;
import com.logsystem.model.LogEntry;
import com.logsystem.model.LogLevel;
import com.logsystem.raft.NodeRole;
import com.logsystem.raft.VoteRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
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

    // Raft variables
    private volatile long currentTerm = 0;
    private volatile NodeRole nodeRole = NodeRole.FOLLOWER;
    private volatile String votedFor = null;

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

    public NodeRole getNodeRole() {
        return nodeRole;
    }

    public long getCurrentTerm() {
        return currentTerm;
    }

    public void startElection() {
        currentTerm++;
        nodeRole = NodeRole.CANDIDATE;
        votedFor = nodeId;

        List<ClusterNode> nodes = nodeRegistryService.getAllNodes().stream()
                .filter(node -> !node.getNodeId().equals(nodeId))
                .collect(Collectors.toList());

        AtomicInteger voteCount = new AtomicInteger(1); // Vote for itself

        for (ClusterNode node : nodes) {
            try {
                String followerUrl = "http://" + node.getHost() + ":" + node.getPort() + "/api/raft/vote";
                VoteRequest voteRequest = new VoteRequest(nodeId, currentTerm);
                Boolean voteGranted = restTemplate.postForObject(followerUrl, voteRequest, Boolean.class);

                if (Boolean.TRUE.equals(voteGranted)) {
                    voteCount.incrementAndGet();
                }
            } catch (Exception e) {
                System.out.println("Failed to request vote from: " + node.getNodeId());
            }
        }

        int majority = (nodeRegistryService.getAllNodes().size() / 2) + 1;
        if (voteCount.get() >= majority) {
            becomeLeader();
        } else {
            nodeRole = NodeRole.FOLLOWER;
            System.out.println(nodeId + " failed to win election, remains follower.");
        }
    }

    private void becomeLeader() {
        System.out.println(nodeId + " is elected as Leader for term " + currentTerm);
        currentLeader = nodeId;
        isLeader.set(true);
        nodeRole = NodeRole.LEADER;
        nodeRegistryService.setLeader(nodeId);
    }

    @Scheduled(fixedRate = 5000)
    public void leaderHeartbeat() {
        if (nodeRole == NodeRole.LEADER) {
            System.out.println(nodeId + " (Leader) sending heartbeat...");
            lastHeartbeatReceivedTime = System.currentTimeMillis();
        } else {
            System.out.println(nodeId + " waiting for leader heartbeat...");
            if (System.currentTimeMillis() - lastHeartbeatReceivedTime > 10000) {
                System.out.println("Leader timeout detected. Starting election...");
                startElection();
            }
        }
    }

    public boolean receiveVoteRequest(VoteRequest request) {
        if (request.getTerm() > currentTerm) {
            currentTerm = request.getTerm();
            votedFor = null;
            nodeRole = NodeRole.FOLLOWER;
        }

        if ((votedFor == null || votedFor.equals(request.getCandidateId())) && request.getTerm() >= currentTerm) {
            votedFor = request.getCandidateId();
            lastHeartbeatReceivedTime = System.currentTimeMillis();
            return true;
        }
        return false;
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
            System.out.println("Cannot recover logs, not the leader.");
        }
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        startElection();
    }
}