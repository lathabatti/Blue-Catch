package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.FishViewModel
import com.example.ui.OwnerSession
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: FishViewModel,
    onNavigateToSection: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val stockList by viewModel.stock.collectAsState()
    val salesList by viewModel.sales.collectAsState()
    val purchaseList by viewModel.purchases.collectAsState()
    val supplierList by viewModel.suppliers.collectAsState()
    val customerList by viewModel.customers.collectAsState()
    val alertList by viewModel.alerts.collectAsState()
    val expenseList by viewModel.expenses.collectAsState()

    // Aggregate statistics
    val totalStock = stockList.sumOf { it.currentStock }
    val pendingSupplierDues = supplierList.sumOf { it.pendingDues }
    val pendingCustomerDues = customerList.sumOf { it.pendingAmounts }

    // Today's boundaries
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val startOfToday = calendar.timeInMillis

    val todaySales = salesList.filter { it.saleDate >= startOfToday }.sumOf { it.totalAmount }
    val todayPurchases = purchaseList.filter { it.purchaseDate >= startOfToday }.sumOf { it.totalAmount }
    val todayExpenses = expenseList.filter { it.date >= startOfToday }.sumOf { it.amount }

    val todayProfit = todaySales - (todayPurchases + todayExpenses)

    val lowStockItems = stockList.filter { it.currentStock <= 10.0 }
    val unreadAlertsCount = alertList.filter { !it.isRead }.size

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(OceanDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and Time Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = OceanSurface),
                shape = RoundedCornerShape(16.dp),
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
                            text = "Welcome, ${currentUser?.name ?: "Owner"}",
                            color = TealNeon,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Blue Catch ERP • ${(currentUser as? OwnerSession)?.email ?: currentUser?.phone ?: "sanjusmily128@gmail.com"}",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    val df = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
                    Card(
                        colors = CardDefaults.cardColors(containerColor = OceanCard),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = df.format(Date()),
                            color = TextWhite,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Key Business Performance Metrics Grid
        item {
            Text(
                text = "Business Metrics",
                color = TextWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricCard(
                        title = "Today Sales",
                        value = "₹${todaySales.toInt()}",
                        icon = Icons.Default.TrendingUp,
                        iconColor = TealNeon,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Today Buy",
                        value = "₹${todayPurchases.toInt()}",
                        icon = Icons.Default.ShoppingCart,
                        iconColor = BlueDeep,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val profitColor = if (todayProfit >= 0) TealNeon else CoralRed
                    val profitSign = if (todayProfit >= 0) "+" else ""
                    MetricCard(
                        title = "Today Profit",
                        value = "$profitSign₹${todayProfit.toInt()}",
                        icon = if (todayProfit >= 0) Icons.Default.AddCircle else Icons.Default.RemoveCircle,
                        iconColor = profitColor,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Total Stock",
                        value = "${String.format("%.1f", totalStock)} kg",
                        icon = Icons.Default.Inventory,
                        iconColor = OrangeAlert,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricCard(
                        title = "Cust. Pending",
                        value = "₹${pendingCustomerDues.toInt()}",
                        icon = Icons.Default.People,
                        iconColor = TealNeon,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Supplier Due",
                        value = "₹${pendingSupplierDues.toInt()}",
                        icon = Icons.Default.Business,
                        iconColor = OrangeAlert,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Native canvas weekly sales/profit Trend line graph
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = OceanSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Weekly Analytics Trend",
                        color = TextWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Visualizing daily revenue flow vs profit trends",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                    ) {
                        val width = size.width
                        val height = size.height

                        // Draw Grid Lines
                        val gridLines = 3
                        for (i in 1..gridLines) {
                            val y = height * i / (gridLines + 1)
                            drawLine(
                                color = OceanCard,
                                start = Offset(0f, y),
                                end = Offset(width, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        // Simulated points for 7 days
                        // Sales points (higher) and Profit points (lower)
                        val salesPoints = listOf(0.2f, 0.4f, 0.3f, 0.7f, 0.5f, 0.8f, 0.9f)
                        val profitPoints = listOf(0.1f, 0.2f, 0.15f, 0.4f, 0.3f, 0.5f, 0.6f)

                        val pointCount = salesPoints.size
                        val stepX = width / (pointCount - 1)

                        // Draw Sales Line (TealNeon)
                        for (i in 0 until pointCount - 1) {
                            val startX = i * stepX
                            val startY = height - (salesPoints[i] * height * 0.8f) - 10f
                            val endX = (i + 1) * stepX
                            val endY = height - (salesPoints[i + 1] * height * 0.8f) - 10f

                            drawLine(
                                color = TealNeon,
                                start = Offset(startX, startY),
                                end = Offset(endX, endY),
                                strokeWidth = 3.dp.toPx()
                            )
                            drawCircle(
                                color = TealNeon,
                                radius = 4.dp.toPx(),
                                center = Offset(startX, startY)
                            )
                        }
                        drawCircle(
                            color = TealNeon,
                            radius = 4.dp.toPx(),
                            center = Offset((pointCount - 1) * stepX, height - (salesPoints.last() * height * 0.8f) - 10f)
                        )

                        // Draw Profit Line (BlueDeep)
                        for (i in 0 until pointCount - 1) {
                            val startX = i * stepX
                            val startY = height - (profitPoints[i] * height * 0.8f) - 10f
                            val endX = (i + 1) * stepX
                            val endY = height - (profitPoints[i + 1] * height * 0.8f) - 10f

                            drawLine(
                                color = BlueDeep,
                                start = Offset(startX, startY),
                                end = Offset(endX, endY),
                                strokeWidth = 2.dp.toPx(),
                                pathEffect = null
                            )
                            drawCircle(
                                color = BlueDeep,
                                radius = 3.dp.toPx(),
                                center = Offset(startX, startY)
                            )
                        }
                        drawCircle(
                            color = BlueDeep,
                            radius = 3.dp.toPx(),
                            center = Offset((pointCount - 1) * stepX, height - (profitPoints.last() * height * 0.8f) - 10f)
                        )
                    }

                    // Legend
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(TealNeon, RoundedCornerShape(2.dp))
                            )
                            Text(
                                text = " Sales",
                                color = TextWhite,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(end = 16.dp)
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(BlueDeep, RoundedCornerShape(2.dp))
                            )
                            Text(
                                text = " Net Profit",
                                color = TextWhite,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Active low stock warnings
        if (lowStockItems.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Low Stock Alerts",
                        color = TextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { onNavigateToSection("/owner/stock") }) {
                        Text("View Stock", color = TealNeon, fontSize = 13.sp)
                    }
                }
            }

            items(lowStockItems.take(3)) { stockItem ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = OceanSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("low_stock_card_${stockItem.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Warning",
                                tint = OrangeAlert,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = stockItem.fishName,
                                    color = TextWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Box: ${stockItem.storageBox} • ${stockItem.freshness}",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${stockItem.currentStock} kg",
                                color = CoralRed,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Restock now",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Pending dues reminder quick actions
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = OceanSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Quick Control Shortcuts",
                        color = TextWhite,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        QuickActionBtn(
                            title = "Wholesale Buy",
                            icon = Icons.Default.AddShoppingCart,
                            color = BlueDeep,
                            onClick = { onNavigateToSection("/owner/purchase") }
                        )
                        QuickActionBtn(
                            title = "Create Bill",
                            icon = Icons.Default.PostAdd,
                            color = TealNeon,
                            onClick = { onNavigateToSection("/owner/sales") }
                        )
                        QuickActionBtn(
                            title = "Deliveries",
                            icon = Icons.Default.LocalShipping,
                            color = OrangeAlert,
                            onClick = { onNavigateToSection("/owner/delivery") }
                        )
                        QuickActionBtn(
                            title = "Biz Reports",
                            icon = Icons.Default.Assessment,
                            color = Purple80,
                            onClick = { onNavigateToSection("/owner/reports") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = OceanSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = TextWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun QuickActionBtn(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = OceanCard),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.size(48.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            color = TextWhite,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
