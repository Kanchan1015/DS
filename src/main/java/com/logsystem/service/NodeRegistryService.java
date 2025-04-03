package com.logsystem.service;

import com.logsystem.model.ClusterNode;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NodeRegistryService {
    private final List<ClusterNode> clusterNodes = new ArrayList<>();

    public void registerNode(ClusterNode node) {
        clusterNodes.add(node);
    }

    public List<ClusterNode> getAllNodes() {
        return clusterNodes;
    }

    public ClusterNode getLeaderNode() {
        return clusterNodes.stream()
                .filter(ClusterNode::isLeader)
                .findFirst()
                .orElse(null);
    }

    public void setLeader(String nodeId) {
        for (ClusterNode node : clusterNodes) {
            node.setLeader(node.getNodeId().equals(nodeId));
        }
    }
}