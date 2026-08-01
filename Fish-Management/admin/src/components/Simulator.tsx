/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState } from 'react';
import { 
  Activity, 
  ShoppingBag, 
  CheckSquare, 
  Truck, 
  MapPin, 
  CheckCircle, 
  AlertTriangle, 
  UserPlus,
  ArrowRight,
  Terminal,
  RotateCcw,
  BookOpen
} from 'lucide-react';
import { Customer, Owner, DeliveryStaff, Order, SalesReportEntry, Complaint } from '../types';

interface SimulatorProps {
  customers: Customer[];
  owners: Owner[];
  deliveryStaff: DeliveryStaff[];
  orders: Order[];
  salesReport: SalesReportEntry[];
  complaints: Complaint[];
  
  onSimulateOrder: (order: Order) => void;
  onUpdateOrderStatus: (orderId: string, status: Order['status'], driverName?: string) => void;
  onSettleFinancials: (order: Order) => void;
  onAddPendingOwner: (owner: Owner) => void;
  onAddComplaint: (complaint: Complaint) => void;
}

interface LogEntry {
  timestamp: string;
  type: 'info' | 'success' | 'warn' | 'error';
  message: string;
}

export default function Simulator({
  customers,
  owners,
  deliveryStaff,
  orders,
  salesReport,
  complaints,
  
  onSimulateOrder,
  onUpdateOrderStatus,
  onSettleFinancials,
  onAddPendingOwner,
  onAddComplaint
}: SimulatorProps) {

  // Console Logs
  const [logs, setLogs] = useState<LogEntry[]>([
    { timestamp: '07:29:54 AM', type: 'info', message: 'Simulator initialized.' },
    { timestamp: '07:30:00 AM', type: 'success', message: 'Ready. 5 customers, 4 merchants, 4 courier slots mapped.' }
  ]);

  const addLog = (message: string, type: LogEntry['type'] = 'info') => {
    const now = new Date();
    const timeString = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
    setLogs(prev => [...prev, { timestamp: timeString, type, message }]);
  };

  const clearLogs = () => {
    setLogs([{ timestamp: new Date().toLocaleTimeString(), type: 'info', message: 'Console logs cleared.' }]);
  };

  // 1. Simulate Order Placement
  const handleSimulateOrder = () => {
    const activeCusts = customers.filter(c => c.status === 'active');
    const activeMerchants = owners.filter(o => o.status === 'active');

    if (activeCusts.length === 0 || activeMerchants.length === 0) {
      addLog('Cannot place order: No active customers or active shop merchants found!', 'error');
      return;
    }

    // Select random customer and shop
    const randomCustomer = activeCusts[Math.floor(Math.random() * activeCusts.length)];
    const randomOwner = activeMerchants[Math.floor(Math.random() * activeMerchants.length)];

    // Define random fish items pool
    const fishPool = [
      { name: 'Atlantic Salmon', price: 18.00 },
      { name: 'Yellowfin Tuna', price: 24.00 },
      { name: 'Tiger Shrimp', price: 22.00 },
      { name: 'Red Snapper', price: 16.00 },
      { name: 'Sea Bass', price: 20.00 }
    ];

    // Pick 1 or 2 items
    const numItems = Math.random() > 0.5 ? 2 : 1;
    const fishItems: Array<{ name: string; quantityKg: number; pricePerKg: number }> = [];
    let totalAmount = 0;

    for (let i = 0; i < numItems; i++) {
      const fish = fishPool[Math.floor(Math.random() * fishPool.length)];
      // Prevent duplicates
      if (fishItems.some(item => item.name === fish.name)) continue;
      
      const quantityKg = Math.round((Math.random() * 3 + 1) * 2) / 2; // 1 to 4 kg in 0.5 steps
      fishItems.push({
        name: fish.name,
        quantityKg,
        pricePerKg: fish.price
      });
      totalAmount += quantityKg * fish.price;
    }

    const orderId = `ORD-${Math.floor(Math.random() * 9000 + 1000)}`;
    const newOrder: Order = {
      id: orderId,
      customerName: randomCustomer.name,
      shopName: randomOwner.shopName,
      fishItems,
      totalAmount: Number(totalAmount.toFixed(2)),
      status: 'pending',
      date: new Date().toISOString().split('T')[0]
    };

    onSimulateOrder(newOrder);
    addLog(`[CUSTOMER APP] ${randomCustomer.name} placed a new order ${orderId} at "${randomOwner.shopName}". Total: $${totalAmount.toFixed(2)}`, 'success');
  };

  // 2. Simulate Shop Owner Accepting & Assigning Delivery
  const handleSimulateAccept = () => {
    const pendingOrders = orders.filter(o => o.status === 'pending');
    if (pendingOrders.length === 0) {
      addLog('Accept Action Rejected: No "pending" orders available to accept.', 'warn');
      return;
    }

    const targetOrder = pendingOrders[0];
    const activeDrivers = deliveryStaff.filter(d => d.status === 'active');
    if (activeDrivers.length === 0) {
      addLog(`Cannot accept ${targetOrder.id}: No active courier drivers on duty!`, 'error');
      return;
    }

    // Pick random driver
    const randomDriver = activeDrivers[Math.floor(Math.random() * activeDrivers.length)];
    onUpdateOrderStatus(targetOrder.id, 'accepted', randomDriver.name);
    addLog(`[OWNER APP] Merchant accepted order ${targetOrder.id} and assigned dispatch driver ${randomDriver.name}.`, 'success');
  };

  // 3. Simulate Driver Pickup
  const handleSimulateTransit = () => {
    const acceptedOrders = orders.filter(o => o.status === 'accepted');
    if (acceptedOrders.length === 0) {
      addLog('Transit Action Rejected: No "accepted" orders ready for pickup.', 'warn');
      return;
    }

    const targetOrder = acceptedOrders[0];
    onUpdateOrderStatus(targetOrder.id, 'delivering');
    addLog(`[DRIVER APP] Courier ${targetOrder.deliveryStaffName} picked up order ${targetOrder.id}. Cargo is currently in transit!`, 'info');
  };

  // 4. Simulate Delivery Completion & Settle Ledger
  const handleSimulateDelivery = () => {
    const transitOrders = orders.filter(o => o.status === 'delivering');
    if (transitOrders.length === 0) {
      addLog('Delivery Settle Rejected: No orders are currently in "delivering" transit status.', 'warn');
      return;
    }

    const targetOrder = transitOrders[0];
    
    // updates customer spent, merchant sales, courier deliveries, and system reports!
    onSettleFinancials(targetOrder);
    addLog(`[DELIVERY SUCCESS] Order ${targetOrder.id} delivered safely to ${targetOrder.customerName}!`, 'success');
    addLog(`[LEDGER] Settlement logged! Shop "${targetOrder.shopName}" credited $${(targetOrder.totalAmount * 0.9).toFixed(2)}. Platform collected $${(targetOrder.totalAmount * 0.1).toFixed(2)} commission.`, 'success');
    addLog('[REPORTS] Analytics daily sales ledger updated dynamically.', 'info');
  };

  // 5. Simulate Random Owner Registration Request
  const handleSimulateNewOwner = () => {
    const shopsPool = [
      { name: 'Salty Waves Catch', owner: 'Barnaby Finch', reg: 'FSH-8822-S' },
      { name: 'Bayview Crabs & Fish', owner: 'Clara Oswald', reg: 'FSH-4120-M' },
      { name: 'Glacial Premium Salmon', owner: 'Ivar Ragnarson', reg: 'FSH-7109-G' }
    ];

    const pick = shopsPool[Math.floor(Math.random() * shopsPool.length)];
    // Check if shop already registered
    if (owners.some(o => o.shopName === pick.name)) {
      addLog(`Registration rejected: "${pick.name}" is already registered or has a pending file!`, 'warn');
      return;
    }

    const id = `OWN-${Math.floor(Math.random() * 900 + 100)}`;
    const newOwner: Owner = {
      id,
      shopName: pick.name,
      ownerName: pick.owner,
      email: `${pick.owner.toLowerCase().replace(' ', '.')}@example.com`,
      phone: `+1 (555) ${Math.floor(Math.random() * 900 + 100)}-9811`,
      status: 'pending_approval',
      totalSales: 0,
      totalRevenue: 0.00,
      businessRegNo: pick.reg,
      registrationDate: new Date().toISOString().split('T')[0]
    };

    onAddPendingOwner(newOwner);
    addLog(`[REGISTRATION API] New shop owner application submitted for "${pick.name}" by applicant ${pick.owner}. Assigned ID: ${id}`, 'info');
  };

  // 6. Simulate New Customer Complaint
  const handleSimulateComplaint = () => {
    const titles = [
      'Fish quality issue',
      'Cold package, lack of ice',
      'Driver did not drop off at doorstep',
      'Missing red snapper fillet'
    ];
    const desc = [
      'The sea bass fillets smelled somewhat warm upon courier drop-off. Packaging could have used more ice packs.',
      'Order arrived missing 1kg of the requested Red Snapper. I was charged for the full bundle.',
      'Driver left the delivery bag by the community gate instead of walking up to Apt 12C. Extremely disappointed.',
      'We received 2kg of cod instead of the requested salmon fillets. Please rectify.'
    ];

    const idx = Math.floor(Math.random() * titles.length);
    const activeCusts = customers.filter(c => c.status === 'active');
    if (activeCusts.length === 0) return;
    const customer = activeCusts[Math.floor(Math.random() * activeCusts.length)];

    const id = `CMP-${Math.floor(Math.random() * 900 + 100)}`;
    const newComplaint: Complaint = {
      id,
      senderType: 'customer',
      senderName: customer.name,
      title: titles[idx],
      description: desc[idx],
      status: 'pending',
      date: new Date().toISOString().split('T')[0]
    };

    onAddComplaint(newComplaint);
    addLog(`[COMPLAINT GATEWAY] Customer ${customer.name} opened ticket ${id}: "${titles[idx]}".`, 'warn');
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h2 className="text-xl font-display font-semibold text-white tracking-tight flex items-center gap-2">
          <Activity className="text-cyan-400" size={20} />
          Workflow Simulator
        </h2>
        <p className="text-xs text-slate-400">Trigger simulated order lifecycle events to test real-time updates across customer accounts, merchant shops, and delivery riders</p>
      </div>

      {/* Workflow Visual Timeline */}
      <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl space-y-4">
        <h3 className="text-xs font-mono uppercase text-slate-500">Active Pipeline Visualizer</h3>
        
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4 relative">
          
          {/* Step 1 */}
          <div className="bg-slate-800/40 border border-slate-800 p-4 rounded-xl space-y-1.5 relative">
            <div className="flex items-center justify-between">
              <span className="w-5 h-5 bg-blue-500/20 text-blue-400 border border-blue-500/30 rounded-full flex items-center justify-center text-xs font-bold font-mono">1</span>
              <span className="text-[10px] font-mono text-slate-500">CONSUMER</span>
            </div>
            <h4 className="text-xs font-semibold text-slate-200">Customer Places Order</h4>
            <p className="text-[10px] text-slate-400">Consumers choose fish and buy. Order logs in pending pool.</p>
            <span className="absolute -right-3 top-1/2 -translate-y-1/2 hidden md:block text-slate-600">
              <ArrowRight size={14} />
            </span>
          </div>

          {/* Step 2 */}
          <div className="bg-slate-800/40 border border-slate-800 p-4 rounded-xl space-y-1.5 relative">
            <div className="flex items-center justify-between">
              <span className="w-5 h-5 bg-amber-500/20 text-amber-400 border border-amber-500/30 rounded-full flex items-center justify-center text-xs font-bold font-mono">2</span>
              <span className="text-[10px] font-mono text-slate-500">MERCHANT</span>
            </div>
            <h4 className="text-xs font-semibold text-slate-200">Owner Accepts & Assigns</h4>
            <p className="text-[10px] text-slate-400">Seafood shop accepts and packs. Driver is booked by sector.</p>
            <span className="absolute -right-3 top-1/2 -translate-y-1/2 hidden md:block text-slate-600">
              <ArrowRight size={14} />
            </span>
          </div>

          {/* Step 3 */}
          <div className="bg-slate-800/40 border border-slate-800 p-4 rounded-xl space-y-1.5 relative">
            <div className="flex items-center justify-between">
              <span className="w-5 h-5 bg-purple-500/20 text-purple-400 border border-purple-500/30 rounded-full flex items-center justify-center text-xs font-bold font-mono">3</span>
              <span className="text-[10px] font-mono text-slate-500">LOGISTICS</span>
            </div>
            <h4 className="text-xs font-semibold text-slate-200">Courier Transit</h4>
            <p className="text-[10px] text-slate-400">Courier picks up insulated cold box and sets navigation path.</p>
            <span className="absolute -right-3 top-1/2 -translate-y-1/2 hidden md:block text-slate-600">
              <ArrowRight size={14} />
            </span>
          </div>

          {/* Step 4 */}
          <div className="bg-slate-800/40 border border-slate-800 p-4 rounded-xl space-y-1.5">
            <div className="flex items-center justify-between">
              <span className="w-5 h-5 bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 rounded-full flex items-center justify-center text-xs font-bold font-mono">4</span>
              <span className="text-[10px] font-mono text-slate-500">SETTLEMENT</span>
            </div>
            <h4 className="text-xs font-semibold text-slate-200">Ledger & Khatha Settled</h4>
            <p className="text-[10px] text-slate-400">Safe handoff. Cash/card splits register and system reports update.</p>
          </div>

        </div>
      </div>

      {/* Main Grid: Control Station vs Console Logs */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        
        {/* Trigger Buttons Control Panel (takes 5 cols) */}
        <div className="lg:col-span-5 bg-slate-900 border border-slate-800 rounded-2xl p-5 shadow-xl space-y-4">
          <div className="pb-3 border-b border-slate-800 flex justify-between items-center">
            <h3 className="font-display font-semibold text-white text-sm">Flow Triggers</h3>
            <span className="text-[10px] text-slate-500 font-mono">CONTROLS</span>
          </div>

          <div className="space-y-3.5">
            
            {/* Step 1 Button */}
            <button
              onClick={handleSimulateOrder}
              className="w-full p-3.5 bg-slate-800/70 hover:bg-slate-800 border border-slate-700/60 rounded-xl flex items-center gap-3.5 text-left transition-colors group"
            >
              <div className="p-2.5 bg-blue-500/10 text-blue-400 rounded-xl group-hover:scale-105 transition-transform">
                <ShoppingBag size={18} />
              </div>
              <div>
                <h4 className="text-xs font-bold text-white flex items-center gap-1.5">
                  1. Simulate Order Placement
                </h4>
                <p className="text-[10px] text-slate-400 mt-0.5">Places a random consumer order into the pending stack</p>
              </div>
            </button>

            {/* Step 2 Button */}
            <button
              onClick={handleSimulateAccept}
              className="w-full p-3.5 bg-slate-800/70 hover:bg-slate-800 border border-slate-700/60 rounded-xl flex items-center gap-3.5 text-left transition-colors group"
            >
              <div className="p-2.5 bg-amber-500/10 text-amber-400 rounded-xl group-hover:scale-105 transition-transform">
                <CheckSquare size={18} />
              </div>
              <div>
                <h4 className="text-xs font-bold text-white">
                  2. Simulate Shop Owner Acceptance
                </h4>
                <p className="text-[10px] text-slate-400 mt-0.5">Finds a pending order, accepts it, and books a rider</p>
              </div>
            </button>

            {/* Step 3 Button */}
            <button
              onClick={handleSimulateTransit}
              className="w-full p-3.5 bg-slate-800/70 hover:bg-slate-800 border border-slate-700/60 rounded-xl flex items-center gap-3.5 text-left transition-colors group"
            >
              <div className="p-2.5 bg-purple-500/10 text-purple-400 rounded-xl group-hover:scale-105 transition-transform">
                <Truck size={18} />
              </div>
              <div>
                <h4 className="text-xs font-bold text-white">
                  3. Simulate Driver Pickup Transit
                </h4>
                <p className="text-[10px] text-slate-400 mt-0.5">Launches dispatch rider onto navigation map routing</p>
              </div>
            </button>

            {/* Step 4 Button */}
            <button
              onClick={handleSimulateDelivery}
              className="w-full p-3.5 bg-slate-800/70 hover:bg-slate-800 border border-slate-700/60 rounded-xl flex items-center gap-3.5 text-left transition-colors group"
            >
              <div className="p-2.5 bg-emerald-500/10 text-emerald-400 rounded-xl group-hover:scale-105 transition-transform">
                <CheckCircle size={18} />
              </div>
              <div>
                <h4 className="text-xs font-bold text-white">
                  4. Simulate Settle & Delivery Met
                </h4>
                <p className="text-[10px] text-slate-400 mt-0.5">Closes loop: charges customer, payouts store, updates reports</p>
              </div>
            </button>

            {/* Secondary Triggers */}
            <div className="grid grid-cols-2 gap-2 pt-2">
              <button
                onClick={handleSimulateNewOwner}
                className="py-2.5 bg-slate-850 hover:bg-slate-800 border border-slate-800 rounded-xl text-[10px] font-bold text-slate-200 flex flex-col items-center justify-center gap-1 transition-colors group"
              >
                <UserPlus size={14} className="text-cyan-400 group-hover:scale-110 transition-transform" />
                Apply New Owner
              </button>
              
              <button
                onClick={handleSimulateComplaint}
                className="py-2.5 bg-slate-850 hover:bg-slate-800 border border-slate-800 rounded-xl text-[10px] font-bold text-slate-200 flex flex-col items-center justify-center gap-1 transition-colors group"
              >
                <AlertTriangle size={14} className="text-amber-500 group-hover:scale-110 transition-transform" />
                Trigger Customer Dispute
              </button>
            </div>

          </div>
        </div>

        {/* Live Simulation Terminal Console (takes 7 cols) */}
        <div className="lg:col-span-7 bg-slate-950 border border-slate-800 rounded-2xl p-5 flex flex-col shadow-xl h-[480px]">
          <div className="pb-3 border-b border-slate-800/80 flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Terminal size={14} className="text-cyan-400" />
              <h3 className="font-mono text-xs font-semibold text-white tracking-wider">CONSOLE_LOGS</h3>
            </div>
            <button
              onClick={clearLogs}
              className="text-[10px] text-slate-500 hover:text-slate-300 font-mono flex items-center gap-1 transition-colors"
            >
              <RotateCcw size={11} /> CLEAR
            </button>
          </div>

          {/* Console Text Window */}
          <div className="flex-1 overflow-y-auto font-mono text-[11px] space-y-2 py-4 pr-2 text-left">
            {logs.map((log, i) => (
              <div key={i} className="flex gap-2.5 leading-relaxed">
                <span className="text-slate-600 shrink-0 select-none">[{log.timestamp}]</span>
                <span className={`shrink-0 select-none font-bold ${
                  log.type === 'success' ? 'text-emerald-500' :
                  log.type === 'warn' ? 'text-amber-500' :
                  log.type === 'error' ? 'text-red-500' : 'text-cyan-400'
                }`}>
                  [{log.type.toUpperCase()}]
                </span>
                <span className={log.type === 'error' ? 'text-red-300' : 'text-slate-300'}>
                  {log.message}
                </span>
              </div>
            ))}
          </div>

          <div className="pt-3 border-t border-slate-800/80 flex items-center justify-between text-[10px] font-mono text-slate-500">
            <span>Buffer: {logs.length}/500 lines</span>
            <span>Encoding: UTF-8</span>
          </div>
        </div>

      </div>
    </div>
  );
}
