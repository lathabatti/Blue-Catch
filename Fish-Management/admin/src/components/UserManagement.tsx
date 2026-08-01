/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState } from 'react';
import { 
  Search, 
  UserX, 
  UserCheck, 
  Trash2, 
  ShieldAlert, 
  Calendar, 
  ShoppingBag, 
  DollarSign,
  Users
} from 'lucide-react';
import { Customer } from '../types';

interface UserManagementProps {
  customers: Customer[];
  onBlockCustomer: (id: string) => void;
  onUnblockCustomer: (id: string) => void;
  onDeleteCustomer: (id: string) => void;
}

export default function UserManagement({
  customers,
  onBlockCustomer,
  onUnblockCustomer,
  onDeleteCustomer
}: UserManagementProps) {
  
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<'all' | 'active' | 'blocked'>('all');
  const [confirmDeleteId, setConfirmDeleteId] = useState<string | null>(null);

  // Filter customers
  const filteredCustomers = customers.filter(cust => {
    const matchesSearch = 
      cust.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      cust.email.toLowerCase().includes(searchQuery.toLowerCase()) ||
      cust.phone.includes(searchQuery);

    const matchesStatus = 
      statusFilter === 'all' || 
      cust.status === statusFilter;

    return matchesSearch && matchesStatus;
  });

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-display font-semibold text-white tracking-tight flex items-center gap-2">
            <Users className="text-cyan-400" size={20} />
            Customer Management
          </h2>
          <p className="text-xs text-slate-400">View and moderate all registered seafood consumers on the marketplace</p>
        </div>
      </div>

      {/* Search & Filter Bar */}
      <div className="bg-slate-900 border border-slate-800 p-4 rounded-2xl flex flex-col md:flex-row gap-4 items-center justify-between shadow-lg">
        {/* Search */}
        <div className="relative w-full md:w-96">
          <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-500" size={16} />
          <input
            type="text"
            placeholder="Search customer name, email or phone..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full pl-10 pr-4 py-2 bg-slate-800/80 hover:bg-slate-800 border border-slate-700/60 focus:border-cyan-500 focus:outline-none rounded-xl text-xs text-slate-200 transition-colors placeholder:text-slate-500"
          />
        </div>

        {/* Status Filters */}
        <div className="flex gap-1.5 self-start md:self-auto bg-slate-800 p-1 rounded-xl border border-slate-700/40">
          <button
            onClick={() => setStatusFilter('all')}
            className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-all ${
              statusFilter === 'all' 
                ? 'bg-slate-900 text-white shadow' 
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            All Customers ({customers.length})
          </button>
          <button
            onClick={() => setStatusFilter('active')}
            className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-all ${
              statusFilter === 'active' 
                ? 'bg-slate-900 text-emerald-400 shadow' 
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            Active ({customers.filter(c => c.status === 'active').length})
          </button>
          <button
            onClick={() => setStatusFilter('blocked')}
            className={`px-3 py-1.5 rounded-lg text-xs font-medium transition-all ${
              statusFilter === 'blocked' 
                ? 'bg-slate-900 text-red-400 shadow' 
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            Blocked ({customers.filter(c => c.status === 'blocked').length})
          </button>
        </div>
      </div>

      {/* Main Table Card */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden shadow-xl">
        {filteredCustomers.length === 0 ? (
          <div className="p-16 text-center text-slate-500">
            <Users size={40} className="mx-auto text-slate-700 mb-3" />
            <p className="text-sm font-medium text-slate-400">No customers found</p>
            <p className="text-xs text-slate-500 mt-1">Try resetting the search terms or filters</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-slate-800 bg-slate-950/40 text-xs font-mono uppercase text-slate-500">
                  <th className="py-3.5 px-6 font-medium">Customer Details</th>
                  <th className="py-3.5 px-4 font-medium">Contact</th>
                  <th className="py-3.5 px-4 font-medium">Joined Date</th>
                  <th className="py-3.5 px-4 font-medium text-center">Orders</th>
                  <th className="py-3.5 px-4 font-medium text-right">Total Spent</th>
                  <th className="py-3.5 px-4 font-medium text-center">Status</th>
                  <th className="py-3.5 px-6 font-medium text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/60 text-sm">
                {filteredCustomers.map((cust) => (
                  <tr key={cust.id} className="hover:bg-slate-800/30 transition-colors group">
                    {/* Customer Info */}
                    <td className="py-4 px-6">
                      <div className="flex items-center gap-3">
                        <div className="w-9 h-9 rounded-xl bg-slate-800/80 border border-slate-700/50 flex items-center justify-center font-display font-semibold text-slate-300">
                          {cust.name.split(' ').map(n => n[0]).join('')}
                        </div>
                        <div>
                          <div className="font-medium text-slate-200 group-hover:text-white transition-colors">
                            {cust.name}
                          </div>
                          <div className="text-xs text-slate-500 font-mono mt-0.5">{cust.id}</div>
                        </div>
                      </div>
                    </td>

                    {/* Contact Details */}
                    <td className="py-4 px-4">
                      <div className="text-slate-300 font-mono text-xs">{cust.email}</div>
                      <div className="text-slate-500 font-mono text-xs mt-0.5">{cust.phone}</div>
                    </td>

                    {/* Joined Date */}
                    <td className="py-4 px-4 text-slate-400 font-mono text-xs">
                      <span className="flex items-center gap-1.5">
                        <Calendar size={13} className="text-slate-600" />
                        {cust.registrationDate}
                      </span>
                    </td>

                    {/* Total Orders */}
                    <td className="py-4 px-4 text-center font-mono font-medium text-slate-300">
                      <span className="inline-flex items-center gap-1">
                        <ShoppingBag size={13} className="text-slate-600" />
                        {cust.totalOrders}
                      </span>
                    </td>

                    {/* Total Spent */}
                    <td className="py-4 px-4 text-right font-mono font-medium text-white">
                      ${cust.totalSpent.toFixed(2)}
                    </td>

                    {/* Status badge */}
                    <td className="py-4 px-4 text-center">
                      <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-[11px] font-medium border ${
                        cust.status === 'active'
                          ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20'
                          : 'bg-red-500/10 text-red-400 border-red-500/20'
                      }`}>
                        {cust.status}
                      </span>
                    </td>

                    {/* Actions */}
                    <td className="py-4 px-6 text-right">
                      {confirmDeleteId === cust.id ? (
                        <div className="flex items-center justify-end gap-1.5">
                          <span className="text-[10px] text-red-400 font-medium font-mono">Confirm delete?</span>
                          <button
                            onClick={() => {
                              onDeleteCustomer(cust.id);
                              setConfirmDeleteId(null);
                            }}
                            className="bg-red-500 text-white px-2 py-1 rounded text-[11px] font-semibold hover:bg-red-400"
                          >
                            Yes
                          </button>
                          <button
                            onClick={() => setConfirmDeleteId(null)}
                            className="bg-slate-800 text-slate-400 px-2 py-1 rounded text-[11px] font-semibold hover:bg-slate-700 hover:text-slate-200"
                          >
                            No
                          </button>
                        </div>
                      ) : (
                        <div className="flex items-center justify-end gap-1.5 opacity-80 group-hover:opacity-100 transition-opacity">
                          {/* Block / Unblock toggle */}
                          {cust.status === 'active' ? (
                            <button
                              onClick={() => onBlockCustomer(cust.id)}
                              title="Block Customer"
                              className="p-1.5 bg-slate-800 hover:bg-red-500/15 text-slate-400 hover:text-red-400 border border-slate-700/60 rounded-lg transition-colors"
                            >
                              <UserX size={14} />
                            </button>
                          ) : (
                            <button
                              onClick={() => onUnblockCustomer(cust.id)}
                              title="Unblock Customer"
                              className="p-1.5 bg-slate-800 hover:bg-emerald-500/15 text-slate-400 hover:text-emerald-400 border border-slate-700/60 rounded-lg transition-colors"
                            >
                              <UserCheck size={14} />
                            </button>
                          )}

                          {/* Delete Account */}
                          <button
                            onClick={() => setConfirmDeleteId(cust.id)}
                            title="Delete Customer"
                            className="p-1.5 bg-slate-800 hover:bg-red-500/15 text-slate-400 hover:text-red-400 border border-slate-700/60 rounded-lg transition-colors"
                          >
                            <Trash2 size={14} />
                          </button>
                        </div>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Info Card banner */}
      <div className="p-4 bg-slate-900 border border-slate-800 rounded-2xl flex gap-3 text-xs text-slate-400 leading-relaxed shadow-lg">
        <ShieldAlert className="text-cyan-400 shrink-0" size={18} />
        <div>
          <span className="font-semibold text-slate-200">Security & GDPR Compliance Notice:</span> Blocking a customer immediately denies them entry to checkout APIs and app logins. Deleting customer records removes their credentials but retains normalized financial logs under transaction history tables for legal tax bookkeeping.
        </div>
      </div>
    </div>
  );
}
