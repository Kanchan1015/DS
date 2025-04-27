import React, { useState, useEffect } from "react";
import api from "../api/api";

const LogsByLevel = () => {
  const [level, setLevel] = useState("INFO");
  const [logs, setLogs] = useState([]);
  const [response, setResponse] = useState("");

  const fetchLogsByLevel = async () => {
    try {
      const res = await api.get(`/logs/level/${level}`);
      setLogs(res.data.content || []);
      setResponse("");
    } catch (error) {
      setResponse("Error fetching logs by level.");
    }
  };

  useEffect(() => {
    fetchLogsByLevel();
  }, [level]);

  return (
    <div className="p-4 max-w-md mx-auto bg-white shadow-md rounded-md mt-6">
      <h2 className="text-2xl font-bold mb-4">Logs by Level</h2>
      <select
        value={level}
        onChange={(e) => setLevel(e.target.value)}
        className="w-full px-4 py-2 border rounded-md mb-4"
      >
        <option value="INFO">INFO</option>
        <option value="WARNING">WARNING</option>
        <option value="ERROR">ERROR</option>
        <option value="DEBUG">DEBUG</option>
      </select>
      <button
        onClick={fetchLogsByLevel}
        className="w-full bg-blue-500 text-white py-2 rounded-md hover:bg-blue-600"
      >
        Fetch Logs by Level
      </button>
      {response && <p className="mt-4 text-center text-red-600">{response}</p>}
      <div className="overflow-x-auto mt-6">
        <table className="w-full table-auto border-collapse">
          <thead>
            <tr className="bg-gray-200">
              <th className="border px-4 py-2">ID</th>
              <th className="border px-4 py-2">Message</th>
              <th className="border px-4 py-2">Level</th>
              <th className="border px-4 py-2">Timestamp</th>
            </tr>
          </thead>
          <tbody>
            {logs.map((log) => (
              <tr key={log.id} className="text-center">
                <td className="border px-4 py-2">{log.id}</td>
                <td className="border px-4 py-2">{log.message}</td>
                <td className="border px-4 py-2">{log.level}</td>
                <td className="border px-4 py-2">
                  {new Date(log.timestamp).toLocaleString()}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default LogsByLevel;
