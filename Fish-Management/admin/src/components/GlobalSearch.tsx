/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState, useEffect, useRef } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { 
  Search, 
  X, 
  User, 
  Store, 
  ShoppingBag, 
  CornerDownLeft, 
  ShieldAlert, 
  CheckCircle2, 
  ExternalLink,
  Phone,
  Mail,
  Calendar,
  DollarSign,
  Package,
  Clock,
  ArrowRight
} from 'lucide-react';
import { Customer, Owner, Order, DeliveryStaff } from '../types';

interface GlobalSearchProps {
  customers: Customer[];
  owners: Owner[];
  orders: Order[];
  deliveryStaff: DeliveryStaff[];
  onBlockCustomer: (id: string) => void;
  onUnblockCustomer: (id: string) => void;
  onApproveOwner: (id: string) => void;
  onBlockOwner: (id: string) => void;
  onUnblockOwner: (id: string) => void;
  onUpdateOrderStatus: (orderId: string, status: Order['status'], driverName?: string) => void;
  onSettleFinancials: (deliveredOrder: Order) => void;
  setActiveTab: (tab: string) => void;
}

type CustomerSearchResult = { type: 'customer'; id: string; name: string; item: Customer };
type OwnerSearchResult = { type: 'owner'; id: string; name: string; item: Owner };
type OrderSearchResult = { type: 'order'; id: string; name: string; item: Order };

type SearchResult = CustomerSearchResult | OwnerSearchResult | OrderSearchResult;

export default function GlobalSearch({
  customers,
  owners,
  orders,
  deliveryStaff,
  onBlockCustomer,
  onUnblockCustomer,
  onApproveOwner,
  onBlockOwner,
  onUnblockOwner,
  onUpdateOrderStatus,
  onSettleFinancials,
  setActiveTab
}: GlobalSearchProps) {
  const [query, setQuery] = useState('');
  const [isOpen, setIsOpen] = useState(false);
  const [selectedIndex, setSelectedIndex] = useState(0);
  const [selectedItem, setSelectedItem] = useState<SearchResult | null>(null);
  
  const containerRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  // Global Ctrl+K / Cmd+K key listener to focus search
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
        e.preventDefault();
        inputRef.current?.focus();
        setIsOpen(true);
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);

  // Close search popup on click outside
  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  // Reset selected index when query changes
  useEffect(() => {
    setSelectedIndex(0);
  }, [query]);

  const lowercaseQuery = query.toLowerCase().trim();

  // Filter lists
  const matchedCustomers: CustomerSearchResult[] = lowercaseQuery === '' ? [] : customers
    .filter(c => 
      c.name.toLowerCase().includes(lowercaseQuery) || 
      c.id.toLowerCase().includes(lowercaseQuery) ||
      c.email.toLowerCase().includes(lowercaseQuery) ||
      c.phone.includes(lowercaseQuery)
    )
    .slice(0, 4)
    .map(c => ({ type: 'customer', id: c.id, name: c.name, item: c }));

  const matchedOwners: OwnerSearchResult[] = lowercaseQuery === '' ? [] : owners
    .filter(o => 
      o.ownerName.toLowerCase().includes(lowercaseQuery) || 
      o.shopName.toLowerCase().includes(lowercaseQuery) ||
      o.id.toLowerCase().includes(lowercaseQuery) ||
      o.email.toLowerCase().includes(lowercaseQuery)
    )
    .slice(0, 4)
    .map(o => ({ type: 'owner', id: o.id, name: `${o.ownerName} (${o.shopName})`, item: o }));

  const matchedOrders: OrderSearchResult[] = lowercaseQuery === '' ? [] : orders
    .filter(o => 
      o.id.toLowerCase().includes(lowercaseQuery) || 
      o.customerName.toLowerCase().includes(lowercaseQuery) ||
      o.shopName.toLowerCase().includes(lowercaseQuery)
    )
    .slice(0, 4)
    .map(o => ({ type: 'order', id: o.id, name: `Order ${o.id} - ${o.customerName}`, item: o }));

  const allResults: SearchResult[] = [...matchedCustomers, ...matchedOwners, ...matchedOrders];

  // Key handlers inside the input field
  const handleInputKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Escape') {
      setIsOpen(false);
      inputRef.current?.blur();
    } else if (e.key === 'ArrowDown') {
      e.preventDefault();
      setSelectedIndex(prev => (prev + 1) % Math.max(allResults.length, 1));
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      setSelectedIndex(prev => (prev - 1 + allResults.length) % Math.max(allResults.length, 1));
    } else if (e.key === 'Enter') {
      e.preventDefault();
      if (allResults[selectedIndex]) {
        handleSelectItem(allResults[selectedIndex]);
      }
    }
  };

  const handleSelectItem = (item: SearchResult) => {
    setSelectedItem(item);
    setIsOpen(false);
    setQuery('');
  };

  // Close details modal
  const handleCloseModal = () => {
    setSelectedItem(null);
  };

  // Switch tab and close modal
  const handleNavigateToTab = (tab: string) => {
    setActiveTab(tab);
    setSelectedItem(null);
  };

  return (
    <div id="global-search-container" ref={containerRef} className="relative w-72 md:w-96">
      {/* Search Input Box */}
      <div className="relative">
        <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-500" size={15} />
        <input
          ref={inputRef}
          type="text"
          placeholder="Global Search (Ctrl+K)..."
          value={query}
          onChange={(e) => {
            setQuery(e.target.value);
            setIsOpen(true);
          }}
          onFocus={() => setIsOpen(true)}
          onKeyDown={handleInputKeyDown}
          className="w-full pl-10 pr-12 py-2 bg-slate-900/90 hover:bg-slate-900 border border-slate-800/80 focus:border-cyan-500/50 focus:outline-none focus:ring-1 focus:ring-cyan-500/10 rounded-xl text-xs text-slate-300 transition-all placeholder:text-slate-500"
        />
        {query ? (
          <button 
            onClick={() => setQuery('')}
            className="absolute right-3.5 top-1/2 -translate-y-1/2 text-slate-500 hover:text-slate-300 transition-colors"
          >
            <X size={14} />
          </button>
        ) : (
          <div className="absolute right-3 top-1/2 -translate-y-1/2 px-1.5 py-0.5 rounded bg-slate-800 border border-slate-700/60 text-[9px] text-slate-500 font-mono select-none pointer-events-none hidden sm:block">
            ⌘K
          </div>
        )}
      </div>

      {/* Floating Results Popover Dropdown */}
      <AnimatePresence>
        {isOpen && query.trim() !== '' && (
          <motion.div
            initial={{ opacity: 0, y: 8 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: 8 }}
            transition={{ duration: 0.12 }}
            className="absolute top-full left-0 right-0 mt-2 bg-slate-900 border border-slate-800 rounded-xl shadow-2xl overflow-hidden z-50 max-h-96 overflow-y-auto"
          >
            {allResults.length === 0 ? (
              <div className="p-6 text-center text-slate-500 text-xs">
                No matching customers, orders, or owners.
              </div>
            ) : (
              <div className="p-2 space-y-3">
                {/* Customers Group */}
                {matchedCustomers.length > 0 && (
                  <div>
                    <div className="px-3 py-1 text-[10px] font-mono font-bold tracking-wider text-slate-500 uppercase flex items-center gap-1">
                      <User size={10} className="text-blue-400" /> Customers
                    </div>
                    <div className="mt-1 space-y-0.5">
                      {matchedCustomers.map((res, idx) => {
                        const globalIdx = allResults.indexOf(res);
                        const isSelected = selectedIndex === globalIdx;
                        return (
                          <button
                            key={res.id}
                            onClick={() => handleSelectItem(res)}
                            className={`w-full text-left px-3 py-2 rounded-lg text-xs flex items-center justify-between transition-colors ${
                              isSelected ? 'bg-slate-800 text-white font-medium' : 'text-slate-300 hover:bg-slate-800/40'
                            }`}
                          >
                            <div className="flex items-center gap-2 overflow-hidden">
                              <span className="w-1.5 h-1.5 rounded-full bg-blue-500 shrink-0" />
                              <span className="truncate">{res.name}</span>
                              <span className="text-[10px] text-slate-500 font-mono shrink-0">#{res.id}</span>
                            </div>
                            {isSelected && <CornerDownLeft size={10} className="text-slate-400 shrink-0" />}
                          </button>
                        );
                      })}
                    </div>
                  </div>
                )}

                {/* Shop Owners Group */}
                {matchedOwners.length > 0 && (
                  <div>
                    <div className="px-3 py-1 text-[10px] font-mono font-bold tracking-wider text-slate-500 uppercase flex items-center gap-1">
                      <Store size={10} className="text-amber-400" /> Seafood Shop Owners
                    </div>
                    <div className="mt-1 space-y-0.5">
                      {matchedOwners.map((res, idx) => {
                        const globalIdx = allResults.indexOf(res);
                        const isSelected = selectedIndex === globalIdx;
                        return (
                          <button
                            key={res.id}
                            onClick={() => handleSelectItem(res)}
                            className={`w-full text-left px-3 py-2 rounded-lg text-xs flex items-center justify-between transition-colors ${
                              isSelected ? 'bg-slate-800 text-white font-medium' : 'text-slate-300 hover:bg-slate-800/40'
                            }`}
                          >
                            <div className="flex items-center gap-2 overflow-hidden">
                              <span className="w-1.5 h-1.5 rounded-full bg-amber-500 shrink-0" />
                              <span className="truncate font-medium">{res.item.shopName}</span>
                              <span className="text-slate-500 text-[11px] truncate">({res.item.ownerName})</span>
                              <span className="text-[10px] text-slate-500 font-mono shrink-0">#{res.id}</span>
                            </div>
                            {isSelected && <CornerDownLeft size={10} className="text-slate-400 shrink-0" />}
                          </button>
                        );
                      })}
                    </div>
                  </div>
                )}

                {/* Orders Group */}
                {matchedOrders.length > 0 && (
                  <div>
                    <div className="px-3 py-1 text-[10px] font-mono font-bold tracking-wider text-slate-500 uppercase flex items-center gap-1">
                      <ShoppingBag size={10} className="text-cyan-400" /> Orders
                    </div>
                    <div className="mt-1 space-y-0.5">
                      {matchedOrders.map((res, idx) => {
                        const globalIdx = allResults.indexOf(res);
                        const isSelected = selectedIndex === globalIdx;
                        return (
                          <button
                            key={res.id}
                            onClick={() => handleSelectItem(res)}
                            className={`w-full text-left px-3 py-2 rounded-lg text-xs flex items-center justify-between transition-colors ${
                              isSelected ? 'bg-slate-800 text-white font-medium' : 'text-slate-300 hover:bg-slate-800/40'
                            }`}
                          >
                            <div className="flex items-center gap-2 overflow-hidden">
                              <span className="w-1.5 h-1.5 rounded-full bg-cyan-500 shrink-0" />
                              <span className="font-mono text-[11px] font-semibold text-slate-200 shrink-0">{res.id}</span>
                              <span className="truncate text-slate-400 text-[11px]">{res.item.customerName} → {res.item.shopName}</span>
                              <span className="text-[10px] text-emerald-400 font-mono shrink-0">${res.item.totalAmount.toFixed(2)}</span>
                            </div>
                            {isSelected && <CornerDownLeft size={10} className="text-slate-400 shrink-0" />}
                          </button>
                        );
                      })}
                    </div>
                  </div>
                )}
              </div>
            )}
          </motion.div>
        )}
      </AnimatePresence>

      {/* Detail Overlay Modal */}
      <AnimatePresence>
        {selectedItem && (
          <div className="fixed inset-0 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center z-[100] p-4">
            <motion.div
              initial={{ scale: 0.95, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.95, opacity: 0 }}
              transition={{ duration: 0.18 }}
              className="bg-slate-900 border border-slate-800 rounded-2xl w-full max-w-lg overflow-hidden shadow-2xl flex flex-col"
            >
              {/* Modal Header */}
              <div className="px-6 py-4 bg-slate-950/40 border-b border-slate-800/80 flex items-center justify-between">
                <div className="flex items-center gap-2">
                  {selectedItem.type === 'customer' && <User className="text-blue-400" size={18} />}
                  {selectedItem.type === 'owner' && <Store className="text-amber-400" size={18} />}
                  {selectedItem.type === 'order' && <ShoppingBag className="text-cyan-400" size={18} />}
                  <span className="text-xs font-mono uppercase tracking-wider text-slate-400">
                    {selectedItem.type} Details
                  </span>
                </div>
                <button 
                  onClick={handleCloseModal}
                  className="p-1 rounded-lg bg-slate-800/50 hover:bg-slate-800 text-slate-400 hover:text-slate-200 transition-colors"
                >
                  <X size={16} />
                </button>
              </div>

              {/* Modal Body */}
              <div className="p-6 space-y-6 overflow-y-auto max-h-[70vh]">
                
                {/* CUSTOMER DETAIL VIEW */}
                {selectedItem.type === 'customer' && (
                  <div className="space-y-6">
                    <div className="flex items-center gap-4">
                      <div className="w-12 h-12 rounded-2xl bg-blue-500/10 border border-blue-500/20 flex items-center justify-center font-display font-semibold text-lg text-blue-400">
                        {selectedItem.name.split(' ').map(n => n[0]).join('')}
                      </div>
                      <div>
                        <h4 className="text-lg font-semibold text-white">{selectedItem.name}</h4>
                        <div className="text-xs text-slate-500 font-mono mt-0.5">ID: {selectedItem.id}</div>
                      </div>
                    </div>

                    <div className="grid grid-cols-2 gap-4 bg-slate-950/30 p-4 rounded-xl border border-slate-800/60 text-xs">
                      <div>
                        <span className="text-slate-500 block mb-1">Email Address</span>
                        <span className="font-mono text-slate-300 flex items-center gap-1.5">
                          <Mail size={12} className="text-slate-600" />
                          {selectedItem.item.email}
                        </span>
                      </div>
                      <div>
                        <span className="text-slate-500 block mb-1">Phone Number</span>
                        <span className="font-mono text-slate-300 flex items-center gap-1.5">
                          <Phone size={12} className="text-slate-600" />
                          {selectedItem.item.phone}
                        </span>
                      </div>
                      <div className="pt-2">
                        <span className="text-slate-500 block mb-1">Join Date</span>
                        <span className="text-slate-300 flex items-center gap-1.5 font-mono">
                          <Calendar size={12} className="text-slate-600" />
                          {selectedItem.item.registrationDate}
                        </span>
                      </div>
                      <div className="pt-2">
                        <span className="text-slate-500 block mb-1">Status</span>
                        <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-medium border ${
                          selectedItem.item.status === 'active'
                            ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20'
                            : 'bg-red-500/10 text-red-400 border-red-500/20'
                        }`}>
                          {selectedItem.item.status}
                        </span>
                      </div>
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                      <div className="p-4 bg-slate-950/20 border border-slate-800/60 rounded-xl text-center">
                        <span className="text-slate-500 text-xs block mb-1">Total Purchases</span>
                        <span className="text-2xl font-bold text-white font-mono">{selectedItem.item.totalOrders}</span>
                      </div>
                      <div className="p-4 bg-slate-950/20 border border-slate-800/60 rounded-xl text-center">
                        <span className="text-slate-500 text-xs block mb-1">Total Spent</span>
                        <span className="text-2xl font-bold text-emerald-400 font-mono">${selectedItem.item.totalSpent.toFixed(2)}</span>
                      </div>
                    </div>

                    {/* Action Panel */}
                    <div className="pt-4 border-t border-slate-800/60 flex items-center justify-between gap-3">
                      <div>
                        {selectedItem.item.status === 'active' ? (
                          <button
                            onClick={() => {
                              onBlockCustomer(selectedItem.id);
                              // Update local preview state
                              selectedItem.item.status = 'blocked';
                              setSelectedItem({ ...selectedItem });
                            }}
                            className="px-4 py-2 bg-red-500/10 hover:bg-red-500/20 border border-red-500/20 text-red-400 text-xs font-semibold rounded-xl flex items-center gap-1.5 transition-all"
                          >
                            <ShieldAlert size={14} /> Block Customer
                          </button>
                        ) : (
                          <button
                            onClick={() => {
                              onUnblockCustomer(selectedItem.id);
                              // Update local preview state
                              selectedItem.item.status = 'active';
                              setSelectedItem({ ...selectedItem });
                            }}
                            className="px-4 py-2 bg-emerald-500/10 hover:bg-emerald-500/20 border border-emerald-500/20 text-emerald-400 text-xs font-semibold rounded-xl flex items-center gap-1.5 transition-all"
                          >
                            <CheckCircle2 size={14} /> Unblock Customer
                          </button>
                        )}
                      </div>

                      <button
                        onClick={() => handleNavigateToTab('users')}
                        className="px-4 py-2 bg-slate-800 hover:bg-slate-750 border border-slate-700/60 text-slate-300 text-xs font-semibold rounded-xl flex items-center gap-1.5 transition-all"
                      >
                        Manage in Tab <ExternalLink size={13} className="text-slate-500" />
                      </button>
                    </div>
                  </div>
                )}

                {/* OWNER DETAIL VIEW */}
                {selectedItem.type === 'owner' && (
                  <div className="space-y-6">
                    <div className="flex items-center gap-4">
                      <div className="w-12 h-12 rounded-2xl bg-amber-500/10 border border-amber-500/20 flex items-center justify-center font-display font-semibold text-lg text-amber-400">
                        <Store size={22} />
                      </div>
                      <div>
                        <h4 className="text-lg font-semibold text-white">{selectedItem.item.shopName}</h4>
                        <div className="text-xs text-slate-400">Owner: {selectedItem.item.ownerName}</div>
                        <div className="text-[11px] text-slate-500 font-mono mt-0.5">ID: {selectedItem.id}</div>
                      </div>
                    </div>

                    <div className="grid grid-cols-2 gap-4 bg-slate-950/30 p-4 rounded-xl border border-slate-800/60 text-xs">
                      <div>
                        <span className="text-slate-500 block mb-1">Email Contact</span>
                        <span className="font-mono text-slate-300 flex items-center gap-1.5">
                          <Mail size={12} className="text-slate-600" />
                          {selectedItem.item.email}
                        </span>
                      </div>
                      <div>
                        <span className="text-slate-500 block mb-1">Phone Number</span>
                        <span className="font-mono text-slate-300 flex items-center gap-1.5">
                          <Phone size={12} className="text-slate-600" />
                          {selectedItem.item.phone}
                        </span>
                      </div>
                      <div className="pt-2">
                        <span className="text-slate-500 block mb-1">Business Registration</span>
                        <span className="text-slate-300 font-mono">
                          {selectedItem.item.businessRegNo}
                        </span>
                      </div>
                      <div className="pt-2">
                        <span className="text-slate-500 block mb-1">Status</span>
                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-[10px] font-medium border ${
                          selectedItem.item.status === 'active'
                            ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20'
                            : selectedItem.item.status === 'pending_approval'
                            ? 'bg-amber-500/10 text-amber-400 border-amber-500/20'
                            : 'bg-red-500/10 text-red-400 border-red-500/20'
                        }`}>
                          {selectedItem.item.status === 'pending_approval' ? 'Pending Approval' : selectedItem.item.status}
                        </span>
                      </div>
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                      <div className="p-4 bg-slate-950/20 border border-slate-800/60 rounded-xl text-center">
                        <span className="text-slate-500 text-xs block mb-1">Total Sales Count</span>
                        <span className="text-2xl font-bold text-white font-mono">{selectedItem.item.totalSales}</span>
                      </div>
                      <div className="p-4 bg-slate-950/20 border border-slate-800/60 rounded-xl text-center">
                        <span className="text-slate-500 text-xs block mb-1">Total Shop Revenue</span>
                        <span className="text-2xl font-bold text-amber-400 font-mono">${selectedItem.item.totalRevenue.toFixed(2)}</span>
                      </div>
                    </div>

                    {/* Action Panel */}
                    <div className="pt-4 border-t border-slate-800/60 flex items-center justify-between gap-3">
                      <div className="flex gap-2">
                        {selectedItem.item.status === 'pending_approval' && (
                          <button
                            onClick={() => {
                              onApproveOwner(selectedItem.id);
                              // Update local state
                              selectedItem.item.status = 'active';
                              setSelectedItem({ ...selectedItem });
                            }}
                            className="px-3.5 py-2 bg-emerald-500/10 hover:bg-emerald-500/20 border border-emerald-500/20 text-emerald-400 text-xs font-semibold rounded-xl flex items-center gap-1.5 transition-all"
                          >
                            <CheckCircle2 size={14} /> Approve Owner
                          </button>
                        )}

                        {selectedItem.item.status === 'active' && (
                          <button
                            onClick={() => {
                              onBlockOwner(selectedItem.id);
                              // Update local state
                              selectedItem.item.status = 'blocked';
                              setSelectedItem({ ...selectedItem });
                            }}
                            className="px-3.5 py-2 bg-red-500/10 hover:bg-red-500/20 border border-red-500/20 text-red-400 text-xs font-semibold rounded-xl flex items-center gap-1.5 transition-all"
                          >
                            <ShieldAlert size={14} /> Block Owner
                          </button>
                        )}

                        {selectedItem.item.status === 'blocked' && (
                          <button
                            onClick={() => {
                              onUnblockOwner(selectedItem.id);
                              // Update local state
                              selectedItem.item.status = 'active';
                              setSelectedItem({ ...selectedItem });
                            }}
                            className="px-3.5 py-2 bg-emerald-500/10 hover:bg-emerald-500/20 border border-emerald-500/20 text-emerald-400 text-xs font-semibold rounded-xl flex items-center gap-1.5 transition-all"
                          >
                            <CheckCircle2 size={14} /> Unblock Owner
                          </button>
                        )}
                      </div>

                      <button
                        onClick={() => handleNavigateToTab('owners')}
                        className="px-4 py-2 bg-slate-800 hover:bg-slate-750 border border-slate-700/60 text-slate-300 text-xs font-semibold rounded-xl flex items-center gap-1.5 transition-all"
                      >
                        Manage in Tab <ExternalLink size={13} className="text-slate-500" />
                      </button>
                    </div>
                  </div>
                )}

                {/* ORDER DETAIL VIEW */}
                {selectedItem.type === 'order' && (
                  <div className="space-y-6">
                    <div className="flex items-center justify-between pb-2 border-b border-slate-800">
                      <div>
                        <span className="text-xs text-slate-500 font-mono block">Order ID</span>
                        <span className="text-base font-semibold text-white font-mono">{selectedItem.id}</span>
                      </div>
                      <div className="text-right">
                        <span className="text-xs text-slate-500 block">Date Placed</span>
                        <span className="text-xs text-slate-300 font-mono">{selectedItem.item.date}</span>
                      </div>
                    </div>

                    <div className="grid grid-cols-2 gap-4 bg-slate-950/30 p-4 rounded-xl border border-slate-800/60 text-xs">
                      <div>
                        <span className="text-slate-500 block mb-1">Customer Name</span>
                        <span className="font-semibold text-slate-200">{selectedItem.item.customerName}</span>
                      </div>
                      <div>
                        <span className="text-slate-500 block mb-1">Purchased From Shop</span>
                        <span className="font-semibold text-amber-400">{selectedItem.item.shopName}</span>
                      </div>
                      <div className="pt-2">
                        <span className="text-slate-500 block mb-1">Delivery Operator</span>
                        <span className="text-slate-300 font-mono">
                          {selectedItem.item.deliveryStaffName || 'Not Assigned Yet'}
                        </span>
                      </div>
                      <div className="pt-2">
                        <span className="text-slate-500 block mb-1">Flow Status</span>
                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-[10px] font-medium border ${
                          selectedItem.item.status === 'delivered'
                            ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20'
                            : selectedItem.item.status === 'delivering'
                            ? 'bg-purple-500/10 text-purple-400 border-purple-500/20'
                            : selectedItem.item.status === 'accepted'
                            ? 'bg-blue-500/10 text-blue-400 border-blue-500/20'
                            : 'bg-amber-500/10 text-amber-400 border-amber-500/20'
                        }`}>
                          {selectedItem.item.status}
                        </span>
                      </div>
                    </div>

                    {/* Fish Items Cart */}
                    <div className="space-y-2">
                      <span className="text-xs text-slate-500 font-mono block">Order Composition</span>
                      <div className="bg-slate-950/40 border border-slate-800/80 rounded-xl overflow-hidden divide-y divide-slate-800/50">
                        {selectedItem.item.fishItems.map((item, index) => (
                          <div key={index} className="p-3 flex items-center justify-between text-xs">
                            <div className="flex items-center gap-2">
                              <Package size={13} className="text-slate-500" />
                              <span className="text-slate-200 font-medium">{item.name}</span>
                            </div>
                            <div className="font-mono text-slate-400">
                              {item.quantityKg}kg @ ${item.pricePerKg.toFixed(2)}/kg
                            </div>
                            <div className="font-mono font-semibold text-white">
                              ${(item.quantityKg * item.pricePerKg).toFixed(2)}
                            </div>
                          </div>
                        ))}
                        <div className="p-3 bg-slate-950/20 flex items-center justify-between text-xs font-semibold">
                          <span className="text-slate-400">Total Sum</span>
                          <span className="text-emerald-400 text-sm font-bold font-mono">
                            ${selectedItem.item.totalAmount.toFixed(2)}
                          </span>
                        </div>
                      </div>
                    </div>

                    {/* Quick Order Actions */}
                    <div className="pt-4 border-t border-slate-800/60 flex items-center justify-between gap-3">
                      <div className="flex gap-2">
                        {selectedItem.item.status === 'pending' && (
                          <button
                            onClick={() => {
                              onUpdateOrderStatus(selectedItem.id, 'accepted');
                              selectedItem.item.status = 'accepted';
                              setSelectedItem({ ...selectedItem });
                            }}
                            className="px-3 py-1.5 bg-blue-500/10 hover:bg-blue-500/20 border border-blue-500/20 text-blue-400 text-[11px] font-semibold rounded-lg flex items-center gap-1 transition-all"
                          >
                            Accept Order
                          </button>
                        )}

                        {selectedItem.item.status === 'accepted' && (
                          <button
                            onClick={() => {
                              // Assign a random active driver if available
                              const drivers = deliveryStaff.filter(d => d.status === 'active');
                              const driverName = drivers.length > 0 ? drivers[0].name : 'Ecosystem Courier';
                              onUpdateOrderStatus(selectedItem.id, 'delivering', driverName);
                              selectedItem.item.status = 'delivering';
                              selectedItem.item.deliveryStaffName = driverName;
                              setSelectedItem({ ...selectedItem });
                            }}
                            className="px-3 py-1.5 bg-purple-500/10 hover:bg-purple-500/20 border border-purple-500/20 text-purple-400 text-[11px] font-semibold rounded-lg flex items-center gap-1 transition-all"
                          >
                            Dispatch Order
                          </button>
                        )}

                        {selectedItem.item.status === 'delivering' && (
                          <button
                            onClick={() => {
                              onSettleFinancials(selectedItem.item);
                              selectedItem.item.status = 'delivered';
                              setSelectedItem({ ...selectedItem });
                            }}
                            className="px-3 py-1.5 bg-emerald-500/10 hover:bg-emerald-500/20 border border-emerald-500/20 text-emerald-400 text-[11px] font-semibold rounded-lg flex items-center gap-1 transition-all"
                          >
                            Mark Delivered
                          </button>
                        )}
                      </div>

                      <button
                        onClick={() => handleNavigateToTab('simulator')}
                        className="px-4 py-2 bg-slate-800 hover:bg-slate-750 border border-slate-700/60 text-slate-300 text-xs font-semibold rounded-xl flex items-center gap-1.5 transition-all"
                      >
                        Manage in Simulator <ExternalLink size={13} className="text-slate-500" />
                      </button>
                    </div>

                  </div>
                )}

              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>
    </div>
  );
}
