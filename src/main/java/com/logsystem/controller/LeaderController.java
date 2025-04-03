package com.logsystem.controller;

import com.logsystem.model.ClusterNode;
import com.logsystem.service.LeaderElectionService;
import com.logsystem.service.NodeRegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
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
        return ResponseEntity.ok("Leader election triggered.");
    }

    @GetMapping("/leader")
    public ResponseEntity<String> getLeader() {
        ClusterNode leaderNode = nodeRegistryService.getLeaderNode();
        if (leaderNode != null) {
            return ResponseEntity.ok("Current Leader: " + leaderNode.getNodeId());
        } else {
            return ResponseEntity.ok("No leader elected yet.");
        }
    }

    @GetMapping("/nodes")
    public ResponseEntity<List<ClusterNode>> getNodes() {
        return ResponseEntity.ok(nodeRegistryService.getAllNodes());
    }
}