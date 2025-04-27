import React, { useState } from "react";
import axios from "axios";

const LeaderElection = () => {
  const [message, setMessage] = useState("");

  const handleElectLeader = async () => {
    try {
      const response = await axios.post(
        "http://localhost:8081/api/elect-leader"
      );
      setMessage(response.data);
    } catch (error) {
      setMessage("Error triggering leader election.");
    }
  };

  return (
    <div className="mb-6">
      <button
        onClick={handleElectLeader}
        className="bg-blue-500 text-white px-4 py-2 rounded"
      >
        Elect Leader
      </button>
      <p className="mt-2">{message}</p>
    </div>
  );
};

export default LeaderElection;
