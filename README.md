# 🧠 Distributed Logging System – Fault-Tolerant, Consistent & Scalable

This project is a **distributed, fault-tolerant logging system** developed using **Java Spring Boot**, **MongoDB replica sets**, **Raft consensus algorithm**, and **NTP-based time synchronization**. It is designed to collect, store, and process logs from multiple clients in real time, ensuring high availability, consistency, and resilience across distributed servers.

---

## 🔧 Tech Stack

- **Java 17 + Spring Boot**
- **MongoDB Replica Sets**
- **Raft Consensus (Custom-Implemented)**
- **NTP (Network Time Protocol) for time synchronization**
- **Docker (optional for Mongo cluster setup)**

---

## 🚀 Features

- 🟢 Real-time distributed log ingestion
- 🛡️ Fault-tolerant through log replication and automatic failover
- ⚙️ Strong consistency using Raft algorithm
- ⏱️ Accurate timestamps with NTP syncing and log reordering
- 🔍 REST APIs for log submission, retrieval, and search
- 🔄 Auto-recovery for failed/rejoined nodes

---

## 📦 Getting Started

### 1. Clone the Repository

bash
git clone https://github.com/Kanchan1015/DS.git
cd DS


### 2. Configure MongoDB Replica Set

Ensure MongoDB is installed and initialized as a replica set


### 3. Edit Application Properties

Update `src/main/resources/application.yml` or `.properties` with:

* MongoDB replica set URI
* NTP server address
* Raft ports (if customized)

### 4. Run the App

bash
./mvn spring-boot:run


Visit: [http://localhost:8080](http://localhost:8001)

---



## 🏗️ System Architecture

```
Clients → Spring Boot Services → MongoDB Replica Set
                          ↘
                       Raft Cluster
                          ↘
                      NTP Time Sync
```

---

## ⚙️ Subsystems

### Fault Tolerance

* Multi-node replication
* Failure detection & auto redirection
* Log recovery on rejoin

### Replication & Consistency

* MongoDB with quorum-based writes
* Log deduplication

### Time Synchronization

* NTP for accurate timestamps
* Event reordering based on logical clocks

### Consensus & Leadership

* Raft for leader election
* Commit agreement across nodes


## 🧪 Testing

* Run unit tests: `mvn test`
* Simulate failures: network drops, node crashes
* Integration tests for log consistency across nodes

---




## 📃 License

This project is developed for academic purposes. You may reuse or extend it with appropriate attribution.

---

