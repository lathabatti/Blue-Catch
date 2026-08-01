/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState } from 'react';
import { 
  Settings, 
  Shield, 
  CreditCard, 
  Percent, 
  Database, 
  Save, 
  RotateCcw, 
  CheckCircle2, 
  AlertCircle,
  HelpCircle,
  KeyRound
} from 'lucide-react';
import { SystemSettings as SettingsType } from '../types';

interface SystemSettingsProps {
  settings: SettingsType;
  onSaveSettings: (settings: SettingsType) => void;
}

export default function SystemSettings({
  settings,
  onSaveSettings
}: SystemSettingsProps) {
  
  // Local notification state
  const [notification, setNotification] = useState<{ message: string; type: 'success' | 'error' } | null>(null);

  const showNotification = (message: string, type: 'success' | 'error' = 'success') => {
    setNotification({ message, type });
    setTimeout(() => {
      setNotification(null);
    }, 4000);
  };
  
  // Local form states
  const [appName, setAppName] = useState(settings.appName);
  const [supportEmail, setSupportEmail] = useState(settings.supportEmail);
  const [defaultTaxPercent, setDefaultTaxPercent] = useState(settings.defaultTaxPercent);
  const [commissionRate, setCommissionRate] = useState(settings.commissionRate);
  
  const [allowSelfRegistration, setAllowSelfRegistration] = useState(settings.allowSelfRegistration);
  const [autoAssignDelivery, setAutoAssignDelivery] = useState(settings.autoAssignDelivery);
  const [isMaintenanceMode, setIsMaintenanceMode] = useState(settings.isMaintenanceMode);
  
  const [paymentCod, setPaymentCod] = useState(settings.paymentCod);
  const [paymentOnline, setPaymentOnline] = useState(settings.paymentOnline);

  const [backupStatus, setBackupStatus] = useState(settings.backupStatus);
  const [lastBackupDate, setLastBackupDate] = useState(settings.lastBackupDate);
  const [isBackingUp, setIsBackingUp] = useState(false);

  const handleFormSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSaveSettings({
      appName,
      supportEmail,
      defaultTaxPercent: Number(defaultTaxPercent),
      commissionRate: Number(commissionRate),
      allowSelfRegistration,
      autoAssignDelivery,
      isMaintenanceMode,
      paymentCod,
      paymentOnline,
      backupStatus,
      lastBackupDate
    });
    showNotification('System settings saved successfully!');
  };

  const handleCreateBackup = () => {
    setIsBackingUp(true);
    setBackupStatus('Generating Archive...');
    setTimeout(() => {
      const now = new Date();
      const dateString = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')} ${now.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}`;
      setBackupStatus('Healthy');
      setLastBackupDate(dateString);
      setIsBackingUp(false);
      
      onSaveSettings({
        appName,
        supportEmail,
        defaultTaxPercent: Number(defaultTaxPercent),
        commissionRate: Number(commissionRate),
        allowSelfRegistration,
        autoAssignDelivery,
        isMaintenanceMode,
        paymentCod,
        paymentOnline,
        backupStatus: 'Healthy',
        lastBackupDate: dateString
      });
      showNotification('Database backup created successfully!');
    }, 1500);
  };

  // Roles & permissions dictionary
  const rolesPermissions = [
    { module: 'Browse Fish catalog', admin: true, owner: true, delivery: true, customer: true },
    { module: 'Order placement & Checkout', admin: false, owner: false, delivery: false, customer: true },
    { module: 'Accept orders & Set items stock', admin: false, owner: true, delivery: false, customer: false },
    { module: 'Assign drivers & Complete deliveries', admin: false, owner: true, delivery: true, customer: false },
    { module: 'View owner financial metrics', admin: true, owner: true, delivery: false, customer: false },
    { module: 'Complete system-wide reports audits', admin: true, owner: false, delivery: false, customer: false },
    { module: 'Manage delivery fleet registry', admin: true, owner: false, delivery: false, customer: false },
    { module: 'Change taxation or gateway triggers', admin: true, owner: false, delivery: false, customer: false }
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h2 className="text-xl font-display font-semibold text-white tracking-tight flex items-center gap-2">
          <Settings className="text-cyan-400" size={20} />
          System Settings & Control
        </h2>
        <p className="text-xs text-slate-400">Configure financial splits, enable payment methods, toggle roles, and trigger backups</p>
      </div>

      {notification && (
        <div className={`p-4 rounded-xl text-xs flex items-center gap-2.5 border transition-all ${
          notification.type === 'success' 
            ? 'bg-emerald-500/10 border-emerald-500/20 text-emerald-400' 
            : 'bg-red-500/10 border-red-500/20 text-red-400'
        }`}>
          <CheckCircle2 size={16} className="shrink-0" />
          <span>{notification.message}</span>
        </div>
      )}

      <form onSubmit={handleFormSubmit} className="space-y-6">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          
          {/* General & Financial Settings */}
          <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-lg space-y-5">
            <div className="flex items-center gap-2 pb-3 border-b border-slate-800">
              <Shield size={16} className="text-cyan-400" />
              <h3 className="font-display font-semibold text-white text-sm">System Configuration</h3>
            </div>

            {/* App Name */}
            <div className="space-y-1.5">
              <label className="text-xs font-medium text-slate-400">Platform Brand Name</label>
              <input
                type="text"
                required
                value={appName}
                onChange={e => setAppName(e.target.value)}
                className="w-full px-3 py-1.5 bg-slate-800 border border-slate-700/50 rounded-lg text-xs text-white focus:border-cyan-500 focus:outline-none"
              />
            </div>

            {/* Support Email */}
            <div className="space-y-1.5">
              <label className="text-xs font-medium text-slate-400">Administrative Support Email</label>
              <input
                type="email"
                required
                value={supportEmail}
                onChange={e => setSupportEmail(e.target.value)}
                className="w-full px-3 py-1.5 bg-slate-800 border border-slate-700/50 rounded-lg text-xs text-white focus:border-cyan-500 focus:outline-none"
              />
            </div>

            {/* Tax & Commissions */}
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-1.5">
                <label className="text-xs font-medium text-slate-400 flex items-center gap-1">
                  <Percent size={12} className="text-slate-500" /> Default Consumer Tax (%)
                </label>
                <input
                  type="number"
                  step="0.01"
                  required
                  value={defaultTaxPercent}
                  onChange={e => setDefaultTaxPercent(e.target.value as any)}
                  className="w-full px-3 py-1.5 bg-slate-800 border border-slate-700/50 rounded-lg text-xs font-mono text-white focus:border-cyan-500"
                />
              </div>

              <div className="space-y-1.5">
                <label className="text-xs font-medium text-slate-400 flex items-center gap-1">
                  <Percent size={12} className="text-slate-500" /> Admin Take Commission (%)
                </label>
                <input
                  type="number"
                  step="0.1"
                  required
                  value={commissionRate}
                  onChange={e => setCommissionRate(e.target.value as any)}
                  className="w-full px-3 py-1.5 bg-slate-800 border border-slate-700/50 rounded-lg text-xs font-mono text-white focus:border-cyan-500"
                />
              </div>
            </div>

            {/* Switch Toggles */}
            <div className="space-y-3.5 pt-2">
              <div className="flex items-center justify-between">
                <div>
                  <h4 className="text-xs font-semibold text-slate-200">Self-Registration</h4>
                  <p className="text-[10px] text-slate-400 mt-0.5">Let new merchants apply in the portal</p>
                </div>
                <button
                  type="button"
                  onClick={() => setAllowSelfRegistration(!allowSelfRegistration)}
                  className={`w-11 h-6 rounded-full p-1 transition-colors duration-150 focus:outline-none ${
                    allowSelfRegistration ? 'bg-cyan-500' : 'bg-slate-800'
                  }`}
                >
                  <div className={`w-4 h-4 rounded-full bg-slate-950 transition-transform duration-150 ${
                    allowSelfRegistration ? 'translate-x-5' : 'translate-x-0'
                  }`}></div>
                </button>
              </div>

              <div className="flex items-center justify-between">
                <div>
                  <h4 className="text-xs font-semibold text-slate-200">Auto-Assign Courier</h4>
                  <p className="text-[10px] text-slate-400 mt-0.5">Automate driver assignments by sector</p>
                </div>
                <button
                  type="button"
                  onClick={() => setAutoAssignDelivery(!autoAssignDelivery)}
                  className={`w-11 h-6 rounded-full p-1 transition-colors duration-150 focus:outline-none ${
                    autoAssignDelivery ? 'bg-cyan-500' : 'bg-slate-800'
                  }`}
                >
                  <div className={`w-4 h-4 rounded-full bg-slate-950 transition-transform duration-150 ${
                    autoAssignDelivery ? 'translate-x-5' : 'translate-x-0'
                  }`}></div>
                </button>
              </div>

              <div className="flex items-center justify-between">
                <div>
                  <h4 className="text-xs font-semibold text-slate-200">Maintenance Mode</h4>
                  <p className="text-[10px] text-slate-400 mt-0.5">Lock database transactions and client logins</p>
                </div>
                <button
                  type="button"
                  onClick={() => setIsMaintenanceMode(!isMaintenanceMode)}
                  className={`w-11 h-6 rounded-full p-1 transition-colors duration-150 focus:outline-none ${
                    isMaintenanceMode ? 'bg-red-500' : 'bg-slate-800'
                  }`}
                >
                  <div className={`w-4 h-4 rounded-full bg-slate-950 transition-transform duration-150 ${
                    isMaintenanceMode ? 'translate-x-5' : 'translate-x-0'
                  }`}></div>
                </button>
              </div>
            </div>
          </div>

          {/* Payment Settings & Backup Database */}
          <div className="space-y-6">
            
            {/* Payment triggers */}
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-lg space-y-4">
              <div className="flex items-center gap-2 pb-3 border-b border-slate-800">
                <CreditCard size={16} className="text-cyan-400" />
                <h3 className="font-display font-semibold text-white text-sm">Payment Gateways</h3>
              </div>

              <div className="flex justify-between items-center bg-slate-800/40 p-3 rounded-xl border border-slate-800">
                <div>
                  <h4 className="text-xs font-semibold text-slate-200">Cash on Delivery (COD)</h4>
                  <p className="text-[10px] text-slate-500">Enable physical cash settlement</p>
                </div>
                <button
                  type="button"
                  onClick={() => setPaymentCod(!paymentCod)}
                  className={`w-10 h-5.5 rounded-full p-0.5 transition-colors duration-150 focus:outline-none ${
                    paymentCod ? 'bg-emerald-500' : 'bg-slate-800'
                  }`}
                >
                  <div className={`w-4.5 h-4.5 rounded-full bg-slate-950 transition-transform duration-150 ${
                    paymentCod ? 'translate-x-4.5' : 'translate-x-0'
                  }`}></div>
                </button>
              </div>

              <div className="flex justify-between items-center bg-slate-800/40 p-3 rounded-xl border border-slate-800">
                <div>
                  <h4 className="text-xs font-semibold text-slate-200">Stripe/Online Processing</h4>
                  <p className="text-[10px] text-slate-500">Enable credit card checkout APIs</p>
                </div>
                <button
                  type="button"
                  onClick={() => setPaymentOnline(!paymentOnline)}
                  className={`w-10 h-5.5 rounded-full p-0.5 transition-colors duration-150 focus:outline-none ${
                    paymentOnline ? 'bg-emerald-500' : 'bg-slate-800'
                  }`}
                >
                  <div className={`w-4.5 h-4.5 rounded-full bg-slate-950 transition-transform duration-150 ${
                    paymentOnline ? 'translate-x-4.5' : 'translate-x-0'
                  }`}></div>
                </button>
              </div>
            </div>

            {/* Backups & Restore */}
            <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-lg space-y-4">
              <div className="flex items-center gap-2 pb-3 border-b border-slate-800">
                <Database size={16} className="text-cyan-400" />
                <h3 className="font-display font-semibold text-white text-sm">Database Backups</h3>
              </div>

              <div className="space-y-2.5 font-mono text-xs">
                <div className="flex justify-between">
                  <span className="text-slate-500">DB Engine:</span>
                  <span className="text-slate-300">SQLite & Local Cache</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-500">Archival Status:</span>
                  <span className={`font-semibold flex items-center gap-1 ${
                    backupStatus === 'Healthy' ? 'text-emerald-400' : 'text-amber-400'
                  }`}>
                    <CheckCircle2 size={12} /> {backupStatus}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-500">Last System Sync:</span>
                  <span className="text-slate-300">{lastBackupDate}</span>
                </div>
              </div>

              <div className="pt-2 flex gap-2">
                <button
                  type="button"
                  disabled={isBackingUp}
                  onClick={handleCreateBackup}
                  className="flex-1 py-1.5 bg-slate-800 hover:bg-slate-750 disabled:bg-slate-900 text-slate-200 border border-slate-700/60 rounded-xl text-xs font-semibold flex items-center justify-center gap-1.5 transition-colors"
                >
                  <Database size={13} className={isBackingUp ? 'animate-bounce' : ''} />
                  Create Backup
                </button>
              </div>
            </div>

          </div>

        </div>

        {/* Roles & Permissions Matrix */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-lg space-y-4">
          <div className="flex items-center gap-2 pb-3 border-b border-slate-800">
            <KeyRound size={16} className="text-cyan-400" />
            <h3 className="font-display font-semibold text-white text-sm">RBAC - Roles & Permissions Mapping</h3>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-slate-800 text-[10px] font-mono text-slate-500 uppercase tracking-wider">
                  <th className="pb-2">Permission Module</th>
                  <th className="pb-2 text-center">System Admin</th>
                  <th className="pb-2 text-center">Shop Owner</th>
                  <th className="pb-2 text-center">Logistics Driver</th>
                  <th className="pb-2 text-center">Customer Consumer</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/50 text-xs">
                {rolesPermissions.map((row, i) => (
                  <tr key={i} className="hover:bg-slate-800/10">
                    <td className="py-2.5 font-medium text-slate-300">{row.module}</td>
                    
                    <td className="py-2.5 text-center">
                      <span className={`inline-block w-2.5 h-2.5 rounded-full ${row.admin ? 'bg-cyan-500' : 'bg-slate-800'}`} />
                    </td>
                    <td className="py-2.5 text-center">
                      <span className={`inline-block w-2.5 h-2.5 rounded-full ${row.owner ? 'bg-amber-500' : 'bg-slate-800'}`} />
                    </td>
                    <td className="py-2.5 text-center">
                      <span className={`inline-block w-2.5 h-2.5 rounded-full ${row.delivery ? 'bg-purple-500' : 'bg-slate-800'}`} />
                    </td>
                    <td className="py-2.5 text-center">
                      <span className={`inline-block w-2.5 h-2.5 rounded-full ${row.customer ? 'bg-blue-500' : 'bg-slate-800'}`} />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* Global Save Button */}
        <div className="flex justify-end pt-4 border-t border-slate-800/60">
          <button
            type="submit"
            className="px-5 py-2.5 bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 text-slate-950 font-bold text-sm rounded-xl flex items-center justify-center gap-2 transition-all shadow-lg shadow-cyan-500/10"
          >
            <Save size={16} />
            Save Settings
          </button>
        </div>
      </form>
    </div>
  );
}
