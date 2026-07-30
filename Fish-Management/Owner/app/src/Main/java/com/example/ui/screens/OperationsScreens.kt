package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import com.example.data.*
import com.example.ui.FishViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OperationsScreens(
    viewModel: FishViewModel,
    initialTab: Int = 0,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val role = currentUser?.role ?: ""

    var selectedTab by remember { mutableIntStateOf(initialTab) }
    val tabs = listOf("Delivery", "Market Price", "Reports", "Alerts")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OceanDark)
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = OceanSurface,
            contentColor = TealNeon,
            edgePadding = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    if (role == "Owner" || role == "Manager" || role == "Delivery Boy") {
                        DeliveryTab(viewModel)
                    } else {
                        RestrictedAccessView("Van Delivery Dispatch")
                    }
                }
                1 -> {
                    if (role == "Owner" || role == "Manager" || role == "Staff") {
                        MarketPriceTab(viewModel)
                    } else {
                        RestrictedAccessView("Daily Market Price Index")
                    }
                }
                2 -> {
                    if (role == "Owner" || role == "Manager") {
                        ReportsTab(viewModel)
                    } else {
                        RestrictedAccessView("Financial Statements & P&L Reports")
                    }
                }
                3 -> AlertsTab(viewModel)
            }
        }
    }
}

// ================== DELIVERY TAB ==================
@Composable
fun DeliveryTab(viewModel: FishViewModel) {
    val deliveries by viewModel.deliveries.collectAsState()
    val workers by viewModel.workers.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showDispatchDialog by remember { mutableStateOf(false) }
    var targetDelivery by remember { mutableStateOf<Delivery?>(null) }
    var selectedDeliveryBoy by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("Pending") }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Van Dispatch & Delivery Control", color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
            if (deliveries.isEmpty()) {
                item {
                    Text("No deliveries registered yet.", color = TextSecondary, fontSize = 13.sp)
                }
            } else {
                items(deliveries) { del ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = OceanSurface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(del.customerName, color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                    Text("Phone: ${del.phone}", color = TextSecondary, fontSize = 11.sp)
                                }
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = when (del.deliveryStatus) {
                                            "Delivered" -> TealNeon.copy(alpha = 0.15f)
                                            "Out for Delivery" -> BlueDeep.copy(alpha = 0.15f)
                                            else -> OrangeAlert.copy(alpha = 0.15f)
                                        }
                                    ),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = del.deliveryStatus,
                                        color = when (del.deliveryStatus) {
                                            "Delivered" -> TealNeon
                                            "Out for Delivery" -> BlueDeep
                                            else -> OrangeAlert
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Destination: ${del.address}", color = LightIceBlue, fontSize = 12.sp)
                            Text("Details: ${del.fishDetails} (Total: ₹${del.totalAmount.toInt()})", color = TextSecondary, fontSize = 12.sp)

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Rider: ${del.deliveryBoyName.ifEmpty { "Unassigned" }}",
                                    color = TealNeon,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Button(
                                    onClick = {
                                        targetDelivery = del
                                        selectedDeliveryBoy = del.deliveryBoyName.ifEmpty { workers.firstOrNull { it.role == "Delivery Boy" }?.name ?: "" }
                                        selectedStatus = del.deliveryStatus
                                        showDispatchDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = OceanCard, contentColor = TextWhite),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Update Dispatch", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDispatchDialog && targetDelivery != null) {
            val del = targetDelivery!!
            AlertDialog(
                onDismissRequest = { showDispatchDialog = false },
                title = { Text("Update Shipment Details", color = TealNeon) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Order: ${del.fishDetails} for ${del.customerName}", color = TextWhite)

                        Text("Delivery Rider Name", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = selectedDeliveryBoy,
                            onValueChange = { selectedDeliveryBoy = it },
                            placeholder = { Text("Enter Rider or Owner Name", color = TextSecondary) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = TealNeon,
                                unfocusedBorderColor = LightIceBlue.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("delivery_rider_input")
                        )

                        // Quick Assign Owner Option
                        val ownerName = currentUser?.name ?: "Sanju Smily"
                        Button(
                            onClick = { selectedDeliveryBoy = ownerName },
                            colors = ButtonDefaults.buttonColors(containerColor = OceanCard, contentColor = TealNeon),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.fillMaxWidth().testTag("assign_owner_button")
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Assign Owner ($ownerName)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Select Registered Rider", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        if (workers.none { it.role == "Delivery Boy" }) {
                            Text("No delivery boys added. You can add them in the Worker tab or type any custom name above.", color = TextSecondary, fontSize = 12.sp)
                        } else {
                            workers.filter { it.role == "Delivery Boy" }.forEach { boy ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedDeliveryBoy = boy.name }
                                        .background(
                                            if (selectedDeliveryBoy == boy.name) OceanCard else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(4.dp)
                                ) {
                                    RadioButton(
                                        selected = selectedDeliveryBoy == boy.name,
                                        onClick = { selectedDeliveryBoy = boy.name }
                                    )
                                    Text(boy.name, color = TextWhite, fontSize = 13.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Delivery Status", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("Pending", "Out for Delivery", "Delivered").forEach { s ->
                                ElevatedFilterChip(
                                    selected = selectedStatus == s,
                                    onClick = { selectedStatus = s },
                                    label = { Text(s, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.elevatedFilterChipColors(
                                        selectedContainerColor = TealNeon,
                                        selectedLabelColor = OceanDark
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.updateDeliveryStatus(del.id, selectedStatus, selectedDeliveryBoy)
                            showDispatchDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealNeon, contentColor = OceanDark)
                    ) {
                        Text("Save Status")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDispatchDialog = false }) { Text("Cancel", color = CoralRed) }
                },
                containerColor = OceanSurface
            )
        }
    }
}

// ================== MARKET PRICE TAB ==================
@Composable
fun MarketPriceTab(viewModel: FishViewModel) {
    val prices by viewModel.marketPrices.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    var breedName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Fresh Water") } // Fresh Water, Sea Water, Shellfish
    var ourPriceText by remember { mutableStateOf("") }
    var marketRateText by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Daily Market Rates comparison", color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = TealNeon, contentColor = OceanDark)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Update Rate")
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
            items(prices) { price ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = OceanSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(price.fishName, color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = OceanCard),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = price.category,
                                        color = BlueDeep,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text("Wholesale standard index rate", color = TextSecondary, fontSize = 12.sp)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Our: ₹${price.currentPrice.toInt()}/kg", color = TealNeon, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Market: ₹${price.marketRate.toInt()}/kg", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Update Fish Market Price", color = TealNeon) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = breedName,
                            onValueChange = { breedName = it },
                            label = { Text("Fish Breed / Name") },
                            textStyle = LocalTextStyle.current.copy(color = TextWhite),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = ourPriceText,
                            onValueChange = { ourPriceText = it },
                            label = { Text("Our Price (₹ per kg)") },
                            textStyle = LocalTextStyle.current.copy(color = TextWhite),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = marketRateText,
                            onValueChange = { marketRateText = it },
                            label = { Text("General Market Rate (₹ per kg)") },
                            textStyle = LocalTextStyle.current.copy(color = TextWhite),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Classification", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("Fresh Water", "Sea Water", "Shellfish").forEach { c ->
                                ElevatedFilterChip(
                                    selected = selectedCategory == c,
                                    onClick = { selectedCategory = c },
                                    label = { Text(c, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.elevatedFilterChipColors(
                                        selectedContainerColor = TealNeon,
                                        selectedLabelColor = OceanDark
                                    ),
                                    modifier = Modifier.weight(1.3f)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val ourPrice = ourPriceText.toDoubleOrNull() ?: 0.0
                            val mktRate = marketRateText.toDoubleOrNull() ?: 0.0
                            if (breedName.isNotBlank() && ourPrice > 0.0 && mktRate > 0.0) {
                                viewModel.addMarketPrice(breedName, selectedCategory, ourPrice, mktRate)
                                breedName = ""
                                ourPriceText = ""
                                marketRateText = ""
                                showAddDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealNeon, contentColor = OceanDark)
                    ) {
                        Text("Add / Update")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) { Text("Cancel", color = CoralRed) }
                },
                containerColor = OceanSurface
            )
        }
    }
}

// ================== REPORTS TAB ==================
@Composable
fun ReportsTab(viewModel: FishViewModel) {
    val sales by viewModel.sales.collectAsState()
    val stock by viewModel.stock.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val purchases by viewModel.purchases.collectAsState()

    // Aggregates
    val totalSalesAmt = sales.sumOf { it.totalAmount }
    val totalPurchAmt = purchases.sumOf { it.totalAmount }
    val totalExpAmt = expenses.sumOf { it.amount }
    val netProfit = totalSalesAmt - (totalPurchAmt + totalExpAmt)

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        Text("Profit & Loss Statement reports", color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)

        Card(
            colors = CardDefaults.cardColors(containerColor = OceanSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("All-Time Cumulative Summary", color = TealNeon, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Revenue (Sales)", color = TextWhite, fontSize = 13.sp)
                    Text("₹${totalSalesAmt.toInt()}", color = TealNeon, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Fish Sourced Cost", color = TextWhite, fontSize = 13.sp)
                    Text("₹${totalPurchAmt.toInt()}", color = CoralRed, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Logistics & Ice Expenses", color = TextWhite, fontSize = 13.sp)
                    Text("₹${totalExpAmt.toInt()}", color = CoralRed, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Divider(color = OceanCard, modifier = Modifier.padding(vertical = 12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Net Business Profit", color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Black)
                    val color = if (netProfit >= 0) TealNeon else CoralRed
                    Text("₹${netProfit.toInt()}", color = color, fontSize = 18.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        // Expense categories breakdown
        Card(
            colors = CardDefaults.cardColors(containerColor = OceanSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Operational Cost Distribution", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))

                val categories = listOf("Ice Charges", "Transport", "Salaries", "Packing", "Misc")
                categories.forEach { cat ->
                    val sum = expenses.filter { it.category.contains(cat, ignoreCase = true) || (cat == "Salaries" && it.category.contains("Worker", ignoreCase = true)) }.sumOf { it.amount }
                    val percent = if (totalExpAmt > 0) (sum / totalExpAmt * 100).toInt() else 0

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(cat, color = TextSecondary, fontSize = 12.sp)
                        Text("₹${sum.toInt()} ($percent%)", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

// ================== ALERTS TAB ==================
@Composable
fun AlertsTab(viewModel: FishViewModel) {
    val alerts by viewModel.alerts.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Automated Alerts & Logs", color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = { viewModel.clearAllAlerts() }) {
                Text("Clear All", color = CoralRed, fontSize = 12.sp)
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
            if (alerts.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = OceanSurface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("No warning alerts currently active.", color = TextSecondary, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                items(alerts) { alert ->
                    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (alert.isRead) OceanSurface.copy(alpha = 0.5f) else OceanSurface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.markAlertAsRead(alert.id) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = when (alert.type) {
                                        "LowStock" -> Icons.Default.Warning
                                        "OldStock" -> Icons.Default.Info
                                        "CustomerDue" -> Icons.Default.Person
                                        else -> Icons.Default.Business
                                    },
                                    contentDescription = null,
                                    tint = when (alert.type) {
                                        "LowStock" -> OrangeAlert
                                        "OldStock" -> BlueDeep
                                        "CustomerDue" -> TealNeon
                                        else -> OrangeAlert
                                    },
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = alert.title,
                                        color = if (alert.isRead) TextSecondary else TextWhite,
                                        fontSize = 14.sp,
                                        fontWeight = if (alert.isRead) FontWeight.Normal else FontWeight.Bold
                                    )
                                    Text(
                                        text = alert.message,
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = sdf.format(Date(alert.timestamp)),
                                        color = TextSecondary.copy(alpha = 0.6f),
                                        fontSize = 9.sp
                                    )
                                }
                            }

                            if (!alert.isRead) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(TealNeon, RoundedCornerShape(4.dp))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
