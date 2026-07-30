package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "delivery_orders")
data class DeliveryOrder(
    @PrimaryKey(autoGenerate = true) val deliveryId: Int = 0,
    val orderId: String,
    val deliveryStaffId: Int,
    val customerName: String,
    val mobileNumber: String,
    val deliveryAddress: String,
    val fishName: String,
    val quantity: Double, // in kg
    val amount: Double,
    val paymentStatus: String, // "Paid", "Pending", "Cash on Delivery"
    val deliveryStatus: String, // "Assigned", "Picked Up", "Out for Delivery", "Delivered"
    val assignedTime: Long,
    val deliveredTime: Long? = null,
    val priority: String = "Medium", // "High", "Medium", "Low"
    val deliveryNotes: String = "",
    val statusHistory: String = ""
)

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val mobile: String,
    val address: String,
    val status: String = "Active" // "Active", "Blocked"
)

@Entity(tableName = "owners")
data class Owner(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val shopName: String,
    val status: String = "Approved" // "Pending", "Approved", "Blocked"
)

@Entity(tableName = "delivery_staff")
data class DeliveryStaff(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val assignedArea: String,
    val status: String = "Active" // "Active", "Blocked"
)

@Entity(tableName = "complaints")
data class Complaint(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderName: String,
    val role: String, // "Customer", "Owner", "Delivery"
    val complaintText: String,
    val status: String = "Pending", // "Pending", "Resolved"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val type: String = "Announcement", // "Promotion", "Announcement"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "settings")
data class SystemSettings(
    @PrimaryKey val id: Int = 1,
    val taxRate: Double = 5.0,
    val deliveryCharge: Double = 30.0,
    val backupFrequency: String = "Weekly",
    val paymentGateway: String = "Razorpay"
)
