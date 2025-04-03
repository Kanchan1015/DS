package com.logsystem.model;

public class ClusterNode {
    private String nodeId;
    private String host;
    private int port;
    private boolean isLeader;

    public ClusterNode(String nodeId, String host, int port) {
        this.nodeId = nodeId;
        this.host = host;
        this.port = port;
        this.isLeader = false; // Default: Not a leader
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public boolean isLeader() {
        return isLeader;
    }

    public void setLeader(boolean leader) {
        isLeader = leader;
    }

    @Override
    public String toString() {
        return "ClusterNode{" +
                "nodeId='" + nodeId + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", isLeader=" + isLeader +
                '}';
    }
}