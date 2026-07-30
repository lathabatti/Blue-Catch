package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// User Session models representing role-based authenticated users
sealed interface UserSession {
    val name: String
    val phone: String
    val role: String
}

data class OwnerSession(
    override val name: String,
    override val phone: String,
    val email: String
) : UserSession {
    override val role: String = "Owner"
}

data class WorkerSession(
    val workerId: Int,
    override val name: String,
    override val phone: String,
    override val role: String
) : UserSession

data class CustomerSession(
    val customerId: Int,
    override val name: String,
    override val phone: String
) : UserSession {
    override val role: String = "Customer"
}

data class GuestSession(
    override val name: String = "Guest Viewer",
    override val phone: String = ""
) : UserSession {
    override val role: String = "Guest"
}

data class OwnerCredentials(
    val name: String,
    val phone: String,
    val email: String,
    val password: String
)

class FishViewModel(private val repository: FishRepository) : ViewModel() {

    // Current logged-in user state
    private val _currentUser = MutableStateFlow<UserSession?>(null)
    val currentUser: StateFlow<UserSession?> = _currentUser.asStateFlow()

    // Map of registered owners
    private val _registeredOwners = MutableStateFlow<Map<String, OwnerCredentials>>(
        mapOf(
            "sanjusmily128@gmail.com" to OwnerCredentials("Sanju Smily", "9876543210", "sanjusmily128@gmail.com", "admin")
        )
    )
    val registeredOwners: StateFlow<Map<String, OwnerCredentials>> = _registeredOwners.asStateFlow()

    fun loginAsOwner(email: String, password: String): Boolean {
        val credentials = _registeredOwners.value[email.trim().lowercase()]
        if (credentials != null && credentials.password == password) {
            _currentUser.value = OwnerSession(credentials.name, credentials.phone, credentials.email)
            return true
        }
        return false
    }

    fun loginAsWorker(phone: String, pin: String): Boolean {
        val workerList = workers.value
        val match = workerList.find { it.phone.trim() == phone.trim() && it.loginPin.trim() == pin.trim() }
        if (match != null) {
            _currentUser.value = WorkerSession(match.id, match.name, match.phone, match.role)
            return true
        }
        return false
    }

    fun signUpOwner(name: String, email: String, phone: String, password: String): Boolean {
        val emailClean = email.trim().lowercase()
        if (_registeredOwners.value.containsKey(emailClean)) {
            return false // Already exists
        }
        val newMap = _registeredOwners.value.toMutableMap()
        newMap[emailClean] = OwnerCredentials(name, phone, emailClean, password)
        _registeredOwners.value = newMap
        _currentUser.value = OwnerSession(name, phone, emailClean)
        return true
    }

    fun loginAsCustomer(phone: String): Boolean {
        val phoneClean = phone.trim()
        val cust = customers.value.find { it.phone.trim() == phoneClean }
        if (cust != null) {
            _currentUser.value = CustomerSession(cust.id, cust.name, cust.phone)
            return true
        }
        return false
    }

    fun signUpCustomer(name: String, phone: String): Boolean {
        val phoneClean = phone.trim()
        val exists = customers.value.any { it.phone.trim() == phoneClean }
        if (exists) {
            return false // Customer already exists
        }
        viewModelScope.launch(Dispatchers.IO) {
            val newCust = Customer(name = name, phone = phoneClean, creditBookBalance = 0.0, pendingAmounts = 0.0)
            repository.insertCustomer(newCust)
            // Retrieve inserted customer or find it
            _currentUser.value = CustomerSession(0, name, phoneClean)
        }
        return true
    }

    fun broadcastStockAlert(fishName: String, messageText: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertAlert(
                Alert(
                    title = "Broadcast: $fishName Promo!",
                    message = messageText,
                    type = "StockPromo"
                )
            )
        }
    }

    fun placeCustomerOrder(
        fishName: String,
        qtyKg: Double,
        pricePerKg: Double,
        customerName: String,
        customerPhone: String,
        deliveryAddress: String,
        paymentType: String = "COD / UPI on Delivery"
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val totalAmt = qtyKg * pricePerKg

            // Create pending sale
            val sale = Sale(
                fishName = fishName,
                customerName = customerName,
                customerPhone = customerPhone,
                qtyKg = qtyKg,
                pricePerKg = pricePerKg,
                totalAmount = totalAmt,
                paymentType = paymentType,
                orderType = "Direct",
                deliveryStatus = "Pending Delivery"
            )
            repository.insertSale(sale)

            // Deduct stock
            val targetStock = stock.value.find { it.fishName.lowercase() == fishName.lowercase() }
            if (targetStock != null) {
                val newQty = (targetStock.currentStock - qtyKg).coerceAtLeast(0.0)
                repository.updateStock(
                    targetStock.copy(
                        currentStock = newQty,
                        weightAfterCleaning = (targetStock.weightAfterCleaning - (qtyKg * 0.9)).coerceAtLeast(0.0)
                    )
                )
            }

            // Create Delivery
            val delivery = Delivery(
                customerName = customerName,
                address = if (deliveryAddress.isNotBlank()) deliveryAddress else "Store Pickup Requested",
                phone = customerPhone,
                fishDetails = "$fishName - $qtyKg kg",
                totalAmount = totalAmt,
                deliveryBoyName = "Suresh Patil",
                deliveryStatus = "Pending"
            )
            repository.insertDelivery(delivery)

            // Generate Owner Alert
            repository.insertAlert(
                Alert(
                    title = "New Customer Pre-Order!",
                    message = "Customer $customerName ($customerPhone) ordered $qtyKg kg of $fishName. Address: $deliveryAddress",
                    type = "CustomerDue"
                )
            )
        }
    }

    fun logout() {
        _currentUser.value = null
    }

    fun loginAsGuest() {
        _currentUser.value = GuestSession()
    }

    // Streams of data from database
    val suppliers: StateFlow<List<Supplier>> = repository.allSuppliers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val purchases: StateFlow<List<Purchase>> = repository.allPurchases
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stock: StateFlow<List<Stock>> = repository.allStock
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sales: StateFlow<List<Sale>> = repository.allSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customers: StateFlow<List<Customer>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<Expense>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deliveries: StateFlow<List<Delivery>> = repository.allDeliveries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val workers: StateFlow<List<Worker>> = repository.allWorkers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val marketPrices: StateFlow<List<MarketPrice>> = repository.allMarketPrices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val alerts: StateFlow<List<Alert>> = repository.allAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        seedDatabaseIfEmpty()
    }

    private fun seedDatabaseIfEmpty() {
        viewModelScope.launch(Dispatchers.IO) {
            // Check if stock is empty, and seed if so
            repository.allStock.first().let { currentStockList ->
                if (currentStockList.isEmpty()) {
                    // Seed Market Prices
                    listOf(
                        MarketPrice(fishName = "Salmon", category = "Fresh Water", currentPrice = 850.0, marketRate = 900.0),
                        MarketPrice(fishName = "Yellowfin Tuna", category = "Sea Water", currentPrice = 520.0, marketRate = 550.0),
                        MarketPrice(fishName = "Tiger Shrimp", category = "Shellfish", currentPrice = 650.0, marketRate = 700.0),
                        MarketPrice(fishName = "White Pomfret", category = "Sea Water", currentPrice = 750.0, marketRate = 800.0),
                        MarketPrice(fishName = "Mud Crabs", category = "Shellfish", currentPrice = 580.0, marketRate = 620.0),
                        MarketPrice(fishName = "Catfish", category = "Fresh Water", currentPrice = 350.0, marketRate = 380.0)
                    ).forEach { repository.insertMarketPrice(it) }

                    // Seed Suppliers
                    val sup1 = Supplier(name = "Sea Breeze Seafoods", phone = "9876543201", details = "Bulk marine fish supplier", totalPurchased = 15000.0, pendingDues = 4500.0, leadTimeDays = 2, contactPerson = "Captain Raj", totalWeight = 25.0)
                    val sup2 = Supplier(name = "Deep Ocean Catch", phone = "9876543202", details = "Premium deep sea tuna", totalPurchased = 24000.0, pendingDues = 0.0, leadTimeDays = 4, contactPerson = "Sarah Connor", totalWeight = 53.3)
                    val sup3 = Supplier(name = "Delta Rivers Ltd", phone = "9876543203", details = "Freshwater pond trout & catfish", totalPurchased = 8000.0, pendingDues = 1200.0, leadTimeDays = 1, contactPerson = "John Doe", totalWeight = 18.5)
                    listOf(sup1, sup2, sup3).forEach { repository.insertSupplier(it) }

                    // Seed Stock
                    listOf(
                        Stock(shopName = "Main Harbor Branch", fishPhoto = "img_fish_salmon", fishName = "Salmon", currentStock = 45.0, sellingPrice = 950.0, storageBox = "Box A1", freshness = "Fresh", weightAfterCleaning = 42.0, weightLoss = 3.0, expiryDate = "2026-07-06", offers = "10% off on 3kg+", category = "Fresh"),
                        Stock(shopName = "Main Harbor Branch", fishPhoto = "img_fish_tuna", fishName = "Yellowfin Tuna", currentStock = 8.0, sellingPrice = 620.0, storageBox = "Box B3", freshness = "Fresh", weightAfterCleaning = 7.5, weightLoss = 0.5, expiryDate = "2026-07-05", offers = "Save ₹50/kg today!", category = "Frozen"),
                        Stock(shopName = "Main Harbor Branch", fishPhoto = "img_fish_shrimp", fishName = "Tiger Shrimp", currentStock = 3.0, sellingPrice = 780.0, storageBox = "Box C2", freshness = "Fresh", weightAfterCleaning = 2.7, weightLoss = 0.3, expiryDate = "2026-07-07", offers = "", category = "Processed"),
                        Stock(shopName = "Waterfront Outlet", fishPhoto = "img_fish_pomfret", fishName = "White Pomfret", currentStock = 12.0, sellingPrice = 850.0, storageBox = "Box A4", freshness = "Medium", weightAfterCleaning = 11.2, weightLoss = 0.8, expiryDate = "2026-07-05", offers = "Quick clearance sale", category = "Fresh")
                    ).forEach { repository.insertStock(it) }

                    // Seed Purchases
                    listOf(
                        Purchase(supplierId = 1, supplierName = "Sea Breeze Seafoods", fishName = "White Pomfret", qtyKg = 50.0, pricePerKg = 600.0, totalAmount = 30000.0, qualityType = "Fresh"),
                        Purchase(supplierId = 2, supplierName = "Deep Ocean Catch", fishName = "Yellowfin Tuna", qtyKg = 40.0, pricePerKg = 450.0, totalAmount = 18000.0, qualityType = "Fresh")
                    ).forEach { repository.insertPurchase(it) }

                    // Seed Customers
                    listOf(
                        Customer(name = "Aroma Fine Dining", phone = "9123456780", creditBookBalance = 8500.0, pendingAmounts = 8500.0),
                        Customer(name = "Grand Plaza Hotel", phone = "9123456781", creditBookBalance = 12000.0, pendingAmounts = 12000.0),
                        Customer(name = "John Retailer", phone = "9123456782", creditBookBalance = 0.0, pendingAmounts = 0.0)
                    ).forEach { repository.insertCustomer(it) }

                    // Seed Sales
                    listOf(
                        Sale(fishName = "Salmon", customerName = "Aroma Fine Dining", qtyKg = 10.0, pricePerKg = 950.0, totalAmount = 9500.0, paymentType = "Credit", orderType = "Hotel"),
                        Sale(fishName = "Tiger Shrimp", customerName = "John Retailer", qtyKg = 3.0, pricePerKg = 780.0, totalAmount = 2340.0, paymentType = "UPI", orderType = "Direct")
                    ).forEach { repository.insertSale(it) }

                    // Seed Expenses
                    listOf(
                        Expense(category = "Ice Charges", description = "20 Blocks dry ice for Box A1-C2", amount = 1500.0),
                        Expense(category = "Transport Charges", description = "Morning logistics delivery van", amount = 3200.0),
                        Expense(category = "Worker Salaries", description = "Part-time cleaning assistant wages", amount = 800.0)
                    ).forEach { repository.insertExpense(it) }

                    // Seed Workers
                    listOf(
                        Worker(name = "Ramesh Kumar", role = "Manager", salary = 28000.0, phone = "9876543210", loginPin = "1234"),
                        Worker(name = "Suresh Patil", role = "Delivery Boy", salary = 16000.0, phone = "9876543211", loginPin = "1111"),
                        Worker(name = "Anjali Sharma", role = "Staff", salary = 18000.0, phone = "9876543212", loginPin = "2222")
                    ).forEach { repository.insertWorker(it) }

                    // Seed Deliveries
                    listOf(
                        Delivery(customerName = "Aroma Fine Dining", address = "Sector 5, Galleria Mall", phone = "9123456780", fishDetails = "Salmon - 10kg", totalAmount = 9500.0, deliveryBoyName = "Suresh Patil", deliveryStatus = "Delivered"),
                        Delivery(customerName = "Grand Plaza Hotel", address = "Main Highway Junction", phone = "9123456781", fishDetails = "Tiger Shrimp - 15kg", totalAmount = 11700.0, deliveryBoyName = "Suresh Patil", deliveryStatus = "Pending")
                    ).forEach { repository.insertDelivery(it) }

                    // Seed Alerts
                    listOf(
                        Alert(title = "Low Stock Alert", message = "Yellowfin Tuna stock is critically low (8.0 kg remaining)", type = "LowStock"),
                        Alert(title = "Supplier Payment Due", message = "Sea Breeze Seafoods has a pending due of ₹4,500", type = "SupplierDue"),
                        Alert(title = "Quality Warning", message = "White Pomfret in Box A4 has freshness rating: Medium. Push for sales!", type = "OldStock")
                    ).forEach { repository.insertAlert(it) }
                }
            }
        }
    }

    // --- Action Methods ---

    // 1. Buy Fish (Marketplace)
    fun buyFish(
        supplierId: Int,
        supplierName: String,
        fishName: String,
        qtyKg: Double,
        pricePerKg: Double,
        qualityType: String,
        paymentStatusPending: Boolean,
        boatName: String = "",
        buyerName: String = "",
        buyerPhone: String = ""
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val totalAmount = qtyKg * pricePerKg

            val allSups = suppliers.value
            val targetSup = allSups.find { it.id == supplierId }

            // Create Purchase record
            val purchase = Purchase(
                supplierId = supplierId,
                supplierName = supplierName,
                fishName = fishName,
                qtyKg = qtyKg,
                pricePerKg = pricePerKg,
                totalAmount = totalAmount,
                qualityType = qualityType,
                boatName = boatName,
                buyerName = buyerName,
                buyerPhone = buyerPhone,
                paymentStatus = if (paymentStatusPending) "Pending" else "Paid",
                supplierPhone = targetSup?.phone ?: ""
            )
            repository.insertPurchase(purchase)

            // Update Supplier record (totals & dues)
            if (targetSup != null) {
                val updatedSup = targetSup.copy(
                    totalPurchased = targetSup.totalPurchased + totalAmount,
                    totalWeight = targetSup.totalWeight + qtyKg,
                    pendingDues = targetSup.pendingDues + (if (paymentStatusPending) totalAmount else 0.0)
                )
                repository.updateSupplier(updatedSup)
            }

            // Update Stock
            val allStockItems = stock.value
            val existingStock = allStockItems.find { it.fishName.lowercase() == fishName.lowercase() && it.freshness == qualityType }
            if (existingStock != null) {
                val updatedStock = existingStock.copy(
                    currentStock = existingStock.currentStock + qtyKg,
                    weightAfterCleaning = existingStock.weightAfterCleaning + (qtyKg * 0.9),
                    weightLoss = existingStock.weightLoss + (qtyKg * 0.1)
                )
                repository.updateStock(updatedStock)
            } else {
                val newStock = Stock(
                    shopName = "Main Harbor Branch",
                    fishName = fishName,
                    currentStock = qtyKg,
                    sellingPrice = pricePerKg * 1.3, // 30% default markup
                    storageBox = "Box " + ('A'..'F').random() + (1..9).random(),
                    freshness = qualityType,
                    weightAfterCleaning = qtyKg * 0.9,
                    weightLoss = qtyKg * 0.1,
                    expiryDate = "2026-07-08",
                    offers = ""
                )
                repository.insertStock(newStock)
            }

            // Create alert
            repository.insertAlert(
                Alert(
                    title = "Purchase Registered",
                    message = "Successfully bought $qtyKg kg of $fishName from $supplierName for ₹$totalAmount.",
                    type = "SupplierDue"
                )
            )
        }
    }

    // 2. Sell Fish (Sales / Billing)
    fun sellFish(
        fishName: String,
        customerName: String,
        customerPhone: String,
        qtyKg: Double,
        sellingPrice: Double,
        paymentType: String, // Cash, Credit, UPI
        orderType: String, // Direct, Hotel, Shop
        deliveryStatus: String, // Completed, Pending Delivery
        deliveryAddress: String = ""
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val totalAmount = qtyKg * sellingPrice

            // Register Sale record
            val sale = Sale(
                fishName = fishName,
                customerName = customerName,
                customerPhone = customerPhone,
                qtyKg = qtyKg,
                pricePerKg = sellingPrice,
                totalAmount = totalAmount,
                paymentType = paymentType,
                orderType = orderType,
                deliveryStatus = deliveryStatus,
                paymentStatus = if (paymentType == "Credit") "Pending" else "Paid"
            )
            repository.insertSale(sale)

            // Deduct Stock
            val allStockItems = stock.value
            val targetStock = allStockItems.find { it.fishName.lowercase() == fishName.lowercase() }
            if (targetStock != null) {
                val newQty = (targetStock.currentStock - qtyKg).coerceAtLeast(0.0)
                val updatedStock = targetStock.copy(
                    currentStock = newQty,
                    weightAfterCleaning = (targetStock.weightAfterCleaning - (qtyKg * 0.9)).coerceAtLeast(0.0)
                )
                repository.updateStock(updatedStock)

                // Trigger Low Stock Alert if goes below threshold
                if (newQty <= 10.0) {
                    repository.insertAlert(
                        Alert(
                            title = "Critical Low Stock",
                            message = "${targetStock.fishName} is running critically low. Only $newQty kg remaining!",
                            type = "LowStock"
                        )
                    )
                }
            }

            // Update Customer (or insert new if not found)
            val allCusts = customers.value
            val targetCust = allCusts.find { it.name.lowercase() == customerName.lowercase() }
            if (targetCust != null) {
                if (paymentType == "Credit") {
                    val updatedCust = targetCust.copy(
                        creditBookBalance = targetCust.creditBookBalance + totalAmount,
                        pendingAmounts = targetCust.pendingAmounts + totalAmount
                    )
                    repository.updateCustomer(updatedCust)
                }
            } else {
                val newCust = Customer(
                    name = customerName,
                    phone = customerPhone,
                    creditBookBalance = if (paymentType == "Credit") totalAmount else 0.0,
                    pendingAmounts = if (paymentType == "Credit") totalAmount else 0.0
                )
                repository.insertCustomer(newCust)
            }

            // Register Delivery if Pending Delivery
            if (deliveryStatus == "Pending Delivery") {
                val delivery = Delivery(
                    customerName = customerName,
                    address = if (deliveryAddress.isNotEmpty()) deliveryAddress else "Store Pickup / Not Provided",
                    phone = customerPhone,
                    fishDetails = "$fishName - $qtyKg kg",
                    totalAmount = totalAmount,
                    deliveryBoyName = "Suresh Patil", // Assigned default delivery boy
                    deliveryStatus = "Pending"
                )
                repository.insertDelivery(delivery)
            }

            // Create alert
            repository.insertAlert(
                Alert(
                    title = "New Sale Billing",
                    message = "Successfully billed $qtyKg kg of $fishName to $customerName. Total: ₹$totalAmount.",
                    type = "CustomerDue"
                )
            )
        }
    }

    // 3. Add Supplier
    fun addSupplier(name: String, phone: String, details: String, leadTimeDays: Int = 3, contactPerson: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertSupplier(Supplier(name = name, phone = phone, details = details, leadTimeDays = leadTimeDays, contactPerson = contactPerson))
        }
    }

    // 4. Add Customer
    fun addCustomer(name: String, phone: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertCustomer(Customer(name = name, phone = phone))
        }
    }

    // 5. Add Stock Manual
    fun addStockManual(
        shopName: String,
        fishName: String,
        currentStock: Double,
        sellingPrice: Double,
        storageBox: String,
        freshness: String,
        offers: String,
        expiryDate: String,
        category: String = "Fresh",
        fishPhoto: String = ""
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val stockItem = Stock(
                shopName = shopName,
                fishPhoto = fishPhoto,
                fishName = fishName,
                currentStock = currentStock,
                sellingPrice = sellingPrice,
                storageBox = storageBox,
                freshness = freshness,
                weightAfterCleaning = currentStock * 0.9,
                weightLoss = currentStock * 0.1,
                expiryDate = expiryDate,
                offers = offers,
                category = category
            )
            repository.insertStock(stockItem)

            // Trigger alert if freshness is Medium/Old
            if (freshness == "Medium" || freshness == "Old") {
                repository.insertAlert(
                    Alert(
                        title = "Storage Alert: Non-Fresh Stock",
                        message = "$fishName added as '$freshness' quality in $storageBox. Selling price: ₹$sellingPrice.",
                        type = "OldStock"
                    )
                )
            }

            // Trigger low stock alert immediately if currentStock is below threshold
            if (currentStock <= 10.0) {
                repository.insertAlert(
                    Alert(
                        title = "Low Stock Alert: $fishName",
                        message = "$fishName in $storageBox has critical stock level of $currentStock kg. Reorder soon!",
                        type = "LowStock"
                    )
                )
            }
        }
    }

    // 6. Delete Stock
    fun deleteStock(stockItem: Stock) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteStock(stockItem)
        }
    }

    // 7. Add Expense
    fun addExpense(category: String, description: String, amount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertExpense(Expense(category = category, description = description, amount = amount))
        }
    }

    // 8. Delete Expense
    fun deleteExpense(expense: Expense) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteExpense(expense)
        }
    }

    // 9. Add Worker
    fun addWorker(name: String, role: String, salary: Double, phone: String, pin: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertWorker(Worker(name = name, role = role, salary = salary, phone = phone, loginPin = pin))
        }
    }

    // 10. Delete Worker
    fun deleteWorker(worker: Worker) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteWorker(worker)
        }
    }

    // 11. Add Market Price
    fun addMarketPrice(fishName: String, category: String, currentPrice: Double, marketRate: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertMarketPrice(MarketPrice(fishName = fishName, category = category, currentPrice = currentPrice, marketRate = marketRate))
        }
    }

    // 12. Update Delivery Status
    fun updateDeliveryStatus(deliveryId: Int, status: String, deliveryBoyName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val allDels = deliveries.value
            val targetDel = allDels.find { it.id == deliveryId }
            if (targetDel != null) {
                val updatedDel = targetDel.copy(
                    deliveryStatus = status,
                    deliveryBoyName = deliveryBoyName
                )
                repository.updateDelivery(updatedDel)

                // If delivered, log a small alert
                if (status == "Delivered") {
                    repository.insertAlert(
                        Alert(
                            title = "Order Delivered",
                            message = "Order for ${targetDel.customerName} has been successfully delivered by $deliveryBoyName.",
                            type = "CustomerDue"
                        )
                    )
                }
            }
        }
    }

    // 13. Customer Payment Settle (Credit Book)
    fun settleCustomerPayment(customerId: Int, paymentAmount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val allCusts = customers.value
            val targetCust = allCusts.find { it.id == customerId }
            if (targetCust != null) {
                val updatedCust = targetCust.copy(
                    creditBookBalance = (targetCust.creditBookBalance - paymentAmount).coerceAtLeast(0.0),
                    pendingAmounts = (targetCust.pendingAmounts - paymentAmount).coerceAtLeast(0.0)
                )
                repository.updateCustomer(updatedCust)

                // Log as a cash inflow, or a special cash Sale
                repository.insertSale(
                    Sale(
                        fishName = "Payment Settlement",
                        customerName = targetCust.name,
                        qtyKg = 0.0,
                        pricePerKg = 0.0,
                        totalAmount = paymentAmount,
                        paymentType = "Cash",
                        orderType = "Direct",
                        deliveryStatus = "Completed"
                    )
                )

                repository.insertAlert(
                    Alert(
                        title = "Payment Cleared",
                        message = "Customer ${targetCust.name} cleared payment of ₹$paymentAmount. Pending balance: ₹${updatedCust.creditBookBalance}",
                        type = "CustomerDue"
                    )
                )
            }
        }
    }

    // 14. Supplier Payment Settle
    fun settleSupplierPayment(supplierId: Int, paymentAmount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val allSups = suppliers.value
            val targetSup = allSups.find { it.id == supplierId }
            if (targetSup != null) {
                val updatedSup = targetSup.copy(
                    pendingDues = (targetSup.pendingDues - paymentAmount).coerceAtLeast(0.0)
                )
                repository.updateSupplier(updatedSup)

                // Register as an expense
                repository.insertExpense(
                    Expense(
                        category = "Supplier Settlement",
                        description = "Cleared outstanding balance to ${targetSup.name}",
                        amount = paymentAmount
                    )
                )

                repository.insertAlert(
                    Alert(
                        title = "Supplier Paid",
                        message = "Cleared payment of ₹$paymentAmount to ${targetSup.name}. Outstanding: ₹${updatedSup.pendingDues}",
                        type = "SupplierDue"
                    )
                )
            }
        }
    }

    // 14.5 Direct updates for Spreadsheet module
    fun updatePurchase(purchase: Purchase) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updatePurchase(purchase)
        }
    }

    fun updateSale(sale: Sale) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateSale(sale)
        }
    }

    fun updateCustomer(customer: Customer) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateCustomer(customer)
        }
    }

    fun updateSupplier(supplier: Supplier) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateSupplier(supplier)
        }
    }

    fun updateExpense(expense: Expense) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateExpense(expense)
        }
    }

    // 15. Clear all alerts
    fun clearAllAlerts() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllAlerts()
        }
    }

    // 16. Mark single alert as read
    fun markAlertAsRead(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.markAlertAsRead(id)
        }
    }
}
