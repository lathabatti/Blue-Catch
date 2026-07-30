package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fish_products")
data class FishProduct(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val pricePerKg: Double,
    val discount: Int, // e.g., 10 for 10% OFF
    val stockKg: Double,
    val quality: String, // e.g., "Fresh", "Chilled", "Premium"
    val description: String,
    val imageResName: String,
    val category: String,
    val shopName: String = "OceanFresh Harbor"
) {
    val finalPrice: Double
        get() = pricePerKg * (1 - discount / 100.0)
}

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey val productId: Int,
    val name: String,
    val pricePerKg: Double,
    val discount: Int,
    val quantityKg: Double,
    val imageResName: String
) {
    val finalPricePerKg: Double
        get() = pricePerKg * (1 - discount / 100.0)

    val totalCost: Double
        get() = finalPricePerKg * quantityKg
}

@Entity(tableName = "user_sessions")
data class UserSession(
    @PrimaryKey val id: Int = 1, // Singleton session ID
    val name: String = "",
    val email: String = "",
    val mobile: String = "",
    val isLoggedIn: Boolean = false,
    val tempOtp: String = "" // For OTP validation simulation
)

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val address: String,
    val mobile: String,
    val paymentMethod: String,
    val orderSummaryText: String, // "King Fish (2kg), Prawns (1kg)"
    val totalAmount: Double,
    val paymentStatus: String, // "Paid", "Pending"
    val deliveryStatus: String, // "Placed", "Preparing", "Out for Delivery", "Delivered"
    val date: Long = System.currentTimeMillis()
)

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val mobile: String,
    val email: String,
    val password: String,
    val address: String,
    val role: String = "customer",
    val isVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

