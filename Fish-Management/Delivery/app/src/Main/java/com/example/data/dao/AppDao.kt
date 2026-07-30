package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // --- Delivery Orders ---
    @Query("SELECT * FROM delivery_orders ORDER BY assignedTime DESC")
    fun getAllDeliveryOrders(): Flow<List<DeliveryOrder>>

    @Query("SELECT * FROM delivery_orders WHERE deliveryStaffId = :staffId ORDER BY assignedTime DESC")
    fun getOrdersForStaff(staffId: Int): Flow<List<DeliveryOrder>>

    @Query("SELECT * FROM delivery_orders WHERE deliveryId = :deliveryId")
    suspend fun getDeliveryOrderById(deliveryId: Int): DeliveryOrder?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeliveryOrder(order: DeliveryOrder)

    @Update
    suspend fun updateDeliveryOrder(order: DeliveryOrder)

    @Query("UPDATE delivery_orders SET deliveryStatus = :status, deliveredTime = :deliveredTime WHERE deliveryId = :deliveryId")
    suspend fun updateDeliveryStatus(deliveryId: Int, status: String, deliveredTime: Long?)

    // --- Users (Customers) ---
    @Query("SELECT * FROM users ORDER BY id DESC")
    fun getAllUsers(): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Query("UPDATE users SET status = :status WHERE id = :userId")
    suspend fun updateUserStatus(userId: Int, status: String)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: Int)

    // --- Owners ---
    @Query("SELECT * FROM owners ORDER BY id DESC")
    fun getAllOwners(): Flow<List<Owner>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOwner(owner: Owner)

    @Update
    suspend fun updateOwner(owner: Owner)

    @Query("UPDATE owners SET status = :status WHERE id = :ownerId")
    suspend fun updateOwnerStatus(ownerId: Int, status: String)

    // --- Delivery Staff ---
    @Query("SELECT * FROM delivery_staff ORDER BY id DESC")
    fun getAllDeliveryStaff(): Flow<List<DeliveryStaff>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeliveryStaff(staff: DeliveryStaff)

    @Update
    suspend fun updateDeliveryStaff(staff: DeliveryStaff)

    @Delete
    suspend fun deleteDeliveryStaff(staff: DeliveryStaff)

    @Query("DELETE FROM delivery_staff WHERE id = :staffId")
    suspend fun deleteDeliveryStaffById(staffId: Int)

    // --- Complaints ---
    @Query("SELECT * FROM complaints ORDER BY timestamp DESC")
    fun getAllComplaints(): Flow<List<Complaint>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComplaint(complaint: Complaint)

    @Query("UPDATE complaints SET status = :status WHERE id = :complaintId")
    suspend fun updateComplaintStatus(complaintId: Int, status: String)

    // --- Notifications ---
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<Notification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: Notification)

    // --- System Settings ---
    @Query("SELECT * FROM settings WHERE id = 1")
    fun getSettingsFlow(): Flow<SystemSettings?>

    @Query("SELECT * FROM settings WHERE id = 1")
    suspend fun getSettings(): SystemSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: SystemSettings)

    // --- Analytics / Stats Queries ---
    @Query("SELECT COUNT(*) FROM users")
    fun getTotalCustomersCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM owners")
    fun getTotalOwnersCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM delivery_staff")
    fun getTotalDeliveryStaffCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM delivery_orders")
    fun getTotalOrdersCount(): Flow<Int>

    @Query("SELECT SUM(amount) FROM delivery_orders WHERE deliveryStatus = 'Delivered'")
    fun getTotalSalesAmount(): Flow<Double?>
}
