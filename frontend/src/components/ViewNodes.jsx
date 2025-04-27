import React, { useState } from "react";
import axios from "axios";

const ViewNodes = () => {
  const [nodes, setNodes] = useState([]);

  const fetchNodes = async () => {
    try {
      const response = await axios.get("http://localhost:8081/api/nodes");
      setNodes(response.data);
    } catch (error) {
      setNodes([]);
    }
  };

  return (
    <div className="mb-6">
      <button
        onClick={fetchNodes}
        className="bg-yellow-500 text-white px-4 py-2 rounded"
      >
        Get All Nodes
      </button>
      <ul className="mt-4">
        {nodes.length > 0 ? (
          nodes.map((node, index) => (
            <li key={index} className="bg-gray-200 p-2 rounded mb-2">
              Node ID: {node.nodeId}, Status: {node.status}
            </li>
          ))
        ) : (
          <li>No nodes found or error fetching nodes.</li>
        )}
      </ul>
    </div>
  );
};

export default ViewNodes;
