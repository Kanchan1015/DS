import React from "react";
import CreateLog from "./components/CreateLog";
import ViewLogs from "./components/ViewLogs";
import LogsByLevel from "./components/LogsByLevel";
import SimulateLogs from "./components/SimulateLogs";
import SortedLogs from "./components/SortedLogs";
import CounterComponent from "./components/CounterComponent";
import RecoverLog from "./components/RecoverLog";
import LeaderElection from "./components/LeaderElection";
import ViewLeader from "./components/ViewLeader";
import ViewNodes from "./components/ViewNodes";

function App() {
  return (
    <div className="min-h-screen bg-gray-100 p-6">
      <h1 className="text-3xl font-bold text-center mb-8">
        Log System Frontend
      </h1>

      {/* Log-related components */}
      <CreateLog />
      <ViewLogs />
      <LogsByLevel />
      <SimulateLogs />
      <SortedLogs />
      <CounterComponent />
      <RecoverLog />

      {/* Cluster and leader election components */}
      <LeaderElection />
      <ViewLeader />
      <ViewNodes />
    </div>
  );
}

export default App;
