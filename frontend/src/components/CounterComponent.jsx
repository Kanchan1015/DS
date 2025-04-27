import React, { useState } from "react";
import api from "../api/api";

const CounterComponent = () => {
  const [counter, setCounter] = useState(0);
  const [counterName, setCounterName] = useState("");
  const [response, setResponse] = useState("");

  const getNextCounter = async () => {
    try {
      const res = await api.get(
        `/logs/counter/next?counterName=${counterName}`
      );
      setCounter(res.data);
      setResponse("");
    } catch (error) {
      setResponse("Error fetching next counter.");
    }
  };

  return (
    <div className="p-4 max-w-md mx-auto bg-white shadow-md rounded-md mt-6">
      <h2 className="text-2xl font-bold mb-4">Counter</h2>
      <input
        type="text"
        placeholder="Counter Name"
        value={counterName}
        onChange={(e) => setCounterName(e.target.value)}
        className="w-full px-4 py-2 border rounded-md mb-4"
      />
      <button
        onClick={getNextCounter}
        className="w-full bg-blue-500 text-white py-2 rounded-md hover:bg-blue-600"
      >
        Get Next Counter
      </button>
      {response && <p className="mt-4 text-center text-red-600">{response}</p>}
      {counter !== 0 && (
        <div className="mt-6">
          <h3 className="text-lg font-bold">Next Counter: {counter}</h3>
        </div>
      )}
    </div>
  );
};

export default CounterComponent;
