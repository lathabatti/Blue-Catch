/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React from 'react';
import { 
  LayoutDashboard, 
  Users, 
  Store, 
  Truck, 
  BarChart3, 
  AlertTriangle, 
  Bell, 
  Settings, 
  Activity,
  ShieldCheck,
  CircleDot,
  LogOut
} from 'lucide-react';

import { AdminUser } from '../types';

interface SidebarProps {
  activeTab: string;
  setActiveTab: (tab: string) => void;
  pendingOwnersCount: number;
  pendingComplaintsCount: number;
  appName: string;
  currentUser?: AdminUser | null;
  onLogout?: () => void;
}

export default function Sidebar({ 
  activeTab, 
  setActiveTab, 
  pendingOwnersCount, 
  pendingComplaintsCount,
  appName,
  currentUser,
  onLogout
}: SidebarProps) {
  
  const menuItems = [
    { id: 'dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { id: 'users', label: 'Manage Customers', icon: Users },
    { 
      id: 'owners', 
      label: 'Manage Owners', 
      icon: Store,
      badge: pendingOwnersCount > 0 ? pendingOwnersCount : undefined 
    },
    { id: 'delivery', label: 'Delivery Staff', icon: Truck },
    { id: 'reports', label: 'Reports & Analytics', icon: BarChart3 },
    { 
      id: 'complaints', 
      label: 'Complaints', 
      icon: AlertTriangle,
      badge: pendingComplaintsCount > 0 ? pendingComplaintsCount : undefined 
    },
    { id: 'notifications', label: 'Notifications', icon: Bell },
    { id: 'settings', label: 'System Settings', icon: Settings },
    { id: 'simulator', label: 'Workflow Simulator', icon: Activity, highlight: true }
  ];

  return (
    <aside className="w-64 bg-slate-900 text-slate-100 flex flex-col h-screen border-r border-slate-800 shrink-0">
      {/* Brand Header */}
      <div className="p-6 border-b border-slate-800 flex items-center gap-3">
        <div className="bg-cyan-500 text-slate-950 p-2 rounded-xl shadow-lg shadow-cyan-500/20">
          <Activity size={22} className="animate-pulse" />
        </div>
        <div>
          <h1 className="font-display font-bold text-lg leading-tight tracking-tight text-white">
            {appName}
          </h1>
          <p className="text-xs text-slate-400 font-mono flex items-center gap-1 mt-0.5">
            <ShieldCheck size={12} className="text-cyan-400" /> System Control
          </p>
        </div>
      </div>

      {/* Admin Profile */}
      <div className="p-4 mx-4 my-4 bg-slate-800/50 rounded-xl border border-slate-800 flex items-center justify-between gap-2 overflow-hidden">
        <div className="flex items-center gap-2.5 overflow-hidden">
          <div className="relative shrink-0">
            <div className="w-9 h-9 rounded-lg bg-gradient-to-tr from-cyan-500 to-emerald-400 flex items-center justify-center font-display font-semibold text-slate-950 text-xs shadow">
              {currentUser?.name ? currentUser.name.split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase() : 'AD'}
            </div>
            <span className="absolute -bottom-0.5 -right-0.5 w-2.5 h-2.5 bg-emerald-500 border-2 border-slate-900 rounded-full"></span>
          </div>
          <div className="overflow-hidden">
            <h3 className="font-display font-medium text-xs text-slate-200 truncate" title={currentUser?.name || 'System Administrator'}>
              {currentUser?.name || 'System Administrator'}
            </h3>
            <p className="text-[10px] text-slate-400 font-mono truncate" title={currentUser?.email || 'admin@bluecatch.com'}>
              {currentUser?.email || 'admin@bluecatch.com'}
            </p>
          </div>
        </div>
        {onLogout && (
          <button 
            onClick={onLogout}
            title="Sign out of system control"
            className="p-1.5 rounded-lg bg-slate-800 hover:bg-red-500/10 text-slate-400 hover:text-red-400 border border-slate-750/50 hover:border-red-500/20 transition-all shrink-0"
          >
            <LogOut size={12} />
          </button>
        )}
      </div>

      {/* Navigation */}
      <nav className="flex-1 px-3 space-y-1 overflow-y-auto pb-4">
        <div className="px-3 mb-2 text-[10px] font-mono text-slate-500 uppercase tracking-widest">
          Control Center
        </div>
        
        {menuItems.map((item) => {
          const Icon = item.icon;
          const isActive = activeTab === item.id;
          
          return (
            <button
              key={item.id}
              onClick={() => setActiveTab(item.id)}
              className={`w-full flex items-center justify-between px-3 py-2.5 rounded-lg text-sm font-medium transition-all duration-150 ${
                isActive
                  ? item.highlight 
                    ? 'bg-cyan-500 text-slate-950 font-semibold shadow-lg shadow-cyan-500/10'
                    : 'bg-slate-800 text-white font-medium border-l-2 border-cyan-400 pl-2.5'
                  : item.highlight
                    ? 'text-cyan-400 hover:bg-slate-800/70'
                    : 'text-slate-400 hover:bg-slate-800/40 hover:text-slate-200'
              }`}
            >
              <div className="flex items-center gap-3">
                <Icon size={18} className={isActive && !item.highlight ? 'text-cyan-400' : ''} />
                <span>{item.label}</span>
              </div>
              
              {item.badge !== undefined && (
                <span className={`px-2 py-0.5 text-[10px] font-bold rounded-full font-mono ${
                  isActive 
                    ? 'bg-slate-900 text-white' 
                    : item.id === 'complaints' 
                      ? 'bg-amber-500/20 text-amber-400 border border-amber-500/30'
                      : 'bg-cyan-500/20 text-cyan-400 border border-cyan-500/30'
                }`}>
                  {item.badge}
                </span>
              )}
            </button>
          );
        })}
      </nav>

      {/* Sidebar Footer */}
      <div className="p-4 border-t border-slate-800 bg-slate-950/40 text-[10px] text-slate-600 font-mono text-center">
        Blue Catch Console
      </div>
    </aside>
  );
}
