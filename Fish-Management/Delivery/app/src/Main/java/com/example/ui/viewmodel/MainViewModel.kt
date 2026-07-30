package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class AppRole {
    object Admin : AppRole()
    data class Delivery(val staffId: Int) : AppRole()
    object Setup : AppRole() // Welcome / Role selection screen
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppRepository(database.appDao())

        // Run pre-population on a background thread
        viewModelScope.launch {
            repository.prePopulateIfEmpty()
        }
    }

    // --- Active User/Role State ---
    private val _currentRole = MutableStateFlow<AppRole>(AppRole.Setup)
    val currentRole: StateFlow<AppRole> = _currentRole.asStateFlow()

    private val _selectedStaffForLogin = MutableStateFlow<DeliveryStaff?>(null)
    val selectedStaffForLogin: StateFlow<DeliveryStaff?> = _selectedStaffForLogin.asStateFlow()

    fun switchRole(role: AppRole) {
        _currentRole.value = role
    }

    fun selectStaffForLogin(staff: DeliveryStaff?) {
        _selectedStaffForLogin.value = staff
    }

    // --- Search & Filter States ---
    private val _userSearchQuery = MutableStateFlow("")
    val userSearchQuery: StateFlow<String> = _userSearchQuery.asStateFlow()

    private val _ownerSearchQuery = MutableStateFlow("")
    val ownerSearchQuery: StateFlow<String> = _ownerSearchQuery.asStateFlow()

    private val _deliverySearchQuery = MutableStateFlow("")
    val deliverySearchQuery: StateFlow<String> = _deliverySearchQuery.asStateFlow()

    fun setUserSearchQuery(query: String) {
        _userSearchQuery.value = query
    }

    fun setOwnerSearchQuery(query: String) {
        _ownerSearchQuery.value = query
    }

    fun setDeliverySearchQuery(query: String) {
        _deliverySearchQuery.value = query
    }

    // --- Live Data Streams from Repository ---
    val allOrders: StateFlow<List<DeliveryOrder>> = repository.allDeliveryOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<User>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOwners: StateFlow<List<Owner>> = repository.allOwners
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDeliveryStaff: StateFlow<List<DeliveryStaff>> = repository.allDeliveryStaff
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allComplaints: StateFlow<List<Complaint>> = repository.allComplaints
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotifications: StateFlow<List<Notification>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val systemSettings: StateFlow<SystemSettings> = repository.settingsFlow
        .map { it ?: SystemSettings() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SystemSettings())

    // --- Live Analytics Streams ---
    val totalCustomers: StateFlow<Int> = repository.totalCustomersCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalOwners: StateFlow<Int> = repository.totalOwnersCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalDeliveryStaff: StateFlow<Int> = repository.totalDeliveryStaffCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalOrders: StateFlow<Int> = repository.totalOrdersCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalSales: StateFlow<Double> = repository.totalSalesAmount
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Derived states
    val activeUsersCount: StateFlow<Int> = combine(allUsers, allOwners, allDeliveryStaff) { users, owners, staff ->
        val activeUsers = users.count { it.status == "Active" }
        val activeOwners = owners.count { it.status == "Approved" }
        val activeStaff = staff.count { it.status == "Active" }
        activeUsers + activeOwners + activeStaff
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)


    // --- Delivery Module Operations ---
    fun updateDeliveryStatus(deliveryId: Int, status: String) {
        viewModelScope.launch {
            val deliveredTime = if (status == "Delivered") System.currentTimeMillis() else null
            repository.updateDeliveryStatus(deliveryId, status, deliveredTime)
            
            // Create notification for status change
            val order = repository.getDeliveryOrderById(deliveryId)
            if (order != null) {
                repository.insertNotification(
                    Notification(
                        title = "Order $status",
                        message = "Fish delivery order ${order.orderId} is now $status.",
                        type = "Status Update"
                    )
                )
            }
        }
    }

    fun assignDriverToRoute(deliveryId: Int, staffId: Int) {
        viewModelScope.launch {
            val order = repository.getDeliveryOrderById(deliveryId)
            val driver = allDeliveryStaff.value.find { it.id == staffId }
            if (order != null && driver != null) {
                val updatedOrder = order.copy(
                    deliveryStaffId = staffId,
                    deliveryStatus = "Assigned",
                    assignedTime = System.currentTimeMillis()
                )
                repository.updateDeliveryOrder(updatedOrder)
                
                // Create notification for route assignment
                repository.insertNotification(
                    Notification(
                        title = "Route Updated: ${order.orderId}",
                        message = "Order ${order.orderId} for ${order.customerName} has been assigned to driver ${driver.name} (Route: ${driver.assignedArea}).",
                        type = "Route Update"
                    )
                )
            }
        }
    }

    fun updateDeliveryNotes(deliveryId: Int, notes: String) {
        viewModelScope.launch {
            repository.updateDeliveryNotes(deliveryId, notes)
        }
    }


    // --- Admin Module: User Management ---
    fun toggleUserStatus(user: User) {
        viewModelScope.launch {
            val newStatus = if (user.status == "Active") "Blocked" else "Active"
            repository.updateUserStatus(user.id, newStatus)
        }
    }

    fun deleteUser(userId: Int) {
        viewModelScope.launch {
            repository.deleteUser(userId)
        }
    }

    fun createCustomer(name: String, mobile: String, address: String) {
        viewModelScope.launch {
            repository.insertUser(User(name = name, mobile = mobile, address = address))
        }
    }


    // --- Admin Module: Owner Management ---
    fun toggleOwnerStatus(owner: Owner) {
        viewModelScope.launch {
            val newStatus = when (owner.status) {
                "Approved" -> "Blocked"
                "Pending" -> "Approved"
                else -> "Approved"
            }
            repository.updateOwnerStatus(owner.id, newStatus)
        }
    }

    fun approveOwner(ownerId: Int) {
        viewModelScope.launch {
            repository.updateOwnerStatus(ownerId, "Approved")
        }
    }

    fun createOwner(name: String, shopName: String) {
        viewModelScope.launch {
            repository.insertOwner(Owner(name = name, shopName = shopName, status = "Approved"))
        }
    }


    // --- Admin Module: Delivery Staff Management ---
    fun addDeliveryStaff(name: String, phone: String, assignedArea: String) {
        viewModelScope.launch {
            repository.insertDeliveryStaff(
                DeliveryStaff(name = name, phone = phone, assignedArea = assignedArea)
            )
        }
    }

    fun updateDeliveryStaff(staff: DeliveryStaff) {
        viewModelScope.launch {
            repository.insertDeliveryStaff(staff)
        }
    }

    fun deleteDeliveryStaff(staffId: Int) {
        viewModelScope.launch {
            repository.deleteDeliveryStaffById(staffId)
        }
    }


    // --- Admin Module: Complaint Management ---
    fun resolveComplaint(complaintId: Int) {
        viewModelScope.launch {
            repository.updateComplaintStatus(complaintId, "Resolved")
        }
    }

    fun submitComplaint(senderName: String, role: String, complaintText: String) {
        viewModelScope.launch {
            repository.insertComplaint(
                Complaint(senderName = senderName, role = role, complaintText = complaintText)
            )
        }
    }


    // --- Admin Module: Notifications ---
    fun sendNotification(title: String, message: String, type: String) {
        viewModelScope.launch {
            repository.insertNotification(
                Notification(title = title, message = message, type = type)
            )
        }
    }


    // --- Admin Module: Settings ---
    fun updateSettings(taxRate: Double, deliveryCharge: Double, backupFreq: String, gateway: String) {
        viewModelScope.launch {
            repository.saveSettings(
                SystemSettings(
                    id = 1,
                    taxRate = taxRate,
                    deliveryCharge = deliveryCharge,
                    backupFrequency = backupFreq,
                    paymentGateway = gateway
                )
            )
        }
    }


    // --- Simulation Utilities ---
    // Easily place a simulated new customer order so delivery and admin staff can instantly interact with it!
    fun placeSimulatedOrder(
        customerName: String,
        fishName: String,
        quantity: Double,
        pricePerKg: Double,
        paymentStatus: String,
        staffId: Int
    ) {
        viewModelScope.launch {
            val amount = quantity * pricePerKg
            val randomNum = (100..999).random()
            val orderId = "ORD-2026-$randomNum"
            val deliveryAddress = when (customerName) {
                "Rajesh Patel" -> "A-124, Shanti Nagar, Delhi"
                "Anjali Sharma" -> "H-45, Regency Enclave, Kolkata"
                "Pooja Hegde" -> "Penthouse 3, Sky Gardens, Bengaluru"
                else -> "G-909, Coastal Sands Colony, Mumbai"
            }
            val mobile = when (customerName) {
                "Rajesh Patel" -> "+91 91234 56789"
                "Anjali Sharma" -> "+91 95432 10987"
                "Pooja Hegde" -> "+91 82345 67890"
                else -> "+91 90000 11122"
            }

            val priorities = listOf("High", "Medium", "Low")
            val newOrder = DeliveryOrder(
                orderId = orderId,
                deliveryStaffId = staffId,
                customerName = customerName,
                mobileNumber = mobile,
                deliveryAddress = deliveryAddress,
                fishName = fishName,
                quantity = quantity,
                amount = amount,
                paymentStatus = paymentStatus,
                deliveryStatus = "Assigned",
                assignedTime = System.currentTimeMillis(),
                priority = priorities.random()
            )
            repository.insertDeliveryOrder(newOrder)
        }
    }
}
