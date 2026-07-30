package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.FishViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreens(
    viewModel: FishViewModel,
    modifier: Modifier = Modifier
) {
    var isSignUp by remember { mutableStateOf(false) }
    var selectedRoleTab by remember { mutableIntStateOf(0) } // 0 = Owner, 1 = Worker/Staff

    // Input States
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var pinCode by remember { mutableStateOf("") }
    var salary by remember { mutableStateOf("") }
    var assignedRole by remember { mutableStateOf("Staff") } // "Manager", "Staff", "Delivery Boy"

    var errorMsg by remember { mutableStateOf<String?>(null) }
    var successMsg by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }

    // Clear messages when tab or state changes
    LaunchedEffect(isSignUp, selectedRoleTab) {
        errorMsg = null
        successMsg = null
        fullName = ""
        email = ""
        phone = ""
        password = ""
        pinCode = ""
        salary = ""
        assignedRole = "Staff"
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OceanDark)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Image Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_login_banner),
                    contentDescription = "Ocean Marine Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Gradient Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(Color.Transparent, OceanDark),
                                startY = 100f
                            )
                        )
                )

                // App Branding Text on Banner
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Blue Catch ERP",
                        color = TealNeon,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Marine Port Supply & Enterprise Console",
                        color = TextWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Authentication Card
            Card(
                colors = CardDefaults.cardColors(containerColor = OceanSurface),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("auth_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header text: Login or Sign Up
                    Text(
                        text = if (isSignUp) "Create Account" else "Account Sign In",
                        color = TealNeon,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Role Tabs Selector: Owner vs Worker
                    TabRow(
                        selectedTabIndex = selectedRoleTab,
                        containerColor = OceanCard,
                        contentColor = TealNeon,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedRoleTab]),
                                color = TealNeon
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                    ) {
                        Tab(
                            selected = selectedRoleTab == 0,
                            onClick = { selectedRoleTab = 0 },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.BusinessCenter, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Text("Owner", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            },
                            selectedContentColor = TealNeon,
                            unselectedContentColor = TextSecondary,
                            modifier = Modifier.testTag("owner_role_tab")
                        )
                        Tab(
                            selected = selectedRoleTab == 1,
                            onClick = { selectedRoleTab = 1 },
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Engineering, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Text("Worker", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            },
                            selectedContentColor = TealNeon,
                            unselectedContentColor = TextSecondary,
                            modifier = Modifier.testTag("worker_role_tab")
                        )
                    }



                    // Error Message
                    AnimatedVisibility(
                        visible = errorMsg != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CoralRed.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = "Error", tint = CoralRed, modifier = Modifier.size(18.dp))
                            Text(text = errorMsg ?: "", color = CoralRed, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    // Success Message
                    AnimatedVisibility(
                        visible = successMsg != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(TealNeon.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = TealNeon, modifier = Modifier.size(18.dp))
                            Text(text = successMsg ?: "", color = TealNeon, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    // --- INPUT FIELDS SECTION ---
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (isSignUp) {
                            // SIGN UP FORM
                            OutlinedTextField(
                                value = fullName,
                                onValueChange = { fullName = it; errorMsg = null },
                                label = { Text("Full Name") },
                                textStyle = LocalTextStyle.current.copy(color = TextWhite),
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = TealNeon) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("signup_name_input")
                            )

                            if (selectedRoleTab == 0) {
                                // Owner Sign Up Fields
                                OutlinedTextField(
                                    value = email,
                                    onValueChange = { email = it; errorMsg = null },
                                    label = { Text("Email Address") },
                                    textStyle = LocalTextStyle.current.copy(color = TextWhite),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TealNeon) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("signup_email_input")
                                )
                                OutlinedTextField(
                                    value = phone,
                                    onValueChange = { phone = it; errorMsg = null },
                                    label = { Text("Phone Number") },
                                    textStyle = LocalTextStyle.current.copy(color = TextWhite),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = TealNeon) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it; errorMsg = null },
                                    label = { Text("Set Password") },
                                    textStyle = LocalTextStyle.current.copy(color = TextWhite),
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TealNeon) },
                                    trailingIcon = {
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(
                                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                                tint = TextSecondary
                                            )
                                        }
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("signup_password_input")
                                )
                            } else {
                                // Worker Sign Up Fields
                                OutlinedTextField(
                                    value = phone,
                                    onValueChange = { phone = it; errorMsg = null },
                                    label = { Text("Phone Number") },
                                    textStyle = LocalTextStyle.current.copy(color = TextWhite),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = TealNeon) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = pinCode,
                                    onValueChange = { pinCode = it; errorMsg = null },
                                    label = { Text("4-Digit Login PIN") },
                                    textStyle = LocalTextStyle.current.copy(color = TextWhite),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null, tint = TealNeon) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        } else {
                            // LOGIN FORM
                            if (selectedRoleTab == 0) {
                                // Owner Login Inputs
                                OutlinedTextField(
                                    value = email,
                                    onValueChange = { email = it; errorMsg = null },
                                    label = { Text("Owner Email or Phone") },
                                    textStyle = LocalTextStyle.current.copy(color = TextWhite),
                                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = TealNeon) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("login_email_input")
                                )
                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it; errorMsg = null },
                                    label = { Text("Password") },
                                    textStyle = LocalTextStyle.current.copy(color = TextWhite),
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = TealNeon) },
                                    trailingIcon = {
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(
                                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                                tint = TextSecondary
                                            )
                                        }
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("login_password_input")
                                )
                            } else {
                                // Worker Login Inputs
                                OutlinedTextField(
                                    value = phone,
                                    onValueChange = { phone = it; errorMsg = null },
                                    label = { Text("Registered Phone Number") },
                                    textStyle = LocalTextStyle.current.copy(color = TextWhite),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = TealNeon) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("login_worker_phone_input")
                                )
                                OutlinedTextField(
                                    value = pinCode,
                                    onValueChange = { pinCode = it; errorMsg = null },
                                    label = { Text("4-Digit Access PIN") },
                                    textStyle = LocalTextStyle.current.copy(color = TextWhite),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    visualTransformation = PasswordVisualTransformation(),
                                    leadingIcon = { Icon(Icons.Default.LockOpen, contentDescription = null, tint = TealNeon) },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("login_worker_pin_input")
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // ACTION SUBMIT BUTTON
                    Button(
                        onClick = {
                            if (isSignUp) {
                                // Handle Sign Up
                                if (fullName.isBlank()) {
                                    errorMsg = "Please fill in all basic details"
                                    return@Button
                                }
                                if (selectedRoleTab == 0) {
                                    if (email.isBlank() || password.isBlank() || phone.isBlank()) {
                                        errorMsg = "All owner details are required"
                                        return@Button
                                    }
                                    if (!email.contains("@")) {
                                        errorMsg = "Please enter a valid email address"
                                        return@Button
                                    }
                                    
                                    val isPasswordValid = password.length >= 4 &&
                                            password[0] in 'A'..'Z' &&
                                            password.substring(1).all { it in 'a'..'z' || it in '0'..'9' || (!it.isLetterOrDigit()) } &&
                                            password.substring(1).any { it in 'a'..'z' } &&
                                            password.substring(1).any { !it.isLetterOrDigit() } &&
                                            password.substring(1).any { it in '0'..'9' }

                                    if (!isPasswordValid) {
                                        errorMsg = "Password must start with an uppercase letter, followed by lowercase letters, digits, and special characters (must include at least one lowercase, one digit, and one special character)."
                                        return@Button
                                    }
                                    
                                    val success = viewModel.signUpOwner(fullName, email, phone, password)
                                    if (success) {
                                        successMsg = "Registration successful! Welcome owner."
                                    } else {
                                        errorMsg = "Email already registered as Owner!"
                                    }
                                } else {
                                    if (phone.isBlank() || pinCode.isBlank()) {
                                        errorMsg = "All worker fields are required"
                                        return@Button
                                    }
                                    if (pinCode.length != 4) {
                                        errorMsg = "Login PIN must be exactly 4 digits"
                                        return@Button
                                    }
                                    viewModel.addWorker(fullName, "Staff", 0.0, phone, pinCode)
                                    successMsg = "Worker registered! Toggle to Login tab to sign in."
                                    isSignUp = false // switch to login so they can login
                                }
                            } else {
                                // Handle Login
                                if (selectedRoleTab == 0) {
                                    if (email.isBlank() || password.isBlank()) {
                                        errorMsg = "Please enter email and password"
                                        return@Button
                                    }
                                    val success = viewModel.loginAsOwner(email, password)
                                    if (!success) {
                                        errorMsg = "Invalid email or password."
                                    }
                                } else {
                                    if (phone.isBlank() || pinCode.isBlank()) {
                                        errorMsg = "Please enter phone and 4-digit PIN"
                                        return@Button
                                    }
                                    val success = viewModel.loginAsWorker(phone, pinCode)
                                    if (!success) {
                                        errorMsg = "Worker PIN matching failed."
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealNeon, contentColor = OceanDark),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("auth_submit_button")
                    ) {
                        Text(
                            text = if (isSignUp) "Complete Registration" else "Verify & Continue",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "— OR —",
                        color = TextSecondary.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    OutlinedButton(
                        onClick = { viewModel.loginAsGuest() },
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp, brush = androidx.compose.ui.graphics.SolidColor(TealNeon.copy(alpha = 0.5f))),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TealNeon),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("browse_as_guest_button")
                    ) {
                        Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Browse Fresh Catch Marketplace",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Toggles between Sign Up and Login
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isSignUp) "Already have an account? " else "Need to register? ",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = if (isSignUp) "Sign In" else "Sign Up Now",
                            color = TealNeon,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable { isSignUp = !isSignUp }
                                .testTag("auth_mode_toggle_text")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun RestrictedAccessView(featureName: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = OceanSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Restricted",
                    tint = CoralRed,
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = "Access Restricted",
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Your current staff role does not have authorization to access $featureName.\n\nPlease contact the Business Owner or Manager to request permission.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
