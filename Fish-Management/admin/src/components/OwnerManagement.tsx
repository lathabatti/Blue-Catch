/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState } from 'react';
import { 
  Store, 
  UserCheck, 
  UserX, 
  Search, 
  Clock, 
  ShieldCheck, 
  TrendingUp, 
  FileText,
  DollarSign,
  Briefcase,
  AlertCircle
} from 'lucide-react';
import { Owner } from '../types';

interface OwnerManagementProps {
  owners: Owner[];
  onApproveOwner: (id: string) => void;
  onBlockOwner: (id: string) => void;
  onUnblockOwner: (id: string) => void;
}

export default function OwnerManagement({
  owners,
  onApproveOwner,
  onBlockOwner,
  onUnblockOwner
}: OwnerManagementProps) {
  
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedOwnerId, setSelectedOwnerId] = useState<string | null>(null);

  const pendingRequests = owners.filter(o => o.status === 'pending_approval');
  const activeBlockedOwners = owners.filter(o => o.status !== 'pending_approval');

  const filteredOwners = activeBlockedOwners.filter(owner => {
    return (
      owner.shopName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      owner.ownerName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      owner.businessRegNo.toLowerCase().includes(searchQuery.toLowerCase())
    );
  });

  const selectedOwner = owners.find(o => o.id === selectedOwnerId);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h2 className="text-xl font-display font-semibold text-white tracking-tight flex items-center gap-2">
          <Store className="text-cyan-400" size={20} />
          Seafood Shop Owners
        </h2>
        <p className="text-xs text-slate-400">Approve shop credentials, block uncompliant stores, and view business audits</p>
      </div>

      {/* Pending Registration Requests Grid */}
      {pendingRequests.length > 0 && (
        <div className="bg-slate-900 border border-amber-500/10 rounded-2xl p-6 shadow-xl relative overflow-hidden">
          <div className="absolute right-0 top-0 bottom-0 w-1/4 bg-gradient-to-l from-amber-500/5 to-transparent pointer-events-none"></div>
          <div className="flex items-center gap-2.5 mb-4">
            <div className="p-1.5 rounded-lg bg-amber-500/10 border border-amber-500/20 text-amber-400">
              <Clock size={16} className="animate-spin" style={{ animationDuration: '6s' }} />
            </div>
            <div>
              <h3 className="font-display font-semibold text-white">Pending Shop Registrations</h3>
              <p className="text-xs text-slate-400">These stores require administrative verification before listing</p>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {pendingRequests.map(owner => (
              <div key={owner.id} className="bg-slate-800/40 border border-slate-700/50 p-4 rounded-xl flex flex-col justify-between gap-4">
                <div className="space-y-1.5">
                  <div className="flex items-center justify-between">
                    <h4 className="font-display font-semibold text-white text-sm">{owner.shopName}</h4>
                    <span className="text-[10px] bg-amber-500/10 text-amber-400 border border-amber-500/20 px-2 py-0.5 rounded-full font-mono">
                      PENDING VERIFICATION
                    </span>
                  </div>
                  <div className="text-xs text-slate-300">
                    <span className="text-slate-500">Applicant:</span> {owner.ownerName}
                  </div>
                  <div className="text-xs text-slate-400 font-mono space-y-0.5">
                    <div>Email: {owner.email}</div>
                    <div>Phone: {owner.phone}</div>
                    <div className="text-cyan-400 flex items-center gap-1 mt-1 text-[11px]">
                      <Briefcase size={12} /> Reg No: {owner.businessRegNo}
                    </div>
                  </div>
                </div>

                <div className="flex gap-2">
                  <button
                    onClick={() => onApproveOwner(owner.id)}
                    className="flex-1 py-1.5 bg-gradient-to-r from-emerald-500 to-teal-600 hover:from-emerald-400 hover:to-teal-500 text-slate-950 text-xs font-semibold rounded-lg flex items-center justify-center gap-1.5 transition-all shadow-md shadow-emerald-500/5"
                  >
                    <UserCheck size={14} />
                    Approve Store
                  </button>
                  <button
                    onClick={() => onBlockOwner(owner.id)}
                    className="px-3 py-1.5 bg-slate-800 hover:bg-red-500/10 border border-slate-700 hover:border-red-500/20 text-slate-400 hover:text-red-400 text-xs font-semibold rounded-lg transition-colors"
                  >
                    Decline
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Main Grid Layout - Owners List vs. Reports Drawer */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        
        {/* Active Owners List */}
        <div className="lg:col-span-2 space-y-4">
          
          {/* Search Bar */}
          <div className="bg-slate-900 border border-slate-800 p-4 rounded-2xl flex items-center justify-between shadow-lg">
            <div className="relative w-full">
              <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-500" size={16} />
              <input
                type="text"
                placeholder="Search by store name, owner, or business reg number..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full pl-10 pr-4 py-2 bg-slate-800/80 hover:bg-slate-800 border border-slate-700/60 focus:border-cyan-500 focus:outline-none rounded-xl text-xs text-slate-200 transition-colors placeholder:text-slate-500"
              />
            </div>
          </div>

          {/* Table Card */}
          <div className="bg-slate-900 border border-slate-800 rounded-2xl overflow-hidden shadow-xl">
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="border-b border-slate-800 bg-slate-950/40 text-xs font-mono uppercase text-slate-500">
                    <th className="py-3 px-5 font-medium">Store & Owner</th>
                    <th className="py-3 px-4 font-medium">Registration</th>
                    <th className="py-3 px-4 font-medium text-center">Status</th>
                    <th className="py-3 px-4 font-medium text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-800/60 text-sm">
                  {filteredOwners.map(owner => {
                    const isSelected = selectedOwnerId === owner.id;
                    return (
                      <tr 
                        key={owner.id} 
                        className={`hover:bg-slate-800/20 transition-colors cursor-pointer ${
                          isSelected ? 'bg-cyan-500/5 hover:bg-cyan-500/5' : ''
                        }`}
                        onClick={() => setSelectedOwnerId(owner.id)}
                      >
                        {/* Store Info */}
                        <td className="py-3.5 px-5">
                          <div className="font-semibold text-slate-200">{owner.shopName}</div>
                          <div className="text-xs text-slate-500 font-medium mt-0.5">{owner.ownerName}</div>
                        </td>

                        {/* License */}
                        <td className="py-3.5 px-4 font-mono text-xs text-slate-400">
                          {owner.businessRegNo}
                        </td>

                        {/* Status */}
                        <td className="py-3.5 px-4 text-center">
                          <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-medium border ${
                            owner.status === 'active'
                              ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20'
                              : 'bg-red-500/10 text-red-400 border-red-500/20'
                          }`}>
                            {owner.status}
                          </span>
                        </td>

                        {/* Actions */}
                        <td className="py-3.5 px-4 text-right" onClick={(e) => e.stopPropagation()}>
                          <div className="flex items-center justify-end gap-1.5">
                            <button
                              onClick={() => setSelectedOwnerId(owner.id)}
                              title="Audit Store Reports"
                              className="p-1.5 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-lg transition-colors text-xs flex items-center gap-1"
                            >
                              <FileText size={13} /> Audit
                            </button>

                            {owner.status === 'active' ? (
                              <button
                                onClick={() => onBlockOwner(owner.id)}
                                title="Block Merchant"
                                className="p-1.5 bg-slate-800 hover:bg-red-500/10 text-slate-400 hover:text-red-400 border border-slate-700/60 rounded-lg transition-colors"
                              >
                                <UserX size={14} />
                              </button>
                            ) : (
                              <button
                                onClick={() => onUnblockOwner(owner.id)}
                                title="Unblock Merchant"
                                className="p-1.5 bg-slate-800 hover:bg-emerald-500/10 text-slate-400 hover:text-emerald-400 border border-slate-700/60 rounded-lg transition-colors"
                              >
                                <UserCheck size={14} />
                              </button>
                            )}
                          </div>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </div>

        </div>

        {/* Business Audit Reports Section */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl h-fit">
          <div className="flex items-center justify-between pb-4 border-b border-slate-800 mb-4">
            <h3 className="font-display font-semibold text-white flex items-center gap-1.5">
              <TrendingUp size={16} className="text-cyan-400" />
              Merchant Audit
            </h3>
            <span className="text-[10px] text-slate-500 font-mono">BUSINESS REPORTS</span>
          </div>

          {selectedOwner ? (
            <div className="space-y-5 animate-fadeIn">
              <div>
                <h4 className="font-display font-bold text-white text-base leading-tight">
                  {selectedOwner.shopName}
                </h4>
                <p className="text-xs text-slate-400 font-mono mt-1 flex items-center gap-1">
                  <ShieldCheck size={12} className="text-emerald-400" /> Authorized Shop • ID: {selectedOwner.id}
                </p>
              </div>

              {/* Stats Block */}
              <div className="grid grid-cols-2 gap-3 pt-2">
                <div className="bg-slate-800/40 p-3 rounded-xl border border-slate-800">
                  <span className="text-[10px] font-mono uppercase text-slate-500">Gross Sales</span>
                  <p className="text-lg font-mono font-bold text-white mt-0.5">
                    ${selectedOwner.totalRevenue.toFixed(2)}
                  </p>
                </div>
                <div className="bg-slate-800/40 p-3 rounded-xl border border-slate-800">
                  <span className="text-[10px] font-mono uppercase text-slate-500">Orders Met</span>
                  <p className="text-lg font-mono font-bold text-white mt-0.5">
                    {selectedOwner.totalSales}
                  </p>
                </div>
              </div>

              {/* Financial Breakdown */}
              <div className="space-y-2.5 pt-1">
                <h5 className="text-[11px] font-mono uppercase tracking-wider text-slate-500">Platform Split</h5>
                
                <div className="flex justify-between items-center text-xs text-slate-300">
                  <span>Merchant Share (90%):</span>
                  <span className="font-mono text-slate-400">${(selectedOwner.totalRevenue * 0.9).toFixed(2)}</span>
                </div>
                <div className="flex justify-between items-center text-xs text-slate-300">
                  <span>Platform Commission (10%):</span>
                  <span className="font-mono text-emerald-400">+${(selectedOwner.totalRevenue * 0.1).toFixed(2)}</span>
                </div>
                <div className="h-px bg-slate-800/80 my-2"></div>
                <div className="flex justify-between items-center text-xs font-mono">
                  <span className="text-slate-400">Owner Contact:</span>
                  <span className="text-slate-300">{selectedOwner.phone}</span>
                </div>
                <div className="flex justify-between items-center text-xs font-mono">
                  <span className="text-slate-400">Email:</span>
                  <span className="text-slate-300 truncate max-w-[150px]" title={selectedOwner.email}>
                    {selectedOwner.email}
                  </span>
                </div>
                <div className="flex justify-between items-center text-xs font-mono">
                  <span className="text-slate-400">Established:</span>
                  <span className="text-slate-300">{selectedOwner.registrationDate}</span>
                </div>
              </div>

              {/* Quick actions inside Drawer */}
              <div className="pt-2">
                <button
                  onClick={() => alert(`Opening complete business ledger file for ${selectedOwner.shopName}...`)}
                  className="w-full py-2 bg-slate-800 hover:bg-slate-700 text-xs font-semibold text-slate-200 rounded-xl flex items-center justify-center gap-2 transition-colors border border-slate-700/60"
                >
                  <FileText size={14} />
                  Export Invoice Ledger
                </button>
              </div>
            </div>
          ) : (
            <div className="py-16 text-center text-slate-500">
              <Store size={32} className="mx-auto text-slate-800 mb-2" />
              <p className="text-xs font-medium">Select a shop from the list to view its financial audit summary.</p>
            </div>
          )}
        </div>

      </div>
    </div>
  );
}
