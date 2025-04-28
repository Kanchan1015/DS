package com.logsystem.controller;

import com.logsystem.service.LeaderElectionService;
import com.logsystem.service.NodeRegistryService;
import com.logsystem.model.ClusterNode;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class LeaderController {

    private final LeaderElectionService leaderElectionService;
    private final NodeRegistryService nodeRegistryService;

    @Autowired
    public LeaderController(LeaderElectionService leaderElectionService, NodeRegistryService nodeRegistryService) {
        this.leaderElectionService = leaderElectionService;
        this.nodeRegistryService = nodeRegistryService;
    }

    @PostMapping("/elect-leader")
    public ResponseEntity<String> electLeader() {
        leaderElectionService.startElection();
        return ResponseEntity.ok("Leader election triggered for term " + leaderElectionService.getCurrentTerm());
    }

    @GetMapping("/leader")
    public ResponseEntity<Map<String, Object>> getLeader() {
        String leaderId = leaderElectionService.getCurrentLeader();
        Map<String, Object> response = new HashMap<>();
        response.put("leaderId", leaderId != null ? leaderId : "None");
        response.put("term", leaderElectionService.getCurrentTerm());
        response.put("nodeId", leaderElectionService.getNodeId());
        response.put("status", leaderElectionService.getStatus().toString());
        response.put("isLeader", leaderElectionService.isLeader());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/leader/heartbeat")
    public ResponseEntity<String> receiveHeartbeat(
            @RequestParam String leaderId,
            @RequestParam(defaultValue = "0") long term) {
        System.out.println("Received heartbeat from " + leaderId + " with term " + term);
        leaderElectionService.handleLeaderHeartbeat(leaderId, term);
        return ResponseEntity.ok(String.format("Heartbeat received for leader: %s (Term: %d)", leaderId, term));
    }

    @PostMapping("/vote")
    public ResponseEntity<Boolean> vote(
            @RequestParam String candidateId,
            @RequestParam(defaultValue = "0") long term) {
        System.out.println("Received vote request from " + candidateId + " for term " + term);
        boolean voteGranted = leaderElectionService.vote(candidateId, term);
        return ResponseEntity.ok(voteGranted);
    }

    @GetMapping("/nodes")
    public ResponseEntity<?> getAllNodes() {
        try {
            List<ClusterNode> nodes = nodeRegistryService.getAllNodes();
            System.out.println("Fetched all nodes: " + nodes);
            return ResponseEntity.ok(nodes);
        } catch (Exception e) {
            System.err.println("Error fetching nodes: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Error fetching nodes");
        }
    }
    
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getNodeStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("nodeId", leaderElectionService.getNodeId());
        status.put("term", leaderElectionService.getCurrentTerm());
        status.put("status", leaderElectionService.getStatus().toString());
        status.put("isLeader", leaderElectionService.isLeader());
        status.put("currentLeader", leaderElectionService.getCurrentLeader());
        
        return ResponseEntity.ok(status);
    }

    @PostMapping("/nodes/update")
    public ResponseEntity<String> updateNode(@RequestBody ClusterNode node) {
        System.out.println("Received node update: " + node);
        nodeRegistryService.updateNodeStatus(node.getId(), node.isLeader(), node.getStatus());
        return ResponseEntity.ok("Node updated successfully");
    }
}