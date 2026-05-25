import React, { useState, useEffect } from 'react';
import { TrendingUp, DollarSign, Percent, RefreshCw, Calendar, BarChart3, ChevronRight, ArrowUpRight } from 'lucide-react';

export default function App() {
  const [view, setView] = useState('percentage'); // 'percentage' or 'absolute'
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const fetchData = async (currentView) => {
    setLoading(true);
    setError(null);
    const endpoint = currentView === 'percentage' ? '/api/stocks/percentage' : '/api/stocks/absolute';
    
    try {
      const response = await fetch(endpoint);
      if (!response.ok) {
        throw new Error(`Server returned HTTP status ${response.status}`);
      }
      const json = await response.json();
      setData(json);
    } catch (err) {
      setError(err.message || "Failed to fetch stock recommendations.");
      setData(getMockData(currentView));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData(view);
  }, [view]);

  return (
    <div className="min-h-screen bg-slate-950 text-slate-50 selection:bg-emerald-500/30 selection:text-emerald-300">
      <header className="border-b border-slate-800 bg-slate-900/50 backdrop-blur sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-20 flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <div className="p-2.5 bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 rounded-xl">
              <TrendingUp className="w-6 h-6" />
            </div>
            <div>
              <h1 className="text-xl font-bold tracking-tight bg-gradient-to-r from-white via-slate-200 to-slate-400 bg-clip-text text-transparent">
                AlphaPulse <span className="text-xs font-semibold px-2 py-0.5 ml-1.5 bg-slate-800 border border-slate-700 text-slate-400 rounded-full">Demo</span>
              </h1>
              <p className="text-xs text-slate-400">AWS Ecosystem Stock Analytics Engine</p>
            </div>
          </div>
          <button 
            onClick={() => fetchData(view)} 
            disabled={loading}
            className="p-2 text-slate-400 hover:text-white bg-slate-900 border border-slate-800 rounded-lg transition-all hover:border-slate-700 active:scale-95 disabled:opacity-50"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin text-emerald-400' : ''}`} />
          </button>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-8">
          <button 
            onClick={() => setView('percentage')}
            className={`relative flex items-center p-6 rounded-2xl border transition-all text-left overflow-hidden group ${
              view === 'percentage' 
                ? 'bg-gradient-to-br from-slate-900 to-slate-950 border-emerald-500/40 shadow-xl shadow-emerald-950/20' 
                : 'bg-slate-900/40 border-slate-800 hover:border-slate-700/60 hover:bg-slate-900/60'
            }`}
          >
            <div className={`p-4 rounded-xl mr-4 transition-colors ${view === 'percentage' ? 'bg-emerald-500/10 text-emerald-400' : 'bg-slate-800 text-slate-400 group-hover:text-slate-300'}`}>
              <Percent className="w-6 h-6" />
            </div>
            <div className="flex-1">
              <h3 className="text-lg font-semibold text-white flex items-center">
                Top 10 Percentage Gains
                <ChevronRight className={`w-4 h-4 ml-1 opacity-0 transition-all ${view === 'percentage' ? 'opacity-100 transform translate-x-1 text-emerald-400' : ''}`} />
              </h3>
              <p className="text-sm text-slate-400 mt-0.5">Ranked by maximum percentage step relative to price.</p>
            </div>
            {view === 'percentage' && <div className="absolute right-0 top-0 h-full w-1 bg-emerald-500" />}
          </button>

          <button 
            onClick={() => setView('absolute')}
            className={`relative flex items-center p-6 rounded-2xl border transition-all text-left overflow-hidden group ${
              view === 'absolute' 
                ? 'bg-gradient-to-br from-slate-900 to-slate-950 border-blue-500/40 shadow-xl shadow-blue-950/20' 
                : 'bg-slate-900/40 border-slate-800 hover:border-slate-700/60 hover:bg-slate-900/60'
            }`}
          >
            <div className={`p-4 rounded-xl mr-4 transition-colors ${view === 'absolute' ? 'bg-blue-500/10 text-blue-400' : 'bg-slate-800 text-slate-400 group-hover:text-slate-300'}`}>
              <DollarSign className="w-6 h-6" />
            </div>
            <div className="flex-1">
              <h3 className="text-lg font-semibold text-white flex items-center">
                Top 10 Absolute Increases
                <ChevronRight className={`w-4 h-4 ml-1 opacity-0 transition-all ${view === 'absolute' ? 'opacity-100 transform translate-x-1 text-blue-400' : ''}`} />
              </h3>
              <p className="text-sm text-slate-400 mt-0.5">Ranked by raw dollar values regardless of entry tier.</p>
            </div>
            {view === 'absolute' && <div className="absolute right-0 top-0 h-full w-1 bg-blue-500" />}
          </button>
        </div>

        {error && (
          <div className="mb-6 p-4 bg-amber-500/10 border border-amber-500/20 text-amber-400 rounded-xl text-sm flex items-center justify-between">
            <span className="flex items-center gap-2">
              <span className="w-2 h-2 rounded-full bg-amber-400 animate-pulse"></span>
              <strong>Backend connection offline:</strong> Showing local mock database state for dashboard display.
            </span>
          </div>
        )}

        <div className="bg-slate-900 border border-slate-800/80 rounded-2xl shadow-2xl overflow-hidden">
          <div className="px-6 py-5 border-b border-slate-800/60 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
            <div>
              <h2 className="text-lg font-bold text-white flex items-center gap-2">
                <BarChart3 className="w-4 h-4 text-slate-400" />
                {view === 'percentage' ? 'Relative Yield Outliers' : 'Market Leader Point Shifts'}
              </h2>
              <p className="text-xs text-slate-400 mt-0.5">Dynamic batch generated by scheduled engine pipelines</p>
            </div>
            {data.length > 0 && (
              <div className="flex items-center text-xs text-slate-400 bg-slate-950 px-3 py-1.5 rounded-lg border border-slate-800 self-start font-mono">
                <Calendar className="w-3.5 h-3.5 mr-1.5 text-slate-500" />
                Updated: {data[0].updated_date}
              </div>
            )}
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-slate-950/40 text-slate-400 text-xs font-semibold uppercase tracking-wider border-b border-slate-800/50">
                  <th className="py-4 px-6 text-center w-16">Rank</th>
                  <th className="py-4 px-6">Symbol</th>
                  <th className="py-4 px-6 text-right">Delta Run</th>
                  <th className="py-4 px-6 text-right">High Range</th>
                  <th className="py-4 px-6 text-right">Low Range</th>
                  <th className="py-4 px-6 text-right">Volume Block</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/40">
                {loading ? (
                  Array.from({ length: 5 }).map((_, i) => (
                    <tr key={i} className="animate-pulse">
                      <td className="py-5 px-6"><div className="h-5 bg-slate-800 rounded mx-auto w-6" /></td>
                      <td className="py-5 px-6"><div className="h-5 bg-slate-800 rounded w-16" /></td>
                      <td className="py-5 px-6"><div className="h-5 bg-slate-800 rounded ml-auto w-20" /></td>
                      <td className="py-5 px-6"><div className="h-5 bg-slate-800 rounded ml-auto w-16" /></td>
                      <td className="py-5 px-6"><div className="h-5 bg-slate-800 rounded ml-auto w-16" /></td>
                      <td className="py-5 px-6"><div className="h-5 bg-slate-800 rounded ml-auto w-24" /></td>
                    </tr>
                  ))
                ) : data.length === 0 ? (
                  <tr>
                    <td colSpan="6" className="text-center py-12 text-slate-500 text-sm font-medium">
                      No current matrix telemetry found for specified date bounds.
                    </td>
                  </tr>
                ) : (
                  data.map((item) => (
                    <tr key={item.rank} className="hover:bg-slate-800/30 transition-colors group">
                      <td className="py-4 px-6 text-center font-mono text-sm font-semibold text-slate-400 group-hover:text-slate-200">
                        #{item.rank}
                      </td>
                      <td className="py-4 px-6">
                        <div className="flex items-center space-x-2">
                          <span className="font-mono text-base font-bold text-white tracking-wide px-2 py-0.5 bg-slate-950 rounded-md border border-slate-800 group-hover:border-slate-700 transition-colors">
                            {item.symbol}
                          </span>
                        </div>
                      </td>
                      <td className="py-4 px-6 text-right font-mono">
                        {view === 'percentage' ? (
                          <span className="inline-flex items-center px-2.5 py-1 rounded-lg text-xs font-bold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                            <ArrowUpRight className="w-3.5 h-3.5 mr-1" />
                            +{Number(item.price_increase_pct).toFixed(2)}%
                          </span>
                        ) : (
                          <span className="inline-flex items-center px-2.5 py-1 rounded-lg text-xs font-bold bg-blue-500/10 text-blue-400 border border-blue-500/20">
                            <ArrowUpRight className="w-3.5 h-3.5 mr-1" />
                            +${Number(item.price_increase_amt).toFixed(2)}
                          </span>
                        )}
                      </td>
                      <td className="py-4 px-6 text-right font-mono text-sm font-medium text-slate-300">
                        ${Number(item.price_high).toFixed(2)}
                      </td>
                      <td className="py-4 px-6 text-right font-mono text-sm font-medium text-slate-400">
                        ${Number(item.price_low).toFixed(2)}
                      </td>
                      <td className="py-4 px-6 text-right font-mono text-sm text-slate-400 group-hover:text-slate-300">
                        {Number(item.volume).toLocaleString()}
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      </main>
    </div>
  );
}

function getMockData(type) {
  const today = new Date().toISOString().split('T')[0];
  if (type === 'percentage') {
    return [
      { rank: 1, updated_date: today, symbol: 'NVDA', price_high: 945.50, price_low: 912.00, volume: 42500100, price_increase_pct: 14.55 },
      { rank: 2, updated_date: today, symbol: 'AMD', price_high: 182.40, price_low: 165.10, volume: 29400200, price_increase_pct: 11.20 },
      { rank: 3, updated_date: today, symbol: 'PLTR', price_high: 28.15, price_low: 24.80, volume: 68100900, price_increase_pct: 10.85 },
      { rank: 4, updated_date: today, symbol: 'HOLO', price_high: 2.85, price_low: 2.20, volume: 105400000, price_increase_pct: 9.40 },
      { rank: 5, updated_date: today, symbol: 'BABA', price_high: 88.90, price_low: 81.20, volume: 18450000, price_increase_pct: 8.75 }
    ];
  } else {
    return [
      { rank: 1, updated_date: today, symbol: 'AVGO', price_high: 1420.00, price_low: 1350.00, volume: 3100400, price_increase_amt: 65.50 },
      { rank: 2, updated_date: today, symbol: 'REGN', price_high: 980.50, price_low: 932.10, volume: 850200, price_increase_amt: 45.15 },
      { rank: 3, updated_date: today, symbol: 'CMG', price_high: 2910.00, price_low: 2868.00, volume: 1200500, price_increase_amt: 42.00 },
      { rank: 4, updated_date: today, symbol: 'MSFT', price_high: 432.60, price_low: 415.00, volume: 22400100, price_increase_amt: 17.60 },
      { rank: 5, updated_date: today, symbol: 'META', price_high: 485.20, price_low: 470.15, volume: 15800400, price_increase_amt: 15.05 }
    ];
  }
}