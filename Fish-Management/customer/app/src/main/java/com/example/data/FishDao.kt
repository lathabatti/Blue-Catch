package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FishDao {

    // --- Fish Products ---
    @Query("SELECT * FROM fish_products")
    fun getAllProducts(): Flow<List<FishProduct>>

    @Query("SELECT * FROM fish_products WHERE id = :id")
    suspend fun getProductById(id: Int): FishProduct?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<FishProduct>)

    @Query("UPDATE fish_products SET stockKg = :newStock WHERE id = :productId")
    suspend fun updateProductStock(productId: Int, newStock: Double)


    // --- Cart ---
    @Query("SELECT * FROM cart_items")
    fun getCartItems(): Flow<List<CartItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartItem)

    @Query("DELETE FROM cart_items WHERE productId = :productId")
    suspend fun deleteCartItem(productId: Int)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()


    // --- User Session ---
    @Query("SELECT * FROM user_sessions WHERE id = 1")
    fun getUserSessionFlow(): Flow<UserSession?>

    @Query("SELECT * FROM user_sessions WHERE id = 1")
    suspend fun getUserSession(): UserSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserSession(session: UserSession)

    @Query("DELETE FROM user_sessions")
    suspend fun clearUserSession()


    // --- Orders ---
    @Query("SELECT * FROM orders ORDER BY date DESC")
    fun getAllOrders(): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE id = :id")
    fun getOrderByIdFlow(id: Int): Flow<Order?>

    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getOrderById(id: Int): Order?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order): Long

    @Query("UPDATE orders SET deliveryStatus = :status WHERE id = :orderId")
    suspend fun updateDeliveryStatus(orderId: Int, status: String)

    // --- Users ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE mobile = :mobile LIMIT 1")
    suspend fun getUserByMobile(mobile: String): User?

    @Query("SELECT * FROM users WHERE email = :identifier OR mobile = :identifier LIMIT 1")
    suspend fun getUserByEmailOrMobile(identifier: String): User?

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<User>>
}
