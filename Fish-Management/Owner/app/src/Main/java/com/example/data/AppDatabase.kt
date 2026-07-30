package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FishDao {
    // Supplier operations
    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getAllSuppliers(): Flow<List<Supplier>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: Supplier)

    @Delete
    suspend fun deleteSupplier(supplier: Supplier)

    @Update
    suspend fun updateSupplier(supplier: Supplier)

    // Purchase operations
    @Query("SELECT * FROM purchases ORDER BY purchaseDate DESC")
    fun getAllPurchases(): Flow<List<Purchase>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: Purchase)

    @Update
    suspend fun updatePurchase(purchase: Purchase)

    // Stock operations
    @Query("SELECT * FROM stock ORDER BY fishName ASC")
    fun getAllStock(): Flow<List<Stock>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStock(stock: Stock)

    @Update
    suspend fun updateStock(stock: Stock)

    @Delete
    suspend fun deleteStock(stock: Stock)

    @Query("SELECT * FROM stock WHERE currentStock <= :threshold")
    fun getLowStock(threshold: Double = 10.0): Flow<List<Stock>>

    // Sales operations
    @Query("SELECT * FROM sales ORDER BY saleDate DESC")
    fun getAllSales(): Flow<List<Sale>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: Sale)

    @Update
    suspend fun updateSale(sale: Sale)

    // Customer operations
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer)

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    // Expense operations
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    // Delivery operations
    @Query("SELECT * FROM deliveries ORDER BY orderDate DESC")
    fun getAllDeliveries(): Flow<List<Delivery>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDelivery(delivery: Delivery)

    @Update
    suspend fun updateDelivery(delivery: Delivery)

    // Worker operations
    @Query("SELECT * FROM workers ORDER BY name ASC")
    fun getAllWorkers(): Flow<List<Worker>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorker(worker: Worker)

    @Delete
    suspend fun deleteWorker(worker: Worker)

    // MarketPrice operations
    @Query("SELECT * FROM market_prices ORDER BY fishName ASC")
    fun getAllMarketPrices(): Flow<List<MarketPrice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarketPrice(marketPrice: MarketPrice)

    // Alert operations
    @Query("SELECT * FROM alerts ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<Alert>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: Alert)

    @Query("UPDATE alerts SET isRead = 1 WHERE id = :id")
    suspend fun markAlertAsRead(id: Int)

    @Query("DELETE FROM alerts")
    suspend fun clearAllAlerts()
}

@Database(
    entities = [
        Supplier::class,
        Purchase::class,
        Stock::class,
        Sale::class,
        Customer::class,
        Expense::class,
        Delivery::class,
        Worker::class,
        MarketPrice::class,
        Alert::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fishDao(): FishDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fish_business_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
