package com.logsystem.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import java.util.*;

@Service
public class LeaderElectionService {

    private static final int ELECTION_TIMEOUT = 15000;  // Timeout duration in milliseconds
    private String currentLeader;
    private final List<String> nodes;  // List of nodes in the cluster
    private String nodeId;  // Unique node identifier (can be based on host or a random ID)
    private Timer electionTimer;  // Timer for election timeout

    // Constructor
    public LeaderElectionService(List<String> nodes, @Value("${node.id}") String nodeId) {
        this.nodes = nodes;
        this.nodeId = nodeId;
        this.currentLeader = null;
        System.out.println("LeaderElectionService initialized for node: " + nodeId);
    }

    // Initiates the leader election process
    public void startElection() {
        System.out.println("Node " + nodeId + " is starting an election...");

        // Simulate the vote request process for a leader election
        Map<String, Integer> votes = new HashMap<>();
        votes.put(nodeId, 1);  // Vote for itself
        
        // Request votes from other nodes
        for (String node : nodes) {
            if (!node.equals(nodeId)) {
                boolean vote = requestVote(node);
                if (vote) {
                    votes.put(node, votes.getOrDefault(node, 0) + 1);
                }
            }
        }

        // Find the node with the highest votes
        String electedLeader = null;
        int maxVotes = 0;
        for (Map.Entry<String, Integer> entry : votes.entrySet()) {
            if (entry.getValue() > maxVotes) {
                maxVotes = entry.getValue();
                electedLeader = entry.getKey();
            }
        }

        // Check if the node has received a majority of votes
        if (electedLeader != null && maxVotes > (nodes.size() / 2)) {
            currentLeader = electedLeader;
            System.out.println("Node " + currentLeader + " has been elected as the leader!");
        } else {
            System.out.println("Election failed. Not enough votes.");
        }
    }

    private boolean requestVote(String node) {
        System.out.println("Node " + nodeId + " requesting vote from " + node);
        return true;  // Simulated vote granting
    }

    public String getCurrentLeader() {
        return currentLeader;
    }

    public void handleLeaderHeartbeat(String leaderId) {
        if (currentLeader == null || !currentLeader.equals(leaderId)) {
            currentLeader = leaderId;
            System.out.println("Leader changed to: " + leaderId);
        }
    }

    public String getNodeId() {
        return nodeId;
    }

    // Check if the current node is the leader
    public boolean isLeader() {
        return currentLeader != null && currentLeader.equals(nodeId);
    }

    public void startElectionTimeout() {
        electionTimer = new Timer();
        electionTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (currentLeader == null) {
                    System.out.println("Leader not found. Starting new election...");
                    startElection();
                }
            }
        }, ELECTION_TIMEOUT);
    }

    public void cancelElectionTimeout() {
        if (electionTimer != null) {
            electionTimer.cancel();
        }
    }
}