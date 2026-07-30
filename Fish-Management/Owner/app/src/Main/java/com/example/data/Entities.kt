package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "suppliers")
data class Supplier(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val details: String = "",
    val totalPurchased: Double = 0.0,
    val pendingDues: Double = 0.0,
    val leadTimeDays: Int = 3,
    val contactPerson: String = "",
    val totalWeight: Double = 0.0
)

@Entity(tableName = "purchases")
data class Purchase(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val supplierId: Int,
    val supplierName: String,
    val fishName: String,
    val qtyKg: Double,
    val pricePerKg: Double,
    val totalAmount: Double,
    val purchaseDate: Long = System.currentTimeMillis(),
    val qualityType: String, // Fresh, Medium, Old
    val boatName: String = "",
    val buyerName: String = "",
    val buyerPhone: String = "",
    val paymentStatus: String = "Paid",
    val supplierPhone: String = ""
)

@Entity(tableName = "stock")
data class Stock(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val shopName: String,
    val fishPhoto: String = "", // Resource name or custom string
    val fishName: String,
    val currentStock: Double,
    val sellingPrice: Double,
    val storageBox: String,
    val freshness: String, // Fresh, Medium, Old
    val weightAfterCleaning: Double = 0.0,
    val weightLoss: Double = 0.0,
    val expiryDate: String = "",
    val offers: String = "",
    val category: String = "Fresh" // Fresh, Frozen, Processed
)

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fishName: String,
    val customerName: String,
    val customerPhone: String = "",
    val qtyKg: Double,
    val pricePerKg: Double,
    val totalAmount: Double,
    val paymentType: String, // Cash, Credit, UPI
    val orderType: String, // Direct, Hotel, Shop
    val saleDate: Long = System.currentTimeMillis(),
    val deliveryStatus: String = "Completed", // Completed, Pending Delivery
    val paymentStatus: String = "Paid"
)

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val creditBookBalance: Double = 0.0,
    val pendingAmounts: Double = 0.0
)

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String, // Ice, Transport, Salary, Packing, Misc
    val description: String,
    val amount: Double,
    val date: Long = System.currentTimeMillis()
)

@Entity(tableName = "deliveries")
data class Delivery(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerName: String,
    val address: String,
    val phone: String,
    val fishDetails: String,
    val totalAmount: Double,
    val deliveryBoyName: String = "",
    val deliveryStatus: String = "Pending", // Pending, Out for Delivery, Delivered
    val orderDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "workers")
data class Worker(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val role: String, // Manager, Staff, Delivery Boy
    val salary: Double,
    val phone: String,
    val loginPin: String
)

@Entity(tableName = "market_prices")
data class MarketPrice(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fishName: String,
    val category: String, // Fresh Water, Sea Water, Shellfish
    val currentPrice: Double,
    val marketRate: Double
)

@Entity(tableName = "alerts")
data class Alert(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val type: String, // LowStock, OldStock, CustomerDue, SupplierDue
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
