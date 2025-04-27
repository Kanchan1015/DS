import React, { useState, useEffect } from "react";
import api from "../api/api";

const SortedLogs = () => {
  const [logs, setLogs] = useState([]);
  const [response, setResponse] = useState("");

  const fetchSortedLogs = async () => {
    try {
      const res = await api.get("/logs/sorted");
      setLogs(res.data);
      setResponse("");
    } catch (error) {
      setResponse("Error fetching sorted logs.");
    }
  };

  useEffect(() => {
    fetchSortedLogs();
  }, []);

  return (
    <div className="p-4 max-w-md mx-auto bg-white shadow-md rounded-md mt-6">
      <h2 className="text-2xl font-bold mb-4">Sorted Logs</h2>
      <button
        onClick={fetchSortedLogs}
        className="w-full bg-blue-500 text-white py-2 rounded-md hover:bg-blue-600"
      >
        Fetch Sorted Logs
      </button>
      {response && <p className="mt-4 text-center text-red-600">{response}</p>}
      <div className="mt-6">
        <ul>
          {logs.map((log) => (
            <li key={log.id} className="border-b py-2">
              <p>{log.message}</p>
              <span className="text-sm text-gray-500">
                {new Date(log.timestamp).toLocaleString()}
              </span>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
};

export default SortedLogs;
