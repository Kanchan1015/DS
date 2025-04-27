import React, { useState } from "react";
import api from "../api/api";

const RecoverLog = () => {
  const [log, setLog] = useState("");
  const [response, setResponse] = useState("");

  const recoverLog = async () => {
    try {
      const res = await api.post("/logs/recovery", log);
      setResponse("Log recovered successfully!");
      setLog("");
    } catch (error) {
      setResponse("Error recovering log.");
    }
  };

  return (
    <div className="p-4 max-w-md mx-auto bg-white shadow-md rounded-md mt-6">
      <h2 className="text-2xl font-bold mb-4">Recover Log</h2>
      <textarea
        value={log}
        onChange={(e) => setLog(e.target.value)}
        placeholder="Enter log in the format 'message|level'"
        className="w-full px-4 py-2 border rounded-md mb-4"
        rows="4"
      />
      <button
        onClick={recoverLog}
        className="w-full bg-blue-500 text-white py-2 rounded-md hover:bg-blue-600"
      >
        Recover Log
      </button>
      {response && <p className="mt-4 text-center">{response}</p>}
    </div>
  );
};

export default RecoverLog;
