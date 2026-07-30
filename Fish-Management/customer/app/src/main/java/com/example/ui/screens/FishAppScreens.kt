package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.zIndex
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.R
import com.example.data.CartItem
import com.example.data.FishProduct
import com.example.data.Order
import com.example.data.UserSession
import com.example.ui.FishViewModel
import com.example.ui.SignUpStep
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Simple custom helper to format dates nicely
fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Composable
fun CustomTopAppBar(
    title: String,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(56.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (navigationIcon != null) {
                navigationIcon()
            } else {
                Spacer(modifier = Modifier.width(12.dp))
            }
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            if (actions != null) {
                actions()
            }
        }
    }
}

@Composable
fun FishAppNavigation(
    viewModel: FishViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {
        composable("home") {
            HomeScreen(viewModel = viewModel, navController = navController)
        }
        composable("wishlist") {
            WishlistScreen(viewModel = viewModel, navController = navController)
        }
        composable(
            route = "detail/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.IntType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId") ?: 0
            DetailScreen(productId = productId, viewModel = viewModel, navController = navController)
        }
        composable("cart") {
            CartScreen(viewModel = viewModel, navController = navController)
        }
        composable(
            route = "login/{redirectTarget}",
            arguments = listOf(navArgument("redirectTarget") { type = NavType.StringType })
        ) { backStackEntry ->
            val redirectTarget = backStackEntry.arguments?.getString("redirectTarget") ?: "home"
            LoginRegisterScreen(
                viewModel = viewModel,
                navController = navController,
                redirectTarget = redirectTarget
            )
        }
        composable("checkout") {
            CheckoutScreen(viewModel = viewModel, navController = navController)
        }
        composable("orders") {
            OrdersHistoryScreen(viewModel = viewModel, navController = navController)
        }
        composable(
            route = "track/{orderId}",
            arguments = listOf(navArgument("orderId") { type = NavType.IntType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getInt("orderId") ?: 0
            OrderTrackingScreen(orderId = orderId, viewModel = viewModel, navController = navController)
        }
    }
}

// --- COMMON COMPOSABLE: Bottom Navigation Bar ---
@Composable
fun FishBottomNavigation(
    navController: NavController,
    currentRoute: String?
) {
    NavigationBar(
        tonalElevation = 8.dp,
        windowInsets = WindowInsets.navigationBars,
        modifier = Modifier.testTag("bottom_nav_bar")
    ) {
        val items = listOf(
            Triple("home", Icons.Default.Home, "Home"),
            Triple("wishlist", Icons.Default.Favorite, "Wishlist"),
            Triple("cart", Icons.Default.ShoppingCart, "Cart"),
            Triple("orders", Icons.Default.History, "My Orders")
        )

        items.forEach { (route, icon, label) ->
            val isSelected = currentRoute == route
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (currentRoute != route) {
                        navController.navigate(route) {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = { Icon(imageVector = icon, contentDescription = label) },
                label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

// ==========================================
// 1. HOME SCREEN
// ==========================================
@Composable
fun HomeScreen(
    viewModel: FishViewModel,
    navController: NavController
) {
    val products by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val cart by viewModel.cartItems.collectAsStateWithLifecycle()
    val session by viewModel.userSession.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    var isGridView by remember { mutableStateOf(true) }

    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    var isSearchFocused by remember { mutableStateOf(false) }
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()
    val wishlistProductIds by viewModel.wishlistProductIds.collectAsStateWithLifecycle()

    val totalCartItems = cart.sumOf { it.quantityKg }

    Scaffold(
        bottomBar = {
            FishBottomNavigation(navController = navController, currentRoute = "home")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "OceanFresh Fish Co.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Kochi Harbor Market",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                }

                // Profile Badge or Login button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (session?.isLoggedIn == true) {
                        IconButton(
                            onClick = {
                                viewModel.logout()
                            },
                            modifier = Modifier.testTag("logout_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Log out",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        Text(
                            text = session?.name?.take(6) ?: "Guest",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    } else {
                        Button(
                            onClick = { navController.navigate("login/home") },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("login_nav_button")
                        ) {
                            Icon(Icons.Default.Person, contentDescription = "Login", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Login", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Search & Filter Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .zIndex(10f)
            ) {
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { 
                            viewModel.setSearchQuery(it)
                        },
                        placeholder = { Text("Search fresh fish, shellfish...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { 
                                        viewModel.setSearchQuery("") 
                                    }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                                if (isSearchFocused) {
                                    IconButton(onClick = { 
                                        focusManager.clearFocus()
                                        isSearchFocused = false
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = "Close suggestions")
                                    }
                                }
                            }
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f)
                        ),
                        keyboardOptions = KeyboardOptions(
                            imeAction = androidx.compose.ui.text.input.ImeAction.Search
                        ),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                            onSearch = {
                                if (searchQuery.isNotBlank()) {
                                    viewModel.addRecentSearch(searchQuery)
                                }
                                focusManager.clearFocus()
                                isSearchFocused = false
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 56.dp)
                            .testTag("search_bar")
                            .onFocusChanged { 
                                isSearchFocused = it.isFocused 
                            }
                    )
                }

                // Suggestions Dropdown overlay
                if (isSearchFocused) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp)
                            .heightIn(max = 280.dp)
                            .testTag("search_suggestions_dropdown")
                            .zIndex(20f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                                .padding(12.dp)
                        ) {
                            // --- RECENT SEARCHES ---
                            if (recentSearches.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Recent Searches",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                
                                recentSearches.forEach { term ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.setSearchQuery(term)
                                                viewModel.addRecentSearch(term)
                                                focusManager.clearFocus()
                                                isSearchFocused = false
                                            }
                                            .padding(horizontal = 8.dp, vertical = 10.dp)
                                            .testTag("recent_search_item_$term"),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.History,
                                            contentDescription = "History",
                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = term,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = { viewModel.removeRecentSearch(term) },
                                            modifier = Modifier
                                                .size(24.dp)
                                                .testTag("delete_recent_search_$term")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "Remove",
                                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                                
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                                )
                            }

                            // --- POPULAR SUGGESTIONS ---
                            Text(
                                text = "Popular Suggestions",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                            
                            val filteredPopular = viewModel.popularSuggestions.filter { 
                                searchQuery.isEmpty() || it.contains(searchQuery, ignoreCase = true) 
                            }
                            
                            if (filteredPopular.isEmpty()) {
                                Text(
                                    text = "No matching suggestions",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                                )
                            } else {
                                filteredPopular.forEach { suggestion ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.setSearchQuery(suggestion)
                                                viewModel.addRecentSearch(suggestion)
                                                focusManager.clearFocus()
                                                isSearchFocused = false
                                            }
                                            .padding(horizontal = 8.dp, vertical = 10.dp)
                                            .testTag("popular_suggestion_$suggestion"),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.TrendingUp,
                                            contentDescription = "Trending",
                                            tint = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = suggestion,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Categories LazyRow
            val categories = listOf("All", "Sea Fish", "Shellfish", "Fresh", "Premium", "Frozen")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setCategory(category) },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.testTag("category_chip_$category")
                    )
                }
            }

            // Offers Banner / Catch of the Day
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
                    .height(110.dp)
            ) {
                // Background Image
                Image(
                    painter = painterResource(id = R.drawable.fish_hero_banner),
                    contentDescription = "Hero Background",
                    contentScale = ContentScale.Crop,
                    alpha = 0.25f,
                    modifier = Modifier.fillMaxSize()
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Flame",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "TODAY'S SPECIAL OFFERS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                    }
                    Text(
                        text = "Get Up To 15% OFF",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Direct from harbor to your home. Free clean cut.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Section Label
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Fresh Catch of the Day",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${products.size} types available",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                // Layout grid/list toggle
                IconButton(
                    onClick = { isGridView = !isGridView },
                    modifier = Modifier.testTag("layout_toggle_btn")
                ) {
                    Icon(
                        imageVector = if (isGridView) Icons.Default.Menu else Icons.Default.GridView,
                        contentDescription = if (isGridView) "Switch to List View" else "Switch to Grid View",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Products Grid / List
            if (products.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = "Empty",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No seafood found matching search",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                if (isGridView) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 150.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        gridItems(products) { product ->
                            FishGridItem(
                                product = product,
                                isFavorite = wishlistProductIds.contains(product.id),
                                onFavoriteToggle = { viewModel.toggleFavorite(product.id) },
                                onProductClick = { navController.navigate("detail/${product.id}") },
                                onAddToCart = { viewModel.addToCart(product, 1.0) },
                                onBuyNow = {
                                    viewModel.buyNow(product) {
                                        navController.navigate("checkout")
                                    }
                                }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(products) { product ->
                            FishListItem(
                                product = product,
                                isFavorite = wishlistProductIds.contains(product.id),
                                onFavoriteToggle = { viewModel.toggleFavorite(product.id) },
                                onProductClick = { navController.navigate("detail/${product.id}") },
                                onAddToCart = { viewModel.addToCart(product, 1.0) },
                                onBuyNow = {
                                    viewModel.buyNow(product) {
                                        navController.navigate("checkout")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Floating Quick Cart Indicator (Swiggy style overlay when items exist)
        if (totalCartItems > 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(bottom = 12.dp, end = 16.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Button(
                    onClick = { navController.navigate("cart") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("floating_cart_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Cart",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "View Cart (${totalCartItems.toInt()} kg) • ₹${cart.sumOf { it.totalCost }.toInt()}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

// ==========================================
// 1.1 WISHLIST SCREEN
// ==========================================
@Composable
fun WishlistScreen(
    viewModel: FishViewModel,
    navController: NavController
) {
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val wishlistProductIds by viewModel.wishlistProductIds.collectAsStateWithLifecycle()
    val cart by viewModel.cartItems.collectAsStateWithLifecycle()
    val session by viewModel.userSession.collectAsStateWithLifecycle()
    
    val wishlistProducts = allProducts.filter { wishlistProductIds.contains(it.id) }
    val totalCartItems = cart.sumOf { it.quantityKg }
    
    Scaffold(
        bottomBar = {
            FishBottomNavigation(navController = navController, currentRoute = "wishlist")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Wishlist",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                
                if (session?.isLoggedIn == true) {
                    Text(
                        text = "Synced with Cloud",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (wishlistProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FavoriteBorder,
                            contentDescription = "Empty Wishlist",
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Your Wishlist is Empty",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Explore fish items and tap the heart icon on any card to add them to your wishlist.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { navController.navigate("home") },
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text("Browse Fresh Seafood")
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    gridItems(wishlistProducts) { product ->
                        FishGridItem(
                            product = product,
                            isFavorite = true,
                            onFavoriteToggle = { viewModel.toggleFavorite(product.id) },
                            onProductClick = { navController.navigate("detail/${product.id}") },
                            onAddToCart = { viewModel.addToCart(product, 1.0) },
                            onBuyNow = {
                                viewModel.buyNow(product) {
                                    navController.navigate("checkout")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FishListItem(
    product: FishProduct,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onProductClick: () -> Unit,
    onAddToCart: () -> Unit,
    onBuyNow: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProductClick() }
            .testTag("fish_item_${product.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Fish Image representation (Vector-drawn elegant seafood circle badge with gradient)
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.radialGradient(
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (product.category == "Shellfish") Icons.Default.Water else Icons.Default.Waves,
                        contentDescription = product.name,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = product.quality.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 8.sp,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
 
                // Offer badge
                if (product.discount > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.TopStart
                    ) {
                        Text(
                            text = "${product.discount}% OFF",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.tertiary,
                                    RoundedCornerShape(topStart = 12.dp, bottomEnd = 8.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }
 
            Spacer(modifier = Modifier.width(16.dp))
 
            // Description and core info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    IconButton(
                        onClick = onFavoriteToggle,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("fav_btn_${product.id}")
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Text(
                    text = product.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )

                // Shop Name Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Store,
                        contentDescription = "Seller",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = product.shopName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "₹${product.finalPrice.toInt()}/kg",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (product.discount > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "₹${product.pricePerKg.toInt()}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                            ),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Stock status
                Text(
                    text = "Stock: ${product.stockKg.toInt()} kg left",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (product.stockKg < 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Medium
                )
            }

            // Add and Buy Now buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (product.stockKg > 0) {
                    Button(
                        onClick = onAddToCart,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("add_to_cart_btn_${product.id}")
                    ) {
                        Text("ADD", fontWeight = FontWeight.Black, fontSize = 11.sp)
                    }
                    Button(
                        onClick = onBuyNow,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        ),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .height(30.dp)
                            .testTag("buy_now_btn_${product.id}")
                    ) {
                        Text("BUY NOW", fontWeight = FontWeight.Bold, fontSize = 9.sp)
                    }
                } else {
                    Text(
                        text = "OUT OF STOCK",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FishGridItem(
    product: FishProduct,
    isFavorite: Boolean,
    onFavoriteToggle: () -> Unit,
    onProductClick: () -> Unit,
    onAddToCart: () -> Unit,
    onBuyNow: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProductClick() }
            .testTag("fish_grid_item_${product.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Fish Image representation (Vector-drawn elegant seafood badge with radial gradient)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.radialGradient(
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (product.category == "Shellfish") Icons.Default.Water else Icons.Default.Waves,
                        contentDescription = product.name,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = product.quality.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 8.sp,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }

                // Offer badge
                if (product.discount > 0) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopStart
                    ) {
                        Text(
                            text = "${product.discount}% OFF",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.sp,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.tertiary,
                                    RoundedCornerShape(topStart = 8.dp, bottomEnd = 6.dp)
                                )
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                // Favorite Toggle button overlay in Top End
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopEnd
                ) {
                    IconButton(
                        onClick = onFavoriteToggle,
                        modifier = Modifier
                            .padding(4.dp)
                            .size(28.dp)
                            .background(Color.White.copy(alpha = 0.8f), CircleShape)
                            .testTag("fav_grid_btn_${product.id}")
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = product.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = product.category,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Shop Name Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Store,
                    contentDescription = "Seller",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = product.shopName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "₹${product.finalPrice.toInt()}/kg",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (product.discount > 0) {
                        Text(
                            text = "₹${product.pricePerKg.toInt()}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                            ),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            fontSize = 10.sp
                        )
                    }
                }

                // Add button inside grid item
                if (product.stockKg > 0) {
                    IconButton(
                        onClick = onAddToCart,
                        modifier = Modifier
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .testTag("add_to_cart_grid_btn_${product.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add to Cart",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Text(
                        text = "OUT",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            // Buy Now full width button
            if (product.stockKg > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = onBuyNow,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .testTag("buy_now_grid_btn_${product.id}"),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("BUY NOW", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


// Helper function to generate deterministic historical prices over last 30 days
fun getHistoricalPrices(productId: Int, basePrice: Double): List<Float> {
    val prices = mutableListOf<Float>()
    val random = java.util.Random(productId.toLong())
    var currentPrice = basePrice.toFloat()
    for (i in 1..30) {
        val changePercent = (random.nextFloat() * 0.1f - 0.05f) // -5% to +5% change daily
        currentPrice += currentPrice * changePercent
        prices.add(currentPrice)
    }
    return prices
}

@Composable
fun HistoricalPriceChart(
    productId: Int,
    basePrice: Double,
    category: String,
    modifier: Modifier = Modifier
) {
    val prices = remember(productId, basePrice) { getHistoricalPrices(productId, basePrice) }
    val maxPrice = remember(prices) { prices.maxOrNull() ?: 100f }
    val minPrice = remember(prices) { prices.minOrNull() ?: 0f }
    val priceRange = remember(maxPrice, minPrice) { (maxPrice - minPrice).coerceAtLeast(1f) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val errorColor = MaterialTheme.colorScheme.error

    // Pre-calculate px values using density outside Canvas DrawScope
    val density = androidx.compose.ui.platform.LocalDensity.current
    val strokeWidthPx = remember(density) { with(density) { 1.dp.toPx() } }
    val pathStrokeWidthPx = remember(density) { with(density) { 3.dp.toPx() } }
    val dotRadiusFivePx = remember(density) { with(density) { 5.dp.toPx() } }
    val dotRadiusSixPx = remember(density) { with(density) { 6.dp.toPx() } }
    val gridColor = remember(onSurfaceColor) { onSurfaceColor.copy(alpha = 0.08f) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("historical_price_chart_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Historical Price Trend",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = onSurfaceColor
                    )
                    Text(
                        text = "Last 30 Days trend for $category",
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurfaceColor.copy(alpha = 0.5f)
                    )
                }
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Avg: ₹${prices.average().toInt()}/kg",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // The Canvas Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(horizontal = 8.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val paddingBottom = 30f
                    val paddingTop = 10f
                    val chartHeight = height - paddingBottom - paddingTop
                    
                    // Draw horizontal grid lines (Y-axis grid)
                    val gridLinesCount = 4
                    for (i in 0 until gridLinesCount) {
                        val y = paddingTop + (chartHeight / (gridLinesCount - 1)) * i
                        drawLine(
                            color = gridColor,
                            start = androidx.compose.ui.geometry.Offset(0f, y),
                            end = androidx.compose.ui.geometry.Offset(width, y),
                            strokeWidth = strokeWidthPx
                        )
                    }

                    // Map price points to canvas offsets
                    val points = prices.mapIndexed { index, price ->
                        val x = (width / (prices.size - 1)) * index
                        // Flip y-axis (higher price is higher up, i.e. smaller y offset)
                        val relativeY = (price - minPrice) / priceRange
                        val y = height - paddingBottom - (relativeY * chartHeight)
                        androidx.compose.ui.geometry.Offset(x, y)
                    }

                    // Build bezier curved path
                    val path = androidx.compose.ui.graphics.Path().apply {
                        if (points.isNotEmpty()) {
                            moveTo(points.first().x, points.first().y)
                            for (i in 1 until points.size) {
                                val prev = points[i - 1]
                                val curr = points[i]
                                val controlX = (prev.x + curr.x) / 2
                                cubicTo(controlX, prev.y, controlX, curr.y, curr.x, curr.y)
                            }
                        }
                    }

                    // Draw gradient fill under the line
                    val fillPath = androidx.compose.ui.graphics.Path().apply {
                        addPath(path)
                        lineTo(width, height - paddingBottom)
                        lineTo(0f, height - paddingBottom)
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.25f),
                                primaryColor.copy(alpha = 0.0f)
                            )
                        )
                    )

                    // Draw line stroke
                    drawPath(
                        path = path,
                        color = primaryColor,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = pathStrokeWidthPx,
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    )

                    // Draw some accent points (e.g. Min, Max, Current)
                    val maxIndex = prices.indexOf(maxPrice)
                    val minIndex = prices.indexOf(minPrice)
                    
                    if (maxIndex in points.indices) {
                        drawCircle(
                            color = tertiaryColor,
                            radius = dotRadiusFivePx,
                            center = points[maxIndex]
                        )
                    }
                    if (minIndex in points.indices) {
                        drawCircle(
                            color = errorColor,
                            radius = dotRadiusFivePx,
                            center = points[minIndex]
                        )
                    }
                    if (points.isNotEmpty()) {
                        drawCircle(
                            color = primaryColor,
                            radius = dotRadiusSixPx,
                            center = points.last()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // X-Axis labels (30 days ago, 15 days ago, Today)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "30 Days Ago (₹${minPrice.toInt()})",
                    style = MaterialTheme.typography.labelSmall,
                    color = onSurfaceColor.copy(alpha = 0.4f)
                )
                Text(
                    text = "Historical Trend",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
                Text(
                    text = "Today (₹${prices.last().toInt()})",
                    style = MaterialTheme.typography.labelSmall,
                    color = onSurfaceColor.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
fun ProductReviewSection(
    productId: Int,
    viewModel: FishViewModel,
    modifier: Modifier = Modifier
) {
    val allReviews by viewModel.productReviews.collectAsStateWithLifecycle()
    val reviews = remember(allReviews, productId) { allReviews.filter { it.productId == productId } }
    
    var userRating by remember { mutableStateOf(5) }
    var userComment by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    
    val averageRating = remember(reviews) {
        if (reviews.isEmpty()) 5.0 else reviews.map { it.rating }.average()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("product_reviews_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header with average rating stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Customer Reviews",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${reviews.size} verified customer reviews",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format(java.util.Locale.US, "%.1f / 5.0", averageRating),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Form to submit a review
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(14.dp)
            ) {
                Text(
                    text = "Write a Review",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Star Selector Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Your Rating: ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    for (star in 1..5) {
                        IconButton(
                            onClick = { userRating = star },
                            modifier = Modifier.size(28.dp).testTag("star_rating_$star")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "$star Stars",
                                tint = if (star <= userRating) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Name Input
                OutlinedTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    label = { Text("Your Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("review_name_input"),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Comment Input
                OutlinedTextField(
                    value = userComment,
                    onValueChange = { userComment = it },
                    label = { Text("Your Comment") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .testTag("review_comment_input"),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Submit Button
                Button(
                    onClick = {
                        viewModel.addProductReview(productId, userName, userRating, userComment)
                        // Clear fields
                        userComment = ""
                        userName = ""
                        userRating = 5
                    },
                    modifier = Modifier
                        .align(Alignment.End)
                        .height(36.dp)
                        .testTag("submit_review_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Submit Review", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display Reviews List
            if (reviews.isEmpty()) {
                Text(
                    text = "No reviews yet. Be the first to review this product!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                Text(
                    text = "Verified Buyer Feedback",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    reviews.forEach { r ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = r.authorName,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = r.date,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                            }
                            
                            // Rating Star Row
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                for (star in 1..5) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (star <= r.rating) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            Text(
                                text = r.comment,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}


// ==========================================
// 2. FISH DETAILS PAGE
// ==========================================
@Composable
fun DetailScreen(
    productId: Int,
    viewModel: FishViewModel,
    navController: NavController
) {
    val products by viewModel.allProducts.collectAsStateWithLifecycle()
    val session by viewModel.userSession.collectAsStateWithLifecycle()
    val product = products.find { it.id == productId }

    var selectedQty by remember { mutableStateOf(1.0) }

    Scaffold(
        topBar = {
            CustomTopAppBar(
                title = product?.name ?: "Fish Details",
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (product == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Product not found")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Banner / Big Graphic (Beautiful sea-themed card with vector drawings & gradients)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (product.category == "Shellfish") Icons.Default.Water else Icons.Default.Waves,
                            contentDescription = "Fish details graphic",
                            tint = Color.White,
                            modifier = Modifier.size(96.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = product.quality.uppercase() + " QUALITY ASSURED",
                            color = MaterialTheme.colorScheme.tertiary,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    // Floating Discount tag
                    if (product.discount > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            Text(
                                text = "SPECIAL ${product.discount}% DISCOUNT",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.tertiary, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Title and category
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = product.name,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = product.category,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Quality Badge
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.primary
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Certified", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(product.quality, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    // Shop/Seller Information Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .testTag("detail_seller_card"),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Store,
                                    contentDescription = "Shop",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Sold By / Merchant",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = product.shopName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Pricing Details
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "PRICE PER KG",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "₹${product.finalPrice.toInt()}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (product.discount > 0) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "₹${product.pricePerKg.toInt()}",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                        ),
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "AVAILABILITY",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "${product.stockKg.toInt()} kg in Stock",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (product.stockKg < 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Description
                    Text(
                        text = "Description",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    HistoricalPriceChart(productId = product.id, basePrice = product.pricePerKg, category = product.category)

                    Spacer(modifier = Modifier.height(20.dp))

                    // Quantity selector
                    Text(
                        text = "Select Quantity (kg)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        IconButton(
                            onClick = { if (selectedQty > 1.0) selectedQty -= 1.0 },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                .size(40.dp)
                                .testTag("qty_minus_btn")
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease qty")
                        }

                        Text(
                            text = "${selectedQty.toInt()} kg",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier
                                .padding(horizontal = 24.dp)
                                .testTag("qty_display")
                        )

                        IconButton(
                            onClick = { if (selectedQty < product.stockKg) selectedQty += 1.0 },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                .size(40.dp)
                                .testTag("qty_plus_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase qty")
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    // Action buttons (Add to Cart / Buy Now)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.addToCart(product, selectedQty)
                                navController.navigateUp()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("detail_add_to_cart_button")
                        ) {
                            Icon(Icons.Default.AddShoppingCart, contentDescription = "Cart")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add to Cart", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.addToCart(product, selectedQty)
                                if (session?.isLoggedIn == true) {
                                    navController.navigate("checkout")
                                } else {
                                    navController.navigate("login/checkout")
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(52.dp)
                                .testTag("detail_buy_now_button")
                        ) {
                            Text("Buy Now", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    ProductReviewSection(productId = product.id, viewModel = viewModel)
                    
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}


// ==========================================
// 3. CART PAGE
// ==========================================
@Composable
fun CartScreen(
    viewModel: FishViewModel,
    navController: NavController
) {
    val cart by viewModel.cartItems.collectAsStateWithLifecycle()
    val session by viewModel.userSession.collectAsStateWithLifecycle()

    val subtotal = cart.sumOf { it.totalCost }
    val deliveryFee = if (subtotal > 0) 40.0 else 0.0
    val totalAmount = subtotal + deliveryFee

    Scaffold(
        topBar = {
            CustomTopAppBar(
                title = "My Shopping Cart",
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            FishBottomNavigation(navController = navController, currentRoute = "cart")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            if (cart.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Empty Cart",
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Your cart is empty!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Add some ocean fresh catches to your cart and treat yourself to a delicious meal.",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { navController.navigate("home") },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Browse Fresh Catch")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Selected Fresh Seafood",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(
                                onClick = { viewModel.clearCart() },
                                modifier = Modifier.testTag("clear_cart_text")
                            ) {
                                Text("Clear All", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    items(cart) { item ->
                        CartListItem(
                            item = item,
                            onIncrement = { viewModel.updateCartQty(item.productId, item.quantityKg + 1.0) },
                            onDecrement = { viewModel.updateCartQty(item.productId, item.quantityKg - 1.0) },
                            onRemove = { viewModel.removeFromCart(item.productId) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        // Bill Details Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text("Bill Details", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Item Total", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    Text("₹${subtotal.toInt()}")
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Delivery & Handling", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    Text("₹${deliveryFee.toInt()}")
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Grand Total", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                                    Text(
                                        text = "₹${totalAmount.toInt()}",
                                        fontWeight = FontWeight.Black,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                // Proceed Button Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 12.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .navigationBarsPadding()
                    ) {
                        Button(
                            onClick = {
                                if (session?.isLoggedIn == true) {
                                    navController.navigate("checkout")
                                } else {
                                    navController.navigate("login/checkout")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("checkout_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Proceed to Checkout", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = "Proceed")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartListItem(
    item: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cart_item_${item.productId}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left icon shape
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Waves, contentDescription = item.name, tint = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Text info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(item.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("₹${item.finalPricePerKg.toInt()}/kg", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Total: ₹${item.totalCost.toInt()}",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp
                )
            }

            // Adjust buttons + Remove
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("remove_item_btn_${item.productId}")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    IconButton(
                        onClick = onDecrement,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("cart_minus_btn_${item.productId}")
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Less", modifier = Modifier.size(14.dp))
                    }

                    Text(
                        text = "${item.quantityKg.toInt()}kg",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .testTag("cart_qty_${item.productId}")
                    )

                    IconButton(
                        onClick = onIncrement,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("cart_plus_btn_${item.productId}")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "More", modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}


// ==========================================
// 4. LOGIN / REGISTER PAGE
// ==========================================
@Composable
fun LoginRegisterScreen(
    viewModel: FishViewModel,
    navController: NavController,
    redirectTarget: String
) {
    // Tab state: 0 = Sign In, 1 = Sign Up
    var activeTab by remember { mutableStateOf(0) }

    // Sign Up Fields
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var signUpErrors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var isSignUpAttempted by remember { mutableStateOf(false) }

    // Sign In Fields
    var loginIdentifier by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var isSignInAttempted by remember { mutableStateOf(false) }

    // OTP Inputs
    var emailOtpInput by remember { mutableStateOf("") }
    var mobileOtpInput by remember { mutableStateOf("") }

    val signUpStep by viewModel.signUpStep.collectAsStateWithLifecycle()
    val simulatedEmailOtp by viewModel.simulatedEmailOtp.collectAsStateWithLifecycle()
    val simulatedMobileOtp by viewModel.simulatedMobileOtp.collectAsStateWithLifecycle()
    val otpError by viewModel.otpError.collectAsStateWithLifecycle()
    val loginError by viewModel.loginError.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.resetSignUpFlow()
    }

    // Client side validation helper
    fun runSignUpValidation() {
        val errors = mutableMapOf<String, String>()
        
        // 1. Full Name
        if (name.isBlank()) {
            errors["name"] = "Full Name is required"
        } else if (name.trim().length < 3) {
            errors["name"] = "Minimum 3 characters required"
        } else if (!name.all { it.isLetter() || it == ' ' }) {
            errors["name"] = "Only letters and spaces allowed"
        }
        
        // 2. Mobile Number
        val mobileRegex = Regex("^[6-9][0-9]{9}$")
        if (mobile.isBlank()) {
            errors["mobile"] = "Mobile Number is required"
        } else if (!mobileRegex.matches(mobile)) {
            errors["mobile"] = "Must be 10 digits starting with 6, 7, 8, or 9"
        }
        
        // 3. Email
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@(.+)$")
        if (email.isBlank()) {
            errors["email"] = "Email is required"
        } else if (!emailRegex.matches(email)) {
            errors["email"] = "Enter a valid email address"
        }
        
        // 4. Password
        val passwordRegex = Regex("^[A-Z][a-z0-9@#$%^&+=!]{5,}$")
        if (password.length < 6) {
            errors["password"] = "Minimum 6 characters required"
        } else if (password.isNotEmpty() && !password.first().isUpperCase()) {
            errors["password"] = "First letter must be Uppercase"
        } else if (!passwordRegex.matches(password)) {
            errors["password"] = "Must start with Uppercase, min 6 characters"
        }
        
        // 5. Address
        if (address.isBlank()) {
            errors["address"] = "Address is required"
        } else if (address.trim().length < 5) {
            errors["address"] = "Minimum 5 characters required"
        }
        
        signUpErrors = errors
    }

    Scaffold(
        topBar = {
            CustomTopAppBar(
                title = when (signUpStep) {
                    SignUpStep.FORM -> if (activeTab == 0) "Sign In" else "Sign Up"
                    SignUpStep.EMAIL_OTP -> "Verify Email OTP"
                    SignUpStep.MOBILE_OTP -> "Verify Mobile OTP"
                    SignUpStep.SUCCESS -> "Registration Success"
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (signUpStep != SignUpStep.FORM) {
                            viewModel.resetSignUpFlow()
                        } else {
                            navController.navigateUp() 
                        }
                    }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            if (signUpStep == SignUpStep.FORM) {
                // App Logo Header
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Water,
                        contentDescription = "App Icon Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Welcome to OceanFresh Fish Co.",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Premium quality fresh catch delivered straight to you.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Custom Tab Row for Switchable Login/Register
                TabRow(
                    selectedTabIndex = activeTab,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { 
                            activeTab = 0 
                            isSignUpAttempted = false
                            isSignInAttempted = false
                        },
                        text = { Text("Sign In", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { 
                            activeTab = 1 
                            isSignUpAttempted = false
                            isSignInAttempted = false
                        },
                        text = { Text("Sign Up", fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (activeTab == 0) {
                    // --- SIGN IN FORM ---
                    OutlinedTextField(
                        value = loginIdentifier,
                        onValueChange = { loginIdentifier = it },
                        label = { Text("Email Address or Mobile") },
                        placeholder = { Text("example@gmail.com / 9876543210") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Email/Mobile") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_username_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = loginPassword,
                        onValueChange = { loginPassword = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input"),
                        singleLine = true
                    )

                    if (loginError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = loginError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Start)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            isSignInAttempted = true
                            if (loginIdentifier.isNotBlank() && loginPassword.isNotBlank()) {
                                viewModel.loginWithCredentials(loginIdentifier, loginPassword) {
                                    navController.navigate(redirectTarget) {
                                        popUpTo("home")
                                    }
                                }
                            }
                        },
                        enabled = loginIdentifier.isNotBlank() && loginPassword.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("signin_submit_button")
                    ) {
                        Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = { activeTab = 1 },
                        modifier = Modifier.testTag("switch_to_signup_btn")
                    ) {
                        Text("Don't have an account? Sign Up")
                    }

                } else {
                    // --- SIGN UP FORM ---
                    // 1. Full Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { 
                            name = it
                            if (isSignUpAttempted) runSignUpValidation()
                        },
                        label = { Text("Full Name") },
                        placeholder = { Text("Hemalatha") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name") },
                        isError = signUpErrors.containsKey("name"),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("signup_name_input"),
                        singleLine = true
                    )
                    if (signUpErrors.containsKey("name")) {
                        Text(
                            text = signUpErrors["name"] ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.align(Alignment.Start).padding(start = 8.dp, top = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Mobile Number
                    OutlinedTextField(
                        value = mobile,
                        onValueChange = { 
                            mobile = it
                            if (isSignUpAttempted) runSignUpValidation()
                        },
                        label = { Text("Mobile Number") },
                        placeholder = { Text("9876543210") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Mobile") },
                        isError = signUpErrors.containsKey("mobile"),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("signup_mobile_input"),
                        singleLine = true
                    )
                    if (signUpErrors.containsKey("mobile")) {
                        Text(
                            text = signUpErrors["mobile"] ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.align(Alignment.Start).padding(start = 8.dp, top = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. Email Address
                    OutlinedTextField(
                        value = email,
                        onValueChange = { 
                            email = it
                            if (isSignUpAttempted) runSignUpValidation()
                        },
                        label = { Text("Email Address") },
                        placeholder = { Text("example@gmail.com") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                        isError = signUpErrors.containsKey("email"),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("signup_email_input"),
                        singleLine = true
                    )
                    if (signUpErrors.containsKey("email")) {
                        Text(
                            text = signUpErrors["email"] ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.align(Alignment.Start).padding(start = 8.dp, top = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4. Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { 
                            password = it
                            if (isSignUpAttempted) runSignUpValidation()
                        },
                        label = { Text("Password") },
                        placeholder = { Text("Password@123") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock") },
                        isError = signUpErrors.containsKey("password"),
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("signup_password_input"),
                        singleLine = true
                    )
                    if (signUpErrors.containsKey("password")) {
                        Text(
                            text = signUpErrors["password"] ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.align(Alignment.Start).padding(start = 8.dp, top = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 5. Address
                    OutlinedTextField(
                        value = address,
                        onValueChange = { 
                            address = it
                            if (isSignUpAttempted) runSignUpValidation()
                        },
                        label = { Text("Delivery Address") },
                        placeholder = { Text("Main Road, Hyderabad") },
                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = "Address") },
                        isError = signUpErrors.containsKey("address"),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("signup_address_input"),
                        maxLines = 3
                    )
                    if (signUpErrors.containsKey("address")) {
                        Text(
                            text = signUpErrors["address"] ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.align(Alignment.Start).padding(start = 8.dp, top = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Sign Up Button
                    Button(
                        onClick = {
                            isSignUpAttempted = true
                            runSignUpValidation()
                            if (signUpErrors.isEmpty()) {
                                viewModel.startSignUpFlow(name, mobile, email, password, address) {
                                    navController.navigate(redirectTarget) {
                                        popUpTo("home")
                                    }
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("signup_submit_button")
                    ) {
                        Text("Sign Up", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = { activeTab = 0 },
                        modifier = Modifier.testTag("switch_to_signin_btn")
                    ) {
                        Text("Already have an account? Sign In")
                    }
                }

            } else if (signUpStep == SignUpStep.EMAIL_OTP) {
                // --- EMAIL OTP VERIFICATION ---
                Text(
                    text = "Verify Email OTP",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "A simulated OTP verification code has been sent to your email: $email",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Simulated OTP Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "SIMULATED EMAIL INBOX",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = simulatedEmailOtp ?: "",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.tertiary,
                            letterSpacing = 6.sp,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                .padding(horizontal = 24.dp, vertical = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = emailOtpInput,
                    onValueChange = { if (it.length <= 4) emailOtpInput = it },
                    label = { Text("Enter Email OTP") },
                    placeholder = { Text("xxxx") },
                    leadingIcon = { Icon(Icons.Default.LockOpen, contentDescription = "OTP") },
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("email_otp_input_field"),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
                )

                if (otpError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = otpError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        viewModel.verifyEmailOtp(emailOtpInput)
                    },
                    enabled = emailOtpInput.length == 4,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("verify_email_otp_button")
                ) {
                    Text("Verify Email & Proceed", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = { viewModel.resetSignUpFlow() },
                    modifier = Modifier.testTag("back_to_form_btn")
                ) {
                    Text("Back and Edit Form Details")
                }

            } else if (signUpStep == SignUpStep.MOBILE_OTP) {
                // --- MOBILE OTP VERIFICATION ---
                Text(
                    text = "Verify Mobile OTP",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "A simulated OTP verification SMS code has been sent to your mobile: $mobile",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Simulated SMS Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "SIMULATED SMS MESSAGE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = simulatedMobileOtp ?: "",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.tertiary,
                            letterSpacing = 6.sp,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                .padding(horizontal = 24.dp, vertical = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = mobileOtpInput,
                    onValueChange = { if (it.length <= 4) mobileOtpInput = it },
                    label = { Text("Enter Mobile OTP") },
                    placeholder = { Text("xxxx") },
                    leadingIcon = { Icon(Icons.Default.LockOpen, contentDescription = "OTP") },
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("mobile_otp_input_field"),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
                )

                if (otpError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = otpError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        viewModel.verifyMobileOtp(mobileOtpInput) {
                            navController.navigate(redirectTarget) {
                                popUpTo("home")
                            }
                        }
                    },
                    enabled = mobileOtpInput.length == 4,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("verify_mobile_otp_button")
                ) {
                    Text("Verify Mobile & Create Account", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = { viewModel.resetSignUpFlow() },
                    modifier = Modifier.testTag("back_to_form_btn")
                ) {
                    Text("Cancel and Edit Form Details")
                }
            }
        }
    }
}


// ==========================================
// 5. CHECKOUT PAGE
// ==========================================
@Composable
fun CheckoutScreen(
    viewModel: FishViewModel,
    navController: NavController
) {
    val cart by viewModel.cartItems.collectAsStateWithLifecycle()
    val session by viewModel.userSession.collectAsStateWithLifecycle()

    var address by remember { mutableStateOf("") }
    var altMobile by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("UPI") }

    val subtotal = cart.sumOf { it.totalCost }
    val deliveryFee = 40.0
    val grandTotal = subtotal + deliveryFee

    var showExitConfirmationDialog by remember { mutableStateOf(false) }

    if (cart.isNotEmpty()) {
        BackHandler {
            showExitConfirmationDialog = true
        }
    }

    if (showExitConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmationDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Leave Checkout?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Text(
                    text = "You still have items in your cart. Are you sure you want to abandon checkout and go back?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitConfirmationDialog = false
                        navController.navigateUp()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Leave", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showExitConfirmationDialog = false }
                ) {
                    Text("Stay", fontWeight = FontWeight.Bold)
                }
            },
            modifier = Modifier.testTag("exit_checkout_dialog")
        )
    }

    // Check prefilled mobile
    LaunchedEffect(session) {
        if (session != null && altMobile.isEmpty()) {
            altMobile = session?.mobile ?: ""
        }
    }

    Scaffold(
        topBar = {
            CustomTopAppBar(
                title = "Checkout",
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (cart.isNotEmpty()) {
                                showExitConfirmationDialog = true
                            } else {
                                navController.navigateUp()
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // 1. Delivery Address Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = "Delivery", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Delivery Address", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            placeholder = { Text("Enter your complete address with flat number, building, street, landmark...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("checkout_address_input"),
                            minLines = 3,
                            maxLines = 4,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = altMobile,
                            onValueChange = { altMobile = it },
                            label = { Text("Delivery Contact Number") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Contact Phone") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("checkout_phone_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Payment Method Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Payment, contentDescription = "Payment", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Select Payment Method", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val options = listOf(
                            Pair("UPI", "Pay instantly using UPI apps"),
                            Pair("Card", "Credit / Debit Cards"),
                            Pair("Cash on Delivery", "Pay cash/UPI at door step")
                        )

                        options.forEach { (option, desc) ->
                            val isSelected = paymentMethod == option
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { paymentMethod = option }
                                    .background(
                                        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(vertical = 10.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { paymentMethod = option },
                                    modifier = Modifier.testTag("radio_payment_$option")
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(option, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                    Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Order Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Order Summary", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))

                        cart.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${item.name} (${item.quantityKg.toInt()} kg)", style = MaterialTheme.typography.bodyMedium)
                                Text("₹${item.totalCost.toInt()}", fontWeight = FontWeight.Bold)
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Delivery Charges", style = MaterialTheme.typography.bodySmall)
                            Text("₹${deliveryFee.toInt()}", style = MaterialTheme.typography.bodySmall)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Grand Total", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "₹${grandTotal.toInt()}",
                                fontWeight = FontWeight.Black,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Place Order Button
                Button(
                    onClick = {
                        if (address.isNotBlank() && altMobile.isNotBlank()) {
                            viewModel.placeOrder(address, altMobile, paymentMethod) { orderId ->
                                navController.navigate("track/$orderId") {
                                    popUpTo("home")
                                }
                            }
                        }
                    },
                    enabled = address.isNotBlank() && altMobile.isNotBlank() && cart.isNotEmpty(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("place_order_button")
                ) {
                    Icon(Icons.Default.DoneAll, contentDescription = "Place")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Place Order & Pay", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}


// ==========================================
// 6. ORDER HISTORY PAGE
// ==========================================
@Composable
fun OrdersHistoryScreen(
    viewModel: FishViewModel,
    navController: NavController
) {
    val orders by viewModel.allOrders.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CustomTopAppBar(
                title = "My Orders",
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("home") }) {
                        Icon(imageVector = Icons.Default.Home, contentDescription = "Home")
                    }
                }
            )
        },
        bottomBar = {
            FishBottomNavigation(navController = navController, currentRoute = "orders")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            if (orders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "No orders",
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No orders placed yet!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "When you order ocean fresh seafood, your order history and tracking status will appear here.",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { navController.navigate("home") },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Order Fresh Catch Now")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            text = "Order History",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    items(orders) { order ->
                        OrderHistoryListItem(
                            order = order,
                            onTrackClick = { navController.navigate("track/${order.id}") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderHistoryListItem(
    order: Order,
    onTrackClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("order_item_${order.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ORDER ID: #FISH-2026-${order.id}",
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = formatTimestamp(order.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (order.deliveryStatus) {
                        "Delivered" -> Color(0xFFE8F5E9)
                        "Out for Delivery" -> Color(0xFFFFF3E0)
                        else -> MaterialTheme.colorScheme.primaryContainer
                    },
                    contentColor = when (order.deliveryStatus) {
                        "Delivered" -> Color(0xFF2E7D32)
                        "Out for Delivery" -> Color(0xFFE65100)
                        else -> MaterialTheme.colorScheme.primary
                    }
                ) {
                    Text(
                        text = order.deliveryStatus.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Items Ordered:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = order.orderSummaryText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = 2.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Paid via ${order.paymentMethod}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Text(
                        text = "₹${order.totalAmount.toInt()}",
                        fontWeight = FontWeight.Black,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }

                Button(
                    onClick = onTrackClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("track_order_btn_${order.id}")
                ) {
                    Icon(Icons.Default.LocalShipping, contentDescription = "Track", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Track Live", fontSize = 12.sp)
                }
            }
        }
    }
}


// ==========================================
// 7. ORDER TRACKING PAGE
// ==========================================
@Composable
fun OrderTrackingScreen(
    orderId: Int,
    viewModel: FishViewModel,
    navController: NavController
) {
    val orderFlow = remember(orderId) { viewModel.getOrderFlow(orderId) }
    val order by orderFlow.collectAsStateWithLifecycle(initialValue = null)

    val context = LocalContext.current
    var lastStatus by remember { mutableStateOf<String?>(null) }
    var showToastMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(order) {
        val currentOrder = order ?: return@LaunchedEffect
        val currentStatus = currentOrder.deliveryStatus
        if (lastStatus != null && lastStatus != currentStatus) {
            val msg = "Order #${currentOrder.id} status updated to: $currentStatus"
            showToastMessage = msg
            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
        }
        lastStatus = currentStatus
    }

    LaunchedEffect(showToastMessage) {
        if (showToastMessage != null) {
            kotlinx.coroutines.delay(4000)
            showToastMessage = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                CustomTopAppBar(
                    title = "Track Delivery",
                    navigationIcon = {
                        IconButton(onClick = { navController.navigate("orders") }) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(innerPadding)
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
            if (order == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                val currentOrder = order!!

                // Live status header box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "LIVE DELIVERY PROGRESS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "ORDER ID: #FISH-2026-${currentOrder.id}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = when (currentOrder.deliveryStatus) {
                                "Placed" -> "Order placed successfully! We are confirming with harbor."
                                "Preparing" -> "Harbor experts are cleaning, cutting and ice packing your fish."
                                "Out for Delivery" -> "Our fresh delivery agent is rushing to your address."
                                "Delivered" -> "Delivered! Cook fresh, eat fresh. Enjoy your meal!"
                                else -> "Processing order."
                            },
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Horizontal progress bar component showing 'Processing', 'Out for Delivery', and 'Delivered'
                val progressFraction = when (currentOrder.deliveryStatus) {
                    "Placed" -> 0.15f
                    "Preparing" -> 0.50f
                    "Out for Delivery" -> 0.80f
                    "Delivered" -> 1.0f
                    else -> 0.15f
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .testTag("horizontal_progress_bar_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "DELIVERY PROGRESS",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Linear indicator bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progressFraction)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.tertiary
                                            )
                                        )
                                    )
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Status steps label row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val statuses = listOf(
                                "Placed" to 0.15f,
                                "Processing" to 0.50f,
                                "On The Way" to 0.80f,
                                "Delivered" to 1.0f
                            )
                            
                            statuses.forEach { (label, frac) ->
                                val isActive = progressFraction >= frac
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = when (label) {
                                            "Placed" -> Icons.Default.ReceiptLong
                                            "Processing" -> Icons.Default.RoomService
                                            "On The Way" -> Icons.Default.LocalShipping
                                            else -> Icons.Default.CheckCircle
                                        },
                                        contentDescription = label,
                                        tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Vertical Timeline Progress
                val stages = listOf(
                    Triple("Placed", "Order Placed", "Your order has been logged and confirmed"),
                    Triple("Preparing", "Preparing Fresh Catch", "Custom cutting, portioning and premium vacuum sealing with ice"),
                    Triple("Out for Delivery", "Out for Delivery", "Fresh dispatch agent is on the route to your doorstep"),
                    Triple("Delivered", "Delivered", "Delicious premium seafood delivered successfully")
                )

                val activeIndex = stages.indexOfFirst { it.first == currentOrder.deliveryStatus }.coerceAtLeast(0)

                stages.forEachIndexed { index, (status, title, desc) ->
                    val isCompleted = index <= activeIndex
                    val isCurrent = index == activeIndex

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Timeline graphic column (circle and vertical connecting line)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(36.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(
                                        color = if (isCompleted) {
                                            if (isCurrent) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                        },
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isCompleted && !isCurrent) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Completed",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                } else if (isCurrent) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(Color.White, CircleShape)
                                    )
                                }
                            }

                            // Connecting line to next element
                            if (index < stages.size - 1) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(54.dp)
                                        .background(
                                            color = if (index < activeIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                                                alpha = 0.15f
                                            )
                                        )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Text detail
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(bottom = 20.dp)
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (isCurrent) FontWeight.Black else FontWeight.Bold,
                                color = if (isCurrent) MaterialTheme.colorScheme.tertiary else if (isCompleted) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                            )
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isCompleted) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Detail details box (Address, Mobile, Items)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Delivery details", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row {
                            Icon(Icons.Default.LocationOn, contentDescription = "Address", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = currentOrder.address,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row {
                            Icon(Icons.Default.Phone, contentDescription = "Phone", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = currentOrder.mobile,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Items in this shipment:",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = currentOrder.orderSummaryText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Amount Paid", style = MaterialTheme.typography.bodySmall)
                            Text("₹${currentOrder.totalAmount.toInt()}", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Button to home or order list
                Button(
                    onClick = { navController.navigate("home") { popUpTo("home") { inclusive = true } } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("track_to_home_button")
                ) {
                    Text("Return to Shopping", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }

        // Toast notification banner
        AnimatedVisibility(
            visible = showToastMessage != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("status_toast_notification")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Alert",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Status Update",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = showToastMessage ?: "",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

