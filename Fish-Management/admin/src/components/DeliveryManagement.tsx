/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState } from 'react';
import { 
  Truck, 
  Plus, 
  Trash2, 
  Edit2, 
  Star, 
  CheckCircle2, 
  MapPin, 
  X,
  Mail,
  Phone,
  Save,
  RotateCcw
} from 'lucide-react';
import { DeliveryStaff } from '../types';
import { availableDeliveryAreas } from '../data/mockData';

interface DeliveryManagementProps {
  deliveryStaff: DeliveryStaff[];
  onAddDeliveryStaff: (staff: Omit<DeliveryStaff, 'id' | 'totalDeliveries' | 'rating'>) => void;
  onUpdateDeliveryStaff: (staff: DeliveryStaff) => void;
  onRemoveDeliveryStaff: (id: string) => void;
}

export default function DeliveryManagement({
  deliveryStaff,
  onAddDeliveryStaff,
  onUpdateDeliveryStaff,
  onRemoveDeliveryStaff
}: DeliveryManagementProps) {
  
  // State for form
  const [isEditing, setIsEditing] = useState(false);
  const [editingId, setEditingId] = useState<string | null>(null);
  
  // Form fields
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [status, setStatus] = useState<'active' | 'inactive'>('active');
  const [assignedAreas, setAssignedAreas] = useState<string[]>([]);

  // Area toggle helper
  const handleToggleArea = (area: string) => {
    if (assignedAreas.includes(area)) {
      setAssignedAreas(assignedAreas.filter(a => a !== area));
    } else {
      setAssignedAreas([...assignedAreas, area]);
    }
  };

  // Reset form
  const resetForm = () => {
    setName('');
    setEmail('');
    setPhone('');
    setStatus('active');
    setAssignedAreas([]);
    setIsEditing(false);
    setEditingId(null);
  };

  // Submit add or edit
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name || !email || !phone) {
      alert('Please fill out all required fields');
      return;
    }

    if (isEditing && editingId) {
      const existing = deliveryStaff.find(d => d.id === editingId);
      if (existing) {
        onUpdateDeliveryStaff({
          ...existing,
          name,
          email,
          phone,
          status,
          assignedAreas
        });
      }
    } else {
      onAddDeliveryStaff({
        name,
        email,
        phone,
        status,
        assignedAreas
      });
    }
    resetForm();
  };

  // Trigger edit mode
  const handleEdit = (staff: DeliveryStaff) => {
    setIsEditing(true);
    setEditingId(staff.id);
    setName(staff.name);
    setEmail(staff.email);
    setPhone(staff.phone);
    setStatus(staff.status);
    setAssignedAreas(staff.assignedAreas);
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h2 className="text-xl font-display font-semibold text-white tracking-tight flex items-center gap-2">
          <Truck className="text-cyan-400" size={20} />
          Delivery Staff Management
        </h2>
        <p className="text-xs text-slate-400">Add staff, assign zone boundaries, and monitor courier ratings</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        
        {/* Delivery Staff List (2 Columns) */}
        <div className="lg:col-span-2 space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {deliveryStaff.map(staff => (
              <div 
                key={staff.id}
                className="bg-slate-900 border border-slate-800 rounded-2xl p-5 relative overflow-hidden shadow-lg group hover:border-slate-700/60 transition-all flex flex-col justify-between"
              >
                {/* Header info */}
                <div className="space-y-3">
                  <div className="flex justify-between items-start">
                    <div className="flex items-center gap-2.5">
                      <div className="w-9 h-9 rounded-xl bg-slate-800/80 border border-slate-700/40 flex items-center justify-center font-display font-semibold text-cyan-400">
                        {staff.name[0]}
                      </div>
                      <div>
                        <h4 className="font-display font-semibold text-white text-sm">{staff.name}</h4>
                        <p className="text-[10px] text-slate-500 font-mono mt-0.5">{staff.id}</p>
                      </div>
                    </div>

                    <span className={`px-2 py-0.5 text-[10px] font-medium rounded-full font-mono border ${
                      staff.status === 'active' 
                        ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20' 
                        : 'bg-red-500/10 text-red-400 border-red-500/20'
                    }`}>
                      {staff.status}
                    </span>
                  </div>

                  {/* Ratings & Deliveries */}
                  <div className="flex items-center gap-4 text-xs font-mono py-1 border-y border-slate-800/60">
                    <span className="flex items-center gap-1 text-slate-300">
                      <Star size={13} className="text-amber-400 fill-amber-400" />
                      {staff.rating.toFixed(1)}
                    </span>
                    <span className="text-slate-500">|</span>
                    <span className="text-slate-300">
                      <span className="font-bold text-slate-100">{staff.totalDeliveries}</span> Deliveries
                    </span>
                  </div>

                  {/* Area badges */}
                  <div className="space-y-1.5">
                    <span className="text-[10px] font-mono text-slate-500 uppercase tracking-wide flex items-center gap-1">
                      <MapPin size={11} /> Coverage Areas
                    </span>
                    <div className="flex flex-wrap gap-1">
                      {staff.assignedAreas.length === 0 ? (
                        <span className="text-xs text-slate-600 italic">No areas assigned</span>
                      ) : (
                        staff.assignedAreas.map(area => (
                          <span key={area} className="px-2 py-0.5 bg-slate-800 border border-slate-700/40 text-slate-300 rounded text-[10px] font-medium font-mono">
                            {area}
                          </span>
                        ))
                      )}
                    </div>
                  </div>
                </div>

                {/* Actions */}
                <div className="flex justify-end gap-1.5 mt-5 pt-3 border-t border-slate-800/40">
                  <button
                    onClick={() => handleEdit(staff)}
                    className="p-1.5 bg-slate-800/60 hover:bg-slate-800 text-slate-400 hover:text-white border border-slate-700/40 rounded-lg transition-colors"
                    title="Edit Courier"
                  >
                    <Edit2 size={13} />
                  </button>
                  <button
                    onClick={() => onRemoveDeliveryStaff(staff.id)}
                    className="p-1.5 bg-slate-800/60 hover:bg-red-500/15 text-slate-400 hover:text-red-400 border border-slate-700/40 rounded-lg transition-colors"
                    title="Remove Courier"
                  >
                    <Trash2 size={13} />
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Form panel (1 Column) */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-5 shadow-xl h-fit">
          <div className="flex items-center justify-between pb-3 border-b border-slate-800 mb-4">
            <h3 className="font-display font-semibold text-white">
              {isEditing ? 'Edit Courier Staff' : 'Register New Courier'}
            </h3>
            <span className="text-[10px] text-slate-500 font-mono">
              {isEditing ? 'UPDATE' : 'REGISTRATION'}
            </span>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Name */}
            <div className="space-y-1.5">
              <label className="text-xs font-medium text-slate-400">Full Name</label>
              <input
                type="text"
                required
                value={name}
                onChange={e => setName(e.target.value)}
                placeholder="e.g. David swift"
                className="w-full px-3 py-1.5 bg-slate-800 border border-slate-700/50 rounded-lg text-xs text-white focus:border-cyan-500 focus:outline-none placeholder:text-slate-500"
              />
            </div>

            {/* Email */}
            <div className="space-y-1.5">
              <label className="text-xs font-medium text-slate-400">Email Address</label>
              <div className="relative">
                <Mail className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500" size={13} />
                <input
                  type="email"
                  required
                  value={email}
                  onChange={e => setEmail(e.target.value)}
                  placeholder="name@express.com"
                  className="w-full pl-8 pr-3 py-1.5 bg-slate-800 border border-slate-700/50 rounded-lg text-xs text-white focus:border-cyan-500 focus:outline-none placeholder:text-slate-500"
                />
              </div>
            </div>

            {/* Phone */}
            <div className="space-y-1.5">
              <label className="text-xs font-medium text-slate-400">Phone Number</label>
              <div className="relative">
                <Phone className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500" size={13} />
                <input
                  type="text"
                  required
                  value={phone}
                  onChange={e => setPhone(e.target.value)}
                  placeholder="+1 (555) 000-0000"
                  className="w-full pl-8 pr-3 py-1.5 bg-slate-800 border border-slate-700/50 rounded-lg text-xs text-white focus:border-cyan-500 focus:outline-none placeholder:text-slate-500"
                />
              </div>
            </div>

            {/* Status (Only in Edit mode) */}
            <div className="space-y-1.5">
              <label className="text-xs font-medium text-slate-400">Operational Status</label>
              <div className="flex gap-2">
                <button
                  type="button"
                  onClick={() => setStatus('active')}
                  className={`flex-1 py-1.5 rounded-lg text-xs font-medium border transition-colors ${
                    status === 'active'
                      ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/25 font-semibold'
                      : 'bg-slate-800 text-slate-400 border-slate-700/50 hover:bg-slate-800/80'
                  }`}
                >
                  Active Duty
                </button>
                <button
                  type="button"
                  onClick={() => setStatus('inactive')}
                  className={`flex-1 py-1.5 rounded-lg text-xs font-medium border transition-colors ${
                    status === 'inactive'
                      ? 'bg-red-500/10 text-red-400 border-red-500/25 font-semibold'
                      : 'bg-slate-800 text-slate-400 border-slate-700/50 hover:bg-slate-800/80'
                  }`}
                >
                  On Leave
                </button>
              </div>
            </div>

            {/* Areas Assignment checklist */}
            <div className="space-y-1.5">
              <label className="text-xs font-medium text-slate-400">Assign Delivery Sectors</label>
              <div className="bg-slate-800/50 border border-slate-700/40 rounded-lg p-3 max-h-[160px] overflow-y-auto space-y-1.5">
                {availableDeliveryAreas.map(area => {
                  const isChecked = assignedAreas.includes(area);
                  return (
                    <button
                      type="button"
                      key={area}
                      onClick={() => handleToggleArea(area)}
                      className={`w-full flex items-center justify-between px-2.5 py-1 rounded text-left text-xs transition-colors ${
                        isChecked 
                          ? 'bg-cyan-500/10 text-cyan-400 font-medium' 
                          : 'text-slate-400 hover:bg-slate-800'
                      }`}
                    >
                      <span>{area}</span>
                      {isChecked && <CheckCircle2 size={12} className="text-cyan-400" />}
                    </button>
                  );
                })}
              </div>
            </div>

            {/* Submit & Cancel Actions */}
            <div className="flex gap-2 pt-3 border-t border-slate-800">
              <button
                type="submit"
                className="flex-1 py-2 bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 text-slate-950 font-bold text-xs rounded-xl flex items-center justify-center gap-1.5 transition-all shadow-md shadow-cyan-500/5"
              >
                {isEditing ? <Save size={14} /> : <Plus size={14} />}
                {isEditing ? 'Save Changes' : 'Register Courier'}
              </button>
              
              {isEditing && (
                <button
                  type="button"
                  onClick={resetForm}
                  className="px-3 py-2 bg-slate-800 hover:bg-slate-700 border border-slate-700 text-slate-400 hover:text-slate-200 font-semibold text-xs rounded-xl transition-colors"
                >
                  Cancel
                </button>
              )}
            </div>
          </form>
        </div>

      </div>
    </div>
  );
}
