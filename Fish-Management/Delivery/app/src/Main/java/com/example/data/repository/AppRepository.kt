package com.example.data.repository

import com.example.data.dao.AppDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class AppRepository(private val appDao: AppDao) {

    // --- Delivery Orders ---
    val allDeliveryOrders: Flow<List<DeliveryOrder>> = appDao.getAllDeliveryOrders()

    fun getOrdersForStaff(staffId: Int): Flow<List<DeliveryOrder>> = appDao.getOrdersForStaff(staffId)

    suspend fun insertDeliveryOrder(order: DeliveryOrder) = appDao.insertDeliveryOrder(order)

    suspend fun getDeliveryOrderById(deliveryId: Int): DeliveryOrder? = appDao.getDeliveryOrderById(deliveryId)

    suspend fun updateDeliveryOrder(order: DeliveryOrder) = appDao.updateDeliveryOrder(order)

    suspend fun updateDeliveryStatus(deliveryId: Int, status: String, deliveredTime: Long?) {
        val order = appDao.getDeliveryOrderById(deliveryId)
        if (order != null) {
            val now = System.currentTimeMillis()
            val baseHistory = if (order.statusHistory.isEmpty()) "Assigned:${order.assignedTime}" else order.statusHistory
            val updatedHistory = "$baseHistory\n$status:$now"
            
            val updatedOrder = order.copy(
                deliveryStatus = status,
                deliveredTime = deliveredTime,
                statusHistory = updatedHistory
            )
            appDao.updateDeliveryOrder(updatedOrder)
        } else {
            appDao.updateDeliveryStatus(deliveryId, status, deliveredTime)
        }
    }

    suspend fun updateDeliveryNotes(deliveryId: Int, notes: String) {
        val order = appDao.getDeliveryOrderById(deliveryId)
        if (order != null) {
            val updatedOrder = order.copy(deliveryNotes = notes)
            appDao.updateDeliveryOrder(updatedOrder)
        }
    }

    // --- Users (Customers) ---
    val allUsers: Flow<List<User>> = appDao.getAllUsers()

    suspend fun insertUser(user: User) = appDao.insertUser(user)

    suspend fun updateUser(user: User) = appDao.updateUser(user)

    suspend fun updateUserStatus(userId: Int, status: String) = appDao.updateUserStatus(userId, status)

    suspend fun deleteUser(userId: Int) = appDao.deleteUser(userId)

    // --- Owners ---
    val allOwners: Flow<List<Owner>> = appDao.getAllOwners()

    suspend fun insertOwner(owner: Owner) = appDao.insertOwner(owner)

    suspend fun updateOwner(owner: Owner) = appDao.updateOwner(owner)

    suspend fun updateOwnerStatus(ownerId: Int, status: String) = appDao.updateOwnerStatus(ownerId, status)

    // --- Delivery Staff ---
    val allDeliveryStaff: Flow<List<DeliveryStaff>> = appDao.getAllDeliveryStaff()

    suspend fun insertDeliveryStaff(staff: DeliveryStaff) = appDao.insertDeliveryStaff(staff)

    suspend fun updateDeliveryStaff(staff: DeliveryStaff) = appDao.updateDeliveryStaff(staff)

    suspend fun deleteDeliveryStaff(staff: DeliveryStaff) = appDao.deleteDeliveryStaff(staff)

    suspend fun deleteDeliveryStaffById(staffId: Int) = appDao.deleteDeliveryStaffById(staffId)

    // --- Complaints ---
    val allComplaints: Flow<List<Complaint>> = appDao.getAllComplaints()

    suspend fun insertComplaint(complaint: Complaint) = appDao.insertComplaint(complaint)

    suspend fun updateComplaintStatus(complaintId: Int, status: String) = appDao.updateComplaintStatus(complaintId, status)

    // --- Notifications ---
    val allNotifications: Flow<List<Notification>> = appDao.getAllNotifications()

    suspend fun insertNotification(notification: Notification) = appDao.insertNotification(notification)

    // --- Settings ---
    val settingsFlow: Flow<SystemSettings?> = appDao.getSettingsFlow()

    suspend fun getSettings(): SystemSettings? = appDao.getSettings()

    suspend fun saveSettings(settings: SystemSettings) = appDao.saveSettings(settings)

    // --- Analytics ---
    val totalCustomersCount: Flow<Int> = appDao.getTotalCustomersCount()
    val totalOwnersCount: Flow<Int> = appDao.getTotalOwnersCount()
    val totalDeliveryStaffCount: Flow<Int> = appDao.getTotalDeliveryStaffCount()
    val totalOrdersCount: Flow<Int> = appDao.getTotalOrdersCount()
    val totalSalesAmount: Flow<Double?> = appDao.getTotalSalesAmount()

    // --- Pre-populate with beautiful, realistic mock data on first-launch ---
    suspend fun prePopulateIfEmpty() {
        val currentStaffList = appDao.getAllDeliveryStaff().firstOrNull() ?: emptyList()
        if (currentStaffList.isNotEmpty()) {
            return // Database is already populated
        }

        // 1. Setup Default Settings
        appDao.saveSettings(SystemSettings())

        // 2. Setup Delivery Staff
        val staff1 = DeliveryStaff(id = 1, name = "Sanjay Kumar", phone = "+91 98765 43210", assignedArea = "Connaught Place, New Delhi", status = "Active")
        val staff2 = DeliveryStaff(id = 2, name = "Vikram Rathore", phone = "+91 99998 88877", assignedArea = "Salt Lake, Kolkata", status = "Active")
        val staff3 = DeliveryStaff(id = 3, name = "Meera Sen", phone = "+91 88877 66655", assignedArea = "Indiranagar, Bengaluru", status = "Active")
        appDao.insertDeliveryStaff(staff1)
        appDao.insertDeliveryStaff(staff2)
        appDao.insertDeliveryStaff(staff3)

        // 3. Setup Owners
        val owner1 = Owner(id = 1, name = "Senthil Kumar", shopName = "Senthil Fresh Catch Mart", status = "Approved")
        val owner2 = Owner(id = 2, name = "Maria D'Souza", shopName = "Goan Ocean Harvest", status = "Approved")
        val owner3 = Owner(id = 3, name = "Abdul Rahman", shopName = "Royal Coastal Seafoods", status = "Pending")
        appDao.insertOwner(owner1)
        appDao.insertOwner(owner2)
        appDao.insertOwner(owner3)

        // 4. Setup Users (Customers)
        val user1 = User(id = 1, name = "Rajesh Patel", mobile = "+91 91234 56789", address = "A-124, Shanti Nagar, Delhi", status = "Active")
        val user2 = User(id = 2, name = "Anjali Sharma", mobile = "+91 95432 10987", address = "H-45, Regency Enclave, Kolkata", status = "Active")
        val user3 = User(id = 3, name = "Pooja Hegde", mobile = "+91 82345 67890", address = "Penthouse 3, Sky Gardens, Bengaluru", status = "Active")
        val user4 = User(id = 4, name = "Amit Verma", mobile = "+91 76543 21098", address = "Plot 89, Sector 15, Gurugram", status = "Blocked")
        appDao.insertUser(user1)
        appDao.insertUser(user2)
        appDao.insertUser(user3)
        appDao.insertUser(user4)

        // 5. Setup Delivery Orders
        // Let's create a mix of statuses: Assigned, Picked Up, Out for Delivery, Delivered
        val order1 = DeliveryOrder(
            deliveryId = 1,
            orderId = "ORD-2026-001",
            deliveryStaffId = 1, // Sanjay Kumar
            customerName = "Rajesh Patel",
            mobileNumber = "+91 91234 56789",
            deliveryAddress = "A-124, Shanti Nagar, Delhi",
            fishName = "Rohu Fish (Bengali Cut)",
            quantity = 2.5,
            amount = 625.0,
            paymentStatus = "Paid",
            deliveryStatus = "Out for Delivery",
            assignedTime = System.currentTimeMillis() - 7200000, // 2 hours ago
            priority = "High"
        )

        val order2 = DeliveryOrder(
            deliveryId = 2,
            orderId = "ORD-2026-002",
            deliveryStaffId = 1, // Sanjay Kumar
            customerName = "Amit Verma",
            mobileNumber = "+91 76543 21098",
            deliveryAddress = "Plot 89, Sector 15, Gurugram",
            fishName = "Premium Tiger Prawns",
            quantity = 1.0,
            amount = 850.0,
            paymentStatus = "Cash on Delivery",
            deliveryStatus = "Assigned",
            assignedTime = System.currentTimeMillis() - 1800000, // 30 mins ago
            priority = "Medium"
        )

        val order3 = DeliveryOrder(
            deliveryId = 3,
            orderId = "ORD-2026-003",
            deliveryStaffId = 2, // Vikram Rathore
            customerName = "Anjali Sharma",
            mobileNumber = "+91 95432 10987",
            deliveryAddress = "H-45, Regency Enclave, Kolkata",
            fishName = "Hilsa (Ilish) Premium",
            quantity = 1.5,
            amount = 1800.0,
            paymentStatus = "Paid",
            deliveryStatus = "Delivered",
            assignedTime = System.currentTimeMillis() - 86400000, // 1 day ago
            deliveredTime = System.currentTimeMillis() - 80000000,
            priority = "Low"
        )

        val order4 = DeliveryOrder(
            deliveryId = 4,
            orderId = "ORD-2026-004",
            deliveryStaffId = 3, // Meera Sen
            customerName = "Pooja Hegde",
            mobileNumber = "+91 82345 67890",
            deliveryAddress = "Penthouse 3, Sky Gardens, Bengaluru",
            fishName = "Atlantic Salmon Steaks",
            quantity = 1.2,
            amount = 1450.0,
            paymentStatus = "Pending",
            deliveryStatus = "Picked Up",
            assignedTime = System.currentTimeMillis() - 3600000, // 1 hour ago
            priority = "High"
        )
        appDao.insertDeliveryOrder(order1)
        appDao.insertDeliveryOrder(order2)
        appDao.insertDeliveryOrder(order3)
        appDao.insertDeliveryOrder(order4)

        // 6. Setup Complaints
        val complaint1 = Complaint(
            senderName = "Rajesh Patel",
            role = "Customer",
            complaintText = "My delivery is showing Out for Delivery for 2 hours now. Can I get an update?",
            status = "Pending"
        )
        val complaint2 = Complaint(
            senderName = "Senthil Kumar",
            role = "Owner",
            complaintText = "Delivery staff Sanjay Kumar arrived late for order collection today.",
            status = "Resolved"
        )
        appDao.insertComplaint(complaint1)
        appDao.insertComplaint(complaint2)

        // 7. Setup Notifications
        val notif1 = Notification(
            title = "Monsoon Seafood Feast!",
            message = "Special discounts of up to 20% on freshwater Rohu and Catla this weekend.",
            type = "Promotion"
        )
        val notif2 = Notification(
            title = "App Update v1.4 Live",
            message = "You can now track GPS coordinates of your delivery executives directly from the dashboard.",
            type = "Announcement"
        )
        appDao.insertNotification(notif1)
        appDao.insertNotification(notif2)
    }
}
