package com.logsystem.service;

import com.logsystem.model.ClusterNode;

import com.logsystem.service.LeaderElectionService.NodeStatus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class LeaderElectionService {

    @Value("${node.id}")
    private String nodeId;

    @Value("${server.port}")
    private int port;

    @Value("${nodes.list}")
    private List<String> nodeAddresses;

    private final NodeRegistryService nodeRegistryService;
    private final RestTemplate restTemplate;
    private volatile String currentLeader = null;
    private volatile boolean isLeader = false;
    private volatile NodeStatus status = NodeStatus.FOLLOWER;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private long lastHeartbeat = System.currentTimeMillis();
    private final AtomicLong currentTerm = new AtomicLong(0);
    private final AtomicInteger votesReceived = new AtomicInteger(0);
    private final Random random = new Random();
    private long electionTimeout;
    private final AtomicBoolean electionInProgress = new AtomicBoolean(false);

    private static final long HEARTBEAT_INTERVAL = 1000; // 1 second
    private static final long MIN_ELECTION_TIMEOUT = 5000; // 5 seconds
    private static final long MAX_ELECTION_TIMEOUT = 10000; // 10 seconds

    public enum NodeStatus {
        LEADER,
        FOLLOWER,
        CANDIDATE
    }

    @Autowired
    public LeaderElectionService(NodeRegistryService nodeRegistryService) {
        this.nodeRegistryService = nodeRegistryService;
        this.restTemplate = new RestTemplate();
    }

    @PostConstruct
    public void init() {
        System.out.println("[RAFT] Initializing LeaderElectionService for node: " + nodeId + " on port: " + port);
        resetElectionTimeout();
        startHeartbeatChecker();
        // Wait a few seconds for other nodes to start, then try election
        new Thread(() -> {
            try {
                Thread.sleep(4000); // 4 seconds grace period
            } catch (InterruptedException ignored) {}
            System.out.println("[RAFT] Grace period over, triggering initial leader election...");
            startElection();
        }).start();
    }

    private void resetElectionTimeout() {
        electionTimeout = MIN_ELECTION_TIMEOUT + random.nextInt((int)(MAX_ELECTION_TIMEOUT - MIN_ELECTION_TIMEOUT) + 2000);
        System.out.println("[RAFT] " + nodeId + " reset election timeout to " + electionTimeout + "ms");
    }

    public synchronized void startElection() {
        if (status == NodeStatus.LEADER) {
            System.out.println("[RAFT] " + nodeId + " is already leader, skipping election");
            return;
        }
        if (electionInProgress.getAndSet(true)) {
            System.out.println("[RAFT] " + nodeId + " election already in progress, skipping duplicate trigger");
            return;
        }
        System.out.println("[RAFT] " + nodeId + " starting election process...");
        currentTerm.incrementAndGet();
        status = NodeStatus.CANDIDATE;
        votesReceived.set(1); // Vote for self
        isLeader = false;
        currentLeader = null;

        nodeRegistryService.updateNodeStatus(nodeId, false, "CANDIDATE");

        System.out.println("[RAFT] " + nodeId + " became candidate for term " + currentTerm.get());
        for (String nodeAddress : nodeAddresses) {
            if (!nodeAddress.equals("localhost:" + port)) {
                requestVote(nodeAddress);
            }
        }
        resetElectionTimeout();
    }

    private void requestVote(String nodeAddress) {
        try {
            String urlStr = String.format("http://%s/api/vote?candidateId=%s&term=%d", 
                nodeAddress, nodeId, currentTerm.get());
            System.out.println("[RAFT] " + nodeId + " requesting vote from " + nodeAddress);
            Boolean voteGranted = restTemplate.postForObject(urlStr, null, Boolean.class);
            if (voteGranted != null && voteGranted) {
                int votes = votesReceived.incrementAndGet();
                System.out.println("[RAFT] " + nodeId + " received vote from " + nodeAddress + ", total votes: " + votes);
                if (votes > nodeAddresses.size() / 2 && status == NodeStatus.CANDIDATE) {
                    System.out.println("[RAFT] " + nodeId + " received majority of votes, becoming leader");
                    becomeLeader();
                }
            }
        } catch (Exception e) {
            System.out.println("[RAFT] " + nodeId + " failed to request vote from " + nodeAddress + ": " + e.getMessage());
        }
    }

    public synchronized void becomeLeader() {
        if (status != NodeStatus.CANDIDATE || votesReceived.get() <= nodeAddresses.size() / 2) {
            System.out.println("[RAFT] " + nodeId + " cannot become leader: status=" + status + ", votes=" + votesReceived.get());
            electionInProgress.set(false); // allow retry
            return;
        }
        System.out.println("[RAFT] " + nodeId + " transitioning to leader state");
        isLeader = true;
        status = NodeStatus.LEADER;
        currentLeader = nodeId;
        nodeRegistryService.updateNodeStatus(nodeId, true, "LEADER");
        System.out.println("[RAFT] " + nodeId + " became the leader for term " + currentTerm.get() + "!");
        broadcastNewLeader();
        electionInProgress.set(false); // allow future elections if needed
    }

    private void broadcastNewLeader() {
        System.out.println("[RAFT] " + nodeId + " broadcasting leadership change to all nodes");
        ClusterNode leaderNode = nodeRegistryService.getNode(nodeId);
        leaderNode.setLeader(true);
        leaderNode.setStatus("LEADER");
        for (String nodeAddress : nodeAddresses) {
            if (!nodeAddress.equals("localhost:" + port)) {
                try {
                    // First send heartbeat
                    String heartbeatUrl = String.format("http://%s/api/leader/heartbeat?leaderId=%s&term=%d", 
                        nodeAddress, nodeId, currentTerm.get());
                    restTemplate.postForObject(heartbeatUrl, null, Void.class);
                    // Then update node status
                    String updateUrl = String.format("http://%s/api/nodes/update", nodeAddress);
                    restTemplate.postForObject(updateUrl, leaderNode, Void.class);
                    System.out.println("[RAFT] " + nodeId + " successfully sent leader update to " + nodeAddress);
                } catch (Exception e) {
                    System.out.println("[RAFT] " + nodeId + " failed to send leader update to " + nodeAddress + ": " + e.getMessage());
                }
            }
        }
    }

    public synchronized void handleLeaderHeartbeat(String leaderId, long term) {
        System.out.println("[RAFT] " + nodeId + " received heartbeat from " + leaderId + " with term " + term);
        if (term > currentTerm.get()) {
            System.out.println("[RAFT] " + nodeId + " received higher term " + term + ", updating term and becoming follower");
            currentTerm.set(term);
            status = NodeStatus.FOLLOWER;
            isLeader = false;
            currentLeader = null;
            nodeRegistryService.updateNodeStatus(nodeId, false, "FOLLOWER");
        }
        lastHeartbeat = System.currentTimeMillis();
        if (currentLeader == null || !currentLeader.equals(leaderId)) {
            System.out.println("[RAFT] " + nodeId + " updating leader to " + leaderId);
            currentLeader = leaderId;
            isLeader = nodeId.equals(leaderId);
            status = isLeader ? NodeStatus.LEADER : NodeStatus.FOLLOWER;
            nodeRegistryService.updateNodeStatus(nodeId, isLeader, status.toString());
            System.out.println("[RAFT] " + nodeId + " updated leader to: " + leaderId + " for term " + term);
        }
        electionInProgress.set(false); // allow future elections if needed
    }

    private void startHeartbeatChecker() {
        scheduler.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            if (isLeader) {
                sendHeartbeats();
            } else if (now - lastHeartbeat > electionTimeout) {
                System.out.println("[RAFT] " + nodeId + " detected leader failure or no leader. Starting/retrying election...");
                electionInProgress.set(false); // allow new election
                startElection();
            }
            // If this node is a candidate and hasn't become leader, retry election after timeout
            if (status == NodeStatus.CANDIDATE && now - lastHeartbeat > electionTimeout) {
                System.out.println("[RAFT] " + nodeId + " is still candidate, retrying election...");
                electionInProgress.set(false);
                startElection();
            }
        }, 0, HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS);
    }

    private void sendHeartbeats() {
        ClusterNode leaderNode = nodeRegistryService.getNode(nodeId);
        leaderNode.setLeader(true);
        leaderNode.setStatus("LEADER");
        for (String nodeAddress : nodeAddresses) {
            if (!nodeAddress.equals("localhost:" + port)) {
                try {
                    // Send heartbeat
                    String heartbeatUrl = String.format("http://%s/api/leader/heartbeat?leaderId=%s&term=%d", 
                        nodeAddress, nodeId, currentTerm.get());
                    restTemplate.postForObject(heartbeatUrl, null, Void.class);
                    // Update node status
                    String updateUrl = String.format("http://%s/api/nodes/update", nodeAddress);
                    restTemplate.postForObject(updateUrl, leaderNode, Void.class);
                } catch (Exception e) {
                    System.out.println("[RAFT] " + nodeId + " failed to send heartbeat to " + nodeAddress + ": " + e.getMessage());
                }
            }
        }
    }

    // public synchronized boolean vote(String candidateId, long term) {
    //     System.out.println("[RAFT] " + nodeId + " received vote request from " + candidateId + " for term " + term);
    //     if (term < currentTerm.get()) {
    //         System.out.println("[RAFT] " + nodeId + " refusing vote to " + candidateId + " (term " + term + " < " + currentTerm.get() + ")");
    //         return false;
    //     }
    //     if (term > currentTerm.get()) {
    //         System.out.println("[RAFT] " + nodeId + " updating term to " + term + " and becoming follower");
    //         currentTerm.set(term);
    //         status = NodeStatus.FOLLOWER;
    //         isLeader = false;
    //         currentLeader = null;
    //         nodeRegistryService.updateNodeStatus(nodeId, false, "FOLLOWER");
    //     }
    //     if (status == NodeStatus.LEADER || (status == NodeStatus.FOLLOWER && currentLeader != null)) {
    //         System.out.println("[RAFT] " + nodeId + " refusing vote to " + candidateId + " (already " + status + ")");
    //         return false;
    //     }
    //     currentLeader = candidateId;
    //     System.out.println("[RAFT] " + nodeId + " voted for " + candidateId + " in term " + term);
    //     return true;
    // }
    
    private volatile String votedFor = null;

    public synchronized boolean vote(String candidateId, long term) {
    System.out.println("[RAFT] " + nodeId + " received vote request from " + candidateId + " for term " + term);

    if (term < currentTerm.get()) {
        System.out.println("[RAFT] " + nodeId + " refusing vote to " + candidateId + " (term " + term + " < " + currentTerm.get() + ")");
        return false;
    }

    if (term > currentTerm.get()) {
        System.out.println("[RAFT] " + nodeId + " updating term to " + term + " and becoming follower");
        currentTerm.set(term);
        status = NodeStatus.FOLLOWER;
        isLeader = false;
        currentLeader = null;
        votedFor = null;  // Reset vote for new term
        nodeRegistryService.updateNodeStatus(nodeId, false, "FOLLOWER");
    }

    // Vote only if haven't voted in this term
    if (votedFor == null || votedFor.equals(candidateId)) {
        votedFor = candidateId;
        currentLeader = candidateId;
        System.out.println("[RAFT] " + nodeId + " voted for " + candidateId + " in term " + term);
        return true;
    } else {
        System.out.println("[RAFT] " + nodeId + " already voted for " + votedFor + " in term " + currentTerm.get() + ", refusing vote to " + candidateId);
        return false;
    }
}


    public boolean isLeader() {
        return isLeader;
    }

    public String getCurrentLeader() {
        return currentLeader;
    }

    public NodeStatus getStatus() {
        return status;
    }

    public long getCurrentTerm() {
        return currentTerm.get();
    }

    public String getNodeId() {
        return nodeId;
    }
}