/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState } from 'react';
import { 
  BarChart3, 
  TrendingUp, 
  Calendar, 
  ArrowUpRight, 
  ArrowDownRight, 
  Package, 
  DollarSign, 
  ShoppingCart, 
  Download,
  AlertTriangle
} from 'lucide-react';
import { SalesReportEntry, StockItem } from '../types';

interface ReportsAnalyticsProps {
  salesReport: SalesReportEntry[];
  stock: StockItem[];
}

type ReportMetric = 'sales' | 'purchases' | 'profit' | 'stock';
type ReportPeriod = 'daily' | 'weekly' | 'monthly';

export default function ReportsAnalytics({
  salesReport,
  stock
}: ReportsAnalyticsProps) {
  
  const [metric, setMetric] = useState<ReportMetric>('sales');
  const [period, setPeriod] = useState<ReportPeriod>('weekly');

  // Compute Aggregates
  const totalSales = salesReport.reduce((sum, item) => sum + item.sales, 0);
  const totalPurchases = salesReport.reduce((sum, item) => sum + item.purchases, 0);
  const totalProfit = salesReport.reduce((sum, item) => sum + item.profit, 0);
  const averageSaleValue = totalSales / salesReport.length;

  // Find Peak sales day
  const peakEntry = [...salesReport].sort((a, b) => b.sales - a.sales)[0];

  // Prepare custom SVG Chart parameters
  const chartHeight = 180;
  const chartWidth = 560;
  const padding = 30;

  // Max value calculation for scaling graph
  let maxVal = 1000;
  if (metric === 'sales') {
    maxVal = Math.max(...salesReport.map(e => e.sales)) * 1.15;
  } else if (metric === 'purchases') {
    maxVal = Math.max(...salesReport.map(e => e.purchases)) * 1.15;
  } else if (metric === 'profit') {
    maxVal = Math.max(...salesReport.map(e => e.profit)) * 1.15;
  } else if (metric === 'stock') {
    maxVal = Math.max(...stock.map(s => s.stockKg)) * 1.15;
  }

  // Draw chart path
  const getCoordinates = () => {
    if (metric === 'stock') return [];
    
    return salesReport.map((entry, index) => {
      const x = padding + (index / (salesReport.length - 1)) * (chartWidth - padding * 2);
      let value = entry.sales;
      if (metric === 'purchases') value = entry.purchases;
      if (metric === 'profit') value = entry.profit;
      
      const y = chartHeight - padding - (value / maxVal) * (chartHeight - padding * 2);
      return { x, y, value, date: entry.date };
    });
  };

  const coords = getCoordinates();
  const pathString = coords.length > 0 
    ? `M ${coords[0].x} ${coords[0].y} ` + coords.slice(1).map(c => `L ${c.x} ${c.y}`).join(' ')
    : '';

  // Gradient area path string
  const areaPathString = coords.length > 0
    ? `${pathString} L ${coords[coords.length - 1].x} ${chartHeight - padding} L ${coords[0].x} ${chartHeight - padding} Z`
    : '';

  const getMetricColor = (m: ReportMetric) => {
    switch (m) {
      case 'sales': return { stroke: '#06b6d4', fill: 'rgba(6, 182, 212, 0.08)', text: 'text-cyan-400' };
      case 'purchases': return { stroke: '#a855f7', fill: 'rgba(168, 85, 247, 0.08)', text: 'text-purple-400' };
      case 'profit': return { stroke: '#10b981', fill: 'rgba(16, 185, 129, 0.08)', text: 'text-emerald-400' };
      case 'stock': return { stroke: '#3b82f6', fill: 'rgba(59, 130, 246, 0.08)', text: 'text-blue-400' };
    }
  };

  const style = getMetricColor(metric);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-display font-semibold text-white tracking-tight flex items-center gap-2">
            <BarChart3 className="text-cyan-400" size={20} />
            Ecosystem Reports & Analytics
          </h2>
          <p className="text-xs text-slate-400">Track purchase funnels, inventory weights, profit margins, and sales velocity</p>
        </div>
        
        {/* Period Selector */}
        <div className="flex bg-slate-800 p-1 rounded-xl border border-slate-700/50 self-start md:self-auto font-mono">
          {(['daily', 'weekly', 'monthly'] as ReportPeriod[]).map(p => (
            <button
              key={p}
              onClick={() => setPeriod(p)}
              className={`px-3 py-1 rounded-lg text-[11px] font-medium uppercase transition-all ${
                period === p 
                  ? 'bg-slate-900 text-cyan-400 font-semibold shadow' 
                  : 'text-slate-400 hover:text-slate-200'
              }`}
            >
              {p}
            </button>
          ))}
        </div>
      </div>

      {/* Grid Highlights */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Gross Sales */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 shadow-lg">
          <div className="flex justify-between items-start mb-3">
            <span className="text-xs font-mono uppercase text-slate-500">Gross Sales</span>
            <span className="text-[10px] px-1.5 py-0.5 bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 rounded font-mono flex items-center gap-0.5">
              <ArrowUpRight size={11} /> +12.4%
            </span>
          </div>
          <h3 className="text-xl font-mono font-bold text-white">${totalSales.toFixed(2)}</h3>
          <p className="text-xs text-slate-400 mt-1 flex items-center gap-1">
            <ShoppingCart size={11} className="text-slate-500" /> Across registered shops
          </p>
        </div>

        {/* Fleet Procurement Cost */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 shadow-lg">
          <div className="flex justify-between items-start mb-3">
            <span className="text-xs font-mono uppercase text-slate-500">Fleet Purchase Cost</span>
            <span className="text-[10px] px-1.5 py-0.5 bg-red-500/10 text-red-400 border border-red-500/20 rounded font-mono flex items-center gap-0.5">
              <ArrowDownRight size={11} /> +4.2%
            </span>
          </div>
          <h3 className="text-xl font-mono font-bold text-white">${totalPurchases.toFixed(2)}</h3>
          <p className="text-xs text-slate-400 mt-1 flex items-center gap-1">
            <DollarSign size={11} className="text-slate-500" /> Owner procurement spend
          </p>
        </div>

        {/* Operating Profits */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 shadow-lg">
          <div className="flex justify-between items-start mb-3">
            <span className="text-xs font-mono uppercase text-slate-500">System Profit</span>
            <span className="text-[10px] px-1.5 py-0.5 bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 rounded font-mono flex items-center gap-0.5">
              <ArrowUpRight size={11} /> +18.9%
            </span>
          </div>
          <h3 className="text-xl font-mono font-bold text-white">${totalProfit.toFixed(2)}</h3>
          <p className="text-xs text-slate-400 mt-1 flex items-center gap-1">
            <TrendingUp size={11} className="text-slate-500" /> Platform split & fees
          </p>
        </div>

        {/* Average Order Value */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 shadow-lg">
          <div className="flex justify-between items-start mb-3">
            <span className="text-xs font-mono uppercase text-slate-500">Avg. Order Value</span>
            <span className="text-[10px] px-1.5 py-0.5 bg-cyan-500/10 text-cyan-400 border border-cyan-500/20 rounded font-mono">
              STABLE
            </span>
          </div>
          <h3 className="text-xl font-mono font-bold text-white">${averageSaleValue.toFixed(2)}</h3>
          <p className="text-xs text-slate-400 mt-1 flex items-center gap-1">
            <Calendar size={11} className="text-slate-500" /> Calculated weekly flow
          </p>
        </div>
      </div>

      {/* Main Graph Card */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl space-y-6">
        
        {/* Report Metric Selector & Export */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-slate-800">
          <div className="flex flex-wrap gap-2">
            {(['sales', 'purchases', 'profit', 'stock'] as ReportMetric[]).map(m => (
              <button
                key={m}
                onClick={() => setMetric(m)}
                className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-all uppercase ${
                  metric === m
                    ? m === 'sales' ? 'bg-cyan-500/10 text-cyan-400 border border-cyan-500/20'
                      : m === 'purchases' ? 'bg-purple-500/10 text-purple-400 border border-purple-500/20'
                      : m === 'profit' ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                      : 'bg-blue-500/10 text-blue-400 border border-blue-500/20'
                    : 'bg-slate-800 text-slate-400 border border-slate-700/40 hover:text-slate-200'
                }`}
              >
                {m} Report
              </button>
            ))}
          </div>

          <button
            onClick={() => alert(`Downloading CSV Ledger for ${metric.toUpperCase()} (${period.toUpperCase()})`)}
            className="px-3 py-1.5 bg-slate-800 hover:bg-slate-750 text-slate-300 hover:text-white border border-slate-700 text-xs font-semibold rounded-xl flex items-center justify-center gap-1.5 transition-colors self-start sm:self-auto"
          >
            <Download size={13} />
            Export Ledger
          </button>
        </div>

        {/* Render Graph */}
        <div className="flex flex-col md:flex-row gap-6 items-center">
          
          {/* Chart View */}
          <div className="w-full md:flex-1">
            {metric !== 'stock' ? (
              /* Financial Line Chart */
              <div className="relative overflow-hidden border border-slate-800/60 rounded-xl bg-slate-950/20 p-2">
                <svg viewBox={`0 0 ${chartWidth} ${chartHeight}`} className="w-full h-auto overflow-visible">
                  {/* Grid Lines */}
                  {[0, 0.25, 0.5, 0.75, 1].map((ratio, i) => {
                    const y = padding + ratio * (chartHeight - padding * 2);
                    const gridVal = (maxVal * (1 - ratio)).toFixed(0);
                    return (
                      <g key={i} className="opacity-30">
                        <line 
                          x1={padding} 
                          y1={y} 
                          x2={chartWidth - padding} 
                          y2={y} 
                          stroke="#334155" 
                          strokeWidth="1" 
                          strokeDasharray="4 4"
                        />
                        <text 
                          x={padding - 5} 
                          y={y + 3} 
                          fill="#64748b" 
                          fontSize="9" 
                          fontFamily="monospace" 
                          textAnchor="end"
                        >
                          ${gridVal}
                        </text>
                      </g>
                    );
                  })}

                  {/* Horizontal Axis Dates */}
                  {coords.map((c, i) => (
                    <text
                      key={i}
                      x={c.x}
                      y={chartHeight - 10}
                      fill="#64748b"
                      fontSize="9"
                      fontFamily="monospace"
                      textAnchor="middle"
                      className="opacity-60"
                    >
                      {c.date.split('-').slice(1).join('/')}
                    </text>
                  ))}

                  {/* Gradient fill */}
                  <defs>
                    <linearGradient id="chartGradient" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="0%" stopColor={style.stroke} stopOpacity={0.15} />
                      <stop offset="100%" stopColor={style.stroke} stopOpacity={0.0} />
                    </linearGradient>
                  </defs>

                  {/* Graph Paths */}
                  <path d={areaPathString} fill="url(#chartGradient)" />
                  <path 
                    d={pathString} 
                    fill="none" 
                    stroke={style.stroke} 
                    strokeWidth="2.5" 
                    strokeLinecap="round" 
                    strokeLinejoin="round" 
                  />

                  {/* Dots with Hover overlays */}
                  {coords.map((c, i) => (
                    <g key={i} className="group/dot cursor-pointer">
                      <circle 
                        cx={c.x} 
                        cy={c.y} 
                        r="4" 
                        fill="#0f172a" 
                        stroke={style.stroke} 
                        strokeWidth="2" 
                      />
                      <circle 
                        cx={c.x} 
                        cy={c.y} 
                        r="8" 
                        fill={style.stroke} 
                        className="opacity-0 hover:opacity-20 transition-opacity" 
                      />
                      {/* Tooltip Overlay */}
                      <rect
                        x={c.x - 25}
                        y={c.y - 28}
                        width="50"
                        height="18"
                        rx="4"
                        fill="#1e293b"
                        stroke="#334155"
                        strokeWidth="1"
                        className="opacity-0 group-hover/dot:opacity-100 transition-opacity pointer-events-none"
                      />
                      <text
                        x={c.x}
                        y={c.y - 16}
                        fill="#fff"
                        fontSize="9"
                        fontFamily="monospace"
                        fontWeight="bold"
                        textAnchor="middle"
                        className="opacity-0 group-hover/dot:opacity-100 transition-opacity pointer-events-none"
                      >
                        ${c.value.toFixed(0)}
                      </text>
                    </g>
                  ))}
                </svg>
              </div>
            ) : (
              /* Stock levels Horizontal Bar Chart */
              <div className="space-y-3.5 border border-slate-800/60 rounded-xl bg-slate-950/20 p-5">
                {stock.map(item => {
                  const percent = Math.min((item.stockKg / maxVal) * 100, 100);
                  const isLow = item.status === 'low_stock';
                  const isOut = item.status === 'out_of_stock';
                  
                  return (
                    <div key={item.id} className="space-y-1">
                      <div className="flex justify-between items-center text-xs font-mono">
                        <span className="text-slate-300 font-semibold">{item.name}</span>
                        <span className={`font-bold ${
                          isOut ? 'text-red-400' : isLow ? 'text-amber-400' : 'text-emerald-400'
                        }`}>
                          {item.stockKg} kg ({item.status.replace('_', ' ')})
                        </span>
                      </div>
                      <div className="w-full bg-slate-800 h-2.5 rounded-full overflow-hidden border border-slate-700/30">
                        <div 
                          className={`h-full rounded-full transition-all duration-500 ${
                            isOut ? 'bg-red-500' : isLow ? 'bg-amber-500' : 'bg-emerald-500'
                          }`}
                          style={{ width: `${percent}%` }}
                        ></div>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>

          {/* Side summaries */}
          <div className="w-full md:w-56 space-y-4">
            <div className="bg-slate-800/40 p-4 rounded-xl border border-slate-800 space-y-1">
              <span className="text-[10px] font-mono uppercase text-slate-500">Period Summary</span>
              <p className="text-xs text-slate-300">
                Data reflects active flow filters. Top selling item of this period is <span className="font-semibold text-white">Atlantic Salmon</span>.
              </p>
            </div>

            <div className="bg-slate-800/40 p-4 rounded-xl border border-slate-800 space-y-1.5">
              <span className="text-[10px] font-mono uppercase text-slate-500">Peak Performance</span>
              <div className="flex justify-between items-center font-mono">
                <span className="text-xs text-slate-400">Peak Gross:</span>
                <span className="text-xs font-bold text-emerald-400">${peakEntry.sales.toFixed(2)}</span>
              </div>
              <div className="flex justify-between items-center font-mono">
                <span className="text-xs text-slate-400">On Date:</span>
                <span className="text-xs text-slate-300">{peakEntry.date}</span>
              </div>
            </div>

            {stock.some(s => s.status === 'out_of_stock' || s.status === 'low_stock') && (
              <div className="bg-amber-500/5 p-4 rounded-xl border border-amber-500/10 flex gap-2 text-xs text-amber-400">
                <AlertTriangle size={16} className="shrink-0" />
                <p className="leading-snug">
                  <span className="font-semibold">Reorder Alert:</span> Critical stock deficits detected for certain species. Shop owners have been alerted.
                </p>
              </div>
            )}
          </div>

        </div>

      </div>
    </div>
  );
}
