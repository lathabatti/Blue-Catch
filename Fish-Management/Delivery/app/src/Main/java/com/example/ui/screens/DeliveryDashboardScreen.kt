package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeliveryOrder
import com.example.data.model.DeliveryStaff
import com.example.ui.viewmodel.AppRole
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryDashboardScreen(
    viewModel: MainViewModel,
    staffId: Int,
    modifier: Modifier = Modifier
) {
    val allOrders by viewModel.allOrders.collectAsState()
    val staffList by viewModel.allDeliveryStaff.collectAsState()
    val staff = staffList.find { it.id == staffId } ?: DeliveryStaff(name = "Delivery Agent", phone = "", assignedArea = "")

    var selectedTab by remember { mutableStateOf(0) } // 0 = Active, 1 = History
    var showContactDialog by remember { mutableStateOf<DeliveryOrder?>(null) }
    var locationSimulationActive by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    // Mock Live GPS Coordinates
    var currentLatitude by remember { mutableStateOf(28.6139) }
    var currentLongitude by remember { mutableStateOf(77.2090) }

    LaunchedEffect(locationSimulationActive) {
        while (locationSimulationActive) {
            delay(4000)
            currentLatitude += ((-50..50).random() / 100000.0)
            currentLongitude += ((-50..50).random() / 100000.0)
        }
    }

    val staffOrders = allOrders.filter { it.deliveryStaffId == staffId }
    val activeOrders = staffOrders.filter { it.deliveryStatus != "Delivered" }
    val historyOrders = staffOrders.filter { it.deliveryStatus == "Delivered" }

    var sortBy by remember { mutableStateOf("Expected Arrival Time") } // "Status", "Expected Arrival Time", "Customer Name"
    var sortDropdownExpanded by remember { mutableStateOf(false) }
    var showExportChoiceDialog by remember { mutableStateOf(false) }
    var showExportSuccessDialog by remember { mutableStateOf<Pair<String, String>?>(null) } // Pair(SavedPath, ExportedText)

    // Search and filter operations
    val filteredActiveOrders = activeOrders.filter {
        it.customerName.contains(searchQuery, ignoreCase = true) ||
        it.orderId.contains(searchQuery, ignoreCase = true)
    }

    val filteredHistoryOrders = historyOrders.filter {
        it.customerName.contains(searchQuery, ignoreCase = true) ||
        it.orderId.contains(searchQuery, ignoreCase = true)
    }

    val sortedActiveOrders = filteredActiveOrders.sortedWith { o1, o2 ->
        when (sortBy) {
            "Status" -> {
                val statusPriority = mapOf(
                    "In Transit" to 1,
                    "Out for Delivery" to 2,
                    "Assigned" to 3,
                    "Picked Up" to 4
                )
                val p1 = statusPriority[o1.deliveryStatus] ?: 99
                val p2 = statusPriority[o2.deliveryStatus] ?: 99
                p1.compareTo(p2)
            }
            "Expected Arrival Time" -> {
                o1.assignedTime.compareTo(o2.assignedTime)
            }
            "Customer Name" -> {
                o1.customerName.compareTo(o2.customerName, ignoreCase = true)
            }
            else -> 0
        }
    }

    val sortedHistoryOrders = filteredHistoryOrders.sortedWith { o1, o2 ->
        when (sortBy) {
            "Status" -> o1.deliveryStatus.compareTo(o2.deliveryStatus)
            "Expected Arrival Time" -> o1.assignedTime.compareTo(o2.assignedTime)
            "Customer Name" -> o1.customerName.compareTo(o2.customerName, ignoreCase = true)
            else -> 0
        }
    }

    // Selected order for Live Route Visualization Map
    var selectedOrderForRoute by remember { mutableStateOf<DeliveryOrder?>(null) }

    LaunchedEffect(activeOrders) {
        if (activeOrders.isNotEmpty()) {
            if (selectedOrderForRoute == null || !activeOrders.any { it.deliveryId == selectedOrderForRoute?.deliveryId }) {
                selectedOrderForRoute = activeOrders.firstOrNull()
            }
        } else {
            selectedOrderForRoute = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            staff.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            "Delivery Partner • ${staff.assignedArea}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.switchRole(AppRole.Setup) },
                        modifier = Modifier.testTag("delivery_back_button")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Log Out")
                    }
                },
                actions = {
                    IconButton(onClick = { locationSimulationActive = !locationSimulationActive }) {
                        Icon(
                            imageVector = if (locationSimulationActive) Icons.Default.GpsFixed else Icons.Default.GpsOff,
                            contentDescription = "Toggle Mock GPS",
                            tint = if (locationSimulationActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // LazyColumn wrapping all sections to prevent pixel overflows on small screens
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // 1. Live GPS Status Widget
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "GPS",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "GPS Tracking Active",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    String.format(Locale.US, "Lat: %.5f, Lng: %.5f", currentLatitude, currentLongitude),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (locationSimulationActive) androidx.compose.ui.graphics.Color(0xFFE8F5E9)
                                        else androidx.compose.ui.graphics.Color(0xFFFFEBEE)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    if (locationSimulationActive) "TRANSMITTING" else "PAUSED",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (locationSimulationActive) androidx.compose.ui.graphics.Color(0xFF2E7D32)
                                    else androidx.compose.ui.graphics.Color(0xFFC62828)
                                )
                            }
                        }
                    }
                }

                // 2. Live Route Map Interface for selected order
                selectedOrderForRoute?.let { order ->
                    item {
                        DeliveryRouteMap(
                            order = order,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }

                // 3. Statistics Overview Dashboard Row
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DashboardStatCard(
                            title = "In Transit",
                            count = activeOrders.count { it.deliveryStatus == "In Transit" },
                            color = androidx.compose.ui.graphics.Color(0xFF2196F3),
                            icon = Icons.Default.LocalShipping,
                            modifier = Modifier.weight(1f)
                        )
                        DashboardStatCard(
                            title = "Out for Delivery",
                            count = activeOrders.count { it.deliveryStatus == "Out for Delivery" },
                            color = androidx.compose.ui.graphics.Color(0xFF9C27B0),
                            icon = Icons.Default.DirectionsBike,
                            modifier = Modifier.weight(1f)
                        )
                        DashboardStatCard(
                            title = "Delivered Today",
                            count = historyOrders.size,
                            color = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                            icon = Icons.Default.CheckCircle,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 4. Search Filter Input Field & Voice Commands Mic Button
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search customer name or order ID...") },
                            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear Search")
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("delivery_search_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )

                        // Microphone Voice Command Button
                        val context = LocalContext.current
                        val speechRecognizerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                            contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
                        ) { result ->
                            if (result.resultCode == android.app.Activity.RESULT_OK) {
                                val spokenText = result.data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
                                if (!spokenText.isNullOrEmpty()) {
                                    var matched = false
                                    val cleanedText = spokenText.lowercase()
                                    val statusToSet = when {
                                        cleanedText.contains("transit") -> "In Transit"
                                        cleanedText.contains("out") -> "Out for Delivery"
                                        cleanedText.contains("delivered") || cleanedText.contains("deliver") -> "Delivered"
                                        else -> null
                                    }

                                    if (statusToSet != null) {
                                        val orderMatch = staffOrders.find {
                                            cleanedText.contains(it.orderId.lowercase()) || cleanedText.contains(it.deliveryId.toString())
                                        }
                                        if (orderMatch != null) {
                                            viewModel.updateDeliveryStatus(orderMatch.deliveryId, statusToSet)
                                            android.widget.Toast.makeText(context, "Voice command received: Set ${orderMatch.orderId} to $statusToSet", android.widget.Toast.LENGTH_LONG).show()
                                            matched = true
                                        }
                                    }

                                    if (!matched) {
                                        android.widget.Toast.makeText(context, "Could not match command. Try: 'Order 1 delivered'", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }

                        val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                            contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
                        ) { isGranted ->
                            if (isGranted) {
                                val intent = Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault())
                                    putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "Say a command, e.g. 'Order 1 delivered'")
                                }
                                try {
                                    speechRecognizerLauncher.launch(intent)
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(context, "Speech recognition not supported on this device.", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                android.widget.Toast.makeText(context, "Microphone permission is required for voice commands.", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }

                        IconButton(
                            onClick = {
                                val permissionCheck = androidx.core.content.ContextCompat.checkSelfPermission(
                                    context,
                                    android.Manifest.permission.RECORD_AUDIO
                                )
                                if (permissionCheck == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                    val intent = Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                        putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault())
                                        putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "Say a command, e.g. 'Order 1 delivered'")
                                    }
                                    try {
                                        speechRecognizerLauncher.launch(intent)
                                    } catch (e: Exception) {
                                        android.widget.Toast.makeText(context, "Speech recognition not supported.", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .testTag("voice_command_mic_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice Commands Microphone",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // 4b. Sorting & Export Control Panel
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Sorting Dropdown Box
                        Box {
                            OutlinedButton(
                                onClick = { sortDropdownExpanded = true },
                                modifier = Modifier.testTag("sorting_dropdown_button"),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sort,
                                    contentDescription = "Sort Icon",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Sort: $sortBy",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            
                            DropdownMenu(
                                expanded = sortDropdownExpanded,
                                onDismissRequest = { sortDropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Status", fontSize = 13.sp) },
                                    onClick = {
                                        sortBy = "Status"
                                        sortDropdownExpanded = false
                                    },
                                    leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    modifier = Modifier.testTag("sort_by_status")
                                )
                                DropdownMenuItem(
                                    text = { Text("Expected Arrival Time", fontSize = 13.sp) },
                                    onClick = {
                                        sortBy = "Expected Arrival Time"
                                        sortDropdownExpanded = false
                                    },
                                    leadingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    modifier = Modifier.testTag("sort_by_arrival")
                                )
                                DropdownMenuItem(
                                    text = { Text("Customer Name", fontSize = 13.sp) },
                                    onClick = {
                                        sortBy = "Customer Name"
                                        sortDropdownExpanded = false
                                    },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    modifier = Modifier.testTag("sort_by_customer")
                                )
                            }
                        }

                        // Export Button
                        Button(
                            onClick = { showExportChoiceDialog = true },
                            modifier = Modifier.testTag("export_list_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Export Report Icon",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Export",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 5. Tabs Selector
                item {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.DirectionsRun, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Active (${filteredActiveOrders.size})", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                }
                            },
                            modifier = Modifier.testTag("tab_active_deliveries")
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Completed (${filteredHistoryOrders.size})", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                }
                            },
                            modifier = Modifier.testTag("tab_completed_history")
                        )
                    }
                }

                // 6. Content List
                val currentList = if (selectedTab == 0) sortedActiveOrders else sortedHistoryOrders

                if (currentList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (selectedTab == 0) Icons.Default.Inbox else Icons.Default.HistoryEdu,
                                    contentDescription = "Empty State",
                                    modifier = Modifier.size(54.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (searchQuery.isNotEmpty()) "No Results Found" else if (selectedTab == 0) "No Active Orders" else "No History Yet",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (searchQuery.isNotEmpty()) "Try adjusting your search criteria" else if (selectedTab == 0) "No orders assigned to you. Contact dispatch." else "Completed orders will show up here.",
                                    textAlign = TextAlign.Center,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                } else {
                    items(currentList, key = { it.deliveryId }) { order ->
                        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            DeliveryOrderCard(
                                order = order,
                                onUpdateStatus = { newStatus ->
                                    viewModel.updateDeliveryStatus(order.deliveryId, newStatus)
                                },
                                onContact = { showContactDialog = order },
                                onViewRoute = { selectedOrderForRoute = order },
                                isRouteSelected = selectedOrderForRoute?.deliveryId == order.deliveryId,
                                onUpdateNotes = { notes ->
                                    viewModel.updateDeliveryNotes(order.deliveryId, notes)
                                },
                                currentLatitude = currentLatitude,
                                currentLongitude = currentLongitude
                            )
                        }
                    }
                }
            }
        }
    }

    // Export Choice Dialog
    if (showExportChoiceDialog) {
        val context = LocalContext.current
        val currentListToExport = if (selectedTab == 0) sortedActiveOrders else sortedHistoryOrders
        AlertDialog(
            onDismissRequest = { showExportChoiceDialog = false },
            icon = { Icon(imageVector = Icons.Default.Download, contentDescription = null) },
            title = { Text("Export Orders Report") },
            text = {
                Column {
                    Text(
                        text = "Choose a format to export the current filtered and sorted list of ${currentListToExport.size} orders.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                val csvText = exportToCSV(currentListToExport)
                                val filename = "delivery_report_${System.currentTimeMillis()}.csv"
                                saveAndShareFile(context, filename, "text/csv", csvText) { path ->
                                    showExportChoiceDialog = false
                                    showExportSuccessDialog = Pair(path, csvText)
                                }
                            },
                            modifier = Modifier.weight(1f).testTag("export_csv_option"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("CSV Format", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                val jsonText = exportToJSON(currentListToExport)
                                val filename = "delivery_report_${System.currentTimeMillis()}.json"
                                saveAndShareFile(context, filename, "application/json", jsonText) { path ->
                                    showExportChoiceDialog = false
                                    showExportSuccessDialog = Pair(path, jsonText)
                                }
                            },
                            modifier = Modifier.weight(1f).testTag("export_json_option"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("JSON Format", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showExportChoiceDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Export Success & Preview Dialog
    showExportSuccessDialog?.let { (savedPath, contentText) ->
        val context = LocalContext.current
        AlertDialog(
            onDismissRequest = { showExportSuccessDialog = null },
            icon = { Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = androidx.compose.ui.graphics.Color(0xFF2E7D32)) },
            title = { Text("Export Successful", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "File successfully generated and saved to:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = savedPath,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                            .testTag("exported_file_path")
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Preview of exported data:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // Scrollable preview box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item {
                                Text(
                                    text = contentText,
                                    fontSize = 10.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Copy to Clipboard
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("Exported Deliveries", contentText)
                                clipboard.setPrimaryClip(clip)
                                android.widget.Toast.makeText(context, "Copied to clipboard!", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f).testTag("copy_export_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy", fontSize = 12.sp)
                        }

                        // Share Intent
                        Button(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Fish Business Delivery Report")
                                    putExtra(Intent.EXTRA_TEXT, contentText)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Report via"))
                            },
                            modifier = Modifier.weight(1f).testTag("share_export_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share", fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showExportSuccessDialog = null }) {
                    Text("Close")
                }
            }
        )
    }

    // Customer Contact Simulation Dialog
    showContactDialog?.let { order ->
        val context = LocalContext.current
        AlertDialog(
            onDismissRequest = { showContactDialog = null },
            icon = { Icon(imageVector = Icons.Default.Phone, contentDescription = null) },
            title = { Text("Contact ${order.customerName}") },
            text = {
                Column {
                    Text(
                        "Mobile: ${order.mobileNumber}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Select an action to simulate connection with the customer:")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${order.mobileNumber}"))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Non-blocking catch
                        }
                        showContactDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Simulate Call")
                }
            },
            dismissButton = {
                TextButton(onClick = { showContactDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DeliveryRouteMap(
    order: DeliveryOrder,
    modifier: Modifier = Modifier
) {
    val progress = when (order.deliveryStatus) {
        "Assigned" -> 0.15f
        "In Transit" -> 0.5f
        "Out for Delivery" -> 0.8f
        "Delivered" -> 1.0f
        else -> 0.15f
    }

    val progressLabel = when (order.deliveryStatus) {
        "Assigned" -> "Order Ready at Hub"
        "In Transit" -> "On Route (Highway 2)"
        "Out for Delivery" -> "Arrived at Local Sub-Hub"
        "Delivered" -> "Delivered Successfully"
        else -> order.deliveryStatus
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("delivery_route_map_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = "Map Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Route Visualization",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = order.orderId,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Map canvas drawing roads behind and overlay nodes
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
            ) {
                // Drawing grid roads behind
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height

                    val strokeWidth = 2f
                    val roadColor = androidx.compose.ui.graphics.Color.Gray.copy(alpha = 0.1f)

                    // Draw vertical roads
                    for (i in 1..8) {
                        val x = width * (i / 9f)
                        drawLine(
                            color = roadColor,
                            start = androidx.compose.ui.geometry.Offset(x, 0f),
                            end = androidx.compose.ui.geometry.Offset(x, height),
                            strokeWidth = strokeWidth
                        )
                    }

                    // Draw horizontal roads
                    for (i in 1..4) {
                        val y = height * (i / 5f)
                        drawLine(
                            color = roadColor,
                            start = androidx.compose.ui.geometry.Offset(0f, y),
                            end = androidx.compose.ui.geometry.Offset(width, y),
                            strokeWidth = strokeWidth
                        )
                    }

                    // Main route path line
                    val pathColor = if (order.deliveryStatus == "Delivered") {
                        androidx.compose.ui.graphics.Color(0xFF2E7D32)
                    } else {
                        androidx.compose.ui.graphics.Color(0xFF2196F3)
                    }

                    // Shadow/Background route line
                    drawLine(
                        color = pathColor.copy(alpha = 0.15f),
                        start = androidx.compose.ui.geometry.Offset(width * 0.18f, height * 0.5f),
                        end = androidx.compose.ui.geometry.Offset(width * 0.82f, height * 0.5f),
                        strokeWidth = 10f,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )

                    // Active progress route line
                    drawLine(
                        color = pathColor,
                        start = androidx.compose.ui.geometry.Offset(width * 0.18f, height * 0.5f),
                        end = androidx.compose.ui.geometry.Offset(width * (0.18f + 0.64f * progress), height * 0.5f),
                        strokeWidth = 10f,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }

                // UI Nodes: Hub
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = "Hub",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Hub", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }

                // UI Nodes: Destination
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (order.deliveryStatus == "Delivered") androidx.compose.ui.graphics.Color(0xFFE8F5E9)
                            else MaterialTheme.colorScheme.primaryContainer
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (order.deliveryStatus == "Delivered") Icons.Default.CheckCircle else Icons.Default.Home,
                            contentDescription = "Destination",
                            tint = if (order.deliveryStatus == "Delivered") androidx.compose.ui.graphics.Color(0xFF2E7D32) else MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (order.deliveryStatus == "Delivered") "Delivered" else "Destination",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (order.deliveryStatus == "Delivered") androidx.compose.ui.graphics.Color(0xFF2E7D32) else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Moving Agent Node representing current delivery transit position
                val agentOffsetFraction = 0.18f + 0.64f * progress
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val xOffset = maxWidth * agentOffsetFraction - 16.dp
                    Box(
                        modifier = Modifier
                            .offset(x = xOffset, y = maxHeight / 2 - 16.dp)
                            .size(32.dp)
                            .clip(RoundedCornerShape(50))
                            .background(
                                if (order.deliveryStatus == "Delivered") androidx.compose.ui.graphics.Color(0xFF2E7D32)
                                else MaterialTheme.colorScheme.primary
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (order.deliveryStatus) {
                                "Delivered" -> Icons.Default.DoneAll
                                "Out for Delivery" -> Icons.Default.DirectionsBike
                                "In Transit" -> Icons.Default.LocalShipping
                                else -> Icons.Default.DirectionsRun
                            },
                            contentDescription = "Agent Marker",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Path information details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Stage: $progressLabel",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Deliver to: ${order.customerName}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = when (order.deliveryStatus) {
                            "Assigned" -> "Ready at Hub"
                            "In Transit" -> "~3.5 km"
                            "Out for Delivery" -> "~0.8 km"
                            "Delivered" -> "0.0 km"
                            else -> "In Transit"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = when (order.deliveryStatus) {
                            "Assigned" -> "Waiting to Pick Up"
                            "In Transit" -> "15 mins remaining"
                            "Out for Delivery" -> "5 mins remaining"
                            "Delivered" -> "Arrived safely"
                            else -> "En Route"
                        },
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            // Delivery Zone & Sectors Overview Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.FilterVintage,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Delivery Zones & Sectors",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Generated Zone Map Thumbnail
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img_delivery_zones),
                    contentDescription = "Delivery Zones Map",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )

                // Legends of Fish Delivery Zones
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(50)).background(androidx.compose.ui.graphics.Color(0xFFE91E63)))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Rohu Zone (North Sector)", fontSize = 10.sp, fontWeight = FontWeight.Medium)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(50)).background(androidx.compose.ui.graphics.Color(0xFF2196F3)))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Salmon Zone (East Sector)", fontSize = 10.sp, fontWeight = FontWeight.Medium)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(50)).background(androidx.compose.ui.graphics.Color(0xFFFF9800)))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Prawns Zone (West Sector)", fontSize = 10.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardStatCard(
    title: String,
    count: Int,
    color: androidx.compose.ui.graphics.Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.08f)
        ),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = count.toString(),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = color
            )
            Text(
                text = title,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DeliveryOrderCard(
    order: DeliveryOrder,
    onUpdateStatus: (String) -> Unit,
    onContact: () -> Unit,
    onViewRoute: () -> Unit,
    isRouteSelected: Boolean,
    onUpdateNotes: (String) -> Unit,
    currentLatitude: Double = 28.6139,
    currentLongitude: Double = 77.2090
) {
    val dateString = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(order.assignedTime))

    // Expected Arrival Time Estimation (+45 minutes from assignedTime)
    val expectedArrivalCalendar = Calendar.getInstance().apply {
        timeInMillis = order.assignedTime + 45 * 60 * 1000
    }
    val expectedArrivalTimeString = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(expectedArrivalCalendar.time)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("delivery_order_card_${order.deliveryId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRouteSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(
            width = if (isRouteSelected) 2.dp else 1.dp,
            color = if (isRouteSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: ID and Assigned Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = order.orderId,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Priority Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when (order.priority) {
                                    "High" -> androidx.compose.ui.graphics.Color(0xFFFFEBEE)
                                    "Medium" -> androidx.compose.ui.graphics.Color(0xFFFFF3E0)
                                    else -> androidx.compose.ui.graphics.Color(0xFFE8F5E9)
                                }
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                            .testTag("priority_badge_${order.deliveryId}")
                    ) {
                        Text(
                            text = order.priority.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (order.priority) {
                                "High" -> androidx.compose.ui.graphics.Color(0xFFC62828)
                                "Medium" -> androidx.compose.ui.graphics.Color(0xFFE65100)
                                else -> androidx.compose.ui.graphics.Color(0xFF2E7D32)
                            }
                        )
                    }
                }
                Text(
                    text = "Assigned: $dateString",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            // Customer Name & Mobile, Quick Dialer, and View Route Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = order.customerName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Phone: ${order.mobileNumber}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Route View Selector Button
                    IconButton(
                        onClick = onViewRoute,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isRouteSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)
                            )
                            .testTag("view_route_button_${order.deliveryId}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "View Route Map",
                            tint = if (isRouteSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Phone Dialer Button
                    IconButton(
                        onClick = onContact,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                            .testTag("contact_button_${order.deliveryId}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Contact Customer",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Address Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(top = 2.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = order.deliveryAddress,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }

            // Expected Arrival Row (Dashboard Requirement)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Expected Arrival Icon",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Expected Arrival: ",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (order.deliveryStatus == "Delivered") "Arrived" else expectedArrivalTimeString,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (order.deliveryStatus == "Delivered") androidx.compose.ui.graphics.Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                )
            }

            if (order.deliveryStatus == "In Transit" || order.deliveryStatus == "Out for Delivery") {
                val (mins, dist) = calculateMinutesAndDistance(order, currentLatitude, currentLongitude)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f))
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f)), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Countdown",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Arriving in ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "$mins mins",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.testTag("countdown_timer_${order.deliveryId}")
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "• $dist km away",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            // Product Details and Price Info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.fishName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Quantity: ${order.quantity} kg",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹${order.amount}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when (order.paymentStatus) {
                                    "Paid" -> androidx.compose.ui.graphics.Color(0xFFE8F5E9)
                                    "Pending" -> androidx.compose.ui.graphics.Color(0xFFFFF3E0)
                                    else -> androidx.compose.ui.graphics.Color(0xFFECEFF1)
                                }
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = order.paymentStatus.uppercase(),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (order.paymentStatus) {
                                "Paid" -> androidx.compose.ui.graphics.Color(0xFF2E7D32)
                                "Pending" -> androidx.compose.ui.graphics.Color(0xFFE65100)
                                else -> androidx.compose.ui.graphics.Color(0xFF37474F)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Delivery Status Indicator Label
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CURRENT STATUS: ",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            when (order.deliveryStatus) {
                                "Assigned" -> androidx.compose.ui.graphics.Color(0xFF9E9E9E)
                                "In Transit" -> androidx.compose.ui.graphics.Color(0xFF2196F3)
                                "Out for Delivery" -> androidx.compose.ui.graphics.Color(0xFF9C27B0)
                                else -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
                            }
                        )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = order.deliveryStatus.uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = when (order.deliveryStatus) {
                        "Assigned" -> MaterialTheme.colorScheme.onSurfaceVariant
                        "In Transit" -> androidx.compose.ui.graphics.Color(0xFF1976D2)
                        "Out for Delivery" -> androidx.compose.ui.graphics.Color(0xFF7B1FA2)
                        else -> androidx.compose.ui.graphics.Color(0xFF2E7D32)
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons to Toggle Statuses: In Transit, Out for Delivery, Delivered
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "TOGGLE STATUS",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Button In Transit
                    val isInTransit = order.deliveryStatus == "In Transit"
                    OutlinedButton(
                        onClick = { onUpdateStatus("In Transit") },
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("status_button_in_transit_${order.deliveryId}"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isInTransit) androidx.compose.ui.graphics.Color(0xFF2196F3).copy(alpha = 0.12f) else androidx.compose.ui.graphics.Color.Transparent,
                            contentColor = if (isInTransit) androidx.compose.ui.graphics.Color(0xFF1976D2) else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isInTransit) androidx.compose.ui.graphics.Color(0xFF2196F3) else MaterialTheme.colorScheme.outlineVariant
                        ),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Transit", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }

                    // Button Out for Delivery
                    val isOutForDelivery = order.deliveryStatus == "Out for Delivery"
                    OutlinedButton(
                        onClick = { onUpdateStatus("Out for Delivery") },
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("status_button_out_for_delivery_${order.deliveryId}"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isOutForDelivery) androidx.compose.ui.graphics.Color(0xFF9C27B0).copy(alpha = 0.12f) else androidx.compose.ui.graphics.Color.Transparent,
                            contentColor = if (isOutForDelivery) androidx.compose.ui.graphics.Color(0xFF7B1FA2) else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isOutForDelivery) androidx.compose.ui.graphics.Color(0xFF9C27B0) else MaterialTheme.colorScheme.outlineVariant
                        ),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBike,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Out", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }

                    // Button Delivered
                    val isDelivered = order.deliveryStatus == "Delivered"
                    OutlinedButton(
                        onClick = { onUpdateStatus("Delivered") },
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("status_button_delivered_${order.deliveryId}"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isDelivered) androidx.compose.ui.graphics.Color(0xFF4CAF50).copy(alpha = 0.12f) else androidx.compose.ui.graphics.Color.Transparent,
                            contentColor = if (isDelivered) androidx.compose.ui.graphics.Color(0xFF388E3C) else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isDelivered) androidx.compose.ui.graphics.Color(0xFF4CAF50) else MaterialTheme.colorScheme.outlineVariant
                        ),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Delivered", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))

                var isExpanded by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Special Instructions & Activity Log",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (isExpanded) {
                    Spacer(modifier = Modifier.height(8.dp))
                    // Editable notes field
                    var notesText by remember { mutableStateOf(order.deliveryNotes) }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "SPECIAL HANDLING INSTRUCTIONS",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = notesText,
                                onValueChange = { notesText = it },
                                placeholder = { Text("E.g., Keep on ice, deliver to back door", fontSize = 11.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("notes_input_${order.deliveryId}"),
                                textStyle = TextStyle(fontSize = 12.sp),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { onUpdateNotes(notesText) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                                    .testTag("save_notes_button_${order.deliveryId}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = "Save Notes",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Timestamped Activity Log
                        Text(
                            text = "TIMESTAMPED ACTIVITY LOG",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val historyList = remember(order.statusHistory) {
                            if (order.statusHistory.isEmpty()) {
                                listOf("Assigned" to order.assignedTime)
                            } else {
                                order.statusHistory.split("\n").mapNotNull { line ->
                                    val parts = line.split(":")
                                    if (parts.size >= 2) {
                                        val status = parts[0]
                                        val ts = parts[1].toLongOrNull() ?: 0L
                                        status to ts
                                    } else null
                                }
                            }
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            historyList.forEachIndexed { index, (status, ts) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(
                                                if (index == historyList.size - 1) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.outlineVariant
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = status,
                                        fontSize = 11.sp,
                                        fontWeight = if (index == historyList.size - 1) FontWeight.Bold else FontWeight.Normal,
                                        color = if (index == historyList.size - 1) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = SimpleDateFormat("dd MMM, hh:mm:ss a", Locale.getDefault()).format(Date(ts)),
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun calculateMinutesAndDistance(order: DeliveryOrder, currentLatitude: Double, currentLongitude: Double): Pair<Int, Double> {
    val seed = order.orderId.hashCode()
    val random = java.util.Random(seed.toLong())
    
    val baseDistance = when (order.deliveryStatus) {
        "In Transit" -> 4.5
        "Out for Delivery" -> 1.2
        else -> 0.0
    }
    
    if (baseDistance == 0.0) return Pair(0, 0.0)
    
    val latFluctuation = (currentLatitude - 28.6139) * 10.0
    val lngFluctuation = (currentLongitude - 77.2090) * 10.0
    val fluctuation = (random.nextDouble() * 0.1) + (latFluctuation + lngFluctuation).coerceIn(-0.2, 0.2)
    
    val elapsedMins = (System.currentTimeMillis() - order.assignedTime) / 60000.0
    val progress = (elapsedMins / 45.0).coerceIn(0.0, 0.95)
    
    val calculatedDistance = ((baseDistance * (1.0 - progress)) + fluctuation).coerceAtLeast(0.1)
    val calculatedMinutes = (calculatedDistance * 4).toInt().coerceAtLeast(1)
    
    val formattedDistance = (calculatedDistance * 10).toInt() / 10.0
    return Pair(calculatedMinutes, formattedDistance)
}

fun exportToCSV(orders: List<DeliveryOrder>): String {
    val sb = java.lang.StringBuilder()
    sb.append("Order ID,Customer Name,Phone,Address,Fish,Quantity (kg),Amount (INR),Payment,Status,Priority,Assigned Time\n")
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    for (order in orders) {
        val assignedDate = sdf.format(Date(order.assignedTime))
        val escapedAddress = order.deliveryAddress.replace("\"", "\"\"")
        sb.append("${order.orderId},\"${order.customerName}\",\"${order.mobileNumber}\",\"$escapedAddress\",\"${order.fishName}\",${order.quantity},${order.amount},\"${order.paymentStatus}\",\"${order.deliveryStatus}\",\"${order.priority}\",\"$assignedDate\"\n")
    }
    return sb.toString()
}

fun exportToJSON(orders: List<DeliveryOrder>): String {
    val sb = java.lang.StringBuilder()
    sb.append("[\n")
    for (i in orders.indices) {
        val order = orders[i]
        sb.append("  {\n")
        sb.append("    \"orderId\": \"${order.orderId}\",\n")
        sb.append("    \"customerName\": \"${order.customerName}\",\n")
        sb.append("    \"phone\": \"${order.mobileNumber}\",\n")
        sb.append("    \"address\": \"${order.deliveryAddress}\",\n")
        sb.append("    \"fish\": \"${order.fishName}\",\n")
        sb.append("    \"quantity\": ${order.quantity},\n")
        sb.append("    \"amount\": ${order.amount},\n")
        sb.append("    \"paymentStatus\": \"${order.paymentStatus}\",\n")
        sb.append("    \"deliveryStatus\": \"${order.deliveryStatus}\",\n")
        sb.append("    \"priority\": \"${order.priority}\",\n")
        sb.append("    \"assignedTime\": ${order.assignedTime}\n")
        sb.append("  }")
        if (i < orders.size - 1) sb.append(",")
        sb.append("\n")
    }
    sb.append("]")
    return sb.toString()
}

fun saveAndShareFile(
    context: android.content.Context,
    filename: String,
    mimeType: String,
    content: String,
    onComplete: (String) -> Unit
) {
    try {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put("relative_path", android.os.Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(content.toByteArray())
                }
                onComplete("Downloads/$filename")
                return
            }
        }
        
        val externalFilesDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
        val file = java.io.File(externalFilesDir, filename)
        file.writeText(content)
        onComplete(file.absolutePath)
    } catch (e: Exception) {
        try {
            val file = java.io.File(context.filesDir, filename)
            file.writeText(content)
            onComplete("App internal: ${file.absolutePath}")
        } catch (ex: Exception) {
            onComplete("InMemory Preview")
        }
    }
}
