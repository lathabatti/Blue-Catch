package com.example.ui.screens

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.print.PrintAttributes
import android.print.PrintManager
import android.provider.MediaStore
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.FishViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpreadsheetScreen(viewModel: FishViewModel) {
    val context = LocalContext.current
    
    // Tab selector (0: Purchases, 1: Sales, 2: Khatha Book, 3: Supplier Payments, 4: Expenses)
    var activeTab by remember { mutableIntStateOf(0) }
    
    // Search and filter states
    var searchQuery by remember { mutableStateOf("") }
    var paymentFilter by remember { mutableStateOf("All") } // All, Paid, Pending
    
    // Sort states
    var sortColumn by remember { mutableStateOf("") }
    var sortAscending by remember { mutableStateOf(true) }
    
    // Edit Dialog States
    var editingPurchase by remember { mutableStateOf<Purchase?>(null) }
    var editingSale by remember { mutableStateOf<Sale?>(null) }
    var editingCustomer by remember { mutableStateOf<Customer?>(null) }
    var editingSupplier by remember { mutableStateOf<Supplier?>(null) }
    var editingExpense by remember { mutableStateOf<Expense?>(null) }
    
    // Download and high-fidelity preview states
    var showDownloadDialog by remember { mutableStateOf(false) }
    var downloadFormat by remember { mutableStateOf("Excel") } // "Excel" or "PDF"
    

    // Data collections from ViewModel
    val purchases by viewModel.purchases.collectAsState()
    val sales by viewModel.sales.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    fun toggleSort(col: String) {
        if (sortColumn == col) {
            sortAscending = !sortAscending
        } else {
            sortColumn = col
            sortAscending = true
        }
    }

    // Column widths for tabular layout
    val colWidthDate = 130.dp
    val colWidthName = 140.dp
    val colWidthFish = 110.dp
    val colWidthPhone = 120.dp
    val colWidthBoat = 100.dp
    val colWidthQty = 90.dp
    val colWidthPrice = 90.dp
    val colWidthAmount = 100.dp
    val colWidthStatus = 110.dp
    val colWidthAction = 80.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OceanDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Dashboard Header Section
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
                    Text(
                        text = "DIGITAL LEDGER SPREADSHEETS",
                        color = TealNeon,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Excel-style tabular ledger with real-time MySQL database sync",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            showDownloadDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealNeon, contentColor = OceanDark),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("download_spreadsheet_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Download Report Options", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    Button(
                        onClick = {
                            saveCsvToDownloads(
                                context = context,
                                activeTab = activeTab,
                                purchases = purchases,
                                sales = sales,
                                customers = customers,
                                suppliers = suppliers,
                                expenses = expenses,
                                searchQuery = searchQuery,
                                paymentFilter = paymentFilter,
                                sortColumn = sortColumn,
                                sortAscending = sortAscending,
                                sdf = sdf
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF217346)), // Excel Green
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("export_csv_button")
                    ) {
                        Icon(Icons.Default.GridOn, contentDescription = "Download as CSV", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    
                    Button(
                        onClick = {
                            val html = generateHtmlContentFiltered(
                                activeTab = activeTab,
                                purchases = purchases,
                                sales = sales,
                                customers = customers,
                                suppliers = suppliers,
                                expenses = expenses,
                                searchQuery = searchQuery,
                                paymentFilter = paymentFilter,
                                sortColumn = sortColumn,
                                sortAscending = sortAscending,
                                sdf = sdf
                            )
                            val jobName = when (activeTab) {
                                0 -> "Wholesale_Purchases_Ledger"
                                1 -> "Customer_Sales_Ledger"
                                2 -> "Digital_Khatha_Ledger"
                                3 -> "Supplier_Payments_Ledger"
                                else -> "Business_Expenses_Ledger"
                            }
                            printHtmlPdf(context, html, jobName)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE02D30)), // PDF Red
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("export_pdf_button")
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "Export to formatted PDF", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val html = generateHtmlContentFiltered(
                                activeTab = activeTab,
                                purchases = purchases,
                                sales = sales,
                                customers = customers,
                                suppliers = suppliers,
                                expenses = expenses,
                                searchQuery = searchQuery,
                                paymentFilter = paymentFilter,
                                sortColumn = sortColumn,
                                sortAscending = sortAscending,
                                sdf = sdf
                            )
                            val jobName = when (activeTab) {
                                0 -> "Wholesale_Purchases_Ledger_Print"
                                1 -> "Customer_Sales_Ledger_Print"
                                2 -> "Digital_Khatha_Ledger_Print"
                                3 -> "Supplier_Payments_Ledger_Print"
                                else -> "Business_Expenses_Ledger_Print"
                            }
                            printHtmlPdf(context, html, jobName)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0083B0)), // Ocean Teal/Blue
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("print_table_button")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = "Print table", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Print", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. Tab Bar Selector
        ScrollableTabRow(
            selectedTabIndex = activeTab,
            containerColor = OceanSurface,
            contentColor = TealNeon,
            edgePadding = 0.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                    color = TealNeon
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .testTag("spreadsheet_tab_row")
        ) {
            val tabs = listOf(
                Pair(Icons.Default.ShoppingCart, "Purchases"),
                Pair(Icons.Default.Receipt, "Sales"),
                Pair(Icons.Default.People, "Khatha Book"),
                Pair(Icons.Default.Business, "Supplier dues"),
                Pair(Icons.Default.AccountBalanceWallet, "Expenses")
            )
            tabs.forEachIndexed { index, pair ->
                Tab(
                    selected = activeTab == index,
                    onClick = {
                        activeTab = index
                        searchQuery = ""
                        paymentFilter = "All"
                        sortColumn = ""
                    },
                    text = { Text(pair.second, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    icon = { Icon(pair.first, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    selectedContentColor = TealNeon,
                    unselectedContentColor = TextSecondary
                )
            }
        }

        // 3. Search & Quick Filters Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search spreadsheet...", color = TextSecondary, fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TealNeon, modifier = Modifier.size(18.dp)) },
                textStyle = LocalTextStyle.current.copy(color = TextWhite, fontSize = 12.sp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TealNeon,
                    unfocusedBorderColor = LightIceBlue.copy(alpha = 0.3f),
                    focusedContainerColor = OceanSurface,
                    unfocusedContainerColor = OceanSurface
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("spreadsheet_search_input")
            )
            
            // Payment Status Filter Toggle (Where applicable)
            if (activeTab in listOf(0, 1, 2, 3)) {
                Row(
                    modifier = Modifier
                        .background(OceanSurface, shape = RoundedCornerShape(8.dp))
                        .border(1.dp, LightIceBlue.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp))
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    val filters = listOf("All", "Paid", "Pending")
                    filters.forEach { filter ->
                        val isSelected = paymentFilter == filter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) TealNeon else Color.Transparent)
                                .clickable { paymentFilter = filter }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = filter,
                                color = if (isSelected) OceanDark else TextWhite,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 4. Spreadsheets Rendering Container
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(OceanSurface, shape = RoundedCornerShape(12.dp))
                .border(1.dp, LightIceBlue.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Outer Horizontal Scroll for tabular columns
                val horizontalScrollState = rememberScrollState()
                
                Column(modifier = Modifier.fillMaxSize()) {
                    // SPREADSHEET HEADER ROW
                    Row(
                        modifier = Modifier
                            .background(OceanCard)
                            .horizontalScroll(horizontalScrollState)
                            .padding(vertical = 10.dp)
                    ) {
                        // Dynamically render headers based on tab index
                        when (activeTab) {
                            0 -> { // Purchases
                                HeaderCell("Date", colWidthDate, "date", sortColumn, sortAscending) { toggleSort("date") }
                                HeaderCell("Fish Name", colWidthFish, "fish", sortColumn, sortAscending) { toggleSort("fish") }
                                HeaderCell("Supplier", colWidthName, "supplier", sortColumn, sortAscending) { toggleSort("supplier") }
                                HeaderCell("Supplier Mobile", colWidthPhone, "phone", sortColumn, sortAscending) { toggleSort("phone") }
                                HeaderCell("Boat/Harbor", colWidthBoat, "boat", sortColumn, sortAscending) { toggleSort("boat") }
                                HeaderCell("Cost/Kg", colWidthPrice, "price", sortColumn, sortAscending) { toggleSort("price") }
                                HeaderCell("Total Weight", colWidthQty, "qty", sortColumn, sortAscending) { toggleSort("qty") }
                                HeaderCell("Total Amount", colWidthAmount, "total", sortColumn, sortAscending) { toggleSort("total") }
                                HeaderCell("Payment Status", colWidthStatus, "status", sortColumn, sortAscending) { toggleSort("status") }
                                HeaderCell("Action", colWidthAction, "", sortColumn, sortAscending) {}
                            }
                            1 -> { // Sales
                                HeaderCell("Order Date", colWidthDate, "date", sortColumn, sortAscending) { toggleSort("date") }
                                HeaderCell("Customer Name", colWidthName, "customer", sortColumn, sortAscending) { toggleSort("customer") }
                                HeaderCell("Customer Mobile", colWidthPhone, "phone", sortColumn, sortAscending) { toggleSort("phone") }
                                HeaderCell("Fish Name", colWidthFish, "fish", sortColumn, sortAscending) { toggleSort("fish") }
                                HeaderCell("Qty Sold", colWidthQty, "qty", sortColumn, sortAscending) { toggleSort("qty") }
                                HeaderCell("Price/Kg", colWidthPrice, "price", sortColumn, sortAscending) { toggleSort("price") }
                                HeaderCell("Total Amount", colWidthAmount, "total", sortColumn, sortAscending) { toggleSort("total") }
                                HeaderCell("Payment Type", colWidthStatus, "paymentType", sortColumn, sortAscending) { toggleSort("paymentType") }
                                HeaderCell("Payment Status", colWidthStatus, "status", sortColumn, sortAscending) { toggleSort("status") }
                                HeaderCell("Action", colWidthAction, "", sortColumn, sortAscending) {}
                            }
                            2 -> { // Digital Khatha Book
                                HeaderCell("Customer Name", colWidthName, "customer", sortColumn, sortAscending) { toggleSort("customer") }
                                HeaderCell("Mobile Number", colWidthPhone, "phone", sortColumn, sortAscending) { toggleSort("phone") }
                                HeaderCell("Billed Sales", colWidthAmount, "total", sortColumn, sortAscending) { toggleSort("total") }
                                HeaderCell("Pending Credit", colWidthAmount, "pending", sortColumn, sortAscending) { toggleSort("pending") }
                                HeaderCell("Paid Received", colWidthAmount, "paid", sortColumn, sortAscending) { toggleSort("paid") }
                                HeaderCell("Payment Status", colWidthStatus, "status", sortColumn, sortAscending) { toggleSort("status") }
                                HeaderCell("Action", colWidthAction, "", sortColumn, sortAscending) {}
                            }
                            3 -> { // Supplier Payments
                                HeaderCell("Supplier Name", colWidthName, "supplier", sortColumn, sortAscending) { toggleSort("supplier") }
                                HeaderCell("Mobile Number", colWidthPhone, "phone", sortColumn, sortAscending) { toggleSort("phone") }
                                HeaderCell("Total Sourced", colWidthAmount, "total", sortColumn, sortAscending) { toggleSort("total") }
                                HeaderCell("Remaining Balance", colWidthAmount, "balance", sortColumn, sortAscending) { toggleSort("balance") }
                                HeaderCell("Paid To Date", colWidthAmount, "paid", sortColumn, sortAscending) { toggleSort("paid") }
                                HeaderCell("Status", colWidthStatus, "status", sortColumn, sortAscending) { toggleSort("status") }
                                HeaderCell("Action", colWidthAction, "", sortColumn, sortAscending) {}
                            }
                            4 -> { // Business Expenses
                                HeaderCell("Expense Date", colWidthDate, "date", sortColumn, sortAscending) { toggleSort("date") }
                                HeaderCell("Expense Category", colWidthName, "category", sortColumn, sortAscending) { toggleSort("category") }
                                HeaderCell("Description", colWidthName * 2, "desc", sortColumn, sortAscending) { toggleSort("desc") }
                                HeaderCell("Amount", colWidthAmount, "amount", sortColumn, sortAscending) { toggleSort("amount") }
                                HeaderCell("Action", colWidthAction, "", sortColumn, sortAscending) {}
                            }
                        }
                    }
                    
                    Divider(color = LightIceBlue.copy(alpha = 0.15f), thickness = 1.dp)

                    // SPREADSHEET ROWS SCROLL
                    Box(modifier = Modifier.weight(1f)) {
                        when (activeTab) {
                            0 -> { // Purchases Sheet List
                                val filtered = remember(purchases, searchQuery, paymentFilter, sortColumn, sortAscending) {
                                    var list = purchases.filter {
                                        it.fishName.contains(searchQuery, ignoreCase = true) ||
                                        it.supplierName.contains(searchQuery, ignoreCase = true) ||
                                        it.buyerPhone.contains(searchQuery) ||
                                        it.supplierPhone.contains(searchQuery)
                                    }
                                    if (paymentFilter != "All") {
                                        list = list.filter { it.paymentStatus.equals(paymentFilter, ignoreCase = true) }
                                    }
                                    sortPurchases(list, sortColumn, sortAscending)
                                }

                                if (filtered.isEmpty()) {
                                    EmptySpreadsheetState("No Wholesale Purchases found matching filters.")
                                } else {
                                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                                        items(filtered) { item ->
                                            Row(
                                                modifier = Modifier
                                                    .horizontalScroll(horizontalScrollState)
                                                    .background(if (filtered.indexOf(item) % 2 == 0) Color.Transparent else OceanDark.copy(alpha = 0.3f))
                                                    .padding(vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Cell(sdf.format(Date(item.purchaseDate)), colWidthDate)
                                                Cell(item.fishName, colWidthFish, weight = FontWeight.Bold, color = TealNeon)
                                                Cell(item.supplierName, colWidthName)
                                                Cell(item.supplierPhone.ifEmpty { item.buyerPhone }, colWidthPhone)
                                                Cell(item.boatName.ifEmpty { "N/A" }, colWidthBoat)
                                                Cell("₹${item.pricePerKg.toInt()}", colWidthPrice)
                                                Cell("${item.qtyKg.toInt()} kg", colWidthQty)
                                                Cell("₹${item.totalAmount.toInt()}", colWidthAmount, color = BlueDeep, weight = FontWeight.Black)
                                                
                                                val isPaid = item.paymentStatus.lowercase() == "paid"
                                                StatusCell(item.paymentStatus, colWidthStatus, isPaid)
                                                
                                                ActionCell(colWidthAction) { editingPurchase = item }
                                            }
                                            Divider(color = LightIceBlue.copy(alpha = 0.05f))
                                        }
                                    }
                                }
                            }
                            1 -> { // Sales Sheet List
                                val filtered = remember(sales, searchQuery, paymentFilter, sortColumn, sortAscending) {
                                    var list = sales.filter {
                                        it.customerName.contains(searchQuery, ignoreCase = true) ||
                                        it.customerPhone.contains(searchQuery) ||
                                        it.fishName.contains(searchQuery, ignoreCase = true)
                                    }
                                    if (paymentFilter != "All") {
                                        list = list.filter { it.paymentStatus.equals(paymentFilter, ignoreCase = true) }
                                    }
                                    sortSales(list, sortColumn, sortAscending)
                                }

                                if (filtered.isEmpty()) {
                                    EmptySpreadsheetState("No Sales records found matching filters.")
                                } else {
                                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                                        items(filtered) { item ->
                                            Row(
                                                modifier = Modifier
                                                    .horizontalScroll(horizontalScrollState)
                                                    .background(if (filtered.indexOf(item) % 2 == 0) Color.Transparent else OceanDark.copy(alpha = 0.3f))
                                                    .padding(vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Cell(sdf.format(Date(item.saleDate)), colWidthDate)
                                                Cell(item.customerName, colWidthName)
                                                Cell(item.customerPhone.ifEmpty { "N/A" }, colWidthPhone)
                                                Cell(item.fishName, colWidthFish, weight = FontWeight.Bold, color = TealNeon)
                                                Cell("${item.qtyKg.toInt()} kg", colWidthQty)
                                                Cell("₹${item.pricePerKg.toInt()}", colWidthPrice)
                                                Cell("₹${item.totalAmount.toInt()}", colWidthAmount, color = BlueDeep, weight = FontWeight.Black)
                                                Cell(item.paymentType, colWidthStatus)
                                                
                                                val isPaid = item.paymentStatus.lowercase() == "paid"
                                                StatusCell(item.paymentStatus, colWidthStatus, isPaid)
                                                
                                                ActionCell(colWidthAction) { editingSale = item }
                                            }
                                            Divider(color = LightIceBlue.copy(alpha = 0.05f))
                                        }
                                    }
                                }
                            }
                            2 -> { // Digital Khatha Book Sheet List
                                val filtered = remember(customers, sales, searchQuery, paymentFilter, sortColumn, sortAscending) {
                                    var list = customers.map { cust ->
                                        val cSales = sales.filter { it.customerName.lowercase() == cust.name.lowercase() }
                                        val totalBilled = cSales.sumOf { it.totalAmount }
                                        val pending = cust.creditBookBalance
                                        val paid = (totalBilled - pending).coerceAtLeast(0.0)
                                        val status = if (pending <= 0.0) "Paid" else "Pending"
                                        
                                        KhathaRowItem(
                                            customer = cust,
                                            totalBilled = totalBilled,
                                            pendingCredit = pending,
                                            paidAmount = paid,
                                            status = status
                                        )
                                    }.filter {
                                        it.customer.name.contains(searchQuery, ignoreCase = true) ||
                                        it.customer.phone.contains(searchQuery)
                                    }
                                    
                                    if (paymentFilter != "All") {
                                        list = list.filter { it.status.equals(paymentFilter, ignoreCase = true) }
                                    }
                                    sortKhatha(list, sortColumn, sortAscending)
                                }

                                if (filtered.isEmpty()) {
                                    EmptySpreadsheetState("No Customer credit ledger found.")
                                } else {
                                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                                        items(filtered) { item ->
                                            Row(
                                                modifier = Modifier
                                                    .horizontalScroll(horizontalScrollState)
                                                    .background(if (filtered.indexOf(item) % 2 == 0) Color.Transparent else OceanDark.copy(alpha = 0.3f))
                                                    .padding(vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Cell(item.customer.name, colWidthName)
                                                Cell(item.customer.phone, colWidthPhone)
                                                Cell("₹${item.totalBilled.toInt()}", colWidthAmount)
                                                Cell("₹${item.pendingCredit.toInt()}", colWidthAmount, color = OrangeAlert, weight = FontWeight.Bold)
                                                Cell("₹${item.paidAmount.toInt()}", colWidthAmount, color = TealNeon)
                                                
                                                val isPaid = item.pendingCredit <= 0.0
                                                StatusCell(if (isPaid) "Settled" else "Pending dues", colWidthStatus, isPaid)
                                                
                                                ActionCell(colWidthAction) { editingCustomer = item.customer }
                                            }
                                            Divider(color = LightIceBlue.copy(alpha = 0.05f))
                                        }
                                    }
                                }
                            }
                            3 -> { // Supplier Payments Spreadsheet List
                                val filtered = remember(suppliers, searchQuery, paymentFilter, sortColumn, sortAscending) {
                                    var list = suppliers.map { sup ->
                                        val totalPayable = sup.totalPurchased
                                        val remaining = sup.pendingDues
                                        val paid = (totalPayable - remaining).coerceAtLeast(0.0)
                                        val status = if (remaining <= 0.0) "Paid" else "Pending"
                                        
                                        SupplierRowItem(
                                            supplier = sup,
                                            totalSourced = totalPayable,
                                            remainingBalance = remaining,
                                            paidToDate = paid,
                                            status = status
                                        )
                                    }.filter {
                                        it.supplier.name.contains(searchQuery, ignoreCase = true) ||
                                        it.supplier.phone.contains(searchQuery)
                                    }
                                    
                                    if (paymentFilter != "All") {
                                        list = list.filter { it.status.equals(paymentFilter, ignoreCase = true) }
                                    }
                                    sortSuppliers(list, sortColumn, sortAscending)
                                }

                                if (filtered.isEmpty()) {
                                    EmptySpreadsheetState("No Supplier accounts found.")
                                } else {
                                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                                        items(filtered) { item ->
                                            Row(
                                                modifier = Modifier
                                                    .horizontalScroll(horizontalScrollState)
                                                    .background(if (filtered.indexOf(item) % 2 == 0) Color.Transparent else OceanDark.copy(alpha = 0.3f))
                                                    .padding(vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Cell(item.supplier.name, colWidthName)
                                                Cell(item.supplier.phone, colWidthPhone)
                                                Cell("₹${item.totalSourced.toInt()}", colWidthAmount)
                                                Cell("₹${item.remainingBalance.toInt()}", colWidthAmount, color = OrangeAlert, weight = FontWeight.Bold)
                                                Cell("₹${item.paidToDate.toInt()}", colWidthAmount, color = TealNeon)
                                                
                                                val isPaid = item.remainingBalance <= 0.0
                                                StatusCell(if (isPaid) "Paid" else "Pending Dues", colWidthStatus, isPaid)
                                                
                                                ActionCell(colWidthAction) { editingSupplier = item.supplier }
                                            }
                                            Divider(color = LightIceBlue.copy(alpha = 0.05f))
                                        }
                                    }
                                }
                            }
                            4 -> { // Business Expenses Spreadsheet List
                                val filtered = remember(expenses, searchQuery, sortColumn, sortAscending) {
                                    var list = expenses.filter {
                                        it.category.contains(searchQuery, ignoreCase = true) ||
                                        it.description.contains(searchQuery, ignoreCase = true)
                                    }
                                    sortExpenses(list, sortColumn, sortAscending)
                                }

                                if (filtered.isEmpty()) {
                                    EmptySpreadsheetState("No Business Expenses found matching filters.")
                                } else {
                                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                                        items(filtered) { item ->
                                            Row(
                                                modifier = Modifier
                                                    .horizontalScroll(horizontalScrollState)
                                                    .background(if (filtered.indexOf(item) % 2 == 0) Color.Transparent else OceanDark.copy(alpha = 0.3f))
                                                    .padding(vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Cell(sdf.format(Date(item.date)), colWidthDate)
                                                Cell(item.category, colWidthName, weight = FontWeight.Bold, color = CoralRed)
                                                Cell(item.description, colWidthName * 2)
                                                Cell("₹${item.amount.toInt()}", colWidthAmount, color = BlueDeep, weight = FontWeight.Black)
                                                
                                                ActionCell(colWidthAction) { editingExpense = item }
                                            }
                                            Divider(color = LightIceBlue.copy(alpha = 0.05f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ------------------- POPUP EDIT DIALOGS -------------------

    // A. Edit Purchase Dialog
    editingPurchase?.let { purchase ->
        var fish by remember { mutableStateOf(purchase.fishName) }
        var boat by remember { mutableStateOf(purchase.boatName) }
        var qtyText by remember { mutableStateOf(purchase.qtyKg.toString()) }
        var priceText by remember { mutableStateOf(purchase.pricePerKg.toString()) }
        var selectedStatus by remember { mutableStateOf(purchase.paymentStatus) }
        var phoneText by remember { mutableStateOf(purchase.supplierPhone.ifEmpty { purchase.buyerPhone }) }

        AlertDialog(
            onDismissRequest = { editingPurchase = null },
            title = { Text("Edit Wholesale Purchase", color = TealNeon, fontWeight = FontWeight.Bold) },
            containerColor = OceanSurface,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = fish,
                        onValueChange = { fish = it },
                        label = { Text("Fish Name", color = TextSecondary) },
                        colors = editFieldColors(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = phoneText,
                        onValueChange = { phoneText = it },
                        label = { Text("Supplier Mobile", color = TextSecondary) },
                        colors = editFieldColors(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    OutlinedTextField(
                        value = boat,
                        onValueChange = { boat = it },
                        label = { Text("Boat Name", color = TextSecondary) },
                        colors = editFieldColors(),
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = qtyText,
                            onValueChange = { qtyText = it },
                            label = { Text("Weight (kg)", color = TextSecondary) },
                            colors = editFieldColors(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { priceText = it },
                            label = { Text("Rate /kg", color = TextSecondary) },
                            colors = editFieldColors(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Text("Payment Status", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf("Paid", "Pending").forEach { st ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = selectedStatus == st,
                                    onClick = { selectedStatus = st },
                                    colors = RadioButtonDefaults.colors(selectedColor = TealNeon)
                                )
                                Text(st, color = TextWhite, fontSize = 14.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val qty = qtyText.toDoubleOrNull() ?: purchase.qtyKg
                        val rate = priceText.toDoubleOrNull() ?: purchase.pricePerKg
                        val updated = purchase.copy(
                            fishName = fish,
                            boatName = boat,
                            qtyKg = qty,
                            pricePerKg = rate,
                            totalAmount = qty * rate,
                            paymentStatus = selectedStatus,
                            supplierPhone = phoneText
                        )
                        viewModel.updatePurchase(updated)
                        editingPurchase = null
                        Toast.makeText(context, "Purchase record updated!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Save", color = TealNeon)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingPurchase = null }) {
                    Text("Cancel", color = CoralRed)
                }
            }
        )
    }

    // B. Edit Sale Dialog
    editingSale?.let { sale ->
        var qtyText by remember { mutableStateOf(sale.qtyKg.toString()) }
        var priceText by remember { mutableStateOf(sale.pricePerKg.toString()) }
        var selectedStatus by remember { mutableStateOf(sale.paymentStatus) }
        var customerPhone by remember { mutableStateOf(sale.customerPhone) }

        AlertDialog(
            onDismissRequest = { editingSale = null },
            title = { Text("Edit Sale Row", color = TealNeon, fontWeight = FontWeight.Bold) },
            containerColor = OceanSurface,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Text("Customer: ${sale.customerName}", color = TextWhite, fontWeight = FontWeight.Bold)
                    Text("Fish: ${sale.fishName}", color = TextSecondary, fontSize = 12.sp)
                    
                    OutlinedTextField(
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        label = { Text("Customer Mobile", color = TextSecondary) },
                        colors = editFieldColors(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = qtyText,
                            onValueChange = { qtyText = it },
                            label = { Text("Quantity (kg)", color = TextSecondary) },
                            colors = editFieldColors(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { priceText = it },
                            label = { Text("Price per kg", color = TextSecondary) },
                            colors = editFieldColors(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text("Payment Status", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf("Paid", "Pending").forEach { st ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = selectedStatus == st,
                                    onClick = { selectedStatus = st },
                                    colors = RadioButtonDefaults.colors(selectedColor = TealNeon)
                                )
                                Text(st, color = TextWhite, fontSize = 14.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val qty = qtyText.toDoubleOrNull() ?: sale.qtyKg
                        val rate = priceText.toDoubleOrNull() ?: sale.pricePerKg
                        val updated = sale.copy(
                            qtyKg = qty,
                            pricePerKg = rate,
                            totalAmount = qty * rate,
                            paymentStatus = selectedStatus,
                            customerPhone = customerPhone
                        )
                        viewModel.updateSale(updated)
                        editingSale = null
                        Toast.makeText(context, "Sale record updated!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Save", color = TealNeon)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingSale = null }) {
                    Text("Cancel", color = CoralRed)
                }
            }
        )
    }

    // C. Edit Khatha Book Customer Dialog
    editingCustomer?.let { customer ->
        var outstandingText by remember { mutableStateOf(customer.creditBookBalance.toString()) }
        var phoneText by remember { mutableStateOf(customer.phone) }

        AlertDialog(
            onDismissRequest = { editingCustomer = null },
            title = { Text("Adjust Credit Record", color = TealNeon, fontWeight = FontWeight.Bold) },
            containerColor = OceanSurface,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Text("Customer Name: ${customer.name}", color = TextWhite, fontWeight = FontWeight.Bold)
                    
                    OutlinedTextField(
                        value = phoneText,
                        onValueChange = { phoneText = it },
                        label = { Text("Customer Mobile", color = TextSecondary) },
                        colors = editFieldColors(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    
                    OutlinedTextField(
                        value = outstandingText,
                        onValueChange = { outstandingText = it },
                        label = { Text("Outstanding Credit (₹)", color = TextSecondary) },
                        colors = editFieldColors(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    
                    Text(
                        "Updating this modifies the customer's total outstanding balance directly in the database.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val outBalance = outstandingText.toDoubleOrNull() ?: customer.creditBookBalance
                        val updated = customer.copy(
                            phone = phoneText,
                            creditBookBalance = outBalance,
                            pendingAmounts = outBalance
                        )
                        viewModel.updateCustomer(updated)
                        editingCustomer = null
                        Toast.makeText(context, "Customer Credit Ledger updated!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Update", color = TealNeon)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingCustomer = null }) {
                    Text("Cancel", color = CoralRed)
                }
            }
        )
    }

    // D. Edit Supplier Payment Dialog
    editingSupplier?.let { supplier ->
        var pendingDuesText by remember { mutableStateOf(supplier.pendingDues.toString()) }
        var phoneText by remember { mutableStateOf(supplier.phone) }

        AlertDialog(
            onDismissRequest = { editingSupplier = null },
            title = { Text("Adjust Supplier Balance", color = TealNeon, fontWeight = FontWeight.Bold) },
            containerColor = OceanSurface,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Text("Supplier: ${supplier.name}", color = TextWhite, fontWeight = FontWeight.Bold)
                    
                    OutlinedTextField(
                        value = phoneText,
                        onValueChange = { phoneText = it },
                        label = { Text("Supplier Mobile", color = TextSecondary) },
                        colors = editFieldColors(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    
                    OutlinedTextField(
                        value = pendingDuesText,
                        onValueChange = { pendingDuesText = it },
                        label = { Text("Pending Dues (₹)", color = TextSecondary) },
                        colors = editFieldColors(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val dues = pendingDuesText.toDoubleOrNull() ?: supplier.pendingDues
                        val updated = supplier.copy(
                            phone = phoneText,
                            pendingDues = dues
                        )
                        viewModel.updateSupplier(updated)
                        editingSupplier = null
                        Toast.makeText(context, "Supplier account updated!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Update", color = TealNeon)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingSupplier = null }) {
                    Text("Cancel", color = CoralRed)
                }
            }
        )
    }

    // E. Edit Expense Dialog
    editingExpense?.let { expense ->
        var category by remember { mutableStateOf(expense.category) }
        var amountText by remember { mutableStateOf(expense.amount.toString()) }
        var desc by remember { mutableStateOf(expense.description) }

        AlertDialog(
            onDismissRequest = { editingExpense = null },
            title = { Text("Edit Business Expense", color = TealNeon, fontWeight = FontWeight.Bold) },
            containerColor = OceanSurface,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category (Ice, Transport, Salary, etc.)", color = TextSecondary) },
                        colors = editFieldColors(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Description", color = TextSecondary) },
                        colors = editFieldColors(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Amount (₹)", color = TextSecondary) },
                        colors = editFieldColors(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: expense.amount
                        val updated = expense.copy(
                            category = category,
                            description = desc,
                            amount = amount
                        )
                        viewModel.updateExpense(updated)
                        editingExpense = null
                        Toast.makeText(context, "Expense row updated!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Save", color = TealNeon)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingExpense = null }) {
                    Text("Cancel", color = CoralRed)
                }
            }
        )
    }

    // ------------------- HIGH-FIDELITY DOWNLOAD PREVIEW DIALOG -------------------
    if (showDownloadDialog) {
        val titleText = if (downloadFormat == "Excel") "Microsoft Excel Spreadsheet" else "PDF Document"
        val formatIconColor = if (downloadFormat == "Excel") Color(0xFF217346) else Color(0xFFE02D30)
        
        AlertDialog(
            onDismissRequest = { showDownloadDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (downloadFormat == "Excel") Icons.Default.Share else Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        tint = formatIconColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Download & Export Spreadsheet", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = OceanSurface,
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        "Configure your ledger report. Choose the file format and review the sample data below before saving.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    
                    // Format selector buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Excel (CSV)
                        Button(
                            onClick = { downloadFormat = "Excel" },
                            modifier = Modifier.weight(1f).height(38.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (downloadFormat == "Excel") Color(0xFF217346) else OceanDark,
                                contentColor = TextWhite
                            ),
                            border = BorderStroke(1.dp, if (downloadFormat == "Excel") TealNeon else LightIceBlue.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.GridOn, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Excel (CSV)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        // PDF
                        Button(
                            onClick = { downloadFormat = "PDF" },
                            modifier = Modifier.weight(1f).height(38.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (downloadFormat == "PDF") Color(0xFFE02D30) else OceanDark,
                                contentColor = TextWhite
                            ),
                            border = BorderStroke(1.dp, if (downloadFormat == "PDF") TealNeon else LightIceBlue.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PDF Format", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    // Styled Document Container Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(OceanDark)
                            .border(1.dp, TealNeon.copy(alpha = 0.3f), shape = RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Document Header
                            Text("FINTRACK BUSINESS SPREADSHEETS", color = TealNeon, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("Export Date: ${sdf.format(Date())}", color = TextSecondary, fontSize = 9.sp)
                            Text("Target Format: $titleText", color = TextWhite, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                            Text("Authorized By: Owner / Admin", color = TextSecondary, fontSize = 9.sp)
                            
                            HorizontalDivider(color = LightIceBlue.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))
                            
                            // Summary Section
                            Text("SUMMARY STATISTICS", color = TealNeon.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Purchases:", color = TextSecondary, fontSize = 10.sp)
                                Text("${purchases.size} Rows (₹${purchases.sumOf { it.totalAmount }.toInt()})", color = TextWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Sales:", color = TextSecondary, fontSize = 10.sp)
                                Text("${sales.size} Rows (₹${sales.sumOf { it.totalAmount }.toInt()})", color = TextWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Khatha Balance:", color = TextSecondary, fontSize = 10.sp)
                                Text("₹${customers.sumOf { it.creditBookBalance }.toInt()}", color = OrangeAlert, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Supplier Dues:", color = TextSecondary, fontSize = 10.sp)
                                Text("₹${suppliers.sumOf { it.pendingDues }.toInt()}", color = OrangeAlert, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Expenses Logged:", color = TextSecondary, fontSize = 10.sp)
                                Text("₹${expenses.sumOf { it.amount }.toInt()}", color = CoralRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            
                            HorizontalDivider(color = LightIceBlue.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))
                            
                            // Sample Rows Preview
                            Text("SAMPLES PREVIEW (FIRST 3 ROWS)", color = TealNeon, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            
                            when (activeTab) {
                                0 -> { // Purchases preview
                                    if (purchases.isEmpty()) {
                                        Text("No purchases recorded.", color = TextSecondary, fontSize = 9.sp)
                                    } else {
                                        purchases.take(3).forEach {
                                            Text("• ${sdf.format(Date(it.purchaseDate))} | ${it.fishName} | ${it.supplierName} | ₹${it.totalAmount.toInt()}", color = TextWhite, fontSize = 9.sp)
                                        }
                                    }
                                }
                                1 -> { // Sales preview
                                    if (sales.isEmpty()) {
                                        Text("No sales recorded.", color = TextSecondary, fontSize = 9.sp)
                                    } else {
                                        sales.take(3).forEach {
                                            Text("• ${sdf.format(Date(it.saleDate))} | ${it.customerName} | ${it.fishName} | ${it.qtyKg.toInt()}kg | ₹${it.totalAmount.toInt()}", color = TextWhite, fontSize = 9.sp)
                                        }
                                    }
                                }
                                2 -> { // Khatha preview
                                    if (customers.isEmpty()) {
                                        Text("No customer balance records.", color = TextSecondary, fontSize = 9.sp)
                                    } else {
                                        customers.take(3).forEach {
                                            Text("• ${it.name} | Mob: ${it.phone} | Outstanding: ₹${it.creditBookBalance.toInt()}", color = TextWhite, fontSize = 9.sp)
                                        }
                                    }
                                }
                                3 -> { // Supplier preview
                                    if (suppliers.isEmpty()) {
                                        Text("No supplier dues records.", color = TextSecondary, fontSize = 9.sp)
                                    } else {
                                        suppliers.take(3).forEach {
                                            Text("• ${it.name} | Mob: ${it.phone} | Dues: ₹${it.pendingDues.toInt()}", color = TextWhite, fontSize = 9.sp)
                                        }
                                    }
                                }
                                4 -> { // Expense preview
                                    if (expenses.isEmpty()) {
                                        Text("No expenses recorded.", color = TextSecondary, fontSize = 9.sp)
                                    } else {
                                        expenses.take(3).forEach {
                                            Text("• ${sdf.format(Date(it.date))} | [${it.category}] ${it.description} | ₹${it.amount.toInt()}", color = TextWhite, fontSize = 9.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Text(
                        "Reports are generated from live MySQL/SQLite synced records and are stored in your device's Downloads directory.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (downloadFormat == "Excel") {
                            saveCsvToDownloads(
                                context = context,
                                activeTab = activeTab,
                                purchases = purchases,
                                sales = sales,
                                customers = customers,
                                suppliers = suppliers,
                                expenses = expenses,
                                searchQuery = searchQuery,
                                paymentFilter = paymentFilter,
                                sortColumn = sortColumn,
                                sortAscending = sortAscending,
                                sdf = sdf
                            )
                        } else {
                            val html = generateHtmlContentFiltered(
                                activeTab = activeTab,
                                purchases = purchases,
                                sales = sales,
                                customers = customers,
                                suppliers = suppliers,
                                expenses = expenses,
                                searchQuery = searchQuery,
                                paymentFilter = paymentFilter,
                                sortColumn = sortColumn,
                                sortAscending = sortAscending,
                                sdf = sdf
                            )
                            val jobName = when (activeTab) {
                                0 -> "Wholesale_Purchases_Ledger"
                                1 -> "Customer_Sales_Ledger"
                                2 -> "Digital_Khatha_Ledger"
                                3 -> "Supplier_Payments_Ledger"
                                else -> "Business_Expenses_Ledger"
                            }
                            printHtmlPdf(context, html, jobName)
                        }
                        showDownloadDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealNeon, contentColor = OceanDark)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Download File")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDownloadDialog = false }) {
                    Text("Close", color = TextWhite)
                }
            }
        )
    }

}

// ------------------- SORTING HELPERS -------------------
private fun sortPurchases(list: List<Purchase>, col: String, asc: Boolean): List<Purchase> {
    if (col.isEmpty()) return list
    val comparator = when (col) {
        "date" -> compareBy<Purchase> { it.purchaseDate }
        "fish" -> compareBy { it.fishName.lowercase() }
        "supplier" -> compareBy { it.supplierName.lowercase() }
        "phone" -> compareBy { it.supplierPhone }
        "boat" -> compareBy { it.boatName.lowercase() }
        "price" -> compareBy { it.pricePerKg }
        "qty" -> compareBy { it.qtyKg }
        "total" -> compareBy { it.totalAmount }
        "status" -> compareBy { it.paymentStatus.lowercase() }
        else -> compareBy { 0 }
    }
    return if (asc) list.sortedWith(comparator) else list.sortedWith(comparator).reversed()
}

private fun sortSales(list: List<Sale>, col: String, asc: Boolean): List<Sale> {
    if (col.isEmpty()) return list
    val comparator = when (col) {
        "date" -> compareBy<Sale> { it.saleDate }
        "customer" -> compareBy { it.customerName.lowercase() }
        "phone" -> compareBy { it.customerPhone }
        "fish" -> compareBy { it.fishName.lowercase() }
        "qty" -> compareBy { it.qtyKg }
        "price" -> compareBy { it.pricePerKg }
        "total" -> compareBy { it.totalAmount }
        "paymentType" -> compareBy { it.paymentType.lowercase() }
        "status" -> compareBy { it.paymentStatus.lowercase() }
        else -> compareBy { 0 }
    }
    return if (asc) list.sortedWith(comparator) else list.sortedWith(comparator).reversed()
}

private fun sortKhatha(list: List<KhathaRowItem>, col: String, asc: Boolean): List<KhathaRowItem> {
    if (col.isEmpty()) return list
    val comparator = when (col) {
        "customer" -> compareBy<KhathaRowItem> { it.customer.name.lowercase() }
        "phone" -> compareBy { it.customer.phone }
        "total" -> compareBy { it.totalBilled }
        "pending" -> compareBy { it.pendingCredit }
        "paid" -> compareBy { it.paidAmount }
        "status" -> compareBy { it.status }
        else -> compareBy { 0 }
    }
    return if (asc) list.sortedWith(comparator) else list.sortedWith(comparator).reversed()
}

private fun sortSuppliers(list: List<SupplierRowItem>, col: String, asc: Boolean): List<SupplierRowItem> {
    if (col.isEmpty()) return list
    val comparator = when (col) {
        "supplier" -> compareBy<SupplierRowItem> { it.supplier.name.lowercase() }
        "phone" -> compareBy { it.supplier.phone }
        "total" -> compareBy { it.totalSourced }
        "balance" -> compareBy { it.remainingBalance }
        "paid" -> compareBy { it.paidToDate }
        "status" -> compareBy { it.status }
        else -> compareBy { 0 }
    }
    return if (asc) list.sortedWith(comparator) else list.sortedWith(comparator).reversed()
}

private fun sortExpenses(list: List<Expense>, col: String, asc: Boolean): List<Expense> {
    if (col.isEmpty()) return list
    val comparator = when (col) {
        "date" -> compareBy<Expense> { it.date }
        "category" -> compareBy { it.category.lowercase() }
        "desc" -> compareBy { it.description.lowercase() }
        "amount" -> compareBy { it.amount }
        else -> compareBy { 0 }
    }
    return if (asc) list.sortedWith(comparator) else list.sortedWith(comparator).reversed()
}

// Helper states
data class KhathaRowItem(
    val customer: Customer,
    val totalBilled: Double,
    val pendingCredit: Double,
    val paidAmount: Double,
    val status: String
)

data class SupplierRowItem(
    val supplier: Supplier,
    val totalSourced: Double,
    val remainingBalance: Double,
    val paidToDate: Double,
    val status: String
)

// Extension or helper logic to toggle sorts in view
private fun SpreadsheetScreenScope_toggleSort(currentCol: String, targetCol: String, asc: Boolean): Pair<String, Boolean> {
    return if (currentCol == targetCol) {
        Pair(currentCol, !asc)
    } else {
        Pair(targetCol, true)
    }
}

// ------------------- COMPOSE REUSABLE SPREADSHEET VIEWS -------------------

@Composable
fun HeaderCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    sortId: String,
    currentSortCol: String,
    sortAscending: Boolean,
    onClick: () -> Unit
) {
    val isSorted = currentSortCol == sortId && sortId.isNotEmpty()
    Box(
        modifier = Modifier
            .width(width)
            .border(0.5.dp, LightIceBlue.copy(alpha = 0.15f))
            .clickable(enabled = sortId.isNotEmpty()) { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = text.uppercase(),
                color = if (isSorted) TealNeon else TextWhite,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (sortId.isNotEmpty()) {
                Icon(
                    imageVector = if (isSorted && sortAscending) Icons.Default.ArrowUpward else if (isSorted) Icons.Default.ArrowDownward else Icons.Default.Sort,
                    contentDescription = null,
                    tint = if (isSorted) TealNeon else TextSecondary.copy(alpha = 0.5f),
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
fun Cell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    weight: FontWeight = FontWeight.Normal,
    color: Color = TextWhite,
    textAlign: TextAlign = TextAlign.Start
) {
    Box(
        modifier = Modifier
            .width(width)
            .height(34.dp)
            .border(0.5.dp, LightIceBlue.copy(alpha = 0.08f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = weight,
            textAlign = textAlign,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun StatusCell(
    status: String,
    width: androidx.compose.ui.unit.Dp,
    isSuccess: Boolean
) {
    Box(
        modifier = Modifier
            .width(width)
            .height(34.dp)
            .border(0.5.dp, LightIceBlue.copy(alpha = 0.08f))
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .background(
                    if (isSuccess) TealNeon.copy(alpha = 0.15f) else OrangeAlert.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                )
                .border(
                    0.5.dp,
                    if (isSuccess) TealNeon.copy(alpha = 0.5f) else OrangeAlert.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = status.uppercase(),
                color = if (isSuccess) TealNeon else OrangeAlert,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ActionCell(
    width: androidx.compose.ui.unit.Dp,
    onEdit: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(width)
            .height(34.dp)
            .border(0.5.dp, LightIceBlue.copy(alpha = 0.08f)),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onEdit,
            modifier = Modifier
                .size(24.dp)
                .testTag("spreadsheet_edit_row_button")
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit Row",
                tint = TealNeon,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun EmptySpreadsheetState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.GridOn,
            contentDescription = null,
            tint = TextSecondary.copy(alpha = 0.5f),
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = message,
            color = TextSecondary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun editFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextWhite,
    unfocusedTextColor = TextWhite,
    focusedBorderColor = TealNeon,
    unfocusedBorderColor = LightIceBlue.copy(alpha = 0.3f),
    focusedLabelColor = TealNeon,
    unfocusedLabelColor = TextSecondary,
    focusedContainerColor = OceanDark,
    unfocusedContainerColor = OceanDark
)

// ------------------- REAL-WORLD SPREADSHEET EXPORT ENGINE -------------------

fun saveCsvToDownloads(
    context: Context,
    activeTab: Int,
    purchases: List<Purchase>,
    sales: List<Sale>,
    customers: List<Customer>,
    suppliers: List<Supplier>,
    expenses: List<Expense>,
    searchQuery: String,
    paymentFilter: String,
    sortColumn: String,
    sortAscending: Boolean,
    sdf: SimpleDateFormat
) {
    try {
        val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val (fileName, csvContent) = when (activeTab) {
            0 -> {
                val base = purchases.filter {
                    it.fishName.contains(searchQuery, ignoreCase = true) ||
                    it.supplierName.contains(searchQuery, ignoreCase = true) ||
                    it.boatName.contains(searchQuery, ignoreCase = true)
                }.let {
                    if (paymentFilter != "All") it.filter { item -> item.paymentStatus.equals(paymentFilter, ignoreCase = true) } else it
                }.let { sortPurchases(it, sortColumn, sortAscending) }

                val name = "Wholesale_Purchases_$dateStr.csv"
                val csv = StringBuilder()
                csv.append("Purchase Date,Fish Name,Supplier Name,Supplier Phone,Boat/Harbor,Cost Per Kg,Total Weight,Total Amount,Payment Status\n")
                base.forEach {
                    csv.append("\"${sdf.format(Date(it.purchaseDate))}\",\"${it.fishName}\",\"${it.supplierName}\",\"${it.supplierPhone.ifEmpty { it.buyerPhone }}\",\"${it.boatName.ifEmpty { "N/A" }}\",${it.pricePerKg},${it.qtyKg},${it.totalAmount},\"${it.paymentStatus}\"\n")
                }
                Pair(name, csv.toString())
            }
            1 -> {
                val base = sales.filter {
                    it.fishName.contains(searchQuery, ignoreCase = true) ||
                    it.customerName.contains(searchQuery, ignoreCase = true)
                }.let {
                    if (paymentFilter != "All") it.filter { item -> item.paymentStatus.equals(paymentFilter, ignoreCase = true) } else it
                }.let { sortSales(it, sortColumn, sortAscending) }

                val name = "Customer_Sales_$dateStr.csv"
                val csv = StringBuilder()
                csv.append("Order Date,Customer Name,Customer Mobile,Fish Name,Quantity Purchased (kg),Price Per Kg,Total Amount,Payment Type,Payment Status\n")
                base.forEach {
                    csv.append("\"${sdf.format(Date(it.saleDate))}\",\"${it.customerName}\",\"${it.customerPhone.ifEmpty { "N/A" }}\",\"${it.fishName}\",${it.qtyKg},${it.pricePerKg},${it.totalAmount},\"${it.paymentType}\",\"${it.paymentStatus}\"\n")
                }
                Pair(name, csv.toString())
            }
            2 -> {
                val base = customers.map { cust ->
                    val cSales = sales.filter { it.customerName.lowercase() == cust.name.lowercase() }
                    val totalBilled = cSales.sumOf { it.totalAmount }
                    val pendingCredit = cust.creditBookBalance
                    val paidAmount = (totalBilled - pendingCredit).coerceAtLeast(0.0)
                    val status = if (pendingCredit <= 0.0) "Settled" else "Pending"
                    KhathaRowItem(cust, totalBilled, pendingCredit, paidAmount, status)
                }.filter {
                    it.customer.name.contains(searchQuery, ignoreCase = true) ||
                    it.customer.phone.contains(searchQuery, ignoreCase = true)
                }.let {
                    if (paymentFilter != "All") it.filter { item -> item.status.equals(paymentFilter, ignoreCase = true) } else it
                }.let { sortKhatha(it, sortColumn, sortAscending) }

                val name = "Digital_Khatha_Book_$dateStr.csv"
                val csv = StringBuilder()
                csv.append("Customer Name,Mobile Number,Total Billed Sales,Pending Credit (Dues),Paid Received,Status\n")
                base.forEach {
                    csv.append("\"${it.customer.name}\",\"${it.customer.phone}\",${it.totalBilled},${it.pendingCredit},${it.paidAmount},\"${it.status}\"\n")
                }
                Pair(name, csv.toString())
            }
            3 -> {
                val base = suppliers.map { sup ->
                    val totalSourced = sup.totalPurchased
                    val remainingBalance = sup.pendingDues
                    val paidToDate = (totalSourced - remainingBalance).coerceAtLeast(0.0)
                    val status = if (remainingBalance <= 0.0) "Settled" else "Pending"
                    SupplierRowItem(sup, totalSourced, remainingBalance, paidToDate, status)
                }.filter {
                    it.supplier.name.contains(searchQuery, ignoreCase = true) ||
                    it.supplier.phone.contains(searchQuery, ignoreCase = true)
                }.let {
                    if (paymentFilter != "All") it.filter { item -> item.status.equals(paymentFilter, ignoreCase = true) } else it
                }.let { sortSuppliers(it, sortColumn, sortAscending) }

                val name = "Supplier_Payments_$dateStr.csv"
                val csv = StringBuilder()
                csv.append("Supplier Name,Mobile Number,Total Sourced,Remaining Balance,Paid To Date,Status\n")
                base.forEach {
                    csv.append("\"${it.supplier.name}\",\"${it.supplier.phone}\",${it.totalSourced},${it.remainingBalance},${it.paidToDate},\"${it.status}\"\n")
                }
                Pair(name, csv.toString())
            }
            else -> {
                val base = expenses.filter {
                    it.category.contains(searchQuery, ignoreCase = true) ||
                    it.description.contains(searchQuery, ignoreCase = true)
                }.let { sortExpenses(it, sortColumn, sortAscending) }

                val name = "Business_Expenses_$dateStr.csv"
                val csv = StringBuilder()
                csv.append("Expense Date,Expense Category,Description,Amount\n")
                base.forEach {
                    csv.append("\"${sdf.format(Date(it.date))}\",\"${it.category}\",\"${it.description}\",${it.amount}\n")
                }
                Pair(name, csv.toString())
            }
        }

        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/FishBusiness")
            }
        }

        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            resolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(csvContent.toByteArray())
            }
            Toast.makeText(context, "Saved $fileName to Downloads/FishBusiness!", Toast.LENGTH_LONG).show()
        } else {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val appDir = java.io.File(downloadsDir, "FishBusiness")
            if (!appDir.exists()) appDir.mkdirs()
            val file = java.io.File(appDir, fileName)
            file.writeText(csvContent)
            Toast.makeText(context, "Saved $fileName to Downloads/FishBusiness!", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error saving CSV: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

fun generateHtmlContentFiltered(
    activeTab: Int,
    purchases: List<Purchase>,
    sales: List<Sale>,
    customers: List<Customer>,
    suppliers: List<Supplier>,
    expenses: List<Expense>,
    searchQuery: String,
    paymentFilter: String,
    sortColumn: String,
    sortAscending: Boolean,
    sdf: SimpleDateFormat
): String {
    val dateStr = sdf.format(Date())
    val title = when (activeTab) {
        0 -> "Wholesale Purchases Ledger"
        1 -> "Customer Sales & Invoicing Ledger"
        2 -> "Digital Khatha Book - Customer Credit Ledger"
        3 -> "Supplier Payments & Accounts Ledger"
        else -> "Business Daily Expenses Ledger"
    }

    val html = StringBuilder()
    html.append("""
        <html>
        <head>
            <meta charset="utf-8">
            <style>
                body { font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif; color: #222; margin: 30px; line-height: 1.4; }
                h1 { color: #005f73; font-size: 26px; margin-bottom: 5px; text-transform: uppercase; letter-spacing: 0.5px; }
                h2 { color: #0a9396; font-size: 15px; margin-top: 0; margin-bottom: 25px; font-weight: normal; border-bottom: 2px solid #005f73; padding-bottom: 10px; }
                .meta-table { width: 100%; margin-bottom: 30px; font-size: 13px; border-collapse: collapse; }
                .meta-table td { padding: 6px 0; }
                .meta-table td.label { color: #555; font-weight: bold; width: 18%; }
                .meta-table td.value { color: #111; width: 32%; }
                .data-table { width: 100%; border-collapse: collapse; margin-top: 20px; font-size: 11px; }
                .data-table th { background-color: #005f73; color: white; padding: 12px 10px; text-align: left; font-weight: bold; text-transform: uppercase; border: 1px solid #005f73; }
                .data-table td { padding: 10px; border: 1px solid #e0e0e0; }
                .data-table tr:nth-child(even) { background-color: #f9fbfb; }
                .data-table tr.total-row { background-color: #f1f5f6; font-weight: bold; font-size: 12px; }
                .data-table tr.total-row td { border-top: 2px solid #005f73; border-bottom: 2px solid #005f73; }
                .status-paid { color: #2b9348; font-weight: bold; }
                .status-pending { color: #ae2012; font-weight: bold; }
                .footer { margin-top: 50px; font-size: 11px; text-align: center; color: #777; border-top: 1px solid #ddd; padding-top: 20px; }
            </style>
        </head>
        <body>
            <h1>$title</h1>
            <h2>Blue Catch ERP - Digital Fish Business Management System</h2>
            
            <table class="meta-table">
                <tr>
                    <td class="label">Export Date:</td>
                    <td class="value">$dateStr</td>
                    <td class="label">Report Type:</td>
                    <td class="value">Official Business Audit Ledger</td>
                </tr>
                <tr>
                    <td class="label">Generated By:</td>
                    <td class="value">Authorized Owner Device</td>
                    <td class="label">System Status:</td>
                    <td class="value" style="color: #2b9348; font-weight: bold;">Verified & Synced</td>
                </tr>
            </table>
            
            <table class="data-table">
                <thead>
    """.trimIndent())

    when (activeTab) {
        0 -> { // Purchases
            val filtered = purchases.filter {
                it.fishName.contains(searchQuery, ignoreCase = true) ||
                it.supplierName.contains(searchQuery, ignoreCase = true) ||
                it.boatName.contains(searchQuery, ignoreCase = true)
            }.let {
                if (paymentFilter != "All") it.filter { item -> item.paymentStatus.equals(paymentFilter, ignoreCase = true) } else it
            }.let { sortPurchases(it, sortColumn, sortAscending) }

            html.append("""
                <tr>
                    <th>Date</th>
                    <th>Fish Name</th>
                    <th>Supplier</th>
                    <th>Mobile</th>
                    <th>Boat/Harbor</th>
                    <th>Cost/Kg</th>
                    <th>Weight</th>
                    <th>Total Amount</th>
                    <th>Status</th>
                </tr>
                </thead>
                <tbody>
            """.trimIndent())
            filtered.forEach {
                val statusClass = if (it.paymentStatus.lowercase() == "paid") "status-paid" else "status-pending"
                html.append("""
                    <tr>
                        <td>${sdf.format(Date(it.purchaseDate))}</td>
                        <td><b>${it.fishName}</b></td>
                        <td>${it.supplierName}</td>
                        <td>${it.supplierPhone.ifEmpty { it.buyerPhone }}</td>
                        <td>${it.boatName.ifEmpty { "N/A" }}</td>
                        <td>₹${it.pricePerKg.toInt()}</td>
                        <td>${it.qtyKg.toInt()} kg</td>
                        <td><b>₹${it.totalAmount.toInt()}</b></td>
                        <td><span class="$statusClass">${it.paymentStatus}</span></td>
                    </tr>
                """.trimIndent())
            }
            html.append("""
                <tr class="total-row">
                    <td colspan="6">TOTAL SUMMARY</td>
                    <td>${filtered.sumOf { it.qtyKg }.toInt()} kg</td>
                    <td colspan="2">₹${filtered.sumOf { it.totalAmount }.toInt()}</td>
                </tr>
            """.trimIndent())
        }
        1 -> { // Sales
            val filtered = sales.filter {
                it.fishName.contains(searchQuery, ignoreCase = true) ||
                it.customerName.contains(searchQuery, ignoreCase = true)
            }.let {
                if (paymentFilter != "All") it.filter { item -> item.paymentStatus.equals(paymentFilter, ignoreCase = true) } else it
            }.let { sortSales(it, sortColumn, sortAscending) }

            html.append("""
                <tr>
                    <th>Date</th>
                    <th>Customer</th>
                    <th>Mobile</th>
                    <th>Fish Name</th>
                    <th>Weight</th>
                    <th>Price/Kg</th>
                    <th>Total Amount</th>
                    <th>Pay Type</th>
                    <th>Status</th>
                </tr>
                </thead>
                <tbody>
            """.trimIndent())
            filtered.forEach {
                val statusClass = if (it.paymentStatus.lowercase() == "paid") "status-paid" else "status-pending"
                html.append("""
                    <tr>
                        <td>${sdf.format(Date(it.saleDate))}</td>
                        <td>${it.customerName}</td>
                        <td>${it.customerPhone.ifEmpty { "N/A" }}</td>
                        <td><b>${it.fishName}</b></td>
                        <td>${it.qtyKg.toInt()} kg</td>
                        <td>₹${it.pricePerKg.toInt()}</td>
                        <td><b>₹${it.totalAmount.toInt()}</b></td>
                        <td>${it.paymentType}</td>
                        <td><span class="$statusClass">${it.paymentStatus}</span></td>
                    </tr>
                """.trimIndent())
            }
            html.append("""
                <tr class="total-row">
                    <td colspan="4">TOTAL SUMMARY</td>
                    <td>${filtered.sumOf { it.qtyKg }.toInt()} kg</td>
                    <td>-</td>
                    <td colspan="3">₹${filtered.sumOf { it.totalAmount }.toInt()}</td>
                </tr>
            """.trimIndent())
        }
        2 -> { // Khatha Book
            val filtered = customers.map { cust ->
                val cSales = sales.filter { it.customerName.lowercase() == cust.name.lowercase() }
                val totalBilled = cSales.sumOf { it.totalAmount }
                val pendingCredit = cust.creditBookBalance
                val paidAmount = (totalBilled - pendingCredit).coerceAtLeast(0.0)
                val status = if (pendingCredit <= 0.0) "Settled" else "Pending"
                KhathaRowItem(cust, totalBilled, pendingCredit, paidAmount, status)
            }.filter {
                it.customer.name.contains(searchQuery, ignoreCase = true) ||
                it.customer.phone.contains(searchQuery, ignoreCase = true)
            }.let {
                if (paymentFilter != "All") it.filter { item -> item.status.equals(paymentFilter, ignoreCase = true) } else it
            }.let { sortKhatha(it, sortColumn, sortAscending) }

            html.append("""
                <tr>
                    <th>Customer Name</th>
                    <th>Mobile Number</th>
                    <th>Total Billed Sales</th>
                    <th>Pending Credit (Dues)</th>
                    <th>Paid Received</th>
                    <th>Status</th>
                </tr>
                </thead>
                <tbody>
            """.trimIndent())
            filtered.forEach { custItem ->
                val statusClass = if (custItem.pendingCredit <= 0.0) "status-paid" else "status-pending"
                html.append("""
                    <tr>
                        <td><b>${custItem.customer.name}</b></td>
                        <td>${custItem.customer.phone}</td>
                        <td>₹${custItem.totalBilled.toInt()}</td>
                        <td style="color: #ae2012;"><b>₹${custItem.pendingCredit.toInt()}</b></td>
                        <td style="color: #2b9348;">₹${custItem.paidAmount.toInt()}</td>
                        <td><span class="$statusClass">${custItem.status}</span></td>
                    </tr>
                """.trimIndent())
            }
            html.append("""
                <tr class="total-row">
                    <td colspan="2">TOTAL SUMMARY</td>
                    <td>₹${filtered.sumOf { it.totalBilled }.toInt()}</td>
                    <td style="color: #ae2012;">₹${filtered.sumOf { it.pendingCredit }.toInt()}</td>
                    <td colspan="2" style="color: #2b9348;">₹${filtered.sumOf { it.paidAmount }.toInt()}</td>
                </tr>
            """.trimIndent())
        }
        3 -> { // Supplier Payments
            val filtered = suppliers.map { sup ->
                val totalSourced = sup.totalPurchased
                val remainingBalance = sup.pendingDues
                val paidToDate = (totalSourced - remainingBalance).coerceAtLeast(0.0)
                val status = if (remainingBalance <= 0.0) "Settled" else "Pending"
                SupplierRowItem(sup, totalSourced, remainingBalance, paidToDate, status)
            }.filter {
                it.supplier.name.contains(searchQuery, ignoreCase = true) ||
                it.supplier.phone.contains(searchQuery, ignoreCase = true)
            }.let {
                if (paymentFilter != "All") it.filter { item -> item.status.equals(paymentFilter, ignoreCase = true) } else it
            }.let { sortSuppliers(it, sortColumn, sortAscending) }

            html.append("""
                <tr>
                    <th>Supplier Name</th>
                    <th>Mobile Number</th>
                    <th>Total Sourced</th>
                    <th>Remaining Balance</th>
                    <th>Paid To Date</th>
                    <th>Status</th>
                </tr>
                </thead>
                <tbody>
            """.trimIndent())
            filtered.forEach { supItem ->
                val statusClass = if (supItem.remainingBalance <= 0.0) "status-paid" else "status-pending"
                html.append("""
                    <tr>
                        <td><b>${supItem.supplier.name}</b></td>
                        <td>${supItem.supplier.phone}</td>
                        <td>₹${supItem.totalSourced.toInt()}</td>
                        <td style="color: #ae2012;"><b>₹${supItem.remainingBalance.toInt()}</b></td>
                        <td style="color: #2b9348;">₹${supItem.paidToDate.toInt()}</td>
                        <td><span class="$statusClass">${supItem.status}</span></td>
                    </tr>
                """.trimIndent())
            }
            html.append("""
                <tr class="total-row">
                    <td colspan="2">TOTAL SUMMARY</td>
                    <td>₹${filtered.sumOf { it.totalSourced }.toInt()}</td>
                    <td style="color: #ae2012;">₹${filtered.sumOf { it.remainingBalance }.toInt()}</td>
                    <td colspan="2" style="color: #2b9348;">₹${filtered.sumOf { it.paidToDate }.toInt()}</td>
                </tr>
            """.trimIndent())
        }
        4 -> { // Expenses
            val filtered = expenses.filter {
                it.category.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true)
            }.let { sortExpenses(it, sortColumn, sortAscending) }

            html.append("""
                <tr>
                    <th>Date</th>
                    <th>Expense Category</th>
                    <th>Description</th>
                    <th>Amount</th>
                </tr>
                </thead>
                <tbody>
            """.trimIndent())
            filtered.forEach {
                html.append("""
                    <tr>
                        <td>${sdf.format(Date(it.date))}</td>
                        <td style="color: #ae2012;"><b>${it.category}</b></td>
                        <td>${it.description}</td>
                        <td><b>₹${it.amount.toInt()}</b></td>
                    </tr>
                """.trimIndent())
            }
            html.append("""
                <tr class="total-row">
                    <td colspan="3">TOTAL SUMMARY</td>
                    <td style="color: #ae2012;">₹${filtered.sumOf { it.amount }.toInt()}</td>
                </tr>
            """.trimIndent())
        }
    }

    html.append("""
                </tbody>
            </table>
            
            <div class="footer">
                <p>This is a computer-generated official document from Blue Catch ERP fish business management ledger software.</p>
                <p>&copy; ${Calendar.getInstance().get(Calendar.YEAR)} Fish Business Management System. All Rights Reserved.</p>
            </div>
        </body>
        </html>
    """.trimIndent())

    return html.toString()
}

fun printHtmlPdf(context: Context, htmlContent: String, jobName: String) {
    try {
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                val printAdapter = webView.createPrintDocumentAdapter(jobName)
                printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error generating PDF: \${e.message}", Toast.LENGTH_LONG).show()
    }
}
