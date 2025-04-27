import React, { useState } from "react";
import api from "../api/api";

const CreateLog = () => {
  const [message, setMessage] = useState("");
  const [level, setLevel] = useState("INFO");
  const [response, setResponse] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const res = await api.post("/logs", { message, level });
      setResponse("Log created successfully!");
      setMessage("");
      setLevel("INFO");
    } catch (error) {
      setResponse(error.response?.data || "Error creating log");
    }
  };

  return (
    <div className="p-4 max-w-md mx-auto bg-white shadow-md rounded-md mt-6">
      <h2 className="text-2xl font-bold mb-4">Create Log</h2>
      <form onSubmit={handleSubmit} className="space-y-4">
        <input
          type="text"
          placeholder="Log message"
          value={message}
          onChange={(e) => setMessage(e.target.value)}
          className="w-full px-4 py-2 border rounded-md"
          required
        />
        <select
          value={level}
          onChange={(e) => setLevel(e.target.value)}
          className="w-full px-4 py-2 border rounded-md"
        >
          <option value="INFO">INFO</option>
          <option value="WARNING">WARNING</option>
          <option value="ERROR">ERROR</option>
          <option value="DEBUG">DEBUG</option>
        </select>
        <button
          type="submit"
          className="w-full bg-blue-500 text-white py-2 rounded-md hover:bg-blue-600"
        >
          Create Log
        </button>
      </form>
      {response && <p className="mt-4 text-center">{response}</p>}
    </div>
  );
};

export default CreateLog;
