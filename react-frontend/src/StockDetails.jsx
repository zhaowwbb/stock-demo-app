import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';

export default function StockDetails() {
  const { symbol } = useParams(); // Extract stock ticker from URL route parameters
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);

  const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';   

  useEffect(() => {
    setLoading(true);
    fetch(`${API_BASE_URL}/api/stock/${symbol}`)
      .then((res) => res.json())
      .then((data) => {
        setHistory(data);
        setLoading(false);
      })
      .catch((err) => {
        console.error("Failed historical retrieval tracking:", err);
        setLoading(false);
      });
  }, [symbol]);

  if (loading) {
    return <div className="text-center py-10 font-medium text-gray-500">Loading tracking vectors for {symbol}...</div>;
  }

  return (
    <div className="space-y-6">
      {/* Top action context banner */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-extrabold text-gray-900">{symbol} Historical Ledger</h1>
          <p className="text-sm text-gray-500">Time-series ledger history parsed from core telemetry metrics</p>
        </div>
        <Link to="/" className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 shadow-sm">
          ← Back to Dashboard
        </Link>
      </div>

      {/* Main Historical Table Matrix */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse text-sm">
            <thead>
              <tr className="bg-slate-900 text-slate-200 uppercase tracking-wider text-xs font-semibold">
                <th className="p-4">Updated Date</th>
                <th className="p-4 text-right">Open</th>
                <th className="p-4 text-right">High</th>
                <th className="p-4 text-right">Low</th>
                <th className="p-4 text-right">Close</th>
                <th className="p-4 text-right">Volume</th>
              </tr>
            </thead>
            <tbody>
              {history.length === 0 ? (
                <tr>
                  <td colSpan="6" className="text-center p-8 text-gray-400 italic">No historical traces detected for this symbol node.</td>
                </tr>
              ) : (
                history.map((record, index) => (
                  <tr key={record.id || index} className="border-b border-gray-100 hover:bg-gray-50/70 transition">
                    <td className="p-4 font-medium text-gray-700">
                      {new Date(record.updatedDate).toLocaleDateString(undefined, {
                        year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'
                      })}
                    </td>
                    <td className="p-4 text-right font-mono text-gray-600">${record.open?.toFixed(2)}</td>
                    <td className="p-4 text-right font-mono text-gray-600 text-green-600">${record.high?.toFixed(2)}</td>
                    <td className="p-4 text-right font-mono text-gray-600 text-red-500">${record.low?.toFixed(2)}</td>
                    <td className="p-4 text-right font-mono font-bold text-slate-800">${record.close?.toFixed(2)}</td>
                    <td className="p-4 text-right font-mono text-gray-500">{record.volume?.toLocaleString()}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}