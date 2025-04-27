package com.logsystem.controller;

import com.logsystem.service.LeaderElectionService;
import com.logsystem.service.NodeRegistryService;

// import main.java.com.logsystem.model.ClusterNode;
import com.logsystem.model.ClusterNode;

import java.util.List;

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
        System.out.println("Leader election triggered by request.");
        leaderElectionService.startElection();
        leaderElectionService.startElectionTimeout();  // Start election timeout
        return ResponseEntity.ok("Leader election triggered.");
    }

    @GetMapping("/leader")
    public ResponseEntity<String> getLeader() {
        String leaderId = leaderElectionService.getCurrentLeader();
        if (leaderId != null) {
            return ResponseEntity.ok("Current Leader: " + leaderId);
        } else {
            return ResponseEntity.ok("No leader elected yet.");
        }
    }

    @PostMapping("/heartbeat/{leaderId}")
    public ResponseEntity<String> sendHeartbeat(@PathVariable String leaderId) {
        leaderElectionService.handleLeaderHeartbeat(leaderId);
        return ResponseEntity.ok("Heartbeat received for leader: " + leaderId);
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
}