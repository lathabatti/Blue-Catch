/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

export interface Customer {
  id: string;
  name: string;
  email: string;
  phone: string;
  status: 'active' | 'blocked';
  totalOrders: number;
  totalSpent: number;
  registrationDate: string;
}

export interface Owner {
  id: string;
  shopName: string;
  ownerName: string;
  email: string;
  phone: string;
  status: 'pending_approval' | 'active' | 'blocked';
  totalSales: number;
  totalRevenue: number;
  businessRegNo: string;
  registrationDate: string;
}

export interface DeliveryStaff {
  id: string;
  name: string;
  email: string;
  phone: string;
  status: 'active' | 'inactive';
  assignedAreas: string[];
  totalDeliveries: number;
  rating: number;
}

export interface FishItem {
  name: string;
  quantityKg: number;
  pricePerKg: number;
}

export interface Order {
  id: string;
  customerName: string;
  shopName: string;
  fishItems: FishItem[];
  totalAmount: number;
  status: 'pending' | 'accepted' | 'delivering' | 'delivered';
  date: string;
  deliveryStaffName?: string;
}

export interface Complaint {
  id: string;
  senderType: 'customer' | 'owner' | 'delivery';
  senderName: string;
  title: string;
  description: string;
  status: 'pending' | 'resolved';
  date: string;
  resolution?: string;
}

export interface SystemNotification {
  id: string;
  type: 'promotional' | 'announcement';
  title: string;
  message: string;
  date: string;
  recipients: 'all' | 'customers' | 'owners' | 'delivery';
  status: 'sent' | 'draft';
}

export interface SystemSettings {
  appName: string;
  supportEmail: string;
  defaultTaxPercent: number;
  commissionRate: number;
  allowSelfRegistration: boolean;
  autoAssignDelivery: boolean;
  isMaintenanceMode: boolean;
  paymentCod: boolean;
  paymentOnline: boolean;
  backupStatus: string;
  lastBackupDate: string;
}

export interface SalesReportEntry {
  date: string;
  sales: number;
  purchases: number;
  profit: number;
}

export interface StockItem {
  id: string;
  name: string;
  category: string;
  stockKg: number;
  pricePerKg: number;
  status: 'in_stock' | 'low_stock' | 'out_of_stock';
}

export interface AdminUser {
  id: string;
  name: string;
  email: string;
  role: 'super_admin' | 'moderator' | 'operator';
  createdAt: string;
}

