/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState } from 'react';
import { 
  AlertTriangle, 
  MessageSquare, 
  CheckCircle2, 
  Clock, 
  CornerDownRight, 
  Users, 
  Store, 
  Truck,
  Send
} from 'lucide-react';
import { Complaint } from '../types';

interface ComplaintManagementProps {
  complaints: Complaint[];
  onResolveComplaint: (id: string, resolution: string) => void;
}

export default function ComplaintManagement({
  complaints,
  onResolveComplaint
}: ComplaintManagementProps) {
  
  const [filterType, setFilterType] = useState<'all' | 'customer' | 'owner' | 'delivery'>('all');
  const [filterStatus, setFilterStatus] = useState<'all' | 'pending' | 'resolved'>('all');
  
  // Resolution form states
  const [resolvingId, setResolvingId] = useState<string | null>(null);
  const [resolutionText, setResolutionText] = useState('');

  // Filter logic
  const filteredComplaints = complaints.filter(c => {
    const matchesType = filterType === 'all' || c.senderType === filterType;
    const matchesStatus = filterStatus === 'all' || c.status === filterStatus;
    return matchesType && matchesStatus;
  });

  const getSenderIcon = (type: Complaint['senderType']) => {
    switch (type) {
      case 'customer':
        return <Users size={14} className="text-blue-400" />;
      case 'owner':
        return <Store size={14} className="text-amber-400" />;
      case 'delivery':
        return <Truck size={14} className="text-purple-400" />;
    }
  };

  const handleOpenResolve = (id: string) => {
    setResolvingId(id);
    setResolutionText('');
  };

  const handleSubmitResolution = (id: string) => {
    if (!resolutionText.trim()) {
      alert('Please enter a resolution response');
      return;
    }
    onResolveComplaint(id, resolutionText);
    setResolvingId(null);
    setResolutionText('');
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h2 className="text-xl font-display font-semibold text-white tracking-tight flex items-center gap-2">
          <AlertTriangle className="text-cyan-400" size={20} />
          Dispute & Complaint Center
        </h2>
        <p className="text-xs text-slate-400">Mediate customer order issues, shop owner delays, and courier transit blocks</p>
      </div>

      {/* Filter controls */}
      <div className="bg-slate-900 border border-slate-800 p-4 rounded-2xl flex flex-col md:flex-row gap-4 items-center justify-between shadow-lg">
        {/* Type selector */}
        <div className="flex gap-1.5 bg-slate-800 p-1 rounded-xl border border-slate-700/50 w-full md:w-auto">
          <button
            onClick={() => setFilterType('all')}
            className={`flex-1 md:flex-initial px-3.5 py-1.5 rounded-lg text-xs font-medium transition-all ${
              filterType === 'all' ? 'bg-slate-900 text-white shadow' : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            All Channels
          </button>
          <button
            onClick={() => setFilterType('customer')}
            className={`flex-1 md:flex-initial px-3.5 py-1.5 rounded-lg text-xs font-medium transition-all flex items-center justify-center gap-1.5 ${
              filterType === 'customer' ? 'bg-slate-900 text-blue-400 shadow' : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            <Users size={12} /> Customers
          </button>
          <button
            onClick={() => setFilterType('owner')}
            className={`flex-1 md:flex-initial px-3.5 py-1.5 rounded-lg text-xs font-medium transition-all flex items-center justify-center gap-1.5 ${
              filterType === 'owner' ? 'bg-slate-900 text-amber-400 shadow' : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            <Store size={12} /> Store Owners
          </button>
          <button
            onClick={() => setFilterType('delivery')}
            className={`flex-1 md:flex-initial px-3.5 py-1.5 rounded-lg text-xs font-medium transition-all flex items-center justify-center gap-1.5 ${
              filterType === 'delivery' ? 'bg-slate-900 text-purple-400 shadow' : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            <Truck size={12} /> Couriers
          </button>
        </div>

        {/* Status filter tabs */}
        <div className="flex gap-1.5 bg-slate-800 p-1 rounded-xl border border-slate-700/50 w-full md:w-auto">
          <button
            onClick={() => setFilterStatus('all')}
            className={`flex-1 md:flex-initial px-3.5 py-1.5 rounded-lg text-xs font-medium transition-all ${
              filterStatus === 'all' ? 'bg-slate-900 text-white shadow' : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            All ({complaints.length})
          </button>
          <button
            onClick={() => setFilterStatus('pending')}
            className={`flex-1 md:flex-initial px-3.5 py-1.5 rounded-lg text-xs font-medium transition-all flex items-center justify-center gap-1 ${
              filterStatus === 'pending' ? 'bg-slate-900 text-amber-400 shadow' : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            Pending ({complaints.filter(c => c.status === 'pending').length})
          </button>
          <button
            onClick={() => setFilterStatus('resolved')}
            className={`flex-1 md:flex-initial px-3.5 py-1.5 rounded-lg text-xs font-medium transition-all flex items-center justify-center gap-1 ${
              filterStatus === 'resolved' ? 'bg-slate-900 text-emerald-400 shadow' : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            Resolved ({complaints.filter(c => c.status === 'resolved').length})
          </button>
        </div>
      </div>

      {/* Complaints List Container */}
      <div className="space-y-4">
        {filteredComplaints.length === 0 ? (
          <div className="bg-slate-900 border border-slate-800 rounded-2xl py-16 text-center text-slate-500">
            <MessageSquare size={36} className="mx-auto text-slate-800 mb-2" />
            <p className="text-xs font-semibold">No complaints registered in this stream.</p>
          </div>
        ) : (
          filteredComplaints.map(ticket => (
            <div 
              key={ticket.id}
              className={`bg-slate-900 border rounded-2xl p-5 shadow-lg space-y-4 transition-colors ${
                ticket.status === 'pending' 
                  ? 'border-slate-800 hover:border-slate-700' 
                  : 'border-slate-800/45 bg-slate-900/50'
              }`}
            >
              {/* Ticket Top Meta */}
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2.5 pb-3.5 border-b border-slate-800/60">
                <div className="flex items-center gap-2">
                  <div className="px-2.5 py-1 bg-slate-800 rounded-lg flex items-center gap-1.5 text-[11px] font-mono text-slate-300 border border-slate-700/40">
                    {getSenderIcon(ticket.senderType)}
                    <span className="capitalize">{ticket.senderType}</span>
                  </div>
                  <span className="text-xs font-mono font-medium text-slate-400">
                    ID: {ticket.id}
                  </span>
                </div>

                <div className="flex items-center gap-3">
                  <span className="text-xs text-slate-500 font-mono">
                    Logged: {ticket.date}
                  </span>
                  <span className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-mono font-semibold border uppercase ${
                    ticket.status === 'pending'
                      ? 'bg-amber-500/10 text-amber-400 border-amber-500/20'
                      : 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20'
                  }`}>
                    {ticket.status === 'pending' ? <Clock size={10} /> : <CheckCircle2 size={10} />}
                    {ticket.status}
                  </span>
                </div>
              </div>

              {/* Core Content */}
              <div className="space-y-1.5">
                <h3 className="font-display font-semibold text-white text-sm">{ticket.title}</h3>
                <p className="text-xs text-slate-400 leading-relaxed max-w-4xl">
                  {ticket.description}
                </p>
                <div className="text-[11px] text-slate-500 font-mono">
                  Submitted by: <span className="text-slate-300 font-sans font-medium">{ticket.senderName}</span>
                </div>
              </div>

              {/* Action or Existing Resolution Block */}
              {ticket.status === 'resolved' ? (
                /* Resolution Statement */
                <div className="bg-slate-800/40 border border-slate-800 p-3.5 rounded-xl flex gap-3 text-xs leading-relaxed max-w-4xl animate-fadeIn">
                  <CornerDownRight size={16} className="text-emerald-400 shrink-0 mt-0.5" />
                  <div>
                    <span className="font-semibold text-slate-200">System Administrator Action:</span>
                    <p className="text-slate-400 mt-1">{ticket.resolution}</p>
                  </div>
                </div>
              ) : resolvingId === ticket.id ? (
                /* Resolution Input Box */
                <div className="bg-slate-800 border border-slate-700/60 p-4 rounded-xl space-y-3 animate-slideDown max-w-3xl">
                  <span className="text-xs font-semibold text-slate-300">Formulate Resolution Statement</span>
                  <textarea
                    rows={3}
                    placeholder="Enter response, compensatory measures, driver feedback, or refund logs..."
                    value={resolutionText}
                    onChange={e => setResolutionText(e.target.value)}
                    className="w-full p-2.5 bg-slate-900 border border-slate-700 rounded-lg text-xs text-white focus:border-cyan-500 focus:outline-none placeholder:text-slate-500"
                  />
                  <div className="flex gap-2">
                    <button
                      onClick={() => handleSubmitResolution(ticket.id)}
                      className="px-3 py-1.5 bg-emerald-500 hover:bg-emerald-400 text-slate-950 text-xs font-bold rounded-lg flex items-center gap-1.5 transition-colors"
                    >
                      <Send size={12} />
                      Log Dispute Resolved
                    </button>
                    <button
                      onClick={() => setResolvingId(null)}
                      className="px-3 py-1.5 bg-slate-700 hover:bg-slate-600 text-slate-300 text-xs font-medium rounded-lg transition-colors"
                    >
                      Dismiss
                    </button>
                  </div>
                </div>
              ) : (
                /* Open Action Trigger */
                <div className="pt-2">
                  <button
                    onClick={() => handleOpenResolve(ticket.id)}
                    className="px-3.5 py-1.5 bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white border border-slate-700 text-xs font-semibold rounded-lg transition-colors"
                  >
                    Resolve Dispute
                  </button>
                </div>
              )}
            </div>
          ))
        )}
      </div>
    </div>
  );
}
