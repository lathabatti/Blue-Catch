package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Purchase
import com.example.data.Supplier
import com.example.ui.FishViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PurchaseScreen(
    viewModel: FishViewModel,
    modifier: Modifier = Modifier
) {
    val suppliers by viewModel.suppliers.collectAsState()
    val purchases by viewModel.purchases.collectAsState()

    var showBuyDialog by remember { mutableStateOf(false) }
    var showAddSupplierDialog by remember { mutableStateOf(false) }
    var showSettleDialog by remember { mutableStateOf(false) }
    var targetSupplier by remember { mutableStateOf<Supplier?>(null) }

    // Purchase Dialog state variables
    var selectedSupplier by remember { mutableStateOf<Supplier?>(null) }
    var fishName by remember { mutableStateOf("") }
    var qtyKgText by remember { mutableStateOf("") }
    var pricePerKgText by remember { mutableStateOf("") }
    var selectedQuality by remember { mutableStateOf("Fresh") }
    var isPaymentPending by remember { mutableStateOf(false) }
    var boatName by remember { mutableStateOf("") }
    var buyerName by remember { mutableStateOf("") }
    var buyerPhone by remember { mutableStateOf("") }
    var pendingPurchaseItems by remember { mutableStateOf(listOf<PurchaseItem>()) }

    // Supplier Dialog state variables
    var newSupName by remember { mutableStateOf("") }
    var newSupPhone by remember { mutableStateOf("") }
    var newSupDetails by remember { mutableStateOf("") }
    var settleAmountText by remember { mutableStateOf("") }

    val totalCost = remember(qtyKgText, pricePerKgText, pendingPurchaseItems) {
        val currentQty = qtyKgText.toDoubleOrNull() ?: 0.0
        val currentPrice = pricePerKgText.toDoubleOrNull() ?: 0.0
        val currentTotal = currentQty * currentPrice
        val pendingTotal = pendingPurchaseItems.sumOf { it.qtyKg * it.pricePerKg }
        currentTotal + pendingTotal
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OceanDark)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sourcing Header Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = OceanSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Wholesale Purchases",
                                    color = TealNeon,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Log procurement shipments directly into inventory stock",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Inventory,
                                contentDescription = null,
                                tint = TealNeon.copy(alpha = 0.8f),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (suppliers.isNotEmpty()) {
                                        selectedSupplier = suppliers.first()
                                    }
                                    fishName = ""
                                    qtyKgText = ""
                                    pricePerKgText = ""
                                    selectedQuality = "Fresh"
                                    isPaymentPending = false
                                    boatName = ""
                                    buyerName = ""
                                    buyerPhone = ""
                                    pendingPurchaseItems = emptyList()
                                    showBuyDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TealNeon, contentColor = OceanDark),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(44.dp)
                                    .testTag("open_buy_dialog_button")
                            ) {
                                Icon(Icons.Default.AddShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Buy Fish", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    newSupName = ""
                                    newSupPhone = ""
                                    newSupDetails = ""
                                    showAddSupplierDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = OceanCard, contentColor = TextWhite),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("open_add_supplier_button")
                            ) {
                                Icon(Icons.Default.Business, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Wholesaler", fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Wholesaler Directory
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Wholesale Suppliers Directory",
                        color = TextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = OceanSurface),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${suppliers.size} Suppliers",
                            color = TealNeon,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (suppliers.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = OceanSurface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier.padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.People, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No registered wholesale suppliers found.", color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            } else {
                items(suppliers) { sup ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = OceanSurface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = sup.name,
                                        color = TextWhite,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Phone: ${sup.phone} • ${sup.details}",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Due: ₹${sup.pendingDues.toInt()}",
                                        color = if (sup.pendingDues > 0) OrangeAlert else TealNeon,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Sourced: ₹${sup.totalPurchased.toInt()}",
                                        color = TextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            if (sup.pendingDues > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = {
                                            targetSupplier = sup
                                            settleAmountText = ""
                                            showSettleDialog = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = OceanCard, contentColor = TealNeon),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Settle Dues", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Recent Purchases Ledger
            item {
                Text(
                    text = "Historical Purchase Ledger",
                    color = TextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (purchases.isEmpty()) {
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
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.History, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No purchase orders registered.", color = TextSecondary, fontSize = 13.sp)
                            }
                        }
                    }
                }
            } else {
                items(purchases.reversed()) { purchase ->
                    val sdf = SimpleDateFormat("dd MMM • hh:mm a", Locale.getDefault())
                    Card(
                        colors = CardDefaults.cardColors(containerColor = OceanSurface),
                        shape = RoundedCornerShape(12.dp),
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = purchase.fishName,
                                        color = TextWhite,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = when (purchase.qualityType) {
                                                "Fresh" -> TealNeon.copy(alpha = 0.15f)
                                                "Medium" -> OrangeAlert.copy(alpha = 0.15f)
                                                else -> CoralRed.copy(alpha = 0.15f)
                                            }
                                        ),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = purchase.qualityType,
                                            color = when (purchase.qualityType) {
                                                "Fresh" -> TealNeon
                                                "Medium" -> OrangeAlert
                                                else -> CoralRed
                                            },
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "From: ${purchase.supplierName}" + if (purchase.boatName.isNotEmpty()) " • Boat: ${purchase.boatName}" else "",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                                if (purchase.buyerName.isNotEmpty()) {
                                    Text(
                                        text = "Buyer: ${purchase.buyerName}" + if (purchase.buyerPhone.isNotEmpty()) " (${purchase.buyerPhone})" else "",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                                Text(
                                    text = sdf.format(Date(purchase.purchaseDate)),
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "₹${purchase.totalAmount.toInt()}",
                                    color = TextWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${purchase.qtyKg} kg @ ₹${purchase.pricePerKg.toInt()}/kg",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Purchasing Dialog Form
        if (showBuyDialog) {
            AlertDialog(
                onDismissRequest = { showBuyDialog = false },
                title = { Text("Buy Fish (Add to Stock)", color = TealNeon, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                text = {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            Text("Select Wholesaler", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            if (suppliers.isEmpty()) {
                                Text("No suppliers registered. Please add a wholesaler first.", color = CoralRed, fontSize = 12.sp)
                            } else {
                                suppliers.forEach { sup ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedSupplier = sup }
                                            .background(
                                                if (selectedSupplier?.id == sup.id) OceanCard else Color.Transparent,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(8.dp)
                                    ) {
                                        RadioButton(
                                            selected = selectedSupplier?.id == sup.id,
                                            onClick = { selectedSupplier = sup }
                                        )
                                        Text(sup.name, color = TextWhite, fontSize = 13.sp)
                                    }
                                }
                            }
                        }

                        item {
                            OutlinedTextField(
                                value = buyerName,
                                onValueChange = { buyerName = it },
                                label = { Text("Buyer Name") },
                                textStyle = LocalTextStyle.current.copy(color = TextWhite),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("buy_buyer_name_input")
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = buyerPhone,
                                onValueChange = { buyerPhone = it },
                                label = { Text("Buyer Mobile Number") },
                                textStyle = LocalTextStyle.current.copy(color = TextWhite),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("buy_buyer_phone_input")
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = fishName,
                                onValueChange = { fishName = it },
                                label = { Text("Fish Breed / Name") },
                                textStyle = LocalTextStyle.current.copy(color = TextWhite),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("buy_fish_name_input")
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = boatName,
                                onValueChange = { boatName = it },
                                label = { Text("Boat Name") },
                                textStyle = LocalTextStyle.current.copy(color = TextWhite),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("buy_boat_name_input")
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = qtyKgText,
                                onValueChange = { qtyKgText = it },
                                label = { Text("Weight (kg)") },
                                textStyle = LocalTextStyle.current.copy(color = TextWhite),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("buy_qty_input")
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = pricePerKgText,
                                onValueChange = { pricePerKgText = it },
                                label = { Text("Price per kg (₹)") },
                                textStyle = LocalTextStyle.current.copy(color = TextWhite),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("buy_price_input")
                            )
                        }

                        item {
                            Text("Quality Grade", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Fresh", "Medium", "Old").forEach { q ->
                                    ElevatedFilterChip(
                                        selected = selectedQuality == q,
                                        onClick = { selectedQuality = q },
                                        label = { Text(q) },
                                        colors = FilterChipDefaults.elevatedFilterChipColors(
                                            selectedContainerColor = TealNeon,
                                            selectedLabelColor = OceanDark
                                        ),
                                        modifier = Modifier.weight(1.5f)
                                    )
                                }
                            }
                        }

                        item {
                            val currentQty = qtyKgText.toDoubleOrNull() ?: 0.0
                            val currentPrice = pricePerKgText.toDoubleOrNull() ?: 0.0
                            val isCurrentValid = fishName.isNotBlank() && currentQty > 0.0 && currentPrice > 0.0
                            
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Button(
                                    onClick = {
                                        pendingPurchaseItems = pendingPurchaseItems + PurchaseItem(
                                            fishName = fishName,
                                            qtyKg = currentQty,
                                            pricePerKg = currentPrice,
                                            qualityType = selectedQuality,
                                            boatName = boatName
                                        )
                                        fishName = ""
                                        qtyKgText = ""
                                        pricePerKgText = ""
                                    },
                                    enabled = isCurrentValid,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = TealNeon,
                                        contentColor = OceanDark,
                                        disabledContainerColor = TealNeon.copy(alpha = 0.15f),
                                        disabledContentColor = TextSecondary.copy(alpha = 0.5f)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("add_fish_to_purchase_list_button"),
                                    border = if (isCurrentValid) BorderStroke(1.dp, TealNeon) else null
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Add Fish to Invoice", fontWeight = FontWeight.Black, fontSize = 14.sp)
                                }
                                
                                if (!isCurrentValid) {
                                    Text(
                                        text = "Fill Fish Name, Weight & Price to enable \"Add Fish\"",
                                        color = TextSecondary.copy(alpha = 0.8f),
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(top = 6.dp)
                                    )
                                }
                            }
                        }

                        if (pendingPurchaseItems.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = OceanCard.copy(alpha = 0.5f)),
                                    border = BorderStroke(1.dp, TealNeon.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = "ADDED FISH ITEMS (${pendingPurchaseItems.size})",
                                            color = TealNeon,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        pendingPurchaseItems.forEachIndexed { index, item ->
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = OceanDark),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(10.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(item.fishName, color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                                        Text(
                                                            text = "${item.qtyKg} kg @ ₹${item.pricePerKg.toInt()}/kg (${item.qualityType})",
                                                            color = TextSecondary,
                                                            fontSize = 11.sp
                                                        )
                                                        if (item.boatName.isNotBlank()) {
                                                            Text(
                                                                text = "Boat: ${item.boatName}",
                                                                color = TealNeon.copy(alpha = 0.8f),
                                                                fontSize = 10.sp
                                                            )
                                                        }
                                                    }
                                                    IconButton(
                                                        onClick = {
                                                            pendingPurchaseItems = pendingPurchaseItems.filterIndexed { idx, _ -> idx != index }
                                                        }
                                                    ) {
                                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CoralRed, modifier = Modifier.size(20.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = isPaymentPending,
                                    onCheckedChange = { isPaymentPending = it }
                                )
                                Text("Payment Pending (Add to Dues)", color = TextWhite, fontSize = 13.sp)
                            }
                        }

                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = OceanCard),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Est. Invoice Total:", color = TextSecondary, fontSize = 13.sp)
                                    Text("₹${totalCost.toInt()}", color = TealNeon, fontSize = 16.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    val sup = selectedSupplier
                    val currentQty = qtyKgText.toDoubleOrNull() ?: 0.0
                    val currentPrice = pricePerKgText.toDoubleOrNull() ?: 0.0
                    val isCurrentValid = fishName.isNotBlank() && currentQty > 0.0 && currentPrice > 0.0
                    val hasAnyItems = pendingPurchaseItems.isNotEmpty() || isCurrentValid

                    Button(
                        onClick = {
                            if (sup != null) {
                                var finalItems = pendingPurchaseItems
                                if (isCurrentValid) {
                                    finalItems = finalItems + PurchaseItem(
                                        fishName = fishName,
                                        qtyKg = currentQty,
                                        pricePerKg = currentPrice,
                                        qualityType = selectedQuality,
                                        boatName = boatName
                                    )
                                }
                                finalItems.forEach { item ->
                                    viewModel.buyFish(
                                        supplierId = sup.id,
                                        supplierName = sup.name,
                                        fishName = item.fishName,
                                        qtyKg = item.qtyKg,
                                        pricePerKg = item.pricePerKg,
                                        qualityType = item.qualityType,
                                        paymentStatusPending = isPaymentPending,
                                        boatName = item.boatName,
                                        buyerName = buyerName,
                                        buyerPhone = buyerPhone
                                    )
                                }
                                showBuyDialog = false
                            }
                        },
                        enabled = sup != null && hasAnyItems,
                        colors = ButtonDefaults.buttonColors(containerColor = TealNeon, contentColor = OceanDark)
                    ) {
                        Text("Confirm Purchase", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBuyDialog = false }) {
                        Text("Cancel", color = CoralRed)
                    }
                },
                containerColor = OceanSurface
            )
        }

        // Add Wholesaler Dialog Form
        if (showAddSupplierDialog) {
            AlertDialog(
                onDismissRequest = { showAddSupplierDialog = false },
                title = { Text("Add Wholesale Supplier", color = TealNeon, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = newSupName,
                            onValueChange = { newSupName = it },
                            label = { Text("Supplier/Wholesaler Name") },
                            textStyle = LocalTextStyle.current.copy(color = TextWhite),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("add_supplier_name_input")
                        )
                        OutlinedTextField(
                            value = newSupPhone,
                            onValueChange = { newSupPhone = it },
                            label = { Text("Contact Phone") },
                            textStyle = LocalTextStyle.current.copy(color = TextWhite),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("add_supplier_phone_input")
                        )
                        OutlinedTextField(
                            value = newSupDetails,
                            onValueChange = { newSupDetails = it },
                            label = { Text("Scope (e.g. Deep Sea surmai)") },
                            textStyle = LocalTextStyle.current.copy(color = TextWhite),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("add_supplier_scope_input")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newSupName.isNotBlank() && newSupPhone.isNotBlank()) {
                                viewModel.addSupplier(newSupName, newSupPhone, newSupDetails)
                                showAddSupplierDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealNeon, contentColor = OceanDark)
                    ) {
                        Text("Add Supplier", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddSupplierDialog = false }) {
                        Text("Cancel", color = CoralRed)
                    }
                },
                containerColor = OceanSurface
            )
        }

        // Dues Settlement Dialog
        if (showSettleDialog && targetSupplier != null) {
            val sup = targetSupplier!!
            AlertDialog(
                onDismissRequest = { showSettleDialog = false },
                title = { Text("Settle Supplier Balance", color = TealNeon, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Wholesaler: ${sup.name}", color = TextWhite)
                        Text("Outstanding Balance: ₹${sup.pendingDues.toInt()}", color = OrangeAlert)
                        OutlinedTextField(
                            value = settleAmountText,
                            onValueChange = { settleAmountText = it },
                            label = { Text("Settle Payment Amount (₹)") },
                            textStyle = LocalTextStyle.current.copy(color = TextWhite),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("settle_supplier_amount_input")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amt = settleAmountText.toDoubleOrNull() ?: 0.0
                            if (amt > 0) {
                                viewModel.settleSupplierPayment(sup.id, amt)
                                showSettleDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealNeon, contentColor = OceanDark)
                    ) {
                        Text("Clear Dues", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSettleDialog = false }) {
                        Text("Cancel", color = CoralRed)
                    }
                },
                containerColor = OceanSurface
            )
        }
    }
}

data class PurchaseItem(
    val fishName: String,
    val qtyKg: Double,
    val pricePerKg: Double,
    val qualityType: String,
    val boatName: String
)
