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
import com.example.data.Sale
import com.example.data.Stock
import com.example.ui.FishViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SalesScreen(
    viewModel: FishViewModel,
    modifier: Modifier = Modifier
) {
    val sales by viewModel.sales.collectAsState()
    val stockList by viewModel.stock.collectAsState()
    val customers by viewModel.customers.collectAsState()

    var showBillDialog by remember { mutableStateOf(false) }

    // Dialog state variables
    var selectedStock by remember { mutableStateOf<Stock?>(null) }
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var qtySoldText by remember { mutableStateOf("") }
    var selectedPaymentType by remember { mutableStateOf("Cash") } // Cash, Credit, UPI
    var selectedOrderType by remember { mutableStateOf("Direct") } // Direct, Hotel, Shop
    var deliveryRequired by remember { mutableStateOf(false) }
    var deliveryAddress by remember { mutableStateOf("") }
    var pendingSalesItems by remember { mutableStateOf(listOf<SalesCartItem>()) }

    // Validation
    val availableQty = selectedStock?.currentStock ?: 0.0
    val qtySold = qtySoldText.toDoubleOrNull() ?: 0.0
    val isQtyValid = qtySold > 0.0 && qtySold <= availableQty
    val sellingPrice = selectedStock?.sellingPrice ?: 0.0
    val currentItemAmount = if (isQtyValid) qtySold * sellingPrice else 0.0
    val billAmount = pendingSalesItems.sumOf { it.qtyKg * it.pricePerKg } + currentItemAmount

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
            // Billing Header
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = OceanSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Sales & Billing Control",
                            color = TealNeon,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Generate commercial customer invoices and dispatch deliveries",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Button(
                            onClick = {
                                if (stockList.isNotEmpty()) {
                                    selectedStock = stockList.firstOrNull { it.currentStock > 0 } ?: stockList.firstOrNull()
                                }
                                customerName = ""
                                customerPhone = ""
                                qtySoldText = ""
                                selectedPaymentType = "Cash"
                                selectedOrderType = "Direct"
                                deliveryRequired = false
                                deliveryAddress = ""
                                pendingSalesItems = emptyList()
                                showBillDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TealNeon, contentColor = OceanDark),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("open_bill_dialog_button")
                        ) {
                            Icon(Icons.Default.Receipt, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create New Invoice / Bill", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Billing ledger list
            item {
                Text(
                    text = "Sales Journal & Invoices",
                    color = TextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (sales.isEmpty()) {
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
                                Icon(Icons.Default.HistoryToggleOff, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No sales bills cut yet.", color = TextSecondary, fontSize = 13.sp)
                            }
                        }
                    }
                }
            } else {
                items(sales) { sale ->
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
                                        text = sale.fishName,
                                        color = TextWhite,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = when (sale.orderType) {
                                                "Hotel" -> Purple80.copy(alpha = 0.15f)
                                                "Shop" -> BlueDeep.copy(alpha = 0.15f)
                                                else -> TealNeon.copy(alpha = 0.15f)
                                            }
                                        ),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = sale.orderType,
                                            color = when (sale.orderType) {
                                                "Hotel" -> Purple80
                                                "Shop" -> BlueDeep
                                                else -> TealNeon
                                            },
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "To: ${sale.customerName}",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = sdf.format(Date(sale.saleDate)),
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "₹${sale.totalAmount.toInt()}",
                                    color = TealNeon,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    text = if (sale.qtyKg > 0) "${sale.qtyKg} kg @ ₹${sale.pricePerKg.toInt()}/kg" else "Payment Settle",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (sale.paymentType == "Credit") OrangeAlert.copy(alpha = 0.15f) else TealNeon.copy(alpha = 0.1f)
                                    ),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(
                                        text = sale.paymentType,
                                        color = if (sale.paymentType == "Credit") OrangeAlert else TealNeon,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Billing Dialog Form
        if (showBillDialog) {
            AlertDialog(
                onDismissRequest = { showBillDialog = false },
                title = { Text("Create Customer Invoice", color = TealNeon, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                text = {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            Text("Select Breed from Stock", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            if (stockList.isEmpty()) {
                                Text("No inventory stock available to sell. Buy fish first.", color = CoralRed, fontSize = 12.sp)
                            } else {
                                stockList.forEach { s ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedStock = s }
                                            .background(
                                                if (selectedStock?.id == s.id) OceanCard else Color.Transparent,
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(6.dp)
                                    ) {
                                        RadioButton(
                                            selected = selectedStock?.id == s.id,
                                            onClick = { selectedStock = s }
                                        )
                                        Column {
                                            Text("${s.fishName} (${s.freshness})", color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            Text("Avail: ${s.currentStock}kg • Box: ${s.storageBox} • ₹${s.sellingPrice.toInt()}/kg", color = TextSecondary, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            // Header for quick-select
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Customer Details", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                if (customers.isNotEmpty()) {
                                    Text("Select registered customer below to auto-fill", color = TealNeon, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            
                            if (customers.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                androidx.compose.foundation.lazy.LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(customers) { c ->
                                        AssistChip(
                                            onClick = {
                                                customerName = c.name
                                                customerPhone = c.phone
                                            },
                                            label = { Text(c.name, color = if (customerName == c.name) OceanDark else TextWhite, fontSize = 10.sp) },
                                            colors = AssistChipDefaults.assistChipColors(
                                                containerColor = if (customerName == c.name) TealNeon else OceanCard
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            OutlinedTextField(
                                value = customerName,
                                onValueChange = { customerName = it },
                                label = { Text("Customer / Business Name") },
                                textStyle = LocalTextStyle.current.copy(color = TextWhite),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("bill_customer_name")
                            )

                            // Autocomplete suggestion list if typing matches existing customers
                            val matches = remember(customerName, customers) {
                                if (customerName.isBlank()) {
                                    emptyList()
                                } else {
                                    customers.filter {
                                        it.name.contains(customerName, ignoreCase = true) &&
                                        !it.name.equals(customerName, ignoreCase = true)
                                    }
                                }
                            }

                            if (matches.isNotEmpty()) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = OceanCard),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(4.dp)) {
                                        matches.take(3).forEach { match ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        customerName = match.name
                                                        customerPhone = match.phone
                                                    }
                                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(match.name, color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                    Text("Phone: ${match.phone}", color = TextSecondary, fontSize = 10.sp)
                                                }
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = TealNeon,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            OutlinedTextField(
                                value = customerPhone,
                                onValueChange = { customerPhone = it },
                                label = { Text("Customer Phone") },
                                textStyle = LocalTextStyle.current.copy(color = TextWhite),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = qtySoldText,
                                onValueChange = { qtySoldText = it },
                                label = { Text("Quantity Sold (kg)") },
                                isError = qtySoldText.isNotEmpty() && !isQtyValid,
                                textStyle = LocalTextStyle.current.copy(color = TextWhite),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                supportingText = {
                                    if (selectedStock != null) {
                                        Text("Max available: ${selectedStock?.currentStock} kg", color = if (isQtyValid) TealNeon else CoralRed)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("bill_qty_input")
                            )
                        }

                        item {
                            Button(
                                onClick = {
                                    if (selectedStock != null && isQtyValid) {
                                        pendingSalesItems = pendingSalesItems + SalesCartItem(
                                            stock = selectedStock!!,
                                            qtyKg = qtySold,
                                            pricePerKg = selectedStock!!.sellingPrice
                                        )
                                        // Reset single item quantity for next input
                                        qtySoldText = ""
                                    }
                                },
                                enabled = selectedStock != null && isQtyValid,
                                colors = ButtonDefaults.buttonColors(containerColor = TealNeon.copy(alpha = 0.2f), contentColor = TealNeon),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("add_fish_to_sale_list_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Fish to Bill", fontWeight = FontWeight.Bold)
                            }
                        }

                        if (pendingSalesItems.isNotEmpty()) {
                            item {
                                Text("Items Added to Invoice (${pendingSalesItems.size})", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                pendingSalesItems.forEachIndexed { index, item ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = OceanCard),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(item.stock.fishName, color = TextWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                Text("${item.qtyKg} kg @ ₹${item.pricePerKg.toInt()}/kg", color = TextSecondary, fontSize = 11.sp)
                                            }
                                            IconButton(
                                                onClick = {
                                                    pendingSalesItems = pendingSalesItems.filterIndexed { idx, _ -> idx != index }
                                                }
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CoralRed, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Text("Payment Mode", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("Cash", "UPI", "Credit").forEach { p ->
                                    ElevatedFilterChip(
                                        selected = selectedPaymentType == p,
                                        onClick = { selectedPaymentType = p },
                                        label = { Text(p) },
                                        colors = FilterChipDefaults.elevatedFilterChipColors(
                                            selectedContainerColor = TealNeon,
                                            selectedLabelColor = OceanDark
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        item {
                            Text("Order Classification", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("Direct", "Hotel", "Shop").forEach { o ->
                                    ElevatedFilterChip(
                                        selected = selectedOrderType == o,
                                        onClick = { selectedOrderType = o },
                                        label = { Text(o) },
                                        colors = FilterChipDefaults.elevatedFilterChipColors(
                                            selectedContainerColor = TealNeon,
                                            selectedLabelColor = OceanDark
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = deliveryRequired,
                                    onCheckedChange = { deliveryRequired = it }
                                )
                                Text("Require Delivery Van Dispatch", color = TextWhite, fontSize = 13.sp)
                            }
                        }

                        if (deliveryRequired) {
                            item {
                                OutlinedTextField(
                                    value = deliveryAddress,
                                    onValueChange = { deliveryAddress = it },
                                    label = { Text("Delivery Address") },
                                    textStyle = LocalTextStyle.current.copy(color = TextWhite),
                                    modifier = Modifier.fillMaxWidth()
                                )
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
                                    Text("Bill Invoice Total:", color = TextSecondary, fontSize = 13.sp)
                                    Text("₹${billAmount.toInt()}", color = TealNeon, fontSize = 18.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    val hasAnyItems = pendingSalesItems.isNotEmpty() || (selectedStock != null && isQtyValid)
                    Button(
                        onClick = {
                            if (customerName.isNotBlank() && hasAnyItems) {
                                var finalSales = pendingSalesItems
                                if (selectedStock != null && isQtyValid) {
                                    finalSales = finalSales + SalesCartItem(
                                        stock = selectedStock!!,
                                        qtyKg = qtySold,
                                        pricePerKg = selectedStock!!.sellingPrice
                                    )
                                }
                                finalSales.forEach { item ->
                                    viewModel.sellFish(
                                        fishName = item.stock.fishName,
                                        customerName = customerName,
                                        customerPhone = customerPhone,
                                        qtyKg = item.qtyKg,
                                        sellingPrice = item.pricePerKg,
                                        paymentType = selectedPaymentType,
                                        orderType = selectedOrderType,
                                        deliveryStatus = if (deliveryRequired) "Pending Delivery" else "Completed",
                                        deliveryAddress = deliveryAddress
                                    )
                                }
                                showBillDialog = false
                            }
                        },
                        enabled = customerName.isNotBlank() && hasAnyItems,
                        colors = ButtonDefaults.buttonColors(containerColor = TealNeon, contentColor = OceanDark)
                    ) {
                        Text("Create Bill & Print", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBillDialog = false }) {
                        Text("Cancel", color = CoralRed)
                    }
                },
                containerColor = OceanSurface
            )
        }
    }
}

data class SalesCartItem(
    val stock: Stock,
    val qtyKg: Double,
    val pricePerKg: Double
)
