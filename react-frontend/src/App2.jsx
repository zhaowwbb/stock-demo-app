import React from 'react';
import { BrowserRouter, Routes, Route, Link } from 'react-router-dom';
import DashboardHome from './DashboardHome';
import StockDetails from './StockDetails';

export default function App2() {
  return (
    <BrowserRouter>
      <div className="min-h-screen bg-gray-50 text-gray-900">
        {/* Navigation Bar */}
        <nav className="bg-slate-900 text-white shadow-md">
          <div className="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
            <Link to="/" className="text-xl font-bold tracking-wide text-indigo-400">
              📈 StockAnalytics Matrix
            </Link>
            <span className="text-sm text-gray-400 font-mono">Live Dashboard</span>
          </div>
        </nav>

        {/* Dynamic Route Container */}
        <main className="max-w-7xl mx-auto px-4 py-8">
          <Routes>
            <Route path="/" element={<DashboardHome />} />
            <Route path="/stock/:symbol" element={<StockDetails />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  );
}