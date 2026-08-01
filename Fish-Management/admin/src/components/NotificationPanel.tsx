/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState } from 'react';
import { 
  Bell, 
  Send, 
  Megaphone, 
  Tag, 
  Users, 
  Store, 
  Truck, 
  Globe,
  PlusCircle,
  Clock
} from 'lucide-react';
import { SystemNotification } from '../types';

interface NotificationPanelProps {
  notifications: SystemNotification[];
  onSendNotification: (notification: Omit<SystemNotification, 'id' | 'date' | 'status'>) => void;
}

export default function NotificationPanel({
  notifications,
  onSendNotification
}: NotificationPanelProps) {
  
  // Form state
  const [title, setTitle] = useState('');
  const [message, setMessage] = useState('');
  const [type, setType] = useState<'promotional' | 'announcement'>('promotional');
  const [recipients, setRecipients] = useState<'all' | 'customers' | 'owners' | 'delivery'>('all');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim() || !message.trim()) {
      alert('Please fill out the notification title and message');
      return;
    }

    onSendNotification({
      type,
      title,
      message,
      recipients
    });

    // Reset form
    setTitle('');
    setMessage('');
    setType('promotional');
    setRecipients('all');
    alert('System Broadcast Dispatched successfully!');
  };

  const getRecipientLabel = (rec: SystemNotification['recipients']) => {
    switch (rec) {
      case 'all': return { text: 'All Channels', icon: Globe, style: 'bg-slate-800 text-slate-300 border-slate-700/60' };
      case 'customers': return { text: 'Consumers Only', icon: Users, style: 'bg-blue-500/10 text-blue-400 border-blue-500/20' };
      case 'owners': return { text: 'Shop Owners Only', icon: Store, style: 'bg-amber-500/10 text-amber-400 border-amber-500/20' };
      case 'delivery': return { text: 'Couriers Only', icon: Truck, style: 'bg-purple-500/10 text-purple-400 border-purple-500/20' };
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h2 className="text-xl font-display font-semibold text-white tracking-tight flex items-center gap-2">
          <Bell className="text-cyan-400" size={20} />
          Broadcast & Communication Panel
        </h2>
        <p className="text-xs text-slate-400">Dispatch promotional flyers to consumers, logistics mandates, or critical app updates</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        
        {/* Dispatched History (2 Columns) */}
        <div className="lg:col-span-2 space-y-4">
          <div className="flex items-center justify-between pb-3 border-b border-slate-800">
            <h3 className="font-display font-semibold text-white text-sm">Dispatched Broadcast Feed</h3>
            <span className="text-[10px] text-slate-500 font-mono">CHRONOLOGICAL LOG</span>
          </div>

          <div className="space-y-4">
            {notifications.length === 0 ? (
              <div className="bg-slate-900 border border-slate-800 rounded-2xl py-12 text-center text-slate-500">
                <Bell size={32} className="mx-auto text-slate-800 mb-2" />
                <p className="text-xs font-semibold">No broadcasts sent yet.</p>
              </div>
            ) : (
              [...notifications].reverse().map(ntf => {
                const config = getRecipientLabel(ntf.recipients);
                const AudienceIcon = config.icon;
                
                return (
                  <div key={ntf.id} className="bg-slate-900 border border-slate-800 rounded-2xl p-5 shadow-lg relative overflow-hidden group">
                    <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-3 border-b border-slate-800/50 mb-3.5">
                      <div className="flex items-center gap-2">
                        {ntf.type === 'promotional' ? (
                          <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-lg text-[10px] font-mono font-semibold bg-cyan-500/15 text-cyan-400 border border-cyan-500/20 uppercase">
                            <Tag size={11} /> PROMO DEALS
                          </span>
                        ) : (
                          <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-lg text-[10px] font-mono font-semibold bg-purple-500/15 text-purple-400 border border-purple-500/20 uppercase">
                            <Megaphone size={11} /> MANDATORY ANNOUNCEMENT
                          </span>
                        )}

                        <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-lg text-[10px] font-mono font-medium border ${config.style}`}>
                          <AudienceIcon size={11} />
                          {config.text}
                        </span>
                      </div>

                      <span className="text-[10px] text-slate-500 font-mono flex items-center gap-1">
                        <Clock size={11} /> Sent: {ntf.date}
                      </span>
                    </div>

                    <div className="space-y-1">
                      <h4 className="font-display font-semibold text-white text-sm">{ntf.title}</h4>
                      <p className="text-xs text-slate-400 leading-relaxed max-w-3xl">
                        {ntf.message}
                      </p>
                    </div>
                  </div>
                );
              })
            )}
          </div>
        </div>

        {/* Compose Form (1 Column) */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 shadow-xl h-fit">
          <div className="flex items-center justify-between pb-3 border-b border-slate-800 mb-4">
            <h3 className="font-display font-semibold text-white">Compose Broadcast</h3>
            <span className="text-[10px] text-slate-500 font-mono">DRAFTING STATION</span>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Type */}
            <div className="space-y-1.5">
              <label className="text-xs font-medium text-slate-400">Broadcast Class</label>
              <div className="flex gap-2">
                <button
                  type="button"
                  onClick={() => setType('promotional')}
                  className={`flex-1 py-2 rounded-lg text-xs font-medium border transition-colors ${
                    type === 'promotional'
                      ? 'bg-cyan-500/10 text-cyan-400 border-cyan-500/25 font-semibold shadow-inner'
                      : 'bg-slate-800 text-slate-400 border-slate-700/50 hover:bg-slate-800/80'
                  }`}
                >
                  Promotional Offer
                </button>
                <button
                  type="button"
                  onClick={() => setType('announcement')}
                  className={`flex-1 py-2 rounded-lg text-xs font-medium border transition-colors ${
                    type === 'announcement'
                      ? 'bg-purple-500/10 text-purple-400 border-purple-500/25 font-semibold shadow-inner'
                      : 'bg-slate-800 text-slate-400 border-slate-700/50 hover:bg-slate-800/80'
                  }`}
                >
                  Announcement
                </button>
              </div>
            </div>

            {/* Target Channel */}
            <div className="space-y-1.5">
              <label className="text-xs font-medium text-slate-400">Target Segment</label>
              <select
                value={recipients}
                onChange={e => setRecipients(e.target.value as any)}
                className="w-full px-3 py-1.5 bg-slate-800 border border-slate-700/60 rounded-lg text-xs text-white focus:border-cyan-500 focus:outline-none"
              >
                <option value="all">🌐 Broadcast to All (Owners, Drivers & Consumers)</option>
                <option value="customers">🛒 Consumers App Segment Only</option>
                <option value="owners">🏪 Seafood Store Owners Only</option>
                <option value="delivery">🛵 Logistics Delivery Fleet Only</option>
              </select>
            </div>

            {/* Title */}
            <div className="space-y-1.5">
              <label className="text-xs font-medium text-slate-400">Subject Title</label>
              <input
                type="text"
                required
                value={title}
                onChange={e => setTitle(e.target.value)}
                placeholder="e.g. Free delivery on orders above $50"
                className="w-full px-3 py-1.5 bg-slate-800 border border-slate-700/50 rounded-lg text-xs text-white focus:border-cyan-500 focus:outline-none placeholder:text-slate-500"
              />
            </div>

            {/* Message Body */}
            <div className="space-y-1.5">
              <label className="text-xs font-medium text-slate-400">Message Body</label>
              <textarea
                required
                rows={4}
                value={message}
                onChange={e => setMessage(e.target.value)}
                placeholder="Draft broadcast message clearly explaining details..."
                className="w-full p-2.5 bg-slate-800 border border-slate-700/50 rounded-lg text-xs text-white focus:border-cyan-500 focus:outline-none placeholder:text-slate-500"
              />
            </div>

            {/* Dispatch Button */}
            <button
              type="submit"
              className="w-full py-2 bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 text-slate-950 font-bold text-xs rounded-xl flex items-center justify-center gap-2 transition-all shadow-lg shadow-cyan-500/10"
            >
              <Send size={13} />
              Dispatch System Broadcast
            </button>
          </form>
        </div>

      </div>
    </div>
  );
}
