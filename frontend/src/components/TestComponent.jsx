import React from "react";

const TestComponent = () => {
  return (
    <div className="flex justify-center items-center h-screen bg-gray-100">
      <div className="p-10 bg-white shadow-xl rounded-lg">
        <h1 className="text-4xl font-bold text-blue-500 mb-4">
          Hello, Tailwind!
        </h1>
        <p className="text-gray-700">
          If you see this styled component, everything is working properly!
        </p>
        <button className="mt-4 px-6 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600">
          Click Me
        </button>
      </div>
    </div>
  );
};

export default TestComponent;
