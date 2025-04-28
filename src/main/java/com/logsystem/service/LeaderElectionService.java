package com.logsystem.service;

import com.logsystem.model.ClusterNode;
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
        System.out.println("Initializing LeaderElectionService for node: " + nodeId + " on port: " + port);
        resetElectionTimeout();
        startHeartbeatChecker();
        // Start election immediately after initialization
        startElection();
    }

    private void resetElectionTimeout() {
        electionTimeout = MIN_ELECTION_TIMEOUT + random.nextInt((int)(MAX_ELECTION_TIMEOUT - MIN_ELECTION_TIMEOUT));
        System.out.println(nodeId + " reset election timeout to " + electionTimeout + "ms");
    }

    public synchronized void startElection() {
        if (status == NodeStatus.LEADER) {
            System.out.println(nodeId + " is already leader, skipping election");
            return;
        }

        System.out.println(nodeId + " starting election process...");
        currentTerm.incrementAndGet();
        status = NodeStatus.CANDIDATE;
        votesReceived.set(1); // Vote for self
        isLeader = false;
        currentLeader = null;
        
        // Update local node status
        nodeRegistryService.updateNodeStatus(nodeId, false, "CANDIDATE");
        
        System.out.println(nodeId + " became candidate for term " + currentTerm.get());
        
        // Request votes from all other nodes
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
            
            System.out.println(nodeId + " requesting vote from " + nodeAddress);
            Boolean voteGranted = restTemplate.postForObject(urlStr, null, Boolean.class);
            
            if (voteGranted != null && voteGranted) {
                int votes = votesReceived.incrementAndGet();
                System.out.println(nodeId + " received vote from " + nodeAddress + ", total votes: " + votes);
                
                if (votes > nodeAddresses.size() / 2 && status == NodeStatus.CANDIDATE) {
                    System.out.println(nodeId + " received majority of votes, becoming leader");
                    becomeLeader();
                }
            }
        } catch (Exception e) {
            System.out.println(nodeId + " failed to request vote from " + nodeAddress + ": " + e.getMessage());
        }
    }

    public synchronized void becomeLeader() {
        if (status != NodeStatus.CANDIDATE || votesReceived.get() <= nodeAddresses.size() / 2) {
            System.out.println(nodeId + " cannot become leader: status=" + status + ", votes=" + votesReceived.get());
            return;
        }
        
        System.out.println(nodeId + " transitioning to leader state");
        isLeader = true;
        status = NodeStatus.LEADER;
        currentLeader = nodeId;
        
        // Update local node status
        nodeRegistryService.updateNodeStatus(nodeId, true, "LEADER");
        
        System.out.println(nodeId + " became the leader for term " + currentTerm.get() + "!");
        
        // Broadcast leadership change to all nodes
        broadcastNewLeader();
    }

    private void broadcastNewLeader() {
        System.out.println(nodeId + " broadcasting leadership change to all nodes");
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
                    
                    System.out.println(nodeId + " successfully sent leader update to " + nodeAddress);
                } catch (Exception e) {
                    System.out.println(nodeId + " failed to send leader update to " + nodeAddress + ": " + e.getMessage());
                }
            }
        }
    }

    public synchronized void handleLeaderHeartbeat(String leaderId, long term) {
        System.out.println(nodeId + " received heartbeat from " + leaderId + " with term " + term);
        
        if (term > currentTerm.get()) {
            System.out.println(nodeId + " received higher term " + term + ", updating term and becoming follower");
            currentTerm.set(term);
            status = NodeStatus.FOLLOWER;
            isLeader = false;
            currentLeader = null;
            
            // Update local node status
            nodeRegistryService.updateNodeStatus(nodeId, false, "FOLLOWER");
        }
        
        lastHeartbeat = System.currentTimeMillis();
        
        if (currentLeader == null || !currentLeader.equals(leaderId)) {
            System.out.println(nodeId + " updating leader to " + leaderId);
            currentLeader = leaderId;
            isLeader = nodeId.equals(leaderId);
            status = isLeader ? NodeStatus.LEADER : NodeStatus.FOLLOWER;
            
            // Update node status in registry
            nodeRegistryService.updateNodeStatus(nodeId, isLeader, status.toString());
            
            System.out.println(nodeId + " updated leader to: " + leaderId + " for term " + term);
        }
    }

    private void startHeartbeatChecker() {
        scheduler.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            
            if (isLeader) {
                sendHeartbeats();
            } else if (now - lastHeartbeat > electionTimeout) {
                System.out.println(nodeId + " detected leader failure. Starting election...");
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
                    System.out.println(nodeId + " failed to send heartbeat to " + nodeAddress + ": " + e.getMessage());
                }
            }
        }
    }

    public synchronized boolean vote(String candidateId, long term) {
        System.out.println(nodeId + " received vote request from " + candidateId + " for term " + term);
        
        if (term < currentTerm.get()) {
            System.out.println(nodeId + " refusing vote to " + candidateId + " (term " + term + " < " + currentTerm.get() + ")");
            return false;
        }
        
        if (term > currentTerm.get()) {
            System.out.println(nodeId + " updating term to " + term + " and becoming follower");
            currentTerm.set(term);
            status = NodeStatus.FOLLOWER;
            isLeader = false;
            currentLeader = null;
            
            // Update local node status
            nodeRegistryService.updateNodeStatus(nodeId, false, "FOLLOWER");
        }
        
        if (status == NodeStatus.LEADER || (status == NodeStatus.FOLLOWER && currentLeader != null)) {
            System.out.println(nodeId + " refusing vote to " + candidateId + " (already " + status + ")");
            return false;
        }
        
        currentLeader = candidateId;
        System.out.println(nodeId + " voted for " + candidateId + " in term " + term);
        return true;
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