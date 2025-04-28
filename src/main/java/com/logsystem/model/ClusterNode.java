package com.logsystem.model;

import java.io.Serializable;
import java.util.Objects;

public class ClusterNode implements Serializable {

    private String id;
    private String address;
    private int port;
    private boolean leader;
    private String status;
    private long lastHeartbeat;

    public ClusterNode() {
        this.lastHeartbeat = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isLeader() {
        return leader;
    }

    public void setLeader(boolean leader) {
        this.leader = leader;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(long lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    @Override
    public String toString() {
        return "ClusterNode{" +
                "id='" + id + '\'' +
                ", address='" + address + '\'' +
                ", port=" + port +
                ", leader=" + leader +
                ", status='" + status + '\'' +
                ", lastHeartbeat=" + lastHeartbeat +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ClusterNode that = (ClusterNode) obj;
        return port == that.port && id.equals(that.id) && address.equals(that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, address, port);
    }
}