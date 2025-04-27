import React, { useState } from "react";
import axios from "axios";

const ViewLeader = () => {
  const [leader, setLeader] = useState("");

  const fetchLeader = async () => {
    try {
      const response = await axios.get("http://localhost:8081/api/leader");
      setLeader(response.data);
    } catch (error) {
      setLeader("Error fetching leader.");
    }
  };

  return (
    <div className="mb-6">
      <button
        onClick={fetchLeader}
        className="bg-green-500 text-white px-4 py-2 rounded"
      >
        Get Current Leader
      </button>
      <p className="mt-2">{leader}</p>
    </div>
  );
};

export default ViewLeader;
