package com.logsystem.service;

import com.logsystem.model.ClusterNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class NodeRegistryService {

    private final List<ClusterNode> clusterNodes = new ArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();  // Lock to synchronize leader election

    @Value("${server.port}")
    private int serverPort;

    @PostConstruct
    public void registerSelfNode() {
        String nodeId = "node-" + serverPort;
        String address = "localhost:" + serverPort;
        ClusterNode selfNode = new ClusterNode(nodeId, address, serverPort);
        registerNode(selfNode);
        System.out.println("Self node registered: " + selfNode);
    }

    // Registers a new node in the cluster
    public void registerNode(ClusterNode node) {
        lock.lock();  // Synchronizing to avoid race conditions during node registration
        try {
            clusterNodes.add(node);
            System.out.println("Node registered: " + node);
        } finally {
            lock.unlock();
        }
    }

    // Get all nodes in the cluster
    public List<ClusterNode> getAllNodes() {
        return clusterNodes;
    }

    // Get the current leader node
    public ClusterNode getLeaderNode() {
        return clusterNodes.stream()
                .filter(ClusterNode::isLeader)
                .findFirst()
                .orElse(null);
    }

    // Set the leader of the cluster
    public void setLeader(String nodeId) {
        lock.lock();  // Locking to prevent concurrent updates to the leader
        try {
            for (ClusterNode node : clusterNodes) {
                node.setLeader(node.getNodeId().equals(nodeId));
            }
            System.out.println("Leader set to: " + nodeId);
        } finally {
            lock.unlock();
        }
    }
}