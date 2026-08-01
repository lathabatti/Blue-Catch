/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'motion/react';
import { 
  ShieldCheck, 
  Database, 
  HelpCircle,
  ExternalLink,
  LogOut
} from 'lucide-react';

import Sidebar from './components/Sidebar';
import AdminDashboard from './components/AdminDashboard';
import UserManagement from './components/UserManagement';
import OwnerManagement from './components/OwnerManagement';
import DeliveryManagement from './components/DeliveryManagement';
import ReportsAnalytics from './components/ReportsAnalytics';
import ComplaintManagement from './components/ComplaintManagement';
import NotificationPanel from './components/NotificationPanel';
import SystemSettings from './components/SystemSettings';
import Simulator from './components/Simulator';
import GlobalSearch from './components/GlobalSearch';
import AuthPage from './components/AuthPage';

import { 
  initialCustomers, 
  initialOwners, 
  initialDeliveryStaff, 
  initialOrders, 
  initialComplaints, 
  initialNotifications, 
  defaultSettings, 
  initialSalesReport, 
  initialStock 
} from './data/mockData';

import { 
  Customer, 
  Owner, 
  DeliveryStaff, 
  Order, 
  Complaint, 
  SystemNotification, 
  SystemSettings as SettingsType, 
  SalesReportEntry, 
  StockItem,
  AdminUser
} from './types';

export default function App() {
  const [activeTab, setActiveTab] = useState('dashboard');

  // Authenticated admin profile session state
  const [currentUser, setCurrentUser] = useState<AdminUser | null>(() => {
    const saved = localStorage.getItem('fish_logged_in_user');
    return saved ? JSON.parse(saved) : null;
  });

  const handleLogout = () => {
    localStorage.removeItem('fish_logged_in_user');
    setCurrentUser(null);
  };

  // Load state from localStorage with fallbacks
  const [customers, setCustomers] = useState<Customer[]>(() => {
    const saved = localStorage.getItem('fish_customers');
    return saved ? JSON.parse(saved) : initialCustomers;
  });

  const [owners, setOwners] = useState<Owner[]>(() => {
    const saved = localStorage.getItem('fish_owners');
    return saved ? JSON.parse(saved) : initialOwners;
  });

  const [deliveryStaff, setDeliveryStaff] = useState<DeliveryStaff[]>(() => {
    const saved = localStorage.getItem('fish_delivery');
    return saved ? JSON.parse(saved) : initialDeliveryStaff;
  });

  const [orders, setOrders] = useState<Order[]>(() => {
    const saved = localStorage.getItem('fish_orders');
    return saved ? JSON.parse(saved) : initialOrders;
  });

  const [complaints, setComplaints] = useState<Complaint[]>(() => {
    const saved = localStorage.getItem('fish_complaints');
    return saved ? JSON.parse(saved) : initialComplaints;
  });

  const [notifications, setNotifications] = useState<SystemNotification[]>(() => {
    const saved = localStorage.getItem('fish_notifications');
    return saved ? JSON.parse(saved) : initialNotifications;
  });

  const [settings, setSettings] = useState<SettingsType>(() => {
    const saved = localStorage.getItem('fish_settings');
    return saved ? JSON.parse(saved) : defaultSettings;
  });

  const [salesReport, setSalesReport] = useState<SalesReportEntry[]>(() => {
    const saved = localStorage.getItem('fish_reports');
    return saved ? JSON.parse(saved) : initialSalesReport;
  });

  const [stock, setStock] = useState<StockItem[]>(() => {
    const saved = localStorage.getItem('fish_stock');
    return saved ? JSON.parse(saved) : initialStock;
  });

  // Persist states to localStorage when they change
  useEffect(() => {
    localStorage.setItem('fish_customers', JSON.stringify(customers));
  }, [customers]);

  useEffect(() => {
    localStorage.setItem('fish_owners', JSON.stringify(owners));
  }, [owners]);

  useEffect(() => {
    localStorage.setItem('fish_delivery', JSON.stringify(deliveryStaff));
  }, [deliveryStaff]);

  useEffect(() => {
    localStorage.setItem('fish_orders', JSON.stringify(orders));
  }, [orders]);

  useEffect(() => {
    localStorage.setItem('fish_complaints', JSON.stringify(complaints));
  }, [complaints]);

  useEffect(() => {
    localStorage.setItem('fish_notifications', JSON.stringify(notifications));
  }, [notifications]);

  useEffect(() => {
    localStorage.setItem('fish_settings', JSON.stringify(settings));
  }, [settings]);

  useEffect(() => {
    localStorage.setItem('fish_reports', JSON.stringify(salesReport));
  }, [salesReport]);

  useEffect(() => {
    localStorage.setItem('fish_stock', JSON.stringify(stock));
  }, [stock]);

  useEffect(() => {
    if (currentUser) {
      localStorage.setItem('fish_logged_in_user', JSON.stringify(currentUser));
    } else {
      localStorage.removeItem('fish_logged_in_user');
    }
  }, [currentUser]);

  // Handler functions for entity management
  
  // Customers
  const handleBlockCustomer = (id: string) => {
    setCustomers(prev => prev.map(c => c.id === id ? { ...c, status: 'blocked' } : c));
  };

  const handleUnblockCustomer = (id: string) => {
    setCustomers(prev => prev.map(c => c.id === id ? { ...c, status: 'active' } : c));
  };

  const handleDeleteCustomer = (id: string) => {
    setCustomers(prev => prev.filter(c => c.id !== id));
  };

  // Owners
  const handleApproveOwner = (id: string) => {
    setOwners(prev => prev.map(o => o.id === id ? { ...o, status: 'active' } : o));
  };

  const handleBlockOwner = (id: string) => {
    setOwners(prev => prev.map(o => o.id === id ? { ...o, status: 'blocked' } : o));
  };

  const handleUnblockOwner = (id: string) => {
    setOwners(prev => prev.map(o => o.id === id ? { ...o, status: 'active' } : o));
  };

  const handleAddPendingOwner = (newOwner: Owner) => {
    setOwners(prev => [...prev, newOwner]);
  };

  // Delivery Staff
  const handleAddDeliveryStaff = (staff: Omit<DeliveryStaff, 'id' | 'totalDeliveries' | 'rating'>) => {
    const id = `DEL-${Math.floor(Math.random() * 900 + 100)}`;
    const newStaff: DeliveryStaff = {
      ...staff,
      id,
      totalDeliveries: 0,
      rating: 5.0
    };
    setDeliveryStaff(prev => [...prev, newStaff]);
  };

  const handleUpdateDeliveryStaff = (updated: DeliveryStaff) => {
    setDeliveryStaff(prev => prev.map(d => d.id === updated.id ? updated : d));
  };

  const handleRemoveDeliveryStaff = (id: string) => {
    setDeliveryStaff(prev => prev.filter(d => d.id !== id));
  };

  // Complaints
  const handleResolveComplaint = (id: string, resolution: string) => {
    setComplaints(prev => prev.map(c => c.id === id ? { ...c, status: 'resolved', resolution } : c));
  };

  const handleAddComplaint = (newComplaint: Complaint) => {
    setComplaints(prev => [...prev, newComplaint]);
  };

  // Notifications
  const handleSendNotification = (ntf: Omit<SystemNotification, 'id' | 'date' | 'status'>) => {
    const id = `NTF-${Math.floor(Math.random() * 900 + 100)}`;
    const newNtf: SystemNotification = {
      ...ntf,
      id,
      date: new Date().toISOString().split('T')[0],
      status: 'sent'
    };
    setNotifications(prev => [...prev, newNtf]);
  };

  // Settings
  const handleSaveSettings = (updated: SettingsType) => {
    setSettings(updated);
  };

  // Simulator Callbacks
  const handleSimulateOrder = (newOrder: Order) => {
    setOrders(prev => [newOrder, ...prev]);
  };

  const handleUpdateOrderStatus = (orderId: string, status: Order['status'], driverName?: string) => {
    setOrders(prev => prev.map(o => {
      if (o.id === orderId) {
        return {
          ...o,
          status,
          ...(driverName ? { deliveryStaffName: driverName } : {})
        };
      }
      return o;
    }));
  };

  const handleSettleFinancials = (deliveredOrder: Order) => {
    // 1. Set status to delivered
    setOrders(prev => prev.map(o => o.id === deliveredOrder.id ? { ...o, status: 'delivered' } : o));

    // 2. Increment Customer stats
    setCustomers(prev => prev.map(c => {
      if (c.name === deliveredOrder.customerName) {
        return {
          ...c,
          totalOrders: c.totalOrders + 1,
          totalSpent: Number((c.totalSpent + deliveredOrder.totalAmount).toFixed(2))
        };
      }
      return c;
    }));

    // 3. Increment Owner stats
    setOwners(prev => prev.map(o => {
      if (o.shopName === deliveredOrder.shopName) {
        return {
          ...o,
          totalSales: o.totalSales + 1,
          totalRevenue: Number((o.totalRevenue + deliveredOrder.totalAmount).toFixed(2))
        };
      }
      return o;
    }));

    // 4. Increment Delivery driver stats
    if (deliveredOrder.deliveryStaffName) {
      setDeliveryStaff(prev => prev.map(d => {
        if (d.name === deliveredOrder.deliveryStaffName) {
          return {
            ...d,
            totalDeliveries: d.totalDeliveries + 1
          };
        }
        return d;
      }));
    }

    // 5. Update sales reports entry for today
    const today = new Date().toISOString().split('T')[0];
    setSalesReport(prev => {
      const todayExists = prev.find(e => e.date === today);
      if (todayExists) {
        return prev.map(e => e.date === today ? {
          ...e,
          sales: e.sales + deliveredOrder.totalAmount,
          profit: e.profit + (deliveredOrder.totalAmount * (settings.commissionRate / 100))
        } : e);
      } else {
        return [...prev, {
          date: today,
          sales: deliveredOrder.totalAmount,
          purchases: Math.round(deliveredOrder.totalAmount * 0.7 * 100) / 100, // assume 70% cost
          profit: Math.round(deliveredOrder.totalAmount * (settings.commissionRate / 100) * 100) / 100
        }];
      }
    });

    // 6. Reduce stock levels of the ordered items
    setStock(prev => {
      return prev.map(s => {
        const matchingItem = deliveredOrder.fishItems.find(item => item.name === s.name);
        if (matchingItem) {
          const newStock = Math.max(s.stockKg - matchingItem.quantityKg, 0);
          return {
            ...s,
            stockKg: newStock,
            status: newStock === 0 ? 'out_of_stock' : newStock <= 15 ? 'low_stock' : 'in_stock'
          };
        }
        return s;
      });
    });
  };

  const pendingOwnersCount = owners.filter(o => o.status === 'pending_approval').length;
  const pendingComplaintsCount = complaints.filter(c => c.status === 'pending').length;

  if (!currentUser) {
    return (
      <AuthPage 
        onLoginSuccess={(user) => setCurrentUser(user)} 
        defaultEmail="sanjusmily128@gmail.com"
      />
    );
  }

  return (
    <div className="flex h-screen w-full bg-slate-950 font-sans text-slate-300 overflow-hidden select-none">
      
      {/* Sidebar Navigation */}
      <Sidebar 
        activeTab={activeTab} 
        setActiveTab={setActiveTab}
        pendingOwnersCount={pendingOwnersCount}
        pendingComplaintsCount={pendingComplaintsCount}
        appName={settings.appName}
        currentUser={currentUser}
        onLogout={handleLogout}
      />

      {/* Main Workspace */}
      <main className="flex-1 flex flex-col h-screen overflow-hidden bg-slate-950">
        
        {/* Workspace Top Bar Header */}
        <header className="h-16 border-b border-slate-900 px-8 flex items-center justify-between shrink-0 bg-slate-900/10">
          <div className="flex items-center gap-2">
            <span className="text-xs text-slate-400 font-medium">
              Admin Portal
            </span>
            <span className="hidden sm:inline text-xs text-slate-700 font-mono">/</span>
            <span className="hidden sm:inline text-xs text-slate-500 font-mono">
              {activeTab.charAt(0).toUpperCase() + activeTab.slice(1)}
            </span>
          </div>

          <GlobalSearch 
            customers={customers}
            owners={owners}
            orders={orders}
            deliveryStaff={deliveryStaff}
            onBlockCustomer={handleBlockCustomer}
            onUnblockCustomer={handleUnblockCustomer}
            onApproveOwner={handleApproveOwner}
            onBlockOwner={handleBlockOwner}
            onUnblockOwner={handleUnblockOwner}
            onUpdateOrderStatus={handleUpdateOrderStatus}
            onSettleFinancials={handleSettleFinancials}
            setActiveTab={setActiveTab}
          />

          <div className="flex items-center gap-4 text-xs font-mono">
            <div className="hidden lg:flex items-center gap-1.5">
              <span className="text-slate-300 font-semibold">{currentUser.name}</span>
              <span className="text-slate-500">({currentUser.email})</span>
            </div>
            <span className="w-px h-4 bg-slate-800" />
            <button
              onClick={handleLogout}
              title="Sign out of system control"
              className="px-2.5 py-1 text-slate-400 hover:text-red-400 bg-slate-900 hover:bg-red-500/10 rounded-lg border border-slate-800 hover:border-red-500/25 flex items-center gap-1.5 transition-all"
            >
              <LogOut size={12} /> Sign Out
            </button>
          </div>
        </header>

        {/* Content canvas */}
        <div className="flex-1 overflow-y-auto p-8 bg-slate-950/40 relative">
          
          <AnimatePresence mode="wait">
            <motion.div
              key={activeTab}
              initial={{ opacity: 0, y: 4 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -4 }}
              transition={{ duration: 0.15 }}
              className="h-full"
            >
              {activeTab === 'dashboard' && (
                <AdminDashboard 
                  customers={customers}
                  owners={owners}
                  deliveryStaff={deliveryStaff}
                  orders={orders}
                  settings={settings}
                  setActiveTab={setActiveTab}
                />
              )}

              {activeTab === 'users' && (
                <UserManagement 
                  customers={customers}
                  onBlockCustomer={handleBlockCustomer}
                  onUnblockCustomer={handleUnblockCustomer}
                  onDeleteCustomer={handleDeleteCustomer}
                />
              )}

              {activeTab === 'owners' && (
                <OwnerManagement 
                  owners={owners}
                  onApproveOwner={handleApproveOwner}
                  onBlockOwner={handleBlockOwner}
                  onUnblockOwner={handleUnblockOwner}
                />
              )}

              {activeTab === 'delivery' && (
                <DeliveryManagement 
                  deliveryStaff={deliveryStaff}
                  onAddDeliveryStaff={handleAddDeliveryStaff}
                  onUpdateDeliveryStaff={handleUpdateDeliveryStaff}
                  onRemoveDeliveryStaff={handleRemoveDeliveryStaff}
                />
              )}

              {activeTab === 'reports' && (
                <ReportsAnalytics 
                  salesReport={salesReport}
                  stock={stock}
                />
              )}

              {activeTab === 'complaints' && (
                <ComplaintManagement 
                  complaints={complaints}
                  onResolveComplaint={handleResolveComplaint}
                />
              )}

              {activeTab === 'notifications' && (
                <NotificationPanel 
                  notifications={notifications}
                  onSendNotification={handleSendNotification}
                />
              )}

              {activeTab === 'settings' && (
                <SystemSettings 
                  settings={settings}
                  onSaveSettings={handleSaveSettings}
                />
              )}

              {activeTab === 'simulator' && (
                <Simulator 
                  customers={customers}
                  owners={owners}
                  deliveryStaff={deliveryStaff}
                  orders={orders}
                  salesReport={salesReport}
                  complaints={complaints}
                  onSimulateOrder={handleSimulateOrder}
                  onUpdateOrderStatus={handleUpdateOrderStatus}
                  onSettleFinancials={handleSettleFinancials}
                  onAddPendingOwner={handleAddPendingOwner}
                  onAddComplaint={handleAddComplaint}
                />
              )}
            </motion.div>
          </AnimatePresence>

        </div>
      </main>
    </div>
  );
}

