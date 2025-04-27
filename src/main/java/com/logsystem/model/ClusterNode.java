package com.logsystem.model;

import java.util.Objects;

public class ClusterNode {

    private final String nodeId;
    private final String address;
    private final int port;
    private boolean isLeader;

    public ClusterNode(String nodeId, String address, int port) {
        this.nodeId = nodeId;
        this.address = address;
        this.port = port;
        this.isLeader = false;  // Default to non-leader
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public boolean isLeader() {
        return isLeader;
    }

    public void setLeader(boolean isLeader) {
        this.isLeader = isLeader;
    }

    // New getHost() method
    public String getHost() {
        return address.split(":")[0];  // Extracts 'localhost' from 'localhost:8080'
    }

    @Override
    public String toString() {
        return "ClusterNode{" +
               "nodeId='" + nodeId + '\'' +
               ", address='" + address + '\'' +
               ", port=" + port +
               ", isLeader=" + isLeader +
               '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ClusterNode that = (ClusterNode) obj;
        return port == that.port && nodeId.equals(that.nodeId) && address.equals(that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId, address, port);
    }
}