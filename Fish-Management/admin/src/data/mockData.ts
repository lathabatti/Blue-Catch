/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import { 
  Customer, 
  Owner, 
  DeliveryStaff, 
  Order, 
  Complaint, 
  SystemNotification, 
  SystemSettings, 
  SalesReportEntry, 
  StockItem 
} from '../types';

export const initialCustomers: Customer[] = [
  {
    id: 'CUST-001',
    name: 'Sarah Johnson',
    email: 'sarah.j@example.com',
    phone: '+1 (555) 019-2834',
    status: 'active',
    totalOrders: 14,
    totalSpent: 420.50,
    registrationDate: '2026-03-12',
  },
  {
    id: 'CUST-002',
    name: 'Michael Chen',
    email: 'm.chen@example.com',
    phone: '+1 (555) 021-9876',
    status: 'active',
    totalOrders: 28,
    totalSpent: 912.40,
    registrationDate: '2026-01-22',
  },
  {
    id: 'CUST-003',
    name: 'Emily Rodriguez',
    email: 'emily.rod@example.com',
    phone: '+1 (555) 043-1122',
    status: 'active',
    totalOrders: 5,
    totalSpent: 184.20,
    registrationDate: '2026-05-15',
  },
  {
    id: 'CUST-004',
    name: 'David Kim',
    email: 'd.kim@example.com',
    phone: '+1 (555) 089-4455',
    status: 'blocked',
    totalOrders: 3,
    totalSpent: 95.00,
    registrationDate: '2026-04-01',
  },
  {
    id: 'CUST-005',
    name: 'Jessica Taylor',
    email: 'jtaylor@example.com',
    phone: '+1 (555) 076-2299',
    status: 'active',
    totalOrders: 19,
    totalSpent: 610.80,
    registrationDate: '2026-02-28',
  }
];

export const initialOwners: Owner[] = [
  {
    id: 'OWN-001',
    shopName: 'Ocean Fresh Seafoods',
    ownerName: 'Arthur Pendelton',
    email: 'arthur.p@oceanfresh.com',
    phone: '+1 (555) 123-4567',
    status: 'active',
    totalSales: 112,
    totalRevenue: 3450.00,
    businessRegNo: 'FSH-9821-A',
    registrationDate: '2026-01-10',
  },
  {
    id: 'OWN-002',
    shopName: 'Marina Harbor Fish Co.',
    ownerName: 'Captain Robert Shaw',
    email: 'captain@marinaharbor.com',
    phone: '+1 (555) 234-5678',
    status: 'active',
    totalSales: 89,
    totalRevenue: 2890.50,
    businessRegNo: 'FSH-1102-B',
    registrationDate: '2026-02-14',
  },
  {
    id: 'OWN-003',
    shopName: 'Deep Blue Imports',
    ownerName: 'Elena Rostova',
    email: 'elena@deepblue.com',
    phone: '+1 (555) 345-6789',
    status: 'pending_approval',
    totalSales: 0,
    totalRevenue: 0.00,
    businessRegNo: 'FSH-5541-X',
    registrationDate: '2026-07-01',
  },
  {
    id: 'OWN-004',
    shopName: 'Coral Bay Seafood',
    ownerName: 'Marcus Aurel',
    email: 'marcus@coralbay.com',
    phone: '+1 (555) 456-7890',
    status: 'blocked',
    totalSales: 45,
    totalRevenue: 1210.00,
    businessRegNo: 'FSH-0932-C',
    registrationDate: '2026-03-20',
  }
];

export const initialDeliveryStaff: DeliveryStaff[] = [
  {
    id: 'DEL-001',
    name: 'James Swift',
    email: 'james.swift@express.com',
    phone: '+1 (555) 901-2345',
    status: 'active',
    assignedAreas: ['Downtown', 'North End'],
    totalDeliveries: 154,
    rating: 4.8,
  },
  {
    id: 'DEL-002',
    name: 'Carlos Diaz',
    email: 'carlos.d@express.com',
    phone: '+1 (555) 890-1234',
    status: 'active',
    assignedAreas: ['Marina District', 'Bayfront'],
    totalDeliveries: 98,
    rating: 4.6,
  },
  {
    id: 'DEL-003',
    name: 'Maya Lin',
    email: 'maya.l@express.com',
    phone: '+1 (555) 789-0123',
    status: 'active',
    assignedAreas: ['Southside', 'East Coast'],
    totalDeliveries: 210,
    rating: 4.9,
  },
  {
    id: 'DEL-004',
    name: 'Gary Vance',
    email: 'gary.v@express.com',
    phone: '+1 (555) 678-9012',
    status: 'inactive',
    assignedAreas: ['West Hills'],
    totalDeliveries: 42,
    rating: 4.2,
  }
];

export const initialOrders: Order[] = [
  {
    id: 'ORD-1001',
    customerName: 'Sarah Johnson',
    shopName: 'Ocean Fresh Seafoods',
    fishItems: [
      { name: 'Atlantic Salmon', quantityKg: 2.5, pricePerKg: 18.00 },
      { name: 'Tiger Shrimp', quantityKg: 1.0, pricePerKg: 22.00 }
    ],
    totalAmount: 67.00,
    status: 'delivered',
    date: '2026-07-04',
    deliveryStaffName: 'James Swift'
  },
  {
    id: 'ORD-1002',
    customerName: 'Michael Chen',
    shopName: 'Marina Harbor Fish Co.',
    fishItems: [
      { name: 'Yellowfin Tuna', quantityKg: 3.0, pricePerKg: 24.00 }
    ],
    totalAmount: 72.00,
    status: 'delivering',
    date: '2026-07-06',
    deliveryStaffName: 'Carlos Diaz'
  },
  {
    id: 'ORD-1003',
    customerName: 'Emily Rodriguez',
    shopName: 'Ocean Fresh Seafoods',
    fishItems: [
      { name: 'Red Snapper', quantityKg: 1.5, pricePerKg: 16.00 },
      { name: 'Sea Bass', quantityKg: 2.0, pricePerKg: 20.00 }
    ],
    totalAmount: 64.00,
    status: 'accepted',
    date: '2026-07-06',
    deliveryStaffName: 'Maya Lin'
  },
  {
    id: 'ORD-1004',
    customerName: 'Jessica Taylor',
    shopName: 'Marina Harbor Fish Co.',
    fishItems: [
      { name: 'Atlantic Cod', quantityKg: 4.0, pricePerKg: 12.00 }
    ],
    totalAmount: 48.00,
    status: 'pending',
    date: '2026-07-06'
  }
];

export const initialComplaints: Complaint[] = [
  {
    id: 'CMP-501',
    senderType: 'customer',
    senderName: 'Michael Chen',
    title: 'Late Delivery',
    description: 'The salmon ordered was scheduled for 3 PM but arrived at 5 PM. It was well-iced but caused issues with my scheduling.',
    status: 'resolved',
    date: '2026-07-03',
    resolution: 'Refunded delivery fee of $5.00 and contacted the assigned driver, Maya, regarding route delay optimization.'
  },
  {
    id: 'CMP-502',
    senderType: 'owner',
    senderName: 'Ocean Fresh Seafoods',
    title: 'Delivery Pickup Issue',
    description: 'Delivery driver did not bring an thermal insulation bag for a large order. Please remind drivers of food safety standards.',
    status: 'pending',
    date: '2026-07-05',
  },
  {
    id: 'CMP-503',
    senderType: 'delivery',
    senderName: 'James Swift',
    title: 'Inaccessible Gate Code',
    description: 'Customer at Apt 4B did not provide a gate code and did not answer the phone for 15 minutes. Delayed multiple deliveries.',
    status: 'pending',
    date: '2026-07-06',
  }
];

export const initialNotifications: SystemNotification[] = [
  {
    id: 'NTF-001',
    type: 'promotional',
    title: 'Weekend Seafood Extravaganza',
    message: 'Enjoy 15% off on all Premium Yellowfin Tuna and Tiger Shrimp this weekend. Order fresh right from your app!',
    date: '2026-07-03',
    recipients: 'customers',
    status: 'sent'
  },
  {
    id: 'NTF-002',
    type: 'announcement',
    title: 'Monsoon Delivery Surcharges',
    message: 'To support our delivery partners, a safety surcharge of $2.50 per order will apply during heavy rain alerts starting tonight.',
    date: '2026-07-05',
    recipients: 'all',
    status: 'sent'
  }
];

export const defaultSettings: SystemSettings = {
  appName: 'Blue Catch',
  supportEmail: 'admin@bluecatch.com',
  defaultTaxPercent: 5.5,
  commissionRate: 10.0, // admin takes 10% on owner sales
  allowSelfRegistration: true,
  autoAssignDelivery: true,
  isMaintenanceMode: false,
  paymentCod: true,
  paymentOnline: true,
  backupStatus: 'Healthy',
  lastBackupDate: '2026-07-05 02:00 AM'
};

export const initialSalesReport: SalesReportEntry[] = [
  { date: '2026-06-30', sales: 1250, purchases: 950, profit: 300 },
  { date: '2026-07-01', sales: 1420, purchases: 1020, profit: 400 },
  { date: '2026-07-02', sales: 1100, purchases: 800, profit: 300 },
  { date: '2026-07-03', sales: 1680, purchases: 1150, profit: 530 },
  { date: '2026-07-04', sales: 1950, purchases: 1400, profit: 550 },
  { date: '2026-07-05', sales: 1540, purchases: 1100, profit: 440 },
  { date: '2026-07-06', sales: 1890, purchases: 1300, profit: 590 }
];

export const initialStock: StockItem[] = [
  { id: 'STK-01', name: 'Atlantic Salmon', category: 'Premium Fish', stockKg: 145, pricePerKg: 18.00, status: 'in_stock' },
  { id: 'STK-02', name: 'Yellowfin Tuna', category: 'Premium Fish', stockKg: 85, pricePerKg: 24.00, status: 'in_stock' },
  { id: 'STK-03', name: 'Tiger Shrimp', category: 'Shellfish', stockKg: 120, pricePerKg: 22.00, status: 'in_stock' },
  { id: 'STK-04', name: 'Red Snapper', category: 'White Fish', stockKg: 22, pricePerKg: 16.00, status: 'low_stock' },
  { id: 'STK-05', name: 'Sea Bass', category: 'White Fish', stockKg: 40, pricePerKg: 20.00, status: 'in_stock' },
  { id: 'STK-06', name: 'Atlantic Cod', category: 'White Fish', stockKg: 8, pricePerKg: 12.00, status: 'low_stock' },
  { id: 'STK-07', name: 'Mackerel', category: 'Common Fish', stockKg: 0, pricePerKg: 8.50, status: 'out_of_stock' }
];

export const availableDeliveryAreas = [
  'Downtown',
  'North End',
  'Marina District',
  'Bayfront',
  'Southside',
  'East Coast',
  'West Hills',
  'Harbor View',
  'Green Valley'
];
