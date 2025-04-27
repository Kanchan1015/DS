import React, { useState } from "react";
import api from "../api/api";

const SimulateLogs = () => {
  const [simulatedLogs, setSimulatedLogs] = useState([]);
  const [response, setResponse] = useState("");

  const simulateLogs = async () => {
    try {
      const res = await api.post("/logs/simulate");
      setSimulatedLogs(res.data);
      setResponse("");
    } catch (error) {
      setResponse("Error simulating logs.");
    }
  };

  return (
    <div className="p-4 max-w-md mx-auto bg-white shadow-md rounded-md mt-6">
      <h2 className="text-2xl font-bold mb-4">Simulate Logs</h2>
      <button
        onClick={simulateLogs}
        className="w-full bg-blue-500 text-white py-2 rounded-md hover:bg-blue-600"
      >
        Simulate Out-of-Order Logs
      </button>
      {response && <p className="mt-4 text-center text-red-600">{response}</p>}
      <div className="mt-6">
        <h3 className="text-lg font-bold mb-4">Simulated Logs</h3>
        <ul className="list-disc pl-6">
          {simulatedLogs.map((log, index) => (
            <li key={index}>{log.message}</li>
          ))}
        </ul>
      </div>
    </div>
  );
};

export default SimulateLogs;
