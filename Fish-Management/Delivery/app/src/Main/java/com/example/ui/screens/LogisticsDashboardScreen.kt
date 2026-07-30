package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeliveryOrder
import com.example.data.model.DeliveryStaff
import com.example.data.model.Notification
import com.example.ui.viewmodel.AppRole
import com.example.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogisticsDashboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val orders by viewModel.allOrders.collectAsState()
    val drivers by viewModel.allDeliveryStaff.collectAsState()
    val notifications by viewModel.allNotifications.collectAsState()

    val context = LocalContext.current

    // State management
    var driverName by remember { mutableStateOf("") }
    var driverPhone by remember { mutableStateOf("") }
    var driverArea by remember { mutableStateOf("") }
    var driverFormError by remember { mutableStateOf<String?>(null) }

    var selectedOrderForRouteMap by remember { mutableStateOf<DeliveryOrder?>(null) }
    var selectedOrderToAssign by remember { mutableStateOf<DeliveryOrder?>(null) }
    var showAssignDialog by remember { mutableStateOf(false) }
    var selectedDriverIdToAssign by remember { mutableStateOf<Int?>(null) }
    var isDriverDropdownExpanded by remember { mutableStateOf(false) }

    var showNotificationsSheet by remember { mutableStateOf(false) }

    // If no order is selected for map, default to the first active order if any
    LaunchedEffect(orders) {
        if (selectedOrderForRouteMap == null) {
            selectedOrderForRouteMap = orders.firstOrNull { it.deliveryStatus != "Delivered" }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Logistics Hub Dashboard",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.switchRole(AppRole.Setup) },
                        modifier = Modifier.testTag("logistics_back_button")
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back to Role Selection")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showNotificationsSheet = true },
                        modifier = Modifier.testTag("logistics_notifications_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (notifications.isNotEmpty()) {
                                    Badge {
                                        Text(
                                            text = notifications.size.coerceAtMost(99).toString(),
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Notifications, contentDescription = "Alerts Log")
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.testTag("logistics_app_bar")
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title Card and Onboarding
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .testTag("logistics_header_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Anchor,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Fish Logistics Management Console",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Register fresh fish delivery drivers, optimize delivery routes, assign pending orders, and monitor real-time distribution status.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Quick Analytics Stats Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val activeRoutesCount = orders.count { it.deliveryStatus != "Delivered" && it.deliveryStaffId > 0 }
                    val unassignedOrdersCount = orders.count { it.deliveryStaffId == 0 }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stat_card_drivers"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = Icons.Default.People, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Drivers", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${drivers.size}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stat_card_active_routes"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = Icons.Default.DirectionsBike, contentDescription = null, tint = Color(0xFF9C27B0), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Active Routes", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$activeRoutesCount", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stat_card_pending_orders"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = Icons.Default.PendingActions, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Pending Orders", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("$unassignedOrdersCount", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            // Route Visualization Component (requirement 2)
            item {
                selectedOrderForRouteMap?.let { order ->
                    val driver = drivers.find { it.id == order.deliveryStaffId }
                    LogisticsRouteMapVisualizer(
                        order = order,
                        driver = driver,
                        modifier = Modifier.testTag("logistics_route_visualizer_card")
                    )
                } ?: Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("logistics_no_route_map_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.Map, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Select a route or order below to visualize path optimization", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Driver Registration Form (requirement 4)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("driver_registration_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Register Logistics Driver",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = driverName,
                            onValueChange = { driverName = it },
                            label = { Text("Driver Name", fontSize = 12.sp) },
                            placeholder = { Text("e.g. Rahul Sharma", fontSize = 12.sp) },
                            leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_driver_name_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = driverPhone,
                                onValueChange = { driverPhone = it },
                                label = { Text("Phone Number", fontSize = 12.sp) },
                                placeholder = { Text("9876543210", fontSize = 12.sp) },
                                leadingIcon = { Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("register_driver_phone_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )

                            OutlinedTextField(
                                value = driverArea,
                                onValueChange = { driverArea = it },
                                label = { Text("Assigned Route/Area", fontSize = 12.sp) },
                                placeholder = { Text("South Delhi Hub", fontSize = 12.sp) },
                                leadingIcon = { Icon(imageVector = Icons.Default.PinDrop, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .testTag("register_driver_area_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )
                        }

                        driverFormError?.let { err ->
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(err, color = MaterialTheme.colorScheme.error, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (driverName.trim().isEmpty() || driverPhone.trim().isEmpty() || driverArea.trim().isEmpty()) {
                                    driverFormError = "All fields are required to register a driver."
                                    return@Button
                                }
                                val cleanedPhone = driverPhone.filter { it.isDigit() }
                                if (cleanedPhone.length < 10) {
                                    driverFormError = "Please enter a valid 10-digit mobile number."
                                    return@Button
                                }

                                viewModel.addDeliveryStaff(
                                    name = driverName.trim(),
                                    phone = driverPhone.trim(),
                                    assignedArea = driverArea.trim()
                                )

                                Toast.makeText(context, "Registered Driver ${driverName.trim()} Successfully!", Toast.LENGTH_SHORT).show()

                                // Reset
                                driverName = ""
                                driverPhone = ""
                                driverArea = ""
                                driverFormError = null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .testTag("register_driver_submit_button"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Confirm Driver Onboarding", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Quick Fish Order Simulator for testing Driver Assignment
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("simulate_order_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text("Need Orders to Assign?", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text("Generate a fresh pending customer order with no driver assigned yet.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Button(
                                onClick = {
                                    val randomCustomers = listOf("Ananya Roy", "Siddharth Jain", "Sneha Patil", "Dr. Manoj Kumar")
                                    val randomFishes = listOf("Fresh Salmon Fillets", "Rohu Fish Steak", "Sardines (Whole)", "Premium Pomfret")
                                    val randomPrices = listOf(600.0, 400.0, 300.0, 900.0)
                                    val index = (0..3).random()

                                    viewModel.placeSimulatedOrder(
                                        customerName = randomCustomers[index],
                                        fishName = randomFishes[index],
                                        quantity = (1..5).random() * 0.5 + 0.5,
                                        pricePerKg = randomPrices[index],
                                        paymentStatus = listOf("Paid", "Pending", "Cash on Delivery").random(),
                                        staffId = 0 // Represents Unassigned Pending order!
                                    )

                                    Toast.makeText(context, "Generated unassigned order for ${randomCustomers[index]}", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("generate_unassigned_order_button")
                            ) {
                                Text("Generate", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Dashboard Component Section: Pending Orders & Active Routes (requirement 3)
            item {
                Text(
                    text = "Pending Fish Orders",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Pending Orders Table / Cards (requirement 3 - Display in Table layout format)
            val pendingOrders = orders.filter { it.deliveryStaffId == 0 }
            if (pendingOrders.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No pending fish orders. Use the 'Generate' simulator above to inject a pending order!",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Render Table Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Order / Product", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.5f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Customer & Address", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(2f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Priority", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Action", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.2f), color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    }
                }

                items(pendingOrders) { order ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            .clickable { selectedOrderForRouteMap = order }
                            .padding(10.dp)
                            .testTag("pending_order_row_${order.orderId}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1.5f)) {
                            Text(order.orderId, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            Text(order.fishName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${order.quantity} kg • ₹${order.amount}", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E7D32))
                        }

                        Column(modifier = Modifier.weight(2f)) {
                            Text(order.customerName, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            Text(order.deliveryAddress, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 4.dp)
                        ) {
                            val badgeColor = when (order.priority) {
                                "High" -> Color(0xFFFFEBEE)
                                "Medium" -> Color(0xFFFFF3E0)
                                else -> Color(0xFFE8F5E9)
                            }
                            val textColor = when (order.priority) {
                                "High" -> Color(0xFFC62828)
                                "Medium" -> Color(0xFFEF6C00)
                                else -> Color(0xFF2E7D32)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(badgeColor)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(order.priority, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = textColor)
                            }
                        }

                        Button(
                            onClick = {
                                selectedOrderToAssign = order
                                showAssignDialog = true
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(32.dp)
                                .testTag("assign_driver_btn_${order.orderId}")
                        ) {
                            Text("Assign", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Section 2: Active Routes Dashboard Table
            item {
                Text(
                    text = "Active Delivery Routes",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            val activeRoutes = orders.filter { it.deliveryStatus != "Delivered" && it.deliveryStaffId > 0 }
            if (activeRoutes.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(24.dp)
                            .padding(bottom = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No active routes at the moment. Assign a driver to a pending order to start mapping!",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Table Header for Active Routes
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Driver & Area", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.5f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Active Order", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.5f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Status", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.2f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Route Map", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    }
                }

                items(activeRoutes) { route ->
                    val driver = drivers.find { it.id == route.deliveryStaffId }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (selectedOrderForRouteMap?.deliveryId == route.deliveryId) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                                else MaterialTheme.colorScheme.surface
                            )
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            .clickable { selectedOrderForRouteMap = route }
                            .padding(10.dp)
                            .testTag("active_route_row_${route.orderId}"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1.5f)) {
                            Text(driver?.name ?: "Driver #${route.deliveryStaffId}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(driver?.assignedArea ?: "No Hub Assigned", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Column(modifier = Modifier.weight(1.5f)) {
                            Text(route.orderId, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            Text(route.customerName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Box(modifier = Modifier.weight(1.2f)) {
                            val statusColor = when (route.deliveryStatus) {
                                "In Transit" -> Color(0xFF2196F3)
                                "Out for Delivery" -> Color(0xFF9C27B0)
                                else -> MaterialTheme.colorScheme.primary
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(statusColor.copy(alpha = 0.12f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(route.deliveryStatus, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = statusColor)
                            }
                        }

                        IconButton(
                            onClick = { selectedOrderForRouteMap = route },
                            modifier = Modifier
                                .weight(1f)
                                .size(32.dp)
                                .testTag("visualize_route_btn_${route.orderId}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = "Visualize Route",
                                tint = if (selectedOrderForRouteMap?.deliveryId == route.deliveryId) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Assign Driver Dialog Flow (requirement 4)
    if (showAssignDialog && selectedOrderToAssign != null) {
        AlertDialog(
            onDismissRequest = {
                showAssignDialog = false
                selectedDriverIdToAssign = null
                isDriverDropdownExpanded = false
            },
            title = {
                Text(
                    text = "Assign Driver to Route",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Assign order ${selectedOrderToAssign?.orderId} to an onboarded delivery partner:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (drivers.isEmpty()) {
                        Text(
                            text = "⚠️ No registered delivery drivers found. Please onboard drivers using the registration form first.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        // Driver Selection Dropdown Box
                        ExposedDropdownMenuBox(
                            expanded = isDriverDropdownExpanded,
                            onExpandedChange = { isDriverDropdownExpanded = it }
                        ) {
                            val selectedDriver = drivers.find { it.id == selectedDriverIdToAssign }
                            TextField(
                                value = selectedDriver?.name ?: "Select Driver...",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDriverDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                                    .testTag("assign_driver_dropdown"),
                                colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )

                            ExposedDropdownMenu(
                                expanded = isDriverDropdownExpanded,
                                onDismissRequest = { isDriverDropdownExpanded = false }
                            ) {
                                drivers.forEach { driver ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(driver.name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                Text("Area: ${driver.assignedArea}", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                            }
                                        },
                                        onClick = {
                                            selectedDriverIdToAssign = driver.id
                                            isDriverDropdownExpanded = false
                                        },
                                        modifier = Modifier.testTag("dialog_driver_item_${driver.id}")
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val orderId = selectedOrderToAssign?.deliveryId ?: return@Button
                        val driverId = selectedDriverIdToAssign ?: return@Button

                        viewModel.assignDriverToRoute(orderId, driverId)
                        Toast.makeText(context, "Route optimized and assigned successfully!", Toast.LENGTH_SHORT).show()

                        showAssignDialog = false
                        selectedDriverIdToAssign = null
                        selectedOrderToAssign = null
                    },
                    enabled = selectedDriverIdToAssign != null,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("dialog_confirm_assignment_button")
                ) {
                    Text("Confirm Assignment", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAssignDialog = false
                        selectedDriverIdToAssign = null
                        selectedOrderToAssign = null
                    },
                    modifier = Modifier.testTag("dialog_cancel_assignment_button")
                ) {
                    Text("Cancel", fontSize = 12.sp)
                }
            },
            modifier = Modifier.testTag("assignment_alert_dialog")
        )
    }

    // Alerts/Notifications History Bottom Sheet (requirement 1)
    if (showNotificationsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showNotificationsSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.testTag("logistics_notifications_bottom_sheet")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .heightIn(max = 400.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Logistics Notification Logs", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    IconButton(onClick = { showNotificationsSheet = false }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close Sheet")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (notifications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No notifications or route alerts triggered yet.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(notifications) { alert ->
                            NotificationAlertRow(notification = alert)
                        }
                    }
                }
            }
        }
    }
}

// Custom Notification Alert Row component
@Composable
fun NotificationAlertRow(notification: Notification) {
    val dateStr = try {
        val sdf = SimpleDateFormat("HH:mm a", Locale.getDefault())
        sdf.format(Date(notification.timestamp))
    } catch (e: Exception) {
        "Just now"
    }

    val typeColor = when (notification.type) {
        "Route Update" -> Color(0xFF9C27B0)
        "Status Update" -> Color(0xFF2196F3)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(typeColor.copy(alpha = 0.12f))
                    .padding(6.dp)
            ) {
                Icon(
                    imageVector = when (notification.type) {
                        "Route Update" -> Icons.Default.Map
                        "Status Update" -> Icons.Default.LocalShipping
                        else -> Icons.Default.Notifications
                    },
                    contentDescription = null,
                    tint = typeColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = dateStr,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = notification.message,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

// Gorgeous Logistics Route Map Visualizer showing path checkpoints (requirement 2)
@Composable
fun LogisticsRouteMapVisualizer(
    order: DeliveryOrder,
    driver: DeliveryStaff?,
    modifier: Modifier = Modifier
) {
    val progress = when (order.deliveryStatus) {
        "Assigned" -> 0.15f
        "In Transit" -> 0.5f
        "Out for Delivery" -> 0.8f
        "Delivered" -> 1.0f
        else -> 0.15f
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("logistics_route_visualizer_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Navigation, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Optimized Distribution Path",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFE8F5E9))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("SMART PATH OPTIMIZED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // City Grid Path Canvas drawing
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val roadColor = Color.Gray.copy(alpha = 0.08f)

                    // Draw secondary grid lines (roads)
                    for (i in 1..8) {
                        val x = width * (i / 9f)
                        drawLine(color = roadColor, start = Offset(x, 0f), end = Offset(x, height), strokeWidth = 2f)
                    }
                    for (i in 1..4) {
                        val y = height * (i / 5f)
                        drawLine(color = roadColor, start = Offset(0f, y), end = Offset(width, y), strokeWidth = 2f)
                    }

                    // Draw optimized route lines with multiple waypoints
                    // Waypoint coordinates (fractions of width and height)
                    val pStart = Offset(width * 0.12f, height * 0.45f)      // Fresh Fish Hub
                    val pMid1 = Offset(width * 0.38f, height * 0.25f)       // Transit point 1
                    val pMid2 = Offset(width * 0.62f, height * 0.70f)       // Transit point 2
                    val pEnd = Offset(width * 0.88f, height * 0.5f)         // Customer Destination

                    val pathColor = if (order.deliveryStatus == "Delivered") Color(0xFF2E7D32) else Color(0xFF2196F3)

                    // Draw static full line in background
                    drawLine(color = pathColor.copy(alpha = 0.12f), start = pStart, end = pMid1, strokeWidth = 8f, cap = StrokeCap.Round)
                    drawLine(color = pathColor.copy(alpha = 0.12f), start = pMid1, end = pMid2, strokeWidth = 8f, cap = StrokeCap.Round)
                    drawLine(color = pathColor.copy(alpha = 0.12f), start = pMid2, end = pEnd, strokeWidth = 8f, cap = StrokeCap.Round)

                    // Draw dynamic live active route path depending on progress
                    if (progress > 0f) {
                        if (progress <= 0.33f) {
                            val ratio = progress / 0.33f
                            val currentPos = Offset(pStart.x + (pMid1.x - pStart.x) * ratio, pStart.y + (pMid1.y - pStart.y) * ratio)
                            drawLine(color = pathColor, start = pStart, end = currentPos, strokeWidth = 8f, cap = StrokeCap.Round)
                        } else if (progress <= 0.66f) {
                            val ratio = (progress - 0.33f) / 0.33f
                            val currentPos = Offset(pMid1.x + (pMid2.x - pMid1.x) * ratio, pMid1.y + (pMid2.y - pMid1.y) * ratio)
                            drawLine(color = pathColor, start = pStart, end = pMid1, strokeWidth = 8f, cap = StrokeCap.Round)
                            drawLine(color = pathColor, start = pMid1, end = currentPos, strokeWidth = 8f, cap = StrokeCap.Round)
                        } else {
                            val ratio = (progress - 0.66f) / 0.34f
                            val currentPos = Offset(pMid2.x + (pEnd.x - pMid2.x) * ratio, pMid2.y + (pEnd.y - pMid2.y) * ratio)
                            drawLine(color = pathColor, start = pStart, end = pMid1, strokeWidth = 8f, cap = StrokeCap.Round)
                            drawLine(color = pathColor, start = pMid1, end = pMid2, strokeWidth = 8f, cap = StrokeCap.Round)
                            drawLine(color = pathColor, start = pMid2, end = currentPos, strokeWidth = 8f, cap = StrokeCap.Round)
                        }
                    }
                }

                // Node 1: Start Hub
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(10.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Hub", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Node 2: Checkpoint 1
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp, end = 60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("Route-Pt A", fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Node 3: Checkpoint 2
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp, start = 60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("Route-Pt B", fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Node 4: Destination
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (order.deliveryStatus == "Delivered") Color(0xFFE8F5E9) else MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (order.deliveryStatus == "Delivered") Icons.Default.CheckCircle else Icons.Default.Home,
                            contentDescription = null,
                            tint = if (order.deliveryStatus == "Delivered") Color(0xFF2E7D32) else MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (order.deliveryStatus == "Delivered") "Arrived" else "Client",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (order.deliveryStatus == "Delivered") Color(0xFF2E7D32) else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Details on selected active map path
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Selected: ${order.orderId} (${order.fishName})", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text("Driver: ${driver?.name ?: "Driver Not Assigned"}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Address: ${order.deliveryAddress}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = when (order.deliveryStatus) {
                            "Assigned" -> "Order at Hub"
                            "In Transit" -> "In Transit"
                            "Out for Delivery" -> "Out for Delivery"
                            "Delivered" -> "Delivered"
                            else -> "In Transit"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (order.deliveryStatus) {
                            "Delivered" -> Color(0xFF2E7D32)
                            "Out for Delivery" -> Color(0xFF9C27B0)
                            "In Transit" -> Color(0xFF2196F3)
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
                    Text(
                        text = when (order.deliveryStatus) {
                            "Assigned" -> "~4.8 km to Client"
                            "In Transit" -> "~3.1 km remaining"
                            "Out for Delivery" -> "~0.7 km remaining"
                            "Delivered" -> "0.0 km (Successful)"
                            else -> "Transit normal"
                        },
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
