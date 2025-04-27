// src/components/ViewLogs.jsx
import React, { useState, useEffect } from "react";
import axios from "axios";

const ViewLogs = () => {
  const [logs, setLogs] = useState([]);
  const [page, setPage] = useState(0); // Spring uses 0-based pages
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(false);

  const fetchLogs = async (pageNumber) => {
    try {
      setLoading(true);
      const response = await axios.get(`http://localhost:8081/api/logs`, {
        params: {
          page: pageNumber,
          size: 10, // 5 logs per page, you can change this
        },
      });
      setLogs(response.data.content); // because it's Page object
      setTotalPages(response.data.totalPages);
    } catch (error) {
      console.error("Error fetching logs:", error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLogs(page);
  }, [page]);

  const handlePrevious = () => {
    if (page > 0) setPage(page - 1);
  };

  const handleNext = () => {
    if (page < totalPages - 1) setPage(page + 1);
  };

  return (
    <div className="bg-white p-6 rounded-lg shadow-md mb-8">
      <h2 className="text-2xl font-semibold mb-4 text-center">All Logs</h2>

      {loading ? (
        <p className="text-center">Loading...</p>
      ) : (
        <>
          <ul className="space-y-4">
            {logs.map((log) => (
              <li key={log.id} className="p-4 border rounded-md shadow-sm">
                <p>
                  <strong>Message:</strong> {log.message}
                </p>
                <p>
                  <strong>Level:</strong> {log.level}
                </p>
                <p>
                  <strong>Timestamp:</strong>{" "}
                  {new Date(log.timestamp).toLocaleString()}
                </p>
              </li>
            ))}
          </ul>

          {/* Pagination Controls */}
          <div className="flex justify-center items-center space-x-4 mt-6">
            <button
              onClick={handlePrevious}
              disabled={page === 0}
              className="px-4 py-2 bg-blue-500 text-white rounded disabled:opacity-50"
            >
              Previous
            </button>
            <span>
              Page {page + 1} of {totalPages}
            </span>
            <button
              onClick={handleNext}
              disabled={page >= totalPages - 1}
              className="px-4 py-2 bg-blue-500 text-white rounded disabled:opacity-50"
            >
              Next
            </button>
          </div>
        </>
      )}
    </div>
  );
};

export default ViewLogs;
