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

// ... existing imports ...
function App() {
  return (
    <div className="min-h-screen bg-gray-100">
      {/* Navigation Bar */}
      <nav className="bg-blue-700 text-white px-6 py-4 mb-8 shadow">
        <div className="container mx-auto flex justify-between items-center">
          <span className="text-2xl font-bold">Log System Dashboard</span>
          <span className="text-sm">Distributed Systems Project</span>
        </div>
      </nav>

      <div className="container mx-auto px-4">
        {/* Log-related section */}
        <section className="mb-10">
          <h2 className="text-xl font-semibold mb-4 text-blue-700">
            Log Management
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <CreateLog />
            <ViewLogs />
            <LogsByLevel />
            <SimulateLogs />
            <SortedLogs />
            <CounterComponent />
            <RecoverLog />
          </div>
        </section>

        {/* Cluster and leader election section */}
        <section>
          <h2 className="text-xl font-semibold mb-4 text-blue-700">
            Cluster Management
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <LeaderElection />
            <ViewLeader />
            <ViewNodes />
          </div>
        </section>
      </div>
    </div>
  );
}

export default App;
