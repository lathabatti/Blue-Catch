package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class FishRepository(private val fishDao: FishDao) {

    val allProducts: Flow<List<FishProduct>> = fishDao.getAllProducts()
    val cartItems: Flow<List<CartItem>> = fishDao.getCartItems()
    val userSession: Flow<UserSession?> = fishDao.getUserSessionFlow()
    val allOrders: Flow<List<Order>> = fishDao.getAllOrders()

    fun getOrderById(id: Int): Flow<Order?> = fishDao.getOrderByIdFlow(id)

    suspend fun getProduct(id: Int): FishProduct? = fishDao.getProductById(id)

    suspend fun updateProductStock(id: Int, newStock: Double) = fishDao.updateProductStock(id, newStock)

    // Prepopulate database if empty
    suspend fun prepopulateIfEmpty() {
        val currentProducts = allProducts.first()
        if (currentProducts.isEmpty()) {
            val initial = listOf(
                FishProduct(
                    name = "King Fish",
                    pricePerKg = 450.0,
                    discount = 10,
                    stockKg = 20.0,
                    quality = "Fresh",
                    description = "Premium thick steak cuts. Rich in flavor, firm texture, and perfect for tawa frying or traditional curries.",
                    imageResName = "ic_fish_king",
                    category = "Sea Fish",
                    shopName = "Kochi Harbor Market"
                ),
                FishProduct(
                    name = "Tiger Prawns",
                    pricePerKg = 600.0,
                    discount = 5,
                    stockKg = 15.0,
                    quality = "Fresh",
                    description = "Sweet, juicy tiger prawns. Peeled and deveined, ready for garlic butter toss or authentic spicy masala.",
                    imageResName = "ic_fish_prawns",
                    category = "Shellfish",
                    shopName = "Munambam Seafoods"
                ),
                FishProduct(
                    name = "Atlantic Salmon",
                    pricePerKg = 1200.0,
                    discount = 15,
                    stockKg = 8.0,
                    quality = "Premium",
                    description = "Imported cold-water Salmon fillets. Super rich in Omega-3, perfect for pan-searing, baking, or grilling.",
                    imageResName = "ic_fish_salmon",
                    category = "Sea Fish",
                    shopName = "Atlantic Fresh Catch"
                ),
                FishProduct(
                    name = "White Pomfret",
                    pricePerKg = 750.0,
                    discount = 8,
                    stockKg = 12.0,
                    quality = "Fresh",
                    description = "Whole cleaned delicacy. Highly prized for its white, flaky, sweet meat. Delicious when stuffed or shallow fried.",
                    imageResName = "ic_fish_pomfret",
                    category = "Sea Fish",
                    shopName = "Kochi Harbor Market"
                ),
                FishProduct(
                    name = "Mud Crabs",
                    pricePerKg = 500.0,
                    discount = 12,
                    stockKg = 10.0,
                    quality = "Fresh",
                    description = "Live caught mud crabs. Soft, sweet claws and rich succulent body meat, perfect for authentic crab curry.",
                    imageResName = "ic_fish_crab",
                    category = "Shellfish",
                    shopName = "Bayview Crab Traders"
                ),
                FishProduct(
                    name = "Yellowfin Tuna",
                    pricePerKg = 350.0,
                    discount = 0,
                    stockKg = 25.0,
                    quality = "Fresh",
                    description = "Firm and meaty steak cuts. Great for healthy baking, high protein salads, or robust tuna curry.",
                    imageResName = "ic_fish_tuna",
                    category = "Sea Fish",
                    shopName = "Malabar Oceans"
                ),
                FishProduct(
                    name = "Frozen Reef Cod",
                    pricePerKg = 420.0,
                    discount = 10,
                    stockKg = 18.0,
                    quality = "Frozen",
                    description = "Flash-frozen on board at Kochi Harbor to preserve maximum freshness. Perfect for traditional fish curries or frying.",
                    imageResName = "ic_fish_cod",
                    category = "Sea Fish",
                    shopName = "Harbor Ice Traders"
                ),
                FishProduct(
                    name = "Frozen Squid Rings",
                    pricePerKg = 480.0,
                    discount = 5,
                    stockKg = 14.0,
                    quality = "Frozen",
                    description = "Cleaned and sliced squid rings. Deep-frozen immediately to retain sweet ocean taste, perfect for crispy squid stir-fry.",
                    imageResName = "ic_fish_squid",
                    category = "Shellfish",
                    shopName = "Bayview Ocean Imports"
                )
            )
            fishDao.insertProducts(initial)
        }
        
        // Setup initial guest session if empty
        val currentSession = fishDao.getUserSession()
        if (currentSession == null) {
            fishDao.insertUserSession(UserSession(id = 1, isLoggedIn = false))
        }
    }

    // --- Cart Actions ---
    suspend fun addToCart(product: FishProduct, qtyKg: Double) {
        val existing = cartItems.first().find { it.productId == product.id }
        val finalQty = if (existing != null) existing.quantityKg + qtyKg else qtyKg
        
        // Ensure we don't exceed stock
        val clampedQty = finalQty.coerceAtMost(product.stockKg)
        
        fishDao.insertCartItem(
            CartItem(
                productId = product.id,
                name = product.name,
                pricePerKg = product.pricePerKg,
                discount = product.discount,
                quantityKg = clampedQty,
                imageResName = product.imageResName
            )
        )
    }

    suspend fun updateCartItemQty(productId: Int, qtyKg: Double) {
        val existing = cartItems.first().find { it.productId == productId } ?: return
        if (qtyKg <= 0.0) {
            fishDao.deleteCartItem(productId)
        } else {
            fishDao.insertCartItem(existing.copy(quantityKg = qtyKg))
        }
    }

    suspend fun removeFromCart(productId: Int) {
        fishDao.deleteCartItem(productId)
    }

    suspend fun clearCart() {
        fishDao.clearCart()
    }

    // --- User Session Actions ---
    suspend fun loginOrRegister(name: String, email: String, mobile: String): String {
        // Generate a simple simulated OTP
        val mockOtp = (1000..9999).random().toString()
        val session = UserSession(
            id = 1,
            name = name,
            email = email,
            mobile = mobile,
            isLoggedIn = false,
            tempOtp = mockOtp
        )
        fishDao.insertUserSession(session)
        return mockOtp
    }

    suspend fun verifyOtp(enteredOtp: String): Boolean {
        val session = fishDao.getUserSession() ?: return false
        if (session.tempOtp == enteredOtp && enteredOtp.isNotEmpty()) {
            fishDao.insertUserSession(session.copy(isLoggedIn = true, tempOtp = ""))
            return true
        }
        return false
    }

    suspend fun logout() {
        fishDao.insertUserSession(UserSession(id = 1, isLoggedIn = false))
    }

    // --- Users ---
    suspend fun findUserByEmailOrMobile(identifier: String): User? = fishDao.getUserByEmailOrMobile(identifier)
    suspend fun findUserByEmail(email: String): User? = fishDao.getUserByEmail(email)
    suspend fun findUserByMobile(mobile: String): User? = fishDao.getUserByMobile(mobile)
    suspend fun registerUser(user: User): Long = fishDao.insertUser(user)
    suspend fun startSession(name: String, email: String, mobile: String) {
        fishDao.insertUserSession(UserSession(id = 1, name = name, email = email, mobile = mobile, isLoggedIn = true))
    }

    // --- Orders ---
    suspend fun placeOrder(address: String, mobile: String, paymentMethod: String): Int {
        val items = cartItems.first()
        if (items.isEmpty()) return -1

        val session = fishDao.getUserSession() ?: UserSession()

        val summaryText = items.joinToString(", ") { "${it.name} (${it.quantityKg}kg)" }
        val total = items.sumOf { it.totalCost }

        val order = Order(
            address = address,
            mobile = mobile.ifEmpty { session.mobile },
            paymentMethod = paymentMethod,
            orderSummaryText = summaryText,
            totalAmount = total,
            paymentStatus = if (paymentMethod == "Cash on Delivery") "Pending" else "Paid",
            deliveryStatus = "Placed"
        )

        val orderId = fishDao.insertOrder(order).toInt()

        // Deduct stocks
        for (item in items) {
            val prod = fishDao.getProductById(item.productId)
            if (prod != null) {
                val newStock = (prod.stockKg - item.quantityKg).coerceAtLeast(0.0)
                fishDao.updateProductStock(prod.id, newStock)
            }
        }

        // Clear cart
        fishDao.clearCart()

        return orderId
    }

    suspend fun updateDeliveryStatus(orderId: Int, status: String) {
        fishDao.updateDeliveryStatus(orderId, status)
    }

    suspend fun addProduct(product: FishProduct) {
        fishDao.insertProducts(listOf(product))
    }
}
