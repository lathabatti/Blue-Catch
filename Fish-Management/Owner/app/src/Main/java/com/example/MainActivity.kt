package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.FishRepository
import com.example.ui.FishViewModel
import com.example.ui.CustomerSession
import com.example.ui.GuestSession
import com.example.ui.screens.*
import com.example.ui.theme.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Core Database and Repository initialization
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = FishRepository(database.fishDao())

        setContent {
            MyApplicationTheme {
                // Instantiating the ViewModel with the custom repository factory
                val fishViewModel: FishViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return FishViewModel(repository) as T
                        }
                    }
                )

                MainScaffold(viewModel = fishViewModel)
            }
        }
    }
}

// Visual routing structure representing the Sidebar choices
data class DrawerItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val route: String,
    val parentGroup: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(viewModel: FishViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()

    if (currentUser == null) {
        AuthScreens(viewModel = viewModel)
        return
    }

    val user = currentUser
    if (user is CustomerSession || user is GuestSession) {
        CustomerPortalScreen(viewModel = viewModel, session = user)
        return
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Alert system counts for displaying badges in sidebar and top bar
    val alertList by viewModel.alerts.collectAsState()
    val unreadAlertsCount = alertList.filter { !it.isRead }.size

    // Simple reactive state navigation
    var currentRoute by remember { mutableStateOf("/owner") }
    // Auxiliary state to track nested tabs in Management/Operations screens
    var targetSubTab by remember { mutableIntStateOf(0) }

    val drawerItems = listOf(
        DrawerItem("Dashboard Summary", "Business summary", Icons.Default.Home, "/owner", "Home"),
        DrawerItem("Wholesale Purchases", "Buy fish & wholesaler dues", Icons.Default.ShoppingCart, "/owner/purchase", "Sourcing"),
        DrawerItem("Fish Marketplace", "Harbor rates & trends", Icons.Default.TrendingUp, "/owner/marketplace", "Sourcing"),
        DrawerItem("Stock & Inventory", "Storage boxes & weight loss", Icons.Default.Layers, "/owner/stock", "Inventory"),
        DrawerItem("Sales & Billing", "Billing invoices & hotel logs", Icons.Default.Receipt, "/owner/sales", "Sales"),
        
        // Administrative grouped routing (linked directly to the Management screens' tabs)
        DrawerItem("Ledger Spreadsheets", "All databases in Excel format", Icons.Default.GridOn, "/owner/spreadsheets", "Admin"),
        DrawerItem("Customer Ledger", "Credit book dues", Icons.Default.People, "/owner/customers", "Admin"),
        DrawerItem("Supplier Accounts", "Wholesaler dues", Icons.Default.Business, Icons.Default.Business.name, "Admin"), // Helper token
        DrawerItem("Worker Staff", "Roles & PIN creation", Icons.Default.Engineering, "WorkerStaff", "Admin"),
        DrawerItem("Business Expenses", "Ice & transport logs", Icons.Default.AccountBalanceWallet, "BizExpenses", "Admin"),

        // Operational grouped routing (linked directly to the Operations screens' tabs)
        DrawerItem("Van Delivery Dispatch", "Vehicle dispatch status", Icons.Default.LocalShipping, "/owner/delivery", "Operations"),
        DrawerItem("Daily Market Price", "Daily price index", Icons.Default.TrendingUp, "/owner/market-price", "Operations"),
        DrawerItem("Profit & Reports", "P&L Statement & Distribution", Icons.Default.Assessment, "/owner/reports", "Operations"),
        DrawerItem("Automated Alerts", "Warnings center", Icons.Default.Notifications, "/owner/alerts", "Operations")
    )

    val filteredDrawerItems = remember(currentUser) {
        val role = currentUser?.role ?: ""
        drawerItems.filter { item ->
            when (role) {
                "Owner", "Manager" -> true
                "Staff" -> {
                    item.route == "/owner" ||
                    item.route == "/owner/stock" ||
                    item.route == "/owner/sales" ||
                    item.route == "/owner/market-price" ||
                    item.route == "/owner/alerts"
                }
                "Delivery Boy" -> {
                    item.route == "/owner" ||
                    item.route == "/owner/delivery" ||
                    item.route == "/owner/sales" ||
                    item.route == "/owner/alerts"
                }
                else -> false
            }
        }
    }

    LaunchedEffect(currentUser) {
        val user = currentUser
        if (user != null) {
            val role = user.role
            if (role == "Delivery Boy") {
                currentRoute = "Operations"
                targetSubTab = 0
            } else {
                currentRoute = "/owner"
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = OceanSurface,
                modifier = Modifier
                    .width(310.dp)
                    .fillMaxHeight()
            ) {
                // Drawer Brand Header (With session info and Logout action)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(OceanDark)
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_app_icon),
                                contentDescription = "App Icon",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = currentUser?.name ?: "Blue Catch ERP",
                                    color = TealNeon,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = OceanCard),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(
                                        text = currentUser?.role?.uppercase() ?: "STAFF",
                                        color = TealNeon,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                                Text(
                                    text = currentUser?.phone ?: "",
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    maxLines = 1
                                )
                            }
                        }

                        // Logout Button
                        IconButton(
                            onClick = { viewModel.logout() },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("logout_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Sign Out",
                                tint = CoralRed
                            )
                        }
                    }
                }

                Divider(color = OceanDark)

                // List of Sidebar Options grouped by scope
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Grouping header helper
                    var activeGroup = ""

                    items(filteredDrawerItems) { item ->
                        // Show parent group header if changed
                        if (activeGroup != item.parentGroup) {
                            activeGroup = item.parentGroup
                            Text(
                                text = activeGroup.uppercase(),
                                color = TealNeon.copy(alpha = 0.7f),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(start = 12.dp, top = 14.dp, bottom = 4.dp)
                            )
                        }

                        val isSelected = when (item.route) {
                            "/owner" -> currentRoute == "/owner"
                            "/owner/purchase" -> currentRoute == "/owner/purchase"
                            "/owner/marketplace" -> currentRoute == "/owner/marketplace"
                            "/owner/stock" -> currentRoute == "/owner/stock"
                            "/owner/sales" -> currentRoute == "/owner/sales"
                            
                            // Management screen sub-tabs
                            "/owner/customers" -> currentRoute == "Management" && targetSubTab == 0
                            Icons.Default.Business.name -> currentRoute == "Management" && targetSubTab == 1
                            "WorkerStaff" -> currentRoute == "Management" && targetSubTab == 2
                            "BizExpenses" -> currentRoute == "Management" && targetSubTab == 3
                            
                            // Operations screen sub-tabs
                            "/owner/delivery" -> currentRoute == "Operations" && targetSubTab == 0
                            "/owner/market-price" -> currentRoute == "Operations" && targetSubTab == 1
                            "/owner/reports" -> currentRoute == "Operations" && targetSubTab == 2
                            "/owner/alerts" -> currentRoute == "Operations" && targetSubTab == 3
                            else -> false
                        }

                        NavigationDrawerItem(
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) OceanDark else TealNeon
                                )
                            },
                            label = {
                                Column {
                                    Text(
                                        text = item.title,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = item.subtitle,
                                        fontSize = 9.sp,
                                        color = if (isSelected) OceanDark.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.5f)
                                    )
                                }
                            },
                            selected = isSelected,
                            onClick = {
                                scope.launch { drawerState.close() }
                                when (item.route) {
                                    "/owner", "/owner/purchase", "/owner/marketplace", "/owner/stock", "/owner/sales", "/owner/spreadsheets" -> {
                                        currentRoute = item.route
                                    }
                                    "/owner/customers" -> {
                                        currentRoute = "Management"
                                        targetSubTab = 0
                                    }
                                    Icons.Default.Business.name -> {
                                        currentRoute = "Management"
                                        targetSubTab = 1
                                    }
                                    "WorkerStaff" -> {
                                        currentRoute = "Management"
                                        targetSubTab = 2
                                    }
                                    "BizExpenses" -> {
                                        currentRoute = "Management"
                                        targetSubTab = 3
                                    }
                                    "/owner/delivery" -> {
                                        currentRoute = "Operations"
                                        targetSubTab = 0
                                    }
                                    "/owner/market-price" -> {
                                        currentRoute = "Operations"
                                        targetSubTab = 1
                                    }
                                    "/owner/reports" -> {
                                        currentRoute = "Operations"
                                        targetSubTab = 2
                                    }
                                    "/owner/alerts" -> {
                                        currentRoute = "Operations"
                                        targetSubTab = 3
                                    }
                                }
                            },
                            badge = {
                                if (item.route == "/owner/alerts" && unreadAlertsCount > 0) {
                                    Badge(containerColor = OrangeAlert, contentColor = Color.White) {
                                        Text("$unreadAlertsCount")
                                    }
                                }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = TealNeon,
                                selectedTextColor = OceanDark,
                                unselectedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(46.dp)
                                .testTag("drawer_item_${item.title.replace(" ", "_")}")
                        )
                    }
                }
            }
        },
        gesturesEnabled = true
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = when (currentRoute) {
                                "/owner" -> "BLUE CATCH DASHBOARD"
                                "/owner/purchase" -> "WHOLESALE PURCHASES"
                                "/owner/marketplace" -> "FISH MARKETPLACE"
                                "/owner/stock" -> "STOCK & BOXES"
                                "/owner/sales" -> "SALES BILLING"
                                "/owner/spreadsheets" -> "DIGITAL SPREADSHEETS"
                                "Management" -> "ADMIN CONSOLE"
                                "Operations" -> "OPERATIONAL CENTRE"
                                else -> "Blue Catch ERP"
                            },
                            color = TealNeon,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = TealNeon)
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                currentRoute = "Operations"
                                targetSubTab = 3 // Route directly to Alerts tab
                            }
                        ) {
                            BadgedBox(
                                badge = {
                                    if (unreadAlertsCount > 0) {
                                        Badge(containerColor = OrangeAlert, contentColor = Color.White) {
                                            Text("$unreadAlertsCount")
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = TealNeon)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = OceanSurface),
                    modifier = Modifier.testTag("app_top_bar")
                )
            },
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Primary screen selector matching visual routes
                when (currentRoute) {
                    "/owner" -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToSection = { targetRoute ->
                            when (targetRoute) {
                                "/owner/purchase" -> { currentRoute = "/owner/purchase" }
                                "/owner/marketplace" -> { currentRoute = "/owner/marketplace" }
                                "/owner/stock" -> { currentRoute = "/owner/stock" }
                                "/owner/sales" -> { currentRoute = "/owner/sales" }
                                "/owner/delivery" -> { currentRoute = "Operations"; targetSubTab = 0 }
                                "/owner/reports" -> { currentRoute = "Operations"; targetSubTab = 2 }
                            }
                        }
                    )
                    "/owner/purchase" -> PurchaseScreen(viewModel = viewModel)
                    "/owner/marketplace" -> MarketplaceScreen(viewModel = viewModel)
                    "/owner/stock" -> StockScreen(viewModel = viewModel)
                    "/owner/sales" -> SalesScreen(viewModel = viewModel)
                    "/owner/spreadsheets" -> SpreadsheetScreen(viewModel = viewModel)
                    "Management" -> ManagementScreens(viewModel = viewModel, initialTab = targetSubTab, modifier = Modifier.testTag("management_panel"))
                    "Operations" -> OperationsScreens(viewModel = viewModel, initialTab = targetSubTab, modifier = Modifier.testTag("operations_panel"))
                }
            }
        }
    }
}
