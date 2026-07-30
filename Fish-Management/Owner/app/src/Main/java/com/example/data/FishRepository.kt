package com.example.data

import kotlinx.coroutines.flow.Flow

class FishRepository(private val fishDao: FishDao) {
    // Suppliers
    val allSuppliers: Flow<List<Supplier>> = fishDao.getAllSuppliers()
    suspend fun insertSupplier(supplier: Supplier) = fishDao.insertSupplier(supplier)
    suspend fun deleteSupplier(supplier: Supplier) = fishDao.deleteSupplier(supplier)
    suspend fun updateSupplier(supplier: Supplier) = fishDao.updateSupplier(supplier)

    // Purchases
    val allPurchases: Flow<List<Purchase>> = fishDao.getAllPurchases()
    suspend fun insertPurchase(purchase: Purchase) = fishDao.insertPurchase(purchase)
    suspend fun updatePurchase(purchase: Purchase) = fishDao.updatePurchase(purchase)

    // Stock
    val allStock: Flow<List<Stock>> = fishDao.getAllStock()
    fun getLowStock(threshold: Double = 10.0) = fishDao.getLowStock(threshold)
    suspend fun insertStock(stock: Stock) = fishDao.insertStock(stock)
    suspend fun updateStock(stock: Stock) = fishDao.updateStock(stock)
    suspend fun deleteStock(stock: Stock) = fishDao.deleteStock(stock)

    // Sales
    val allSales: Flow<List<Sale>> = fishDao.getAllSales()
    suspend fun insertSale(sale: Sale) = fishDao.insertSale(sale)
    suspend fun updateSale(sale: Sale) = fishDao.updateSale(sale)

    // Customers
    val allCustomers: Flow<List<Customer>> = fishDao.getAllCustomers()
    suspend fun insertCustomer(customer: Customer) = fishDao.insertCustomer(customer)
    suspend fun updateCustomer(customer: Customer) = fishDao.updateCustomer(customer)
    suspend fun deleteCustomer(customer: Customer) = fishDao.deleteCustomer(customer)

    // Expenses
    val allExpenses: Flow<List<Expense>> = fishDao.getAllExpenses()
    suspend fun insertExpense(expense: Expense) = fishDao.insertExpense(expense)
    suspend fun updateExpense(expense: Expense) = fishDao.updateExpense(expense)
    suspend fun deleteExpense(expense: Expense) = fishDao.deleteExpense(expense)

    // Deliveries
    val allDeliveries: Flow<List<Delivery>> = fishDao.getAllDeliveries()
    suspend fun insertDelivery(delivery: Delivery) = fishDao.insertDelivery(delivery)
    suspend fun updateDelivery(delivery: Delivery) = fishDao.updateDelivery(delivery)

    // Workers
    val allWorkers: Flow<List<Worker>> = fishDao.getAllWorkers()
    suspend fun insertWorker(worker: Worker) = fishDao.insertWorker(worker)
    suspend fun deleteWorker(worker: Worker) = fishDao.deleteWorker(worker)

    // Market Prices
    val allMarketPrices: Flow<List<MarketPrice>> = fishDao.getAllMarketPrices()
    suspend fun insertMarketPrice(marketPrice: MarketPrice) = fishDao.insertMarketPrice(marketPrice)

    // Alerts
    val allAlerts: Flow<List<Alert>> = fishDao.getAllAlerts()
    suspend fun insertAlert(alert: Alert) = fishDao.insertAlert(alert)
    suspend fun markAlertAsRead(id: Int) = fishDao.markAlertAsRead(id)
    suspend fun clearAllAlerts() = fishDao.clearAllAlerts()
}
