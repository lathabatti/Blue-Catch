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
import com.example.data.MarketPrice
import com.example.ui.FishViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    viewModel: FishViewModel,
    modifier: Modifier = Modifier
) {
    val marketPrices by viewModel.marketPrices.collectAsState()
    var selectedCategoryTab by remember { mutableStateOf("Sea Water") }
    val categories = listOf("Sea Water", "Fresh Water", "Shellfish")

    // State for rate updater
    var showUpdateRateDialog by remember { mutableStateOf(false) }
    var breedName by remember { mutableStateOf("") }
    var updateCategory by remember { mutableStateOf("Sea Water") }
    var ourPriceText by remember { mutableStateOf("") }
    var marketRateText by remember { mutableStateOf("") }

    // State for Deal Simulator
    var simulatorBreed by remember { mutableStateOf("") }
    var simulatorQtyText by remember { mutableStateOf("") }
    var simulatorPriceText by remember { mutableStateOf("") }
    var showSimResults by remember { mutableStateOf(false) }

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
            // Live Harbor Intelligence Banner
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
                            Column {
                                Text(
                                    text = "B2B Fish Marketplace",
                                    color = TealNeon,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Live harbor auction rates & procurement intelligence",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = TealNeon,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Live Port Feeds
                        Text(
                            text = "PORT FEED TICKER (LIVE AUCTIONS)",
                            color = TealNeon.copy(alpha = 0.8f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            PortTickerChip("Sasson Dock", "Active", TealNeon, Modifier.weight(1f))
                            PortTickerChip("Veraval", "Docked", BlueDeep, Modifier.weight(1f))
                            PortTickerChip("Mangalore", "Open", TealNeon, Modifier.weight(1f))
                        }
                    }
                }
            }

            // Marketplace Navigation Categories
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Daily Market Price Index",
                            color = TextWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = {
                                breedName = ""
                                ourPriceText = ""
                                marketRateText = ""
                                updateCategory = selectedCategoryTab
                                showUpdateRateDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OceanCard, contentColor = TealNeon),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Update Rate", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    ScrollableTabRow(
                        selectedTabIndex = categories.indexOf(selectedCategoryTab),
                        containerColor = OceanSurface,
                        contentColor = TealNeon,
                        edgePadding = 0.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        categories.forEach { cat ->
                            Tab(
                                selected = selectedCategoryTab == cat,
                                onClick = { selectedCategoryTab = cat },
                                text = { Text(cat, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                            )
                        }
                    }
                }
            }

            // Price Index Items matching selected category
            val filteredPrices = marketPrices.filter { it.category == selectedCategoryTab }
            
            if (filteredPrices.isEmpty()) {
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
                                Icon(Icons.Default.LocalMall, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No price index items found in this category.", color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            } else {
                items(filteredPrices) { price ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = OceanSurface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = when (price.category) {
                                            "Sea Water" -> Icons.Default.DirectionsBoat
                                            "Fresh Water" -> Icons.Default.Water
                                            else -> Icons.Default.FilterList
                                        },
                                        contentDescription = null,
                                        tint = TealNeon,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = price.fishName,
                                        color = TextWhite,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = OceanCard),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = price.category,
                                        color = BlueDeep,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Wholesale Harbor Rate", color = TextSecondary, fontSize = 11.sp)
                                    Text("₹${price.marketRate.toInt()}/kg", color = TextWhite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Our Selling Target", color = TextSecondary, fontSize = 11.sp)
                                    Text("₹${price.currentPrice.toInt()}/kg", color = TealNeon, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Margin estimate index bar
                            val profitSpread = price.currentPrice - price.marketRate
                            val isProfitable = profitSpread > 0
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(OceanDark, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Aesthetic Arbitrage Spread", color = TextSecondary, fontSize = 10.sp)
                                Text(
                                    text = if (isProfitable) "Profit Spread: +₹${profitSpread.toInt()}/kg" else "Overpriced: -₹${(-profitSpread).toInt()}/kg",
                                    color = if (isProfitable) TealNeon else CoralRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Interactive Procurement Deal Simulator
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = OceanSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Calculate, contentDescription = null, tint = TealNeon, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Arbitrage Procurement Simulator",
                                color = TextWhite,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Model yield & estimate net profit spread before completing real purchases",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = simulatorBreed,
                            onValueChange = { simulatorBreed = it },
                            label = { Text("Breed / Variety") },
                            textStyle = LocalTextStyle.current.copy(color = TextWhite),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = simulatorQtyText,
                                onValueChange = { simulatorQtyText = it },
                                label = { Text("Weight (kg)") },
                                textStyle = LocalTextStyle.current.copy(color = TextWhite),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = simulatorPriceText,
                                onValueChange = { simulatorPriceText = it },
                                label = { Text("Wholesaler Quote (₹/kg)") },
                                textStyle = LocalTextStyle.current.copy(color = TextWhite),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1.2f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { showSimResults = true },
                            colors = ButtonDefaults.buttonColors(containerColor = TealNeon, contentColor = OceanDark),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Calculate Projected Arbitrage Yield", fontWeight = FontWeight.Bold)
                        }

                        AnimatedVisibility(visible = showSimResults) {
                            val qty = simulatorQtyText.toDoubleOrNull() ?: 0.0
                            val price = simulatorPriceText.toDoubleOrNull() ?: 0.0
                            val estCost = qty * price
                            val cleanYield = qty * 0.9
                            val wastage = qty * 0.1
                            val targetRetail = price * 1.3
                            val projectedRev = cleanYield * targetRetail
                            val netProfit = projectedRev - estCost

                            Column(
                                modifier = Modifier
                                    .padding(top = 12.dp)
                                    .background(OceanDark, RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "SIMULATION SUMMARY: ${simulatorBreed.ifEmpty { "Generic" }}",
                                    color = TealNeon,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Divider(color = OceanCard)
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Estimated Sourcing Cost", color = TextSecondary, fontSize = 11.sp)
                                    Text("₹${estCost.toInt()}", color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Expected Net Cleaned Yield (90%)", color = TextSecondary, fontSize = 11.sp)
                                    Text("${String.format("%.1f", cleanYield)} kg", color = TextWhite, fontSize = 11.sp)
                                }
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Projected Cleaning Wastage Loss (10%)", color = TextSecondary, fontSize = 11.sp)
                                    Text("${String.format("%.1f", wastage)} kg", color = CoralRed, fontSize = 11.sp)
                                }
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Optimal Selling Price (30% Markup)", color = TextSecondary, fontSize = 11.sp)
                                    Text("₹${targetRetail.toInt()}/kg", color = TealNeon, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Divider(color = OceanCard)
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Expected Total Revenues", color = TextSecondary, fontSize = 12.sp)
                                    Text("₹${projectedRev.toInt()}", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Estimated Net Profit", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("₹${netProfit.toInt()}", color = TealNeon, fontSize = 13.sp, fontWeight = FontWeight.Black)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Live Market Update Dialog Form
        if (showUpdateRateDialog) {
            AlertDialog(
                onDismissRequest = { showUpdateRateDialog = false },
                title = { Text("Update Marketplace Benchmarks", color = TealNeon, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = breedName,
                            onValueChange = { breedName = it },
                            label = { Text("Fish Breed / Variety") },
                            textStyle = LocalTextStyle.current.copy(color = TextWhite),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Water Category", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            categories.forEach { cat ->
                                ElevatedButton(
                                    onClick = { updateCategory = cat },
                                    colors = ButtonDefaults.elevatedButtonColors(
                                        containerColor = if (updateCategory == cat) TealNeon else OceanCard,
                                        contentColor = if (updateCategory == cat) OceanDark else TextWhite
                                    ),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(cat, fontSize = 10.sp)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = marketRateText,
                            onValueChange = { marketRateText = it },
                            label = { Text("Harbor Quote (₹/kg)") },
                            textStyle = LocalTextStyle.current.copy(color = TextWhite),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = ourPriceText,
                            onValueChange = { ourPriceText = it },
                            label = { Text("Our Selling Target (₹/kg)") },
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
                            val marketQuote = marketRateText.toDoubleOrNull() ?: 0.0
                            val retailQuote = ourPriceText.toDoubleOrNull() ?: 0.0
                            if (breedName.isNotBlank() && marketQuote > 0 && retailQuote > 0) {
                                viewModel.addMarketPrice(
                                    fishName = breedName,
                                    category = updateCategory,
                                    currentPrice = retailQuote,
                                    marketRate = marketQuote
                                )
                                showUpdateRateDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealNeon, contentColor = OceanDark)
                    ) {
                        Text("Publish Rate", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showUpdateRateDialog = false }) {
                        Text("Cancel", color = CoralRed)
                    }
                },
                containerColor = OceanSurface
            )
        }
    }
}

@Composable
fun PortTickerChip(
    portName: String,
    status: String,
    statusColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = OceanCard),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(portName, color = TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(statusColor, RoundedCornerShape(3.dp))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(status, color = statusColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
