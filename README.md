# Distributed Fault-Tolerant Logging System

This project is a distributed logging system designed for fault tolerance, consistency, and high availability. It supports real-time log ingestion, querying, time synchronization, and coordination across multiple nodes using consensus-based leader election.

## Features

- Leader Election using RAFT
- Fault-Tolerant Log Replication
- Consistent Log Storage with MongoDB Replica Set
- NTP-based Time Synchronization
- REST APIs for Log Management and Cluster Operations
- Node Status Monitoring and Heartbeat Mechanism
- Log Recovery and Out-of-Order Log Simulation

---

## System Modules

### 1. Leader Election & Consensus (RAFT-Based)

- Implemented via `LeaderElectionService.java`
- Nodes exchange heartbeats and vote for leadership
- RAFT-like term tracking ensures a single leader per term
- APIs:
  - `POST /api/elect-leader`: Initiate leader election
  - `POST /api/leader/heartbeat`: Accept heartbeat from leader
  - `POST /api/vote`: Vote for a candidate
  - `GET /api/leader`: Get current leader status
  - `GET /api/status`: Get current node status

### 2. Distributed Log Controller

- Implemented via `LogController.java`
- Supports log creation, retrieval, recovery, and simulation
- Validates log level and message integrity
- Simulates and sorts logs for consistency testing
- APIs:
  - `POST /api/logs`: Create a new log
  - `GET /api/logs`: Retrieve all logs (paginated)
  - `GET /api/logs/level/{level}`: Filter logs by level
  - `GET /api/logs/sorted`: Get logs sorted by timestamp
  - `POST /api/logs/simulate`: Simulate out-of-order logs
  - `POST /api/logs/recovery`: Recover logs manually

### 3. Node Registry & Monitoring

- Implemented within `LeaderController.java`
- Tracks all nodes in the cluster with roles and statuses
- Supports dynamic updates to node metadata
- APIs:
  - `GET /api/nodes`: List all registered nodes
  - `POST /api/nodes/update`: Update node's leader status and role

### 4. Time Synchronization

- NTP-based timestamp generation using `TimeSyncService.java`
- Ensures log timestamps are consistent across all nodes
- Falls back to system time in case of NTP failure
- Automatically applies synchronized timestamps during log creation

---

## ⚙️ Configuration

Each node is defined in a separate properties file:

- `application-8081.properties`
- `application-8082.properties`
- `application-8083.properties`

### Example Node Config:

```properties
spring.data.mongodb.uri=mongodb://localhost:27018,localhost:27019,localhost:27020/logsystem?replicaSet=myReplSet
server.port=8081
node.id=node-8081
nodes.list=localhost:8081,localhost:8082,localhost:8083


```
