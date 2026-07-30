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
import java.util.Date
import java.util.Locale

@Composable
fun ManagementScreens(
    viewModel: FishViewModel,
    initialTab: Int = 0,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val role = currentUser?.role ?: ""

    if (role != "Owner" && role != "Manager") {
        RestrictedAccessView("Administrative Ledger Control Panel")
        return
    }

    var selectedTab by remember { mutableIntStateOf(initialTab) }
    LaunchedEffect(initialTab) {
        selectedTab = initialTab
    }
    val tabs = listOf("Customers", "Suppliers", "Staff", "Expenses")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(OceanDark)
    ) {
        // Tab Selection Row
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
                0 -> CustomerTab(viewModel)
                1 -> SupplierTab(viewModel)
                2 -> StaffTab(viewModel)
                3 -> ExpensesTab(viewModel)
            }
        }
    }
}

// ================== CUSTOMER TAB ==================
@Composable
fun CustomerTab(viewModel: FishViewModel) {
    val customers by viewModel.customers.collectAsState()
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var showSettleDialog by remember { mutableStateOf(false) }
    var targetCustomer by remember { mutableStateOf<Customer?>(null) }

    // Forms state
    var custName by remember { mutableStateOf("") }
    var custPhone by remember { mutableStateOf("") }
    var settleAmountText by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    val filteredCustomers = remember(searchQuery, customers) {
        if (searchQuery.isBlank()) {
            customers
        } else {
            customers.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.phone.contains(searchQuery)
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Customer Credit Ledger", color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Button(
                onClick = { showAddCustomerDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = TealNeon, contentColor = OceanDark)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Customer")
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by customer name or phone...", color = TextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TealNeon) },
            textStyle = LocalTextStyle.current.copy(color = TextWhite),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
            items(filteredCustomers) { cust ->
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
                            Text(cust.name, color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("Phone: ${cust.phone}", color = TextSecondary, fontSize = 12.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Owes: ₹${cust.creditBookBalance.toInt()}", color = OrangeAlert, fontSize = 14.sp, fontWeight = FontWeight.Black)
                            if (cust.creditBookBalance > 0) {
                                Button(
                                    onClick = {
                                        targetCustomer = cust
                                        settleAmountText = ""
                                        showSettleDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = TealNeon, contentColor = OceanDark),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .testTag("settle_cust_${cust.id}")
                                ) {
                                    Text("Settle Payment", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAddCustomerDialog) {
            AlertDialog(
                onDismissRequest = { showAddCustomerDialog = false },
                title = { Text("Add New Customer", color = TealNeon) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = custName,
                            onValueChange = { custName = it },
                            label = { Text("Customer Name") },
                            textStyle = LocalTextStyle.current.copy(color = TextWhite),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = custPhone,
                            onValueChange = { custPhone = it },
                            label = { Text("Phone Number") },
                            textStyle = LocalTextStyle.current.copy(color = TextWhite),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (custName.isNotBlank() && custPhone.isNotBlank()) {
                                viewModel.addCustomer(custName, custPhone)
                                custName = ""
                                custPhone = ""
                                showAddCustomerDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealNeon, contentColor = OceanDark)
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddCustomerDialog = false }) { Text("Cancel", color = CoralRed) }
                },
                containerColor = OceanSurface
            )
        }

        if (showSettleDialog && targetCustomer != null) {
            val cust = targetCustomer!!
            AlertDialog(
                onDismissRequest = { showSettleDialog = false },
                title = { Text("Settle Customer Payment", color = TealNeon) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Customer Name: ${cust.name}", color = TextWhite)
                        Text("Outstanding Credit: ₹${cust.creditBookBalance.toInt()}", color = OrangeAlert)
                        OutlinedTextField(
                            value = settleAmountText,
                            onValueChange = { settleAmountText = it },
                            label = { Text("Payment Amount (₹)") },
                            textStyle = LocalTextStyle.current.copy(color = TextWhite),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settle_cust_amount_input")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amt = settleAmountText.toDoubleOrNull() ?: 0.0
                            if (amt > 0) {
                                viewModel.settleCustomerPayment(cust.id, amt)
                                showSettleDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealNeon, contentColor = OceanDark)
                    ) {
                        Text("Clear Dues")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSettleDialog = false }) { Text("Cancel", color = CoralRed) }
                },
                containerColor = OceanSurface
            )
        }
    }
}

// ================== SUPPLIER TAB ==================
@Composable
fun SupplierTab(viewModel: FishViewModel) {
    val suppliers by viewModel.suppliers.collectAsState()
    val purchases by viewModel.purchases.collectAsState()

    var showAddSupplierDialog by remember { mutableStateOf(false) }
    var showSettleDialog by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }

    var targetSupplier by remember { mutableStateOf<Supplier?>(null) }
    var selectedSupplierForDetails by remember { mutableStateOf<Supplier?>(null) }

    // Forms
    var supName by remember { mutableStateOf("") }
    var supPhone by remember { mutableStateOf("") }
    var supDetails by remember { mutableStateOf("") }
    var supLeadTimeText by remember { mutableStateOf("3") }
    var supContactPerson by remember { mutableStateOf("") }
    var settleAmountText by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    val filteredSuppliers = remember(searchQuery, suppliers) {
        if (searchQuery.isBlank()) {
            suppliers
        } else {
            suppliers.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.phone.contains(searchQuery) ||
                it.details.contains(searchQuery, ignoreCase = true) ||
                it.contactPerson.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Wholesale Suppliers", color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Button(
                onClick = { showAddSupplierDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = TealNeon, contentColor = OceanDark)
            ) {
                Icon(Icons.Default.Business, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Supplier")
            }
        }

        // Search Bar for Suppliers
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by supplier name, phone, or scope...", color = TextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TealNeon) },
            textStyle = LocalTextStyle.current.copy(color = TextWhite),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
            items(filteredSuppliers) { sup ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = OceanSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedSupplierForDetails = sup
                            showDetailsDialog = true
                        }
                        .testTag("supplier_card_${sup.id}")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(sup.name, color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                if (sup.contactPerson.isNotEmpty()) {
                                    Text("Contact: ${sup.contactPerson} • Phone: ${sup.phone}", color = TextSecondary, fontSize = 12.sp)
                                } else {
                                    Text("Phone: ${sup.phone}", color = TextSecondary, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Scope: ${sup.details} • Lead: ${sup.leadTimeDays}d", color = TextSecondary, fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Due: ₹${sup.pendingDues.toInt()}", color = OrangeAlert, fontSize = 14.sp, fontWeight = FontWeight.Black)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Sourced: ₹${sup.totalPurchased.toInt()} • Weight: ${String.format(Locale.getDefault(), "%.1f", sup.totalWeight)} kg", color = LightIceBlue, fontSize = 12.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // View Info Indicator
                                IconButton(
                                    onClick = {
                                        selectedSupplierForDetails = sup
                                        showDetailsDialog = true
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "View Details",
                                        tint = LightIceBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                if (sup.pendingDues > 0) {
                                    Button(
                                        onClick = {
                                            targetSupplier = sup
                                            settleAmountText = ""
                                            showSettleDialog = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = TealNeon, contentColor = OceanDark),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text("Pay Supplier", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 1. ADD SUPPLIER DIALOG
        if (showAddSupplierDialog) {
            AlertDialog(
                onDismissRequest = { showAddSupplierDialog = false },
                title = { Text("Add Wholesale Supplier", color = TealNeon) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = supName,
                            onValueChange = { supName = it },
                            label = { Text("Supplier Name") },
                            textStyle = LocalTextStyle.current.copy(color = TextWhite),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = supPhone,
                            onValueChange = { supPhone = it },
                            label = { Text("Supplier Phone") },
                            textStyle = LocalTextStyle.current.copy(color = TextWhite),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = supContactPerson,
                            onValueChange = { supContactPerson = it },
                            label = { Text("Contact Person") },
                            textStyle = LocalTextStyle.current.copy(color = TextWhite),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = supDetails,
                            onValueChange = { supDetails = it },
                            label = { Text("Wholesale Scope (e.g. marine fish)") },
                            textStyle = LocalTextStyle.current.copy(color = TextWhite),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = supLeadTimeText,
                            onValueChange = { supLeadTimeText = it },
                            label = { Text("Average Lead Time (days)") },
                            textStyle = LocalTextStyle.current.copy(color = TextWhite),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (supName.isNotBlank() && supPhone.isNotBlank()) {
                                viewModel.addSupplier(supName, supPhone, supDetails, supLeadTimeText.toIntOrNull() ?: 3, supContactPerson)
                                supName = ""
                                supPhone = ""
                                supDetails = ""
                                supLeadTimeText = "3"
                                supContactPerson = ""
                                showAddSupplierDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealNeon, contentColor = OceanDark)
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddSupplierDialog = false }) { Text("Cancel", color = CoralRed) }
                },
                containerColor = OceanSurface
            )
        }

        // 2. SETTLE DUES DIALOG
        if (showSettleDialog && targetSupplier != null) {
            val sup = targetSupplier!!
            AlertDialog(
                onDismissRequest = { showSettleDialog = false },
                title = { Text("Pay Supplier Dues", color = TealNeon) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Supplier Name: ${sup.name}", color = TextWhite)
                        Text("Outstanding Balance: ₹${sup.pendingDues.toInt()}", color = OrangeAlert)
                        OutlinedTextField(
                            value = settleAmountText,
                            onValueChange = { settleAmountText = it },
                            label = { Text("Amount Cleared (₹)") },
                            textStyle = LocalTextStyle.current.copy(color = TextWhite),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
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
                        Text("Register Payment")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSettleDialog = false }) { Text("Cancel", color = CoralRed) }
                },
                containerColor = OceanSurface
            )
        }

        // 3. VIEW DETAILS & HISTORY DIALOG
        if (showDetailsDialog && selectedSupplierForDetails != null) {
            val sup = selectedSupplierForDetails!!
            val supplierPurchases = remember(purchases, sup.id) {
                purchases.filter { it.supplierId == sup.id }
            }
            val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

            AlertDialog(
                onDismissRequest = { showDetailsDialog = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = TealNeon,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = sup.name,
                                color = TextWhite,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Supplier Profile & Ledger",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                    ) {
                        // Contact Profile Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = OceanDark),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (sup.contactPerson.isNotEmpty()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Contact person icon",
                                            tint = TealNeon,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Contact: ${sup.contactPerson}",
                                            color = TextWhite,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = "Phone icon",
                                        tint = TealNeon,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Phone: ${sup.phone}",
                                        color = TextWhite,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                if (sup.details.isNotEmpty()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "Info icon",
                                            tint = LightIceBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Scope: ${sup.details}",
                                            color = TextSecondary,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Financial Stats Grid (2x2)
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Total Cost Card
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = OceanDark),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("Total Cost", color = TextSecondary, fontSize = 10.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "₹${sup.totalPurchased.toInt()}",
                                            color = LightIceBlue,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                // Total Weight Card
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = OceanDark),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("Total Weight", color = TextSecondary, fontSize = 10.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "${String.format(Locale.getDefault(), "%.1f", sup.totalWeight)} kg",
                                            color = LightIceBlue,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Pending Dues Card
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = OceanDark),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("Pending Dues", color = TextSecondary, fontSize = 10.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "₹${sup.pendingDues.toInt()}",
                                            color = if (sup.pendingDues > 0) OrangeAlert else TealNeon,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                // Lead Time Card
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = OceanDark),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("Lead Time", color = TextSecondary, fontSize = 10.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "${sup.leadTimeDays} days",
                                            color = TealNeon,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Supply / Purchase History Title
                        Text(
                            text = "Purchase History (${supplierPurchases.size} records)",
                            color = TealNeon,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Purchases scrollable list
                        if (supplierPurchases.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f, fill = false)
                                    .background(OceanDark, RoundedCornerShape(8.dp))
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No purchase records found for this supplier.",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f, fill = false)
                                    .background(OceanDark, RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(supplierPurchases) { purchase ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = OceanSurface),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = purchase.fishName,
                                                    color = TextWhite,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "₹${purchase.totalAmount.toInt()}",
                                                    color = TealNeon,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "${purchase.qtyKg} kg @ ₹${purchase.pricePerKg.toInt()}/kg",
                                                    color = TextSecondary,
                                                    fontSize = 11.sp
                                                )
                                                Text(
                                                    text = purchase.qualityType,
                                                    color = if (purchase.qualityType.equals("Fresh", ignoreCase = true)) TealNeon else OrangeAlert,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                            if (purchase.boatName.isNotEmpty()) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "Boat: ${purchase.boatName}",
                                                    color = LightIceBlue,
                                                    fontSize = 11.sp
                                                )
                                            }
                                            if (purchase.buyerName.isNotEmpty()) {
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "Buyer: ${purchase.buyerName}" + if (purchase.buyerPhone.isNotEmpty()) " (${purchase.buyerPhone})" else "",
                                                    color = LightIceBlue,
                                                    fontSize = 11.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = dateFormat.format(Date(purchase.purchaseDate)),
                                                color = TextSecondary,
                                                fontSize = 9.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showDetailsDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = TealNeon, contentColor = OceanDark)
                    ) {
                        Text("Close")
                    }
                },
                containerColor = OceanSurface
            )
        }
    }
}

// ================== STAFF / WORKER TAB ==================
@Composable
fun StaffTab(viewModel: FishViewModel) {
    val workers by viewModel.workers.collectAsState()
    var showAddWorkerDialog by remember { mutableStateOf(false) }

    // Forms
    var workerName by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Staff") } // Manager, Staff, Delivery Boy
    var salaryText by remember { mutableStateOf("") }
    var phoneText by remember { mutableStateOf("") }
    var pinText by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Worker Management", color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Button(
                onClick = { showAddWorkerDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = TealNeon, contentColor = OceanDark)
            ) {
                Icon(Icons.Default.Engineering, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Worker")
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
            items(workers) { worker ->
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
                                Text(worker.name, color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = OceanCard),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = worker.role,
                                        color = TealNeon,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text("Phone: ${worker.phone} • PIN: ${worker.loginPin}", color = TextSecondary, fontSize = 12.sp)
                            Text("Salary: ₹${worker.salary.toInt()} / month", color = LightIceBlue, fontSize = 12.sp)
                        }

                        IconButton(onClick = { viewModel.deleteWorker(worker) }) {
                            Icon(Icons.Default.PersonRemove, contentDescription = "Remove", tint = CoralRed)
                        }
                    }
                }
            }
        }

        if (showAddWorkerDialog) {
            AlertDialog(
                onDismissRequest = { showAddWorkerDialog = false },
                title = { Text("Add New Worker Account", color = TealNeon) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = workerName,
                            onValueChange = { workerName = it },
                            label = { Text("Worker Full Name") },
                            textStyle = LocalTextStyle.current.copy(color = TextWhite),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = phoneText,
                            onValueChange = { phoneText = it },
                            label = { Text("Phone Number") },
                            textStyle = LocalTextStyle.current.copy(color = TextWhite),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = salaryText,
                            onValueChange = { salaryText = it },
                            label = { Text("Monthly Salary (₹)") },
                            textStyle = LocalTextStyle.current.copy(color = TextWhite),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = pinText,
                            onValueChange = { pinText = it },
                            label = { Text("4-Digit Login PIN") },
                            textStyle = LocalTextStyle.current.copy(color = TextWhite),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Assigned Role", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("Manager", "Staff", "Delivery Boy").forEach { r ->
                                ElevatedFilterChip(
                                    selected = selectedRole == r,
                                    onClick = { selectedRole = r },
                                    label = { Text(r) },
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
                            val sal = salaryText.toDoubleOrNull() ?: 0.0
                            if (workerName.isNotBlank() && phoneText.isNotBlank() && pinText.isNotBlank()) {
                                viewModel.addWorker(workerName, selectedRole, sal, phoneText, pinText)
                                workerName = ""
                                phoneText = ""
                                salaryText = ""
                                pinText = ""
                                selectedRole = "Staff"
                                showAddWorkerDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealNeon, contentColor = OceanDark)
                    ) {
                        Text("Create Login")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddWorkerDialog = false }) { Text("Cancel", color = CoralRed) }
                },
                containerColor = OceanSurface
            )
        }
    }
}

// ================== EXPENSES TAB ==================
@Composable
fun ExpensesTab(viewModel: FishViewModel) {
    val expenses by viewModel.expenses.collectAsState()
    var showAddExpenseDialog by remember { mutableStateOf(false) }

    // Forms
    var selectedCategory by remember { mutableStateOf("Feed Costs") } // Feed, Transport, Maintenance, Ice, Salary, Misc
    var descText by remember { mutableStateOf("") }
    var amtText by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Business Expense Ledger", color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Button(
                onClick = { showAddExpenseDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = TealNeon, contentColor = OceanDark)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Log Expense")
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
            items(expenses) { exp ->
                val sdf = SimpleDateFormat("dd MMM • hh:mm a", Locale.getDefault())
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
                                Text(exp.category, color = TextWhite, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = sdf.format(Date(exp.date)),
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                            Text(exp.description, color = TextSecondary, fontSize = 12.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("₹${exp.amount.toInt()}", color = CoralRed, fontSize = 16.sp, fontWeight = FontWeight.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(onClick = { viewModel.deleteExpense(exp) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CoralRed.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }
        }

        if (showAddExpenseDialog) {
            AlertDialog(
                onDismissRequest = { showAddExpenseDialog = false },
                title = { Text("Log New Business Expense", color = TealNeon) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Category Type", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Row 1
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("Feed" to "Feed Costs", "Transport" to "Transportation", "Maintenance" to "Equipment Maintenance").forEach { (labelShort, labelFull) ->
                                    ElevatedFilterChip(
                                        selected = selectedCategory == labelFull,
                                        onClick = { selectedCategory = labelFull },
                                        label = { Text(labelShort, fontSize = 10.sp, maxLines = 1) },
                                        colors = FilterChipDefaults.elevatedFilterChipColors(
                                            selectedContainerColor = TealNeon,
                                            selectedLabelColor = OceanDark
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            // Row 2
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("Ice" to "Ice Charges", "Salary" to "Salaries", "Misc" to "Misc Expenses").forEach { (labelShort, labelFull) ->
                                    ElevatedFilterChip(
                                        selected = selectedCategory == labelFull,
                                        onClick = { selectedCategory = labelFull },
                                        label = { Text(labelShort, fontSize = 10.sp, maxLines = 1) },
                                        colors = FilterChipDefaults.elevatedFilterChipColors(
                                            selectedContainerColor = TealNeon,
                                            selectedLabelColor = OceanDark
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = descText,
                            onValueChange = { descText = it },
                            label = { Text("Description (e.g. 5 boxes dry ice)") },
                            textStyle = LocalTextStyle.current.copy(color = TextWhite),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = amtText,
                            onValueChange = { amtText = it },
                            label = { Text("Total Amount (₹)") },
                            textStyle = LocalTextStyle.current.copy(color = TextWhite),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amt = amtText.toDoubleOrNull() ?: 0.0
                            if (descText.isNotBlank() && amt > 0.0) {
                                viewModel.addExpense(selectedCategory, descText, amt)
                                descText = ""
                                amtText = ""
                                showAddExpenseDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealNeon, contentColor = OceanDark)
                    ) {
                        Text("Log Expense")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddExpenseDialog = false }) { Text("Cancel", color = CoralRed) }
                },
                containerColor = OceanSurface
            )
        }
    }
}
