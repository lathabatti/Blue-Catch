package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.CartItem
import com.example.data.FishProduct
import com.example.data.FishRepository
import com.example.data.Order
import com.example.data.UserSession
import com.example.data.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log

class FishViewModel(private val repository: FishRepository) : ViewModel() {

    private fun getFirebaseAuth(): FirebaseAuth? {
        return try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w("FishViewModel", "Firebase Auth not available, running in local fallback mode: ${e.message}")
            null
        }
    }

    // --- Search & Filtering States ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _recentSearches = MutableStateFlow<List<String>>(
        listOf("Salmon", "Prawns", "Surmai", "Crab")
    )
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    val popularSuggestions = listOf(
        "Salmon Filet",
        "Tiger Prawns",
        "White Pomfret",
        "Surmai (King Fish)",
        "Blue Crab",
        "Lobster",
        "Rohu",
        "Catla"
    )

    fun addRecentSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        _recentSearches.update { current ->
            val list = current.toMutableList()
            list.remove(trimmed)
            list.add(0, trimmed)
            if (list.size > 5) {
                list.take(5)
            } else {
                list
            }
        }
    }

    fun removeRecentSearch(query: String) {
        _recentSearches.update { current ->
            current.filter { it != query }
        }
    }

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // --- OTP State for Simulation UI ---
    private val _sentOtp = MutableStateFlow<String?>(null)
    val sentOtp: StateFlow<String?> = _sentOtp.asStateFlow()

    private val _otpError = MutableStateFlow<String?>(null)
    val otpError: StateFlow<String?> = _otpError.asStateFlow()

    // --- Robust Sign Up Flow States ---
    private val _signUpStep = MutableStateFlow(SignUpStep.FORM)
    val signUpStep: StateFlow<SignUpStep> = _signUpStep.asStateFlow()

    private val _simulatedEmailOtp = MutableStateFlow<String?>(null)
    val simulatedEmailOtp: StateFlow<String?> = _simulatedEmailOtp.asStateFlow()

    private val _simulatedMobileOtp = MutableStateFlow<String?>(null)
    val simulatedMobileOtp: StateFlow<String?> = _simulatedMobileOtp.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    // Temp holding values for registration
    var tempRegName = ""
    var tempRegMobile = ""
    var tempRegEmail = ""
    var tempRegPassword = ""
    var tempRegAddress = ""

    // --- Navigation Helper State ---
    private val _lastPlacedOrderId = MutableStateFlow<Int?>(null)
    val lastPlacedOrderId: StateFlow<Int?> = _lastPlacedOrderId.asStateFlow()

    // --- Wishlist / Favorites States & Actions ---
    private val _wishlistProductIds = MutableStateFlow<Set<Int>>(emptySet())
    val wishlistProductIds: StateFlow<Set<Int>> = _wishlistProductIds.asStateFlow()

    val userSession: StateFlow<UserSession?> = repository.userSession
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private fun getFirestore(): FirebaseFirestore? {
        return try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w("FishViewModel", "Firebase Firestore not available: ${e.message}")
            null
        }
    }

    // Load wishlist from Firestore for a given email
    fun loadWishlistFromFirestore(email: String) {
        val db = getFirestore() ?: return
        if (email.isBlank()) return
        
        viewModelScope.launch {
            try {
                db.collection("wishlists").document(email)
                    .get()
                    .addOnSuccessListener { document ->
                        if (document != null && document.exists()) {
                            val ids = document.get("productIds") as? List<*>
                            val intIds = ids?.mapNotNull { (it as? Number)?.toInt() }?.toSet() ?: emptySet()
                            _wishlistProductIds.value = intIds
                            Log.d("FishViewModel", "Wishlist loaded from Firestore for $email: $intIds")
                        } else {
                            _wishlistProductIds.value = emptySet()
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("FishViewModel", "Error loading wishlist from Firestore", e)
                    }
            } catch (e: Exception) {
                Log.e("FishViewModel", "Firestore load exception", e)
            }
        }
    }

    // Save wishlist to Firestore
    private fun saveWishlistToFirestore(email: String, productIds: Set<Int>) {
        val db = getFirestore() ?: return
        if (email.isBlank()) return
        
        viewModelScope.launch {
            try {
                val data = mapOf("productIds" to productIds.toList())
                db.collection("wishlists").document(email)
                    .set(data)
                    .addOnSuccessListener {
                        Log.d("FishViewModel", "Wishlist saved to Firestore for $email: $productIds")
                    }
                    .addOnFailureListener { e ->
                        Log.e("FishViewModel", "Error saving wishlist to Firestore", e)
                    }
            } catch (e: Exception) {
                Log.e("FishViewModel", "Firestore save exception", e)
            }
        }
    }

    fun toggleFavorite(productId: Int) {
        val current = _wishlistProductIds.value
        val next = if (current.contains(productId)) {
            current - productId
        } else {
            current + productId
        }
        _wishlistProductIds.value = next
        
        // If logged in, save to Firestore
        val email = userSession.value?.email ?: ""
        if (email.isNotBlank() && userSession.value?.isLoggedIn == true) {
            saveWishlistToFirestore(email, next)
        }
    }

    init {
        viewModelScope.launch {
            repository.prepopulateIfEmpty()
        }
        // Observe userSession to automatically load wishlist
        viewModelScope.launch {
            userSession.collect { session ->
                if (session != null && session.isLoggedIn && session.email.isNotBlank()) {
                    loadWishlistFromFirestore(session.email)
                } else {
                    _wishlistProductIds.value = emptySet()
                }
            }
        }
    }

    // --- Streams of Data ---
    val allProducts: StateFlow<List<FishProduct>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredProducts: StateFlow<List<FishProduct>> = combine(
        allProducts,
        searchQuery,
        selectedCategory
    ) { products, query, category ->
        products.filter { prod ->
            val matchesQuery = prod.name.contains(query, ignoreCase = true) ||
                    prod.category.contains(query, ignoreCase = true) ||
                    prod.description.contains(query, ignoreCase = true)
            
            val matchesCategory = when (category) {
                "All" -> true
                "Fresh" -> prod.quality.equals("Fresh", ignoreCase = true)
                "Premium" -> prod.quality.equals("Premium", ignoreCase = true)
                "Frozen" -> prod.quality.equals("Frozen", ignoreCase = true)
                else -> prod.category.equals(category, ignoreCase = true)
            }
            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartItems: StateFlow<List<CartItem>> = repository.cartItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrders: StateFlow<List<Order>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Product Reviews State ---
    private val _productReviews = MutableStateFlow<List<ProductReview>>(
        listOf(
            ProductReview(1, "Aniket Sharma", 5, "Extremely fresh and delicious. Highly recommended!", "10 July 2026"),
            ProductReview(1, "Rohan Das", 4, "Great quality, delivered perfectly on ice.", "09 July 2026"),
            ProductReview(2, "Meera Nair", 5, "Super juicy prawns, perfect for curry!", "08 July 2026"),
            ProductReview(3, "Subhash Gupta", 4, "Good size and nice clean cuts. Will order again.", "07 July 2026"),
            ProductReview(4, "Priya Patel", 5, "Freshly caught pomfret, delicious fry made!", "06 July 2026")
        )
    )
    val productReviews: StateFlow<List<ProductReview>> = _productReviews.asStateFlow()

    fun addProductReview(productId: Int, authorName: String, rating: Int, comment: String) {
        val formatter = java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale.getDefault())
        val dateStr = formatter.format(java.util.Date())
        val newReview = ProductReview(
            productId = productId,
            authorName = authorName.ifBlank { "Anonymous Buyer" },
            rating = rating.coerceIn(1, 5),
            comment = comment.ifBlank { "Great quality product!" },
            date = dateStr
        )
        _productReviews.update { it + newReview }
    }

    fun getOrderFlow(orderId: Int): Flow<Order?> = repository.getOrderById(orderId)

    // --- Actions ---
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun clearPlacedOrderId() {
        _lastPlacedOrderId.value = null
    }

    fun addToCart(product: FishProduct, qty: Double) {
        viewModelScope.launch {
            repository.addToCart(product, qty)
        }
    }

    fun updateCartQty(productId: Int, qty: Double) {
        viewModelScope.launch {
            repository.updateCartItemQty(productId, qty)
        }
    }

    fun removeFromCart(productId: Int) {
        viewModelScope.launch {
            repository.removeFromCart(productId)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    fun buyNow(product: FishProduct, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.clearCart()
            repository.addToCart(product, 1.0)
            onSuccess()
        }
    }

    fun loginOrRegister(name: String, email: String, mobile: String) {
        viewModelScope.launch {
            _otpError.value = null
            val otp = repository.loginOrRegister(name, email, mobile)
            _sentOtp.value = otp
        }
    }

    fun verifyOtp(enteredOtp: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val success = repository.verifyOtp(enteredOtp)
            if (success) {
                _sentOtp.value = null
                _otpError.value = null
                onSuccess()
            } else {
                _otpError.value = "Invalid OTP! Please try again."
            }
        }
    }

    fun resetOtpFlow() {
        _sentOtp.value = null
        _otpError.value = null
    }

    // --- Robust Sign Up Flow Actions ---
    fun startSignUpFlow(name: String, mobile: String, email: String, passwordState: String, address: String, onSuccess: () -> Unit) {
        tempRegName = name
        tempRegMobile = mobile
        tempRegEmail = email
        tempRegPassword = passwordState
        tempRegAddress = address
        _otpError.value = null
        
        // Complete Flow directly: Create account & Login
        viewModelScope.launch {
            val newUser = User(
                name = tempRegName,
                mobile = tempRegMobile,
                email = tempRegEmail,
                password = tempRegPassword,
                address = tempRegAddress,
                isVerified = true
            )
            repository.registerUser(newUser)
            
            // Attempt Firebase Auth sign up in the background if possible
            val auth = getFirebaseAuth()
            if (auth != null && tempRegEmail.isNotBlank()) {
                try {
                    val task = auth.createUserWithEmailAndPassword(tempRegEmail, tempRegPassword)
                    while (!task.isComplete) {
                        delay(100)
                    }
                    if (task.isSuccessful) {
                        val fbUser = task.result?.user
                        if (fbUser != null) {
                            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                .setDisplayName(tempRegName)
                                .build()
                            fbUser.updateProfile(profileUpdates)
                        }
                        Log.d("FishViewModel", "User successfully registered in Firebase Auth.")
                    } else {
                        Log.e("FishViewModel", "Firebase Auth register failed: ${task.exception?.message}")
                    }
                } catch (e: Exception) {
                    Log.e("FishViewModel", "Firebase Auth register error: ${e.message}")
                }
            }

            repository.startSession(tempRegName, tempRegEmail, tempRegMobile)
            _signUpStep.value = SignUpStep.SUCCESS
            onSuccess()
        }
    }

    fun verifyEmailOtp(enteredOtp: String) {
        if (enteredOtp == _simulatedEmailOtp.value) {
            _otpError.value = null
            // Now generate Mobile OTP and go to MOBILE_OTP step
            val mobileOtp = (1000..9999).random().toString()
            _simulatedMobileOtp.value = mobileOtp
            _signUpStep.value = SignUpStep.MOBILE_OTP
        } else {
            _otpError.value = "Invalid Email OTP. Please try again."
        }
    }

    fun verifyMobileOtp(enteredOtp: String, onSuccess: () -> Unit) {
        if (enteredOtp == _simulatedMobileOtp.value) {
            _otpError.value = null
            // Complete Flow: Create account & Login
            viewModelScope.launch {
                val newUser = User(
                    name = tempRegName,
                    mobile = tempRegMobile,
                    email = tempRegEmail,
                    password = tempRegPassword,
                    address = tempRegAddress,
                    isVerified = true
                )
                repository.registerUser(newUser)
                
                // Attempt Firebase Auth sign up in the background if possible
                val auth = getFirebaseAuth()
                if (auth != null && tempRegEmail.isNotBlank()) {
                    try {
                        val task = auth.createUserWithEmailAndPassword(tempRegEmail, tempRegPassword)
                        while (!task.isComplete) {
                            delay(100)
                        }
                        if (task.isSuccessful) {
                            val fbUser = task.result?.user
                            if (fbUser != null) {
                                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                    .setDisplayName(tempRegName)
                                    .build()
                                fbUser.updateProfile(profileUpdates)
                            }
                            Log.d("FishViewModel", "User successfully registered in Firebase Auth.")
                        } else {
                            Log.e("FishViewModel", "Firebase Auth register failed: ${task.exception?.message}")
                        }
                    } catch (e: Exception) {
                        Log.e("FishViewModel", "Firebase Auth register error: ${e.message}")
                    }
                }

                repository.startSession(tempRegName, tempRegEmail, tempRegMobile)
                _signUpStep.value = SignUpStep.SUCCESS
                onSuccess()
            }
        } else {
            _otpError.value = "Invalid Mobile OTP. Please try again."
        }
    }

    fun resetSignUpFlow() {
        _signUpStep.value = SignUpStep.FORM
        _simulatedEmailOtp.value = null
        _simulatedMobileOtp.value = null
        _otpError.value = null
    }

    fun loginWithCredentials(identifier: String, passwordState: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _loginError.value = null
            
            // Attempt to login using Firebase Auth if configured and identifier is an email
            val auth = getFirebaseAuth()
            if (auth != null && identifier.contains("@")) {
                try {
                    val task = auth.signInWithEmailAndPassword(identifier, passwordState)
                    while (!task.isComplete) {
                        delay(50)
                    }
                    if (task.isSuccessful) {
                        val fbUser = task.result?.user
                        if (fbUser != null) {
                            val name = fbUser.displayName ?: fbUser.email?.substringBefore("@") ?: "User"
                            val email = fbUser.email ?: identifier
                            repository.startSession(name, email, "")
                            _loginError.value = null
                            onSuccess()
                            return@launch
                        }
                    } else {
                        val exception = task.exception
                        Log.e("FishViewModel", "Firebase Auth sign-in failed: ${exception?.message}")
                        if (exception is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException) {
                            _loginError.value = "Incorrect password. Please try again."
                            return@launch
                        } else if (exception is com.google.firebase.auth.FirebaseAuthInvalidUserException) {
                            _loginError.value = "Account not found with this email. Please Sign Up!"
                            return@launch
                        }
                    }
                } catch (e: Exception) {
                    Log.e("FishViewModel", "Firebase Auth process error: ${e.message}")
                }
            }

            // Local fallback login using Room DB
            val user = repository.findUserByEmailOrMobile(identifier)
            if (user != null) {
                if (user.password == passwordState) {
                    repository.startSession(user.name, user.email, user.mobile)
                    _loginError.value = null
                    onSuccess()
                } else {
                    _loginError.value = "Incorrect password. Please try again."
                }
            } else {
                _loginError.value = "Account not found with this Email / Mobile. Please Sign Up!"
            }
        }
    }

    fun placeOrder(address: String, mobile: String, paymentMethod: String, onOrderPlaced: (Int) -> Unit) {
        viewModelScope.launch {
            val orderId = repository.placeOrder(address, mobile, paymentMethod)
            if (orderId != -1) {
                _lastPlacedOrderId.value = orderId
                onOrderPlaced(orderId)
                simulateOrderTracking(orderId)
            }
        }
    }

    // Beautiful Background Simulation of Order tracking:
    // Placed -> Preparing -> Out for Delivery -> Delivered (updates every 10-15 seconds)
    private fun simulateOrderTracking(orderId: Int) {
        viewModelScope.launch {
            delay(12000) // 12 seconds
            repository.updateDeliveryStatus(orderId, "Preparing")
            delay(15000) // 15 seconds
            repository.updateDeliveryStatus(orderId, "Out for Delivery")
            delay(18000) // 18 seconds
            repository.updateDeliveryStatus(orderId, "Delivered")
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun uploadProduct(
        name: String,
        pricePerKg: Double,
        discount: Int,
        stockKg: Double,
        quality: String,
        description: String,
        category: String,
        shopName: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val imageRes = when (category) {
                "Shellfish" -> "ic_fish_crab"
                else -> "ic_fish_king"
            }
            val newProd = FishProduct(
                name = name,
                pricePerKg = pricePerKg,
                discount = discount,
                stockKg = stockKg,
                quality = quality,
                description = description,
                imageResName = imageRes,
                category = category,
                shopName = shopName
            )
            repository.addProduct(newProd)
            onSuccess()
        }
    }
}

class FishViewModelFactory(private val repository: FishRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FishViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FishViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

enum class SignUpStep {
    FORM, EMAIL_OTP, MOBILE_OTP, SUCCESS
}

data class ProductReview(
    val productId: Int,
    val authorName: String,
    val rating: Int,
    val comment: String,
    val date: String
)
