/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React from 'react';
import { 
  Users, 
  Store, 
  Truck, 
  ShoppingBag, 
  DollarSign, 
  Percent, 
  TrendingUp,
  Clock,
  CheckCircle2,
  AlertCircle,
  ArrowUpRight
} from 'lucide-react';
import { Customer, Owner, DeliveryStaff, Order, SystemSettings } from '../types';

interface AdminDashboardProps {
  customers: Customer[];
  owners: Owner[];
  deliveryStaff: DeliveryStaff[];
  orders: Order[];
  settings: SystemSettings;
  setActiveTab: (tab: string) => void;
}

export default function AdminDashboard({
  customers,
  owners,
  deliveryStaff,
  orders,
  settings,
  setActiveTab
}: AdminDashboardProps) {
  
  // Calculate Metrics
  const totalCustomers = customers.length;
  const totalOwners = owners.length;
  const totalDeliveryStaff = deliveryStaff.length;
  const totalOrdersCount = orders.length;
  
  // Sales are calculated from delivered + delivering + accepted orders (completed or active flow)
  // Let's compute total sales of delivered orders specifically
  const totalSales = orders
    .filter(o => o.status === 'delivered')
    .reduce((sum, o) => sum + o.totalAmount, 0);

  // Admin Revenue is based on commission rate % on delivered orders
  const totalRevenue = totalSales * (settings.commissionRate / 100);

  // Active users count: status 'active' for customers, owners, and delivery staff
  const activeCustomersCount = customers.filter(c => c.status === 'active').length;
  const activeOwnersCount = owners.filter(o => o.status === 'active').length;
  const activeDeliveryStaffCount = deliveryStaff.filter(d => d.status === 'active').length;
  const totalActiveUsers = activeCustomersCount + activeOwnersCount + activeDeliveryStaffCount;

  const metrics = [
    {
      label: 'Total Customers',
      value: totalCustomers,
      subtext: `${activeCustomersCount} Active accounts`,
      icon: Users,
      color: 'from-blue-500/10 to-indigo-500/10',
      iconColor: 'text-blue-500',
      borderColor: 'border-blue-500/10',
      tab: 'users'
    },
    {
      label: 'Fish Shop Owners',
      value: totalOwners,
      subtext: `${owners.filter(o => o.status === 'pending_approval').length} Pending approvals`,
      icon: Store,
      color: 'from-amber-500/10 to-orange-500/10',
      iconColor: 'text-amber-500',
      borderColor: 'border-amber-500/10',
      tab: 'owners'
    },
    {
      label: 'Delivery Staff',
      value: totalDeliveryStaff,
      subtext: `${activeDeliveryStaffCount} Drivers active`,
      icon: Truck,
      color: 'from-emerald-500/10 to-teal-500/10',
      iconColor: 'text-emerald-500',
      borderColor: 'border-emerald-500/10',
      tab: 'delivery'
    },
    {
      label: 'Total Orders',
      value: totalOrdersCount,
      subtext: `${orders.filter(o => o.status === 'pending').length} Unassigned orders`,
      icon: ShoppingBag,
      color: 'from-cyan-500/10 to-sky-500/10',
      iconColor: 'text-cyan-500',
      borderColor: 'border-cyan-500/10',
      tab: 'simulator'
    },
    {
      label: 'Total Gross Sales',
      value: `$${totalSales.toFixed(2)}`,
      subtext: 'Delivered volume',
      icon: DollarSign,
      color: 'from-violet-500/10 to-purple-500/10',
      iconColor: 'text-violet-500',
      borderColor: 'border-violet-500/10',
      tab: 'reports'
    },
    {
      label: 'Admin Revenue',
      value: `$${totalRevenue.toFixed(2)}`,
      subtext: `${settings.commissionRate}% Commission rate`,
      icon: Percent,
      color: 'from-pink-500/10 to-rose-500/10',
      iconColor: 'text-pink-500',
      borderColor: 'border-pink-500/10',
      tab: 'settings'
    },
    {
      label: 'Active System Users',
      value: totalActiveUsers,
      subtext: 'Across all 3 applications',
      icon: TrendingUp,
      color: 'from-fuchsia-500/10 to-purple-500/10',
      iconColor: 'text-fuchsia-500',
      borderColor: 'border-fuchsia-500/10',
      tab: 'users'
    }
  ];

  // Helper to get order status class
  const getStatusBadge = (status: Order['status']) => {
    switch (status) {
      case 'pending':
        return 'bg-amber-500/10 text-amber-500 border-amber-500/20';
      case 'accepted':
        return 'bg-blue-500/10 text-blue-500 border-blue-500/20';
      case 'delivering':
        return 'bg-purple-500/10 text-purple-500 border-purple-500/20';
      case 'delivered':
        return 'bg-emerald-500/10 text-emerald-500 border-emerald-500/20';
    }
  };

  return (
    <div className="space-y-6">
      {/* Banner */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 p-6 bg-slate-900 rounded-2xl border border-slate-800 shadow-xl shadow-slate-950/20 relative overflow-hidden">
        <div className="absolute right-0 top-0 bottom-0 w-1/3 bg-gradient-to-l from-cyan-500/5 to-transparent pointer-events-none"></div>
        <div className="space-y-1">
          <h2 className="text-2xl font-display font-semibold text-white tracking-tight">
            Dashboard System Overview
          </h2>
          <p className="text-sm text-slate-400">
            Real-time telemetry and management controls for your fish marketplace ecosystem.
          </p>
        </div>
        <div className="flex items-center gap-3">
          <button 
            onClick={() => setActiveTab('simulator')}
            className="px-4 py-2 bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 text-slate-950 text-sm font-semibold rounded-xl flex items-center gap-2 shadow-lg shadow-cyan-500/10 transition-all duration-200"
          >
            <Clock size={16} />
            Open Flow Simulator
          </button>
        </div>
      </div>

      {/* Grid Metrics */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
        {metrics.map((metric, idx) => {
          const Icon = metric.icon;
          return (
            <button
              key={idx}
              onClick={() => setActiveTab(metric.tab)}
              className={`p-5 rounded-2xl border ${metric.borderColor} bg-slate-900 hover:bg-slate-800/80 text-left transition-all duration-200 group relative overflow-hidden flex flex-col justify-between`}
            >
              <div className="flex items-center justify-between w-full mb-4">
                <span className="text-xs font-mono uppercase tracking-wider text-slate-500">
                  {metric.label}
                </span>
                <div className={`p-2 rounded-xl bg-slate-800 group-hover:scale-110 transition-transform duration-200 ${metric.iconColor}`}>
                  <Icon size={18} />
                </div>
              </div>
              <div>
                <h3 className="text-2xl font-display font-bold text-white tracking-tight">
                  {metric.value}
                </h3>
                <p className="text-xs text-slate-400 mt-1 flex items-center gap-1">
                  {metric.subtext}
                </p>
              </div>
              <div className="absolute bottom-2 right-2 opacity-0 group-hover:opacity-100 transition-opacity duration-200 text-slate-500">
                <ArrowUpRight size={14} />
              </div>
            </button>
          );
        })}
      </div>

      {/* Two Columns Section */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        
        {/* Recent Orders Table (takes 2 cols) */}
        <div className="lg:col-span-2 bg-slate-900 border border-slate-800 rounded-2xl p-6 flex flex-col justify-between shadow-xl">
          <div className="flex items-center justify-between mb-4 pb-4 border-b border-slate-800">
            <div>
              <h3 className="font-display font-semibold text-white">Active Orders Flow</h3>
              <p className="text-xs text-slate-400 mt-0.5">Live status of user purchases across shops</p>
            </div>
            <button 
              onClick={() => setActiveTab('simulator')}
              className="text-xs text-cyan-400 hover:text-cyan-300 flex items-center gap-1"
            >
              Simulate Orders <ArrowUpRight size={14} />
            </button>
          </div>

          <div className="overflow-x-auto">
            {orders.length === 0 ? (
              <div className="py-12 text-center">
                <ShoppingBag size={32} className="mx-auto text-slate-600 mb-2" />
                <p className="text-sm text-slate-500">No orders currently active. Use the Flow Simulator to place orders.</p>
              </div>
            ) : (
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="border-b border-slate-800 text-xs font-mono uppercase text-slate-500">
                    <th className="pb-3 font-medium">Order ID</th>
                    <th className="pb-3 font-medium">Customer / Shop</th>
                    <th className="pb-3 font-medium">Items</th>
                    <th className="pb-3 font-medium text-right">Total</th>
                    <th className="pb-3 font-medium text-center">Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800/60 text-sm">
                  {orders.slice(0, 5).map((order) => {
                    const itemSummary = order.fishItems
                      .map(item => `${item.quantityKg}kg ${item.name}`)
                      .join(', ');
                      
                    return (
                      <tr key={order.id} className="group hover:bg-slate-800/30">
                        <td className="py-3.5 font-mono text-xs font-semibold text-slate-300">
                          {order.id}
                        </td>
                        <td className="py-3.5">
                          <div className="font-medium text-slate-200">{order.customerName}</div>
                          <div className="text-xs text-slate-500 font-mono">{order.shopName}</div>
                        </td>
                        <td className="py-3.5 max-w-[180px] truncate text-slate-400 text-xs" title={itemSummary}>
                          {itemSummary}
                        </td>
                        <td className="py-3.5 text-right font-mono font-medium text-white">
                          ${order.totalAmount.toFixed(2)}
                        </td>
                        <td className="py-3.5 text-center">
                          <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-[11px] font-medium border ${getStatusBadge(order.status)}`}>
                            {order.status}
                          </span>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            )}
          </div>
        </div>

        {/* System Diagnostics & Operations Log (takes 1 col) */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 flex flex-col shadow-xl">
          <div className="mb-4 pb-4 border-b border-slate-800">
            <h3 className="font-display font-semibold text-white">System Telemetry Log</h3>
            <p className="text-xs text-slate-400 mt-0.5">Administrative logs and events</p>
          </div>

          <div className="flex-1 space-y-4 max-h-[300px] overflow-y-auto pr-1">
            <div className="flex gap-3 text-xs">
              <CheckCircle2 size={16} className="text-emerald-500 shrink-0 mt-0.5" />
              <div>
                <p className="text-slate-300 font-medium">Backup completed successfully</p>
                <span className="text-[10px] text-slate-500 font-mono">Today, 2:00 AM • Backup DB</span>
              </div>
            </div>

            <div className="flex gap-3 text-xs">
              <Clock size={16} className="text-cyan-400 shrink-0 mt-0.5" />
              <div>
                <p className="text-slate-300 font-medium">Cron task triggered: Daily Reports</p>
                <span className="text-[10px] text-slate-500 font-mono">Today, 12:00 AM • System Cron</span>
              </div>
            </div>

            <div className="flex gap-3 text-xs">
              <AlertCircle size={16} className="text-amber-500 shrink-0 mt-0.5" />
              <div>
                <p className="text-slate-300 font-medium">New owner registration pending</p>
                <p className="text-slate-500 text-[11px]">Deep Blue Imports submitted credentials</p>
                <span className="text-[10px] text-slate-500 font-mono">Yesterday • Registration API</span>
              </div>
            </div>

            <div className="flex gap-3 text-xs">
              <CheckCircle2 size={16} className="text-emerald-500 shrink-0 mt-0.5" />
              <div>
                <p className="text-slate-300 font-medium">Commission payout rate verified</p>
                <span className="text-[10px] text-slate-500 font-mono">3 days ago • Settings</span>
              </div>
            </div>

            <div className="flex gap-3 text-xs">
              <AlertCircle size={16} className="text-red-500 shrink-0 mt-0.5" />
              <div>
                <p className="text-slate-300 font-medium">User block list updated</p>
                <p className="text-slate-500 text-[11px]">David Kim account restricted for non-compliance</p>
                <span className="text-[10px] text-slate-500 font-mono">4 days ago • Security Engine</span>
              </div>
            </div>
          </div>

          <div className="mt-4 pt-4 border-t border-slate-800/80 flex items-center justify-between text-xs text-slate-400">
            <span>Overall System Health:</span>
            <span className="text-emerald-400 font-mono font-semibold flex items-center gap-1">
              <span className="w-2 h-2 bg-emerald-500 rounded-full animate-ping"></span>
              99.98% OK
            </span>
          </div>
        </div>

      </div>
    </div>
  );
}
