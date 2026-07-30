package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import coil.compose.AsyncImage
import com.example.data.Stock
import com.example.ui.FishViewModel
import com.example.ui.theme.*

@Composable
fun StockScreen(
    viewModel: FishViewModel,
    modifier: Modifier = Modifier
) {
    val stockList by viewModel.stock.collectAsState()
    var showAddStockDialog by remember { mutableStateOf(false) }

    // Search query
    var searchQuery by remember { mutableStateOf("") }

    // Dialog state variables
    var shopName by remember { mutableStateOf("Main Harbor Branch") }
    var fishName by remember { mutableStateOf("") }
    var currentStockText by remember { mutableStateOf("") }
    var sellingPriceText by remember { mutableStateOf("") }
    var storageBox by remember { mutableStateOf("Box A1") }
    var freshness by remember { mutableStateOf("Fresh") }
    var productCategory by remember { mutableStateOf("Fresh") }
    var offers by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("2026-07-08") }
    var selectedPhotoName by remember { mutableStateOf("img_fish_generic") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                selectedPhotoName = uri.toString()
            }
        }
    )

    val filteredStock = stockList.filter {
        it.fishName.contains(searchQuery, ignoreCase = true) ||
                it.storageBox.contains(searchQuery, ignoreCase = true) ||
                it.shopName.contains(searchQuery, ignoreCase = true)
    }

    // Summary calculations
    val freshKg = remember(stockList) { stockList.filter { it.category.equals("Fresh", ignoreCase = true) }.sumOf { it.currentStock } }
    val frozenKg = remember(stockList) { stockList.filter { it.category.equals("Frozen", ignoreCase = true) }.sumOf { it.currentStock } }
    val processedKg = remember(stockList) { stockList.filter { it.category.equals("Processed", ignoreCase = true) }.sumOf { it.currentStock } }
    val alertCount = remember(stockList) { stockList.filter { it.currentStock <= 10.0 }.size }

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
            // Header card with statistics
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
                                text = "Inventory Dashboard",
                                color = TealNeon,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Real-time stock levels by product categories",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        IconButton(
                            onClick = { showAddStockDialog = true },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = TealNeon, contentColor = OceanDark)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Stock")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Responsive metric summaries
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Fresh Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = OceanDark),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Fresh", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("${freshKg.toInt()} kg", color = TealNeon, fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                        }
                        // Frozen Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = OceanDark),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Frozen", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("${frozenKg.toInt()} kg", color = LightIceBlue, fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                        }
                        // Processed Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = OceanDark),
                            modifier = Modifier.weight(1.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Processed", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("${processedKg.toInt()} kg", color = OrangeAlert, fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                        }
                        // Alerts Card
                        Card(
                            colors = CardDefaults.cardColors(containerColor = if (alertCount > 0) CoralRed.copy(alpha = 0.25f) else OceanDark),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Low Stock", color = if (alertCount > 0) CoralRed else TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("$alertCount items", color = if (alertCount > 0) CoralRed else TextWhite, fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Live Filter Search field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search by Breed, Box or Shop Branch...", color = TextSecondary) },
                        textStyle = LocalTextStyle.current.copy(color = TextWhite),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = TealNeon,
                            unfocusedBorderColor = OceanCard
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("stock_search_input")
                    )
                }
            }

            // Low Stock Warning Banner
            if (alertCount > 0) {
                val lowStockFishList = stockList.filter { it.currentStock <= 10.0 }.map { it.fishName }.distinct()
                Card(
                    colors = CardDefaults.cardColors(containerColor = CoralRed.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = "Warning", tint = CoralRed, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Low Stock Warning: ${lowStockFishList.joinToString(", ")} levels are below critical threshold (10 kg)!",
                            color = TextWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Inventory items listing
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (filteredStock.isEmpty()) {
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
                                    Icon(Icons.Default.Inbox, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("No matching fish stock found.", color = TextSecondary, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                } else {
                    items(filteredStock) { item ->
                        StockCard(
                            item = item,
                            viewModel = viewModel,
                            onDelete = { viewModel.deleteStock(item) }
                        )
                    }
                }
            }
        }

        // Add Stock Dialog Form
        if (showAddStockDialog) {
            AlertDialog(
                onDismissRequest = { 
                    selectedPhotoName = "img_fish_generic"
                    showAddStockDialog = false 
                },
                title = { Text("Add New Fish Stock", color = TealNeon, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                text = {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            Text("Select Fish Photo", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 1. Custom Upload Card
                                item {
                                    val isCustomSelected = selectedPhotoName.startsWith("content://") || selectedPhotoName.startsWith("file://")
                                    Card(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clickable {
                                                photoPickerLauncher.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            },
                                        shape = RoundedCornerShape(12.dp),
                                        border = if (isCustomSelected) androidx.compose.foundation.BorderStroke(2.dp, TealNeon) else null,
                                        colors = CardDefaults.cardColors(containerColor = OceanCard)
                                    ) {
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            if (isCustomSelected) {
                                                AsyncImage(
                                                    model = selectedPhotoName,
                                                    contentDescription = "Uploaded Photo",
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .align(Alignment.BottomCenter)
                                                        .background(Color.Black.copy(alpha = 0.6f))
                                                        .padding(vertical = 2.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text("Uploaded", color = TextWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .size(18.dp)
                                                        .background(TealNeon, shape = RoundedCornerShape(bottomEnd = 8.dp))
                                                        .align(Alignment.TopStart),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = OceanDark,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                            } else {
                                                Column(
                                                    modifier = Modifier.fillMaxSize(),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.AddAPhoto,
                                                        contentDescription = "Upload Custom Photo",
                                                        tint = TealNeon,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "Upload",
                                                        color = TextWhite,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                val photos = listOf(
                                    Pair("img_fish_salmon", "Salmon"),
                                    Pair("img_fish_tuna", "Tuna"),
                                    Pair("img_fish_shrimp", "Shrimp"),
                                    Pair("img_fish_pomfret", "Pomfret"),
                                    Pair("img_fish_generic", "General")
                                )
                                items(photos) { (photoKey, label) ->
                                    val isSelected = selectedPhotoName == photoKey
                                    val context = androidx.compose.ui.platform.LocalContext.current
                                    val imageResId = remember(photoKey) {
                                        context.resources.getIdentifier(photoKey, "drawable", context.packageName)
                                    }
                                    Card(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clickable { 
                                                selectedPhotoName = photoKey 
                                                if (fishName.isBlank()) {
                                                    fishName = if (label == "General") "" else label
                                                }
                                            },
                                        shape = RoundedCornerShape(12.dp),
                                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, TealNeon) else null,
                                        colors = CardDefaults.cardColors(containerColor = OceanCard)
                                    ) {
                                        Box(modifier = Modifier.fillMaxSize()) {
                                            if (imageResId != 0) {
                                                Image(
                                                    painter = painterResource(id = imageResId),
                                                    contentDescription = label,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Default.Layers, contentDescription = null, tint = TealNeon)
                                                }
                                            }
                                            // Semi-transparent label background at the bottom
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .align(Alignment.BottomCenter)
                                                    .background(Color.Black.copy(alpha = 0.6f))
                                                    .padding(vertical = 2.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(label, color = TextWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }

                                            if (isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(18.dp)
                                                        .background(TealNeon, shape = RoundedCornerShape(bottomEnd = 8.dp))
                                                        .align(Alignment.TopStart),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = OceanDark,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        item {
                            OutlinedTextField(
                                value = shopName,
                                onValueChange = { shopName = it },
                                label = { Text("Shop / Outlet Name") },
                                textStyle = LocalTextStyle.current.copy(color = TextWhite),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = fishName,
                                onValueChange = { fishName = it },
                                label = { Text("Fish breed / Name") },
                                textStyle = LocalTextStyle.current.copy(color = TextWhite),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("add_stock_fish_name")
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = currentStockText,
                                onValueChange = { currentStockText = it },
                                label = { Text("Current Stock (kg)") },
                                textStyle = LocalTextStyle.current.copy(color = TextWhite),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = sellingPriceText,
                                onValueChange = { sellingPriceText = it },
                                label = { Text("Selling Price (₹ per kg)") },
                                textStyle = LocalTextStyle.current.copy(color = TextWhite),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = storageBox,
                                onValueChange = { storageBox = it },
                                label = { Text("Storage Box No.") },
                                textStyle = LocalTextStyle.current.copy(color = TextWhite),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            Text("Freshness Quality", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Fresh", "Medium", "Old").forEach { q ->
                                    ElevatedFilterChip(
                                        selected = freshness == q,
                                        onClick = { freshness = q },
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
                            Text("Product Category", color = TextWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Fresh", "Frozen", "Processed").forEach { cat ->
                                    ElevatedFilterChip(
                                        selected = productCategory == cat,
                                        onClick = { productCategory = cat },
                                        label = { Text(cat) },
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
                            OutlinedTextField(
                                value = offers,
                                onValueChange = { offers = it },
                                label = { Text("Offers / Discounts (e.g. 5% off)") },
                                textStyle = LocalTextStyle.current.copy(color = TextWhite),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = expiryDate,
                                onValueChange = { expiryDate = it },
                                label = { Text("Expiry / Keep Till Date") },
                                textStyle = LocalTextStyle.current.copy(color = TextWhite),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val stockQty = currentStockText.toDoubleOrNull() ?: 0.0
                            val priceVal = sellingPriceText.toDoubleOrNull() ?: 0.0
                            if (fishName.isNotBlank() && stockQty > 0.0 && priceVal > 0.0) {
                                viewModel.addStockManual(
                                    shopName = shopName,
                                    fishName = fishName,
                                    currentStock = stockQty,
                                    sellingPrice = priceVal,
                                    storageBox = storageBox,
                                    freshness = freshness,
                                    offers = offers,
                                    expiryDate = expiryDate,
                                    category = productCategory,
                                    fishPhoto = selectedPhotoName
                                )
                                fishName = ""
                                currentStockText = ""
                                sellingPriceText = ""
                                productCategory = "Fresh"
                                freshness = "Fresh"
                                selectedPhotoName = "img_fish_generic"
                                showAddStockDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealNeon, contentColor = OceanDark)
                    ) {
                        Text("Add Stock Item", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        selectedPhotoName = "img_fish_generic"
                        showAddStockDialog = false 
                    }) {
                        Text("Cancel", color = CoralRed)
                    }
                },
                containerColor = OceanSurface
            )
        }
    }
}

@Composable
fun StockCard(
    item: Stock,
    viewModel: FishViewModel,
    onDelete: () -> Unit
) {
    var showBroadcastDialog by remember { mutableStateOf(false) }
    var promoMessage by remember { mutableStateOf("Fresh ${item.fishName} has just arrived! Sourced directly at ₹${item.sellingPrice.toInt()}/kg. Available in ${item.storageBox}. Hurry up to pre-order!") }

    Card(
        colors = CardDefaults.cardColors(containerColor = OceanSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // First Row: Fish Name & Box Number
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
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
                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.fishName,
                                color = TextWhite,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = when (item.freshness) {
                                        "Fresh" -> TealNeon.copy(alpha = 0.15f)
                                        "Medium" -> OrangeAlert.copy(alpha = 0.15f)
                                        else -> CoralRed.copy(alpha = 0.15f)
                                    }
                                ),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = item.freshness,
                                    color = when (item.freshness) {
                                        "Fresh" -> TealNeon
                                        "Medium" -> OrangeAlert
                                        else -> CoralRed
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = LightIceBlue.copy(alpha = 0.15f)),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = item.category,
                                    color = LightIceBlue,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            if (item.currentStock <= 10.0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = CoralRed.copy(alpha = 0.2f)),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "Low Stock!",
                                        color = CoralRed,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${item.shopName} • Box No: ${item.storageBox}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showBroadcastDialog = true }
                    ) {
                        Icon(Icons.Default.Campaign, contentDescription = "Broadcast Promo", tint = TealNeon)
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_stock_btn_${item.id}")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CoralRed.copy(alpha = 0.7f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stock Details Progress or Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("CURRENT STOCK", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("${item.currentStock} kg", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Black)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("SELLING PRICE", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("₹${item.sellingPrice.toInt()} / kg", color = TealNeon, fontSize = 16.sp, fontWeight = FontWeight.Black)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Cleaning & Processing details
            Card(
                colors = CardDefaults.cardColors(containerColor = OceanCard),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Net Clean Weight: ${String.format("%.1f", item.weightAfterCleaning)}kg",
                        color = LightIceBlue,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Weight Loss: ${String.format("%.1f", item.weightLoss)}kg (10%)",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            if (item.offers.isNotEmpty() || item.expiryDate.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.offers.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalOffer, contentDescription = null, tint = OrangeAlert, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(item.offers, color = OrangeAlert, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    if (item.expiryDate.isNotEmpty()) {
                        Text("Keep till: ${item.expiryDate}", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }
        }
    }

    // Broadcast Dialog Form
    if (showBroadcastDialog) {
        AlertDialog(
            onDismissRequest = { showBroadcastDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Campaign, contentDescription = null, tint = TealNeon)
                    Text("Broadcast Promo Notification", color = TealNeon, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("This promo bulletin will instantly appear on all registered customer dashboards.", color = TextWhite, fontSize = 11.sp)
                    OutlinedTextField(
                        value = promoMessage,
                        onValueChange = { promoMessage = it },
                        label = { Text("Promo Bulletin Message") },
                        textStyle = LocalTextStyle.current.copy(color = TextWhite),
                        singleLine = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (promoMessage.isNotBlank()) {
                            viewModel.broadcastStockAlert(item.fishName, promoMessage)
                            showBroadcastDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TealNeon, contentColor = OceanDark)
                ) {
                    Text("Broadcast Now", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBroadcastDialog = false }) {
                    Text("Cancel", color = CoralRed)
                }
            },
            containerColor = OceanSurface
        )
    }
}
