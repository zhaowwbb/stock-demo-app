import React, { useState, useEffect, useMemo } from 'react';
import { Link } from 'react-router-dom';
import { ArrowUpDown, ArrowUpRight, Search, ChevronLeft, ChevronRight, Clock } from 'lucide-react';

export default function DashboardHome() {
  const [topAbsolute, setTopAbsolute] = useState([]);
  const [topPercentage, setTopPercentage] = useState([]);
  const [allStocks, setAllStocks] = useState([]);
  const [sortOrder, setSortOrder] = useState('desc'); // 'asc' or 'desc'
  
  // State for timestamp metadata
  const [absoluteUpdatedDate, setAbsoluteUpdatedDate] = useState('');
  const [percentageUpdatedDate, setPercentageUpdatedDate] = useState('');
  
  // State for Search and Pagination
  const [searchQuery, setSearchQuery] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 20;

  // Backend API URL Base Configuration
  const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'; 

  const API_BASE = `${API_BASE_URL}/api/stocks`;
  const STOCK_API_BASE = `${API_BASE_URL}/api/stock`;

  useEffect(() => {
    // Fetch absolute top gains
    fetch(`${API_BASE}/absolute`)
      .then((res) => res.json())
      .then((data) => {
        // If data is an array, check for an updatedDate property on the first element
        // or adapt if the API returns an object format like { data: [...], updatedDate: '...' }
        if (Array.isArray(data)) {
          setTopAbsolute(data);
          if (data[0]?.updatedDate) setAbsoluteUpdatedDate(data[0].updatedDate);
        } else if (data && data.records) {
          setTopAbsolute(data.records);
          if (data.updatedDate) setAbsoluteUpdatedDate(data.updatedDate);
        }
      })
      .catch((err) => console.error("Error loading absolute gains:", err));

    // Fetch percentage top gains
    fetch(`${API_BASE}/percentage`)
      .then((res) => res.json())
      .then((data) => {
        if (Array.isArray(data)) {
          setTopPercentage(data);
          if (data[0]?.updatedDate) setPercentageUpdatedDate(data[0].updatedDate);
        } else if (data && data.records) {
          setTopPercentage(data.records);
          if (data.updatedDate) setPercentageUpdatedDate(data.updatedDate);
        }
      })
      .catch((err) => console.error("Error loading percentage gains:", err));

    // Fetch entire symbol index
    fetch(`${API_BASE}/all-current`)
      .then((res) => res.json())
      .then((data) => setAllStocks(data))
      .catch((err) => console.error("Error loading catalog index:", err));
  }, []);

  // Handle price column sorting toggle
  const toggleSortPrice = () => {
    const nextOrder = sortOrder === 'desc' ? 'asc' : 'desc';
    setSortOrder(nextOrder);
    
    const sorted = [...allStocks].sort((a, b) => {
      const priceA = a.currentPrice || 0;
      const priceB = b.currentPrice || 0;
      return nextOrder === 'desc' ? priceB - priceA : priceA - priceB;
    });
    setAllStocks(sorted);
    setCurrentPage(1);
  };

  // Filter stocks based on Search Query
  const filteredStocks = useMemo(() => {
    return allStocks.filter((stock) =>
      stock.symbol?.toLowerCase().includes(searchQuery.toLowerCase().trim())
    );
  }, [allStocks, searchQuery]);

  // Calculate Pagination Boundaries
  const totalPages = Math.ceil(filteredStocks.length / itemsPerPage) || 1;
  
  const paginatedStocks = useMemo(() => {
    const startIndex = (currentPage - 1) * itemsPerPage;
    return filteredStocks.slice(startIndex, startIndex + itemsPerPage);
  }, [filteredStocks, currentPage]);

  useEffect(() => {
    setCurrentPage(1);
  }, [searchQuery]);

  // Helper helper to format timestamps safely if needed
  const formatUpdateLabel = (dateStr) => {
    if (!dateStr) return null;
    // Returns formatted string or drops it cleanly into the layout context
    return dateStr;
  };

  return (
    <div className="space-y-10">
      {/* SECTION 1 & 2: Top Gainers Dynamic Matrices */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        
        {/* Top 10 Absolute Increase */}
        <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 flex flex-col justify-between">
          <div>
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 mb-4">
              <h2 className="text-lg font-bold text-gray-800 flex items-center gap-2">
                <span className="p-1.5 bg-green-50 rounded-lg text-green-600"><ArrowUpRight size={18} /></span>
                Top 10 Absolute Gains (Today)
              </h2>
              {/* Added: updatedDate badge context */}
              {absoluteUpdatedDate && (
                <div className="flex items-center gap-1 text-xs text-gray-400 bg-gray-50 px-2.5 py-1 rounded-full border border-gray-100 w-fit">
                  <Clock size={12} />
                  <span>Updated: {formatUpdateLabel(absoluteUpdatedDate)}</span>
                </div>
              )}
            </div>
            
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse text-sm">
                <thead>
                  <tr className="border-b border-gray-100 text-gray-500 font-medium">
                    <th className="py-2">Rank</th>
                    <th className="py-2">Symbol</th>
                    <th className="py-2 text-right">High</th>
                    <th className="py-2 text-right">Low</th>
                    <th className="py-2 text-right text-green-600">Increase ($)</th>
                  </tr>
                </thead>
                <tbody>
                  {topAbsolute.map((stock) => (
                    <tr key={stock.rank} className="border-b border-gray-50 hover:bg-gray-50 transition">
                      <td className="py-3 font-semibold text-gray-400">#{stock.rank}</td>
                      <td className="py-3">
                        <Link to={`/stock/${stock.symbol}`} className="font-bold text-indigo-600 hover:underline">
                          {stock.symbol}
                        </Link>
                      </td>
                      <td className="py-3 text-right font-mono">${stock.priceHigh?.toFixed(2)}</td>
                      <td className="py-3 text-right font-mono">${stock.priceLow?.toFixed(2)}</td>
                      <td className="py-3 text-right font-mono font-bold text-green-600">+${stock.priceIncreaseAmt?.toFixed(2)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>

        {/* Top 10 Percentage Increase */}
        <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100 flex flex-col justify-between">
          <div>
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 mb-4">
              <h2 className="text-lg font-bold text-gray-800 flex items-center gap-2">
                <span className="p-1.5 bg-emerald-50 rounded-lg text-emerald-600"><ArrowUpRight size={18} /></span>
                Top 10 Percentage Growth
              </h2>
              {/* Added: updatedDate badge context */}
              {percentageUpdatedDate && (
                <div className="flex items-center gap-1 text-xs text-gray-400 bg-gray-50 px-2.5 py-1 rounded-full border border-gray-100 w-fit">
                  <Clock size={12} />
                  <span>Updated: {formatUpdateLabel(percentageUpdatedDate)}</span>
                </div>
              )}
            </div>

            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse text-sm">
                <thead>
                  <tr className="border-b border-gray-100 text-gray-500 font-medium">
                    <th className="py-2">Rank</th>
                    <th className="py-2">Symbol</th>
                    <th className="py-2 text-right">High</th>
                    <th className="py-2 text-right">Low</th>
                    <th className="py-2 text-right text-emerald-600">Growth (%)</th>
                  </tr>
                </thead>
                <tbody>
                  {topPercentage.map((stock) => (
                    <tr key={stock.rank} className="border-b border-gray-50 hover:bg-gray-50 transition">
                      <td className="py-3 font-semibold text-gray-400">#{stock.rank}</td>
                      <td className="py-3">
                        <Link to={`/stock/${stock.symbol}`} className="font-bold text-indigo-600 hover:underline">
                          {stock.symbol}
                        </Link>
                      </td>
                      <td className="py-3 text-right font-mono">${stock.priceHigh?.toFixed(2)}</td>
                      <td className="py-3 text-right font-mono">${stock.priceLow?.toFixed(2)}</td>
                      <td className="py-3 text-right font-mono font-bold text-emerald-600">+{stock.priceIncreasePct?.toFixed(2)}%</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>

      {/* SECTION 3: Global Catalog Matrix with Search & Pagination */}
      <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
          <h2 className="text-lg font-bold text-gray-800">Stock Market Directory</h2>
          
          <div className="relative w-full sm:w-64">
            <span className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none text-gray-400">
              <Search size={16} />
            </span>
            <input
              type="text"
              placeholder="Search by symbol... (e.g. AAPL)"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-9 pr-4 py-2 border border-gray-200 rounded-lg text-sm bg-gray-50 focus:bg-white focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition"
            />
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse text-sm">
            <thead>
              <tr className="border-b border-gray-100 text-gray-500 font-medium bg-gray-50">
                <th className="p-3">Symbol</th>
                <th className="p-3">Company Name</th>
                <th className="p-3 text-right cursor-pointer select-none hover:text-indigo-600" onClick={toggleSortPrice}>
                  <div className="flex items-center justify-end gap-1">
                    Current Close Price <ArrowUpDown size={14} />
                  </div>
                </th>
                <th className="p-3 text-right">Volume</th>
              </tr>
            </thead>
            <tbody>
              {paginatedStocks.length > 0 ? (
                paginatedStocks.map((stock) => (
                  <tr key={stock.symbol} className="border-b border-gray-50 hover:bg-slate-50/50 transition">
                    <td className="p-3">
                      <Link to={`/stock/${stock.symbol}`} className="font-bold text-indigo-600 hover:underline">
                        {stock.symbol}
                      </Link>
                    </td>
                    <td className="p-3 text-gray-600">{stock.companyName || stock.name || 'N/A'}</td>
                    <td className="p-3 text-right font-mono font-semibold">${stock.currentPrice?.toFixed(2)}</td>
                    <td className="p-3 text-right font-mono text-gray-500">{stock.volume?.toLocaleString()}</td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="4" className="p-8 text-center text-gray-400 italic bg-gray-50/50 rounded-b-xl">
                    No stocks match symbol search criteria "{searchQuery}"
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        {/* Dynamic Pagination Footer Control Panel */}
        {filteredStocks.length > 0 && (
          <div className="flex items-center justify-between border-t border-gray-100 pt-4 mt-4 text-sm text-gray-600">
            <div>
              Showing <span className="font-medium">{Math.min((currentPage - 1) * itemsPerPage + 1, filteredStocks.length)}</span> to{' '}
              <span className="font-medium">{Math.min(currentPage * itemsPerPage, filteredStocks.length)}</span> of{' '}
              <span className="font-medium">{filteredStocks.length}</span> entries
            </div>
            
            <div className="flex items-center gap-2">
              <button
                onClick={() => setCurrentPage(prev => Math.max(prev - 1, 1))}
                disabled={currentPage === 1}
                className="p-2 border border-gray-200 rounded-lg bg-white hover:bg-gray-50 disabled:opacity-40 disabled:hover:bg-white transition"
              >
                <ChevronLeft size={16} />
              </button>
              
              <span className="text-gray-500 px-2">
                Page <span className="font-semibold text-gray-800">{currentPage}</span> of {totalPages}
              </span>

              <button
                onClick={() => setCurrentPage(prev => Math.min(prev + 1, totalPages))}
                disabled={currentPage === totalPages}
                className="p-2 border border-gray-200 rounded-lg bg-white hover:bg-gray-50 disabled:opacity-40 disabled:hover:bg-white transition"
              >
                <ChevronRight size={16} />
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}