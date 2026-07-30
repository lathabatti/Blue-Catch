package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.Stock
import com.example.ui.UserSession
import com.example.ui.CustomerSession
import com.example.ui.GuestSession
import com.example.ui.FishViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerPortalScreen(
    viewModel: FishViewModel,
    session: UserSession,
    modifier: Modifier = Modifier
) {
    val stockList by viewModel.stock.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val sales by viewModel.sales.collectAsState()
    val alerts by viewModel.alerts.collectAsState()

    val isGuest = session is GuestSession

    // Find detailed DB details of this customer
    val customerDbDetail = if (isGuest) null else customers.find { it.phone.trim() == session.phone.trim() }
    val creditBalance = customerDbDetail?.creditBookBalance ?: 0.0

    // Filter past orders
    val myPastOrders = if (isGuest) emptyList() else sales.filter { 
        it.customerPhone.trim() == session.phone.trim() || 
        it.customerName.lowercase().trim() == session.name.lowercase().trim() 
    }

    // Filter promo notifications
    val promos = alerts.filter { it.type == "StockPromo" }

    // Order Placement Dialog State
    var selectedStockItem by remember { mutableStateOf<Stock?>(null) }
    var orderQtyText by remember { mutableStateOf("") }
    var deliveryAddress by remember { mutableStateOf("") }
    var guestNameText by remember { mutableStateOf("") }
    var guestPhoneText by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var successMsg by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OceanDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Customer Branding & Account Balance Card
            Card(
                colors = CardDefaults.cardColors(containerColor = OceanSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isGuest) "ALL-USERS MARKETPLACE (GUEST)" else "CUSTOMER HUB",
                                color = TealNeon,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isGuest) "Hello, Guest Visitor" else "Hello, ${session.name}",
                                color = TextWhite,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = if (isGuest) "Public live catalog access" else "Phone: ${session.phone}",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        IconButton(
                            onClick = { viewModel.logout() },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = OceanCard, contentColor = CoralRed)
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = "Sign Out")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Ledger status
                    Card(
                        colors = CardDefaults.cardColors(containerColor = OceanCard),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isGuest) "Outstanding Balance Tracker" else "Outstanding Dues Balance",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = if (isGuest) "N/A" else "₹${creditBalance.toInt()}",
                                    color = if (creditBalance > 0) OrangeAlert else TealNeon,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Status Indicator", color = TextSecondary, fontSize = 10.sp)
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isGuest) TealNeon.copy(alpha = 0.15f) else (if (creditBalance > 0) OrangeAlert.copy(alpha = 0.15f) else TealNeon.copy(alpha = 0.15f))
                                    ),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = if (isGuest) "ANONYMOUS VIEWER" else (if (creditBalance > 0) "PENDING RECONCILIATION" else "GOOD STANDING"),
                                        color = if (isGuest) TealNeon else (if (creditBalance > 0) OrangeAlert else TealNeon),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Tabs or Scrollable subsections for Stock list vs Promos vs Orders
            TabRow(
                selectedTabIndex = 0,
                containerColor = Color.Transparent,
                contentColor = TealNeon,
                indicator = {},
                modifier = Modifier.fillMaxWidth()
            ) {
                // Label block
                Text(
                    text = "LIVE MARKETPLACE STOCK",
                    color = TealNeon,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Display Promos/Broadcast Bulletins if any
                if (promos.isNotEmpty()) {
                    item {
                        Text(
                            text = "Special Offers & Bulletins",
                            color = TealNeon,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    items(promos) { promo ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = TealNeon.copy(alpha = 0.1f)),
                            border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Campaign, contentDescription = "Promo Alert", tint = TealNeon, modifier = Modifier.size(24.dp))
                                Column {
                                    Text(promo.title, color = TealNeon, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(promo.message, color = TextWhite, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Available Fresh Catches",
                        color = TextWhite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                if (stockList.filter { it.currentStock > 0 }.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = OceanSurface),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No fresh catch entered by the port office today.", color = TextSecondary, fontSize = 13.sp)
                            }
                        }
                    }
                } else {
                    items(stockList.filter { it.currentStock > 0 }) { item ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = OceanSurface),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("customer_stock_item_${item.fishName}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val context = androidx.compose.ui.platform.LocalContext.current
                                val isCustom = item.fishPhoto.startsWith("content://") || item.fishPhoto.startsWith("file://")
                                val imageModel = remember(item.fishPhoto) {
                                    if (isCustom) {
                                        item.fishPhoto
                                    } else {
                                        val resId = context.resources.getIdentifier(item.fishPhoto, "drawable", context.packageName)
                                        if (resId != 0) resId else com.example.R.drawable.img_fish_generic
                                    }
                                }
                                Card(
                                    modifier = Modifier.size(56.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = OceanCard)
                                ) {
                                    AsyncImage(
                                        model = imageModel,
                                        contentDescription = item.fishName,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = item.fishName,
                                            color = TextWhite,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = when (item.freshness) {
                                                    "Fresh" -> TealNeon.copy(alpha = 0.15f)
                                                    "Medium" -> BlueDeep.copy(alpha = 0.15f)
                                                    else -> CoralRed.copy(alpha = 0.15f)
                                                }
                                            ),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = item.freshness.uppercase(),
                                                color = when (item.freshness) {
                                                    "Fresh" -> TealNeon
                                                    "Medium" -> BlueDeep
                                                    else -> CoralRed
                                                },
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Storage: ${item.storageBox} • Location: ${item.shopName}",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                    if (item.offers.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(Icons.Default.LocalOffer, contentDescription = "Promo", tint = TealNeon, modifier = Modifier.size(12.dp))
                                            Text(item.offers, color = TealNeon, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("₹${item.sellingPrice.toInt()}/kg", color = TealNeon, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Text("${item.currentStock.toInt()} kg left", color = TextSecondary, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Button(
                                        onClick = {
                                            selectedStockItem = item
                                            orderQtyText = ""
                                            deliveryAddress = ""
                                            guestNameText = ""
                                            guestPhoneText = ""
                                            errorMsg = null
                                            successMsg = null
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = TealNeon, contentColor = OceanDark),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("Pre-Order", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Customer Purchase History Section
                item {
                    Text(
                        text = "My Order Ledger",
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                    )
                }

                if (myPastOrders.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = OceanSurface),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isGuest) "No orders yet (Guest Mode). Enter your Name & Phone when booking a pre-order to register instantly!" else "No order ledger rows found for your session.",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(myPastOrders.sortedByDescending { it.saleDate }) { sale ->
                        val df = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                        Card(
                            colors = CardDefaults.cardColors(containerColor = OceanSurface),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(sale.fishName, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Qty: ${sale.qtyKg} kg @ ₹${sale.pricePerKg.toInt()}/kg", color = TextSecondary, fontSize = 11.sp)
                                    Text(df.format(Date(sale.saleDate)), color = TextSecondary, fontSize = 10.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("₹${sale.totalAmount.toInt()}", color = TealNeon, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = when (sale.deliveryStatus) {
                                                "Completed" -> TealNeon.copy(alpha = 0.12f)
                                                "Pending Delivery" -> OrangeAlert.copy(alpha = 0.12f)
                                                else -> TextSecondary.copy(alpha = 0.12f)
                                            }
                                        ),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = sale.deliveryStatus.uppercase(),
                                            color = when (sale.deliveryStatus) {
                                                "Completed" -> TealNeon
                                                "Pending Delivery" -> OrangeAlert
                                                else -> TextWhite
                                            },
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Pre-Order Modal Dialog
        selectedStockItem?.let { item ->
            AlertDialog(
                onDismissRequest = { selectedStockItem = null },
                title = { Text("Place Pre-Order: ${item.fishName}", color = TealNeon, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Selling price is ₹${item.sellingPrice.toInt()}/kg. Available stock: ${item.currentStock} kg.",
                            color = TextWhite,
                            fontSize = 12.sp
                        )

                        if (isGuest) {
                            Text(
                                text = "Guest Identity (Required for Pre-Orders):",
                                color = TealNeon,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            OutlinedTextField(
                                value = guestNameText,
                                onValueChange = { guestNameText = it; errorMsg = null; successMsg = null },
                                label = { Text("Your Name / Business Name") },
                                textStyle = LocalTextStyle.current.copy(color = TextWhite),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("guest_name_input")
                            )
                            OutlinedTextField(
                                value = guestPhoneText,
                                onValueChange = { guestPhoneText = it; errorMsg = null; successMsg = null },
                                label = { Text("Your Phone Number") },
                                textStyle = LocalTextStyle.current.copy(color = TextWhite),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("guest_phone_input")
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        OutlinedTextField(
                            value = orderQtyText,
                            onValueChange = { orderQtyText = it; errorMsg = null; successMsg = null },
                            label = { Text("Desired Quantity (kg)") },
                            textStyle = LocalTextStyle.current.copy(color = TextWhite),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("order_qty_input")
                        )

                        OutlinedTextField(
                            value = deliveryAddress,
                            onValueChange = { deliveryAddress = it; errorMsg = null; successMsg = null },
                            label = { Text("Delivery Address (Empty for Store Pickup)") },
                            textStyle = LocalTextStyle.current.copy(color = TextWhite),
                            singleLine = false,
                            modifier = Modifier.fillMaxWidth().testTag("order_address_input")
                        )

                        val inputQty = orderQtyText.toDoubleOrNull() ?: 0.0
                        if (inputQty > 0.0) {
                            val totalCost = inputQty * item.sellingPrice
                            Card(
                                colors = CardDefaults.cardColors(containerColor = OceanCard),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Estimated Total Cost:", color = TextSecondary, fontSize = 11.sp)
                                    Text("₹${totalCost.toInt()}", color = TealNeon, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }

                        errorMsg?.let { msg ->
                            Text(msg, color = CoralRed, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }

                        successMsg?.let { msg ->
                            Text(msg, color = TealNeon, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val qty = orderQtyText.toDoubleOrNull() ?: 0.0
                            if (qty <= 0.0) {
                                errorMsg = "Please enter a valid quantity."
                                return@Button
                            }
                            if (qty > item.currentStock) {
                                errorMsg = "Only ${item.currentStock} kg is available."
                                return@Button
                            }

                            val finalName = if (isGuest) guestNameText.trim() else session.name
                            val finalPhone = if (isGuest) guestPhoneText.trim() else session.phone

                            if (finalName.isBlank() || finalPhone.isBlank()) {
                                errorMsg = "Please enter your Name and Phone."
                                return@Button
                            }

                            if (isGuest) {
                                // Automatically sign them up so they get added as a customer and upgraded to a CustomerSession!
                                viewModel.signUpCustomer(finalName, finalPhone)
                            }

                            // Place order via ViewModel
                            viewModel.placeCustomerOrder(
                                fishName = item.fishName,
                                qtyKg = qty,
                                pricePerKg = item.sellingPrice,
                                customerName = finalName,
                                customerPhone = finalPhone,
                                deliveryAddress = deliveryAddress
                            )

                            successMsg = "Order placed successfully!"
                            // Auto dismiss dialog after delay or immediately
                            selectedStockItem = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealNeon, contentColor = OceanDark),
                        modifier = Modifier.testTag("confirm_preorder_button")
                    ) {
                        Text("Confirm Order", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedStockItem = null }) {
                        Text("Cancel", color = CoralRed)
                    }
                },
                containerColor = OceanSurface
            )
        }
    }
}
