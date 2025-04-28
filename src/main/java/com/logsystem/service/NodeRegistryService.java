package com.logsystem.service;

import com.logsystem.model.ClusterNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import java.util.Arrays;

@Service
public class NodeRegistryService {

    @Value("${node.id}")
    private String nodeId;

    @Value("${nodes.list}")
    private String nodesList;

    private final Map<String, ClusterNode> nodes = new ConcurrentHashMap<>();
    private final RestTemplate restTemplate = new RestTemplate();

    @PostConstruct
    public void init() {
        registerKnownNodes();
    }

    public void registerKnownNodes() {
        System.out.println("Registering known nodes from list: " + nodesList);
        String[] nodeAddresses = nodesList.split(",");
        for (String address : nodeAddresses) {
            String[] parts = address.split(":");
            if (parts.length == 2) {
                String host = parts[0];
                int port = Integer.parseInt(parts[1]);
                String id = "node-" + port;  // This must match the node.id in properties
                
                ClusterNode node = new ClusterNode();
                node.setId(id);
                node.setAddress(host);
                node.setPort(port);
                node.setLeader(false);
                node.setStatus("FOLLOWER");
                
                nodes.put(id, node);
                System.out.println("Registered node: " + id + " at " + address);
            }
        }
        
        // Verify node registration
        System.out.println("Current node registry:");
        nodes.forEach((id, node) -> System.out.println("Node: " + node));
    }

    public List<ClusterNode> getAllNodes() {
        return new ArrayList<>(nodes.values());
    }

    public ClusterNode getNode(String nodeId) {
        return nodes.get(nodeId);
    }

    public synchronized void updateNodeStatus(String nodeId, boolean isLeader, String status) {
        System.out.println("Updating node " + nodeId + " status: leader=" + isLeader + ", status=" + status);
        ClusterNode node = nodes.get(nodeId);
        if (node != null) {
            node.setLeader(isLeader);
            node.setStatus(status);
            nodes.put(nodeId, node);
            System.out.println("Updated node " + nodeId + " status in registry");
            
            // If this node became leader, update other nodes' leader status
            if (isLeader && nodeId.equals(this.nodeId)) {
                for (ClusterNode otherNode : nodes.values()) {
                    if (!otherNode.getId().equals(nodeId)) {
                        otherNode.setLeader(false);
                        otherNode.setStatus("FOLLOWER");
                        System.out.println("Updated " + otherNode.getId() + " to follower status");
                    }
                }
                System.out.println("Updated all other nodes to follower status");
            }
            
            // Print current registry state
            System.out.println("Current node registry after update:");
            nodes.forEach((id, n) -> System.out.println("Node: " + n));
        } else {
            System.out.println("Warning: Attempted to update non-existent node " + nodeId);
        }
    }

    public void removeNode(String nodeId) {
        System.out.println("Removing node " + nodeId + " from registry");
        nodes.remove(nodeId);
    }

    public String getNodeId() {
        return nodeId;
    }

    public boolean isNodeRegistered(String nodeId) {
        return nodes.containsKey(nodeId);
    }

    public void broadcastNodeUpdate(ClusterNode updatedNode) {
        System.out.println("Broadcasting node update for " + updatedNode.getId());
        for (ClusterNode node : nodes.values()) {
            if (!node.getId().equals(nodeId)) {
                try {
                    String url = String.format("http://%s:%d/api/nodes/update", 
                        node.getAddress(), node.getPort());
                    restTemplate.postForEntity(url, updatedNode, Void.class);
                    System.out.println("Successfully broadcasted update to " + node.getId());
                } catch (Exception e) {
                    System.err.println("Failed to broadcast node update to " + node.getId() + 
                        ": " + e.getMessage());
                }
            }
        }
    }
}