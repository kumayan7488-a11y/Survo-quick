package com.example.ui

import com.example.ui.theme.*
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import java.text.SimpleDateFormat
import java.util.*

// Predefined Avatars represented by Icons
val AVATAR_LIST = listOf(
    Icons.Filled.Face to "Cool Explorer",
    Icons.Filled.AccountCircle to "Standard User",
    Icons.Filled.Star to "Pro Earner",
    Icons.Filled.EmojiEmotions to "Happy Saver",
    Icons.Filled.Favorite to "Loyal Member",
    Icons.Filled.Person to "Simple Profile"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(viewModel: AppViewModel) {
    val loggedInUserEmail by viewModel.loggedInUserEmail.collectAsState()
    val loggedInUser by viewModel.loggedInUser.collectAsState()
    val isAdmin by viewModel.isAdmin.collectAsState()

    var currentScreen by remember { mutableStateOf("login") }

    // Navigation trigger based on auth success state
    val authSuccess by viewModel.authSuccess.collectAsState()
    LaunchedEffect(authSuccess) {
        if (authSuccess) {
            if (isAdmin) {
                currentScreen = "admin_dashboard"
            } else {
                currentScreen = "user_dashboard"
            }
        }
    }

    LaunchedEffect(loggedInUserEmail) {
        if (loggedInUserEmail == null) {
            currentScreen = "login"
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                "login" -> LoginScreen(
                    viewModel = viewModel,
                    onNavigateToSignup = { currentScreen = "signup" },
                    onNavigateToForgotPassword = { currentScreen = "forgot_password" }
                )
                "signup" -> SignupScreen(
                    viewModel = viewModel,
                    onNavigateToLogin = { currentScreen = "login" }
                )
                "forgot_password" -> ForgotPasswordScreen(
                    viewModel = viewModel,
                    onNavigateToLogin = { currentScreen = "login" }
                )
                "admin_dashboard" -> AdminDashboardScreen(
                    viewModel = viewModel
                )
                "user_dashboard" -> UserDashboardScreen(
                    viewModel = viewModel,
                    user = loggedInUser
                )
            }
        }
    }
}

// --- AUTHENTICATION SCREENS ---

@Composable
fun AuthBackground(content: @Composable () -> Unit) {
    val primaryColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    val secondaryColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .drawBehind {
                drawCircle(
                    color = primaryColor,
                    radius = size.width * 0.45f,
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.15f, size.height * 0.12f)
                )
                drawCircle(
                    color = secondaryColor,
                    radius = size.width * 0.55f,
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.85f, size.height * 0.78f)
                )
            }
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AppViewModel,
    onNavigateToSignup: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    val authError by viewModel.authError.collectAsState()

    AuthBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.MonetizationOn,
                    contentDescription = "App Logo",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(56.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Survo Quick",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Earn Coins & Redeem Cash Instantly",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome Back",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_email_input"),
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        trailingIcon = {
                            val image = if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            val description = if (showPassword) "Hide password" else "Show password"
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(imageVector = image, contentDescription = description, tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input"),
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true
                    )

                    authError?.let {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Forgot Password?",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .align(Alignment.End)
                            .clickable { onNavigateToForgotPassword() }
                            .padding(4.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.login(email, password) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("login_button"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Login to Dashboard", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Don't have an account? ", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                Text(
                    text = "Sign Up",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.clickable { onNavigateToSignup() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    viewModel: AppViewModel,
    onNavigateToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var selectedAvatarIdx by remember { mutableStateOf(0) }
    var showAvatarPicker by remember { mutableStateOf(false) }

    val authError by viewModel.authError.collectAsState()
    val signupSuccess by viewModel.signupSuccess.collectAsState()

    AuthBackground {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            item {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AddReaction,
                        contentDescription = "Signup Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Create Account",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Join us & grab your 100 coins welcome bonus!",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Choose Your Avatar Profile Icon:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .clickable { showAvatarPicker = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = AVATAR_LIST[selectedAvatarIdx].first,
                                contentDescription = "Selected Avatar",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                        Text(
                            text = AVATAR_LIST[selectedAvatarIdx].second,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            modifier = Modifier.fillMaxWidth().testTag("signup_email"),
                            shape = RoundedCornerShape(16.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            trailingIcon = {
                                val image = if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                                val description = if (showPassword) "Hide password" else "Show password"
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(imageVector = image, contentDescription = description, tint = MaterialTheme.colorScheme.primary)
                                }
                            },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth().testTag("signup_password"),
                            shape = RoundedCornerShape(16.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("First Name") },
                            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = surname,
                            onValueChange = { surname = it },
                            label = { Text("Surname / Last Name") },
                            leadingIcon = { Icon(Icons.Filled.PersonOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = dob,
                            onValueChange = { dob = it },
                            label = { Text("Date of Birth (DD-MM-YYYY)") },
                            leadingIcon = { Icon(Icons.Filled.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true
                        )

                        authError?.let {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                viewModel.signup(email, password, name, surname, dob, selectedAvatarIdx)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("signup_submit_button"),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Register Account", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Already have an account? ", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                    Text(
                        text = "Login",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.clickable { onNavigateToLogin() }
                    )
                }
            }
        }

    // Avatar Picker Dialog
    if (showAvatarPicker) {
        AlertDialog(
            onDismissRequest = { showAvatarPicker = false },
            title = { Text("Select Avatar Profile Icon") },
            text = {
                Column {
                    AVATAR_LIST.forEachIndexed { idx, pair ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedAvatarIdx = idx
                                    showAvatarPicker = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = pair.first,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(pair.second, fontSize = 16.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAvatarPicker = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (signupSuccess) {
        AlertDialog(
            onDismissRequest = { /* Don't dismiss when clicking outside */ },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Email,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            },
            title = {
                Text(
                    text = "Confirmation Email Sent! ✉️",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "A registration confirmation email has been successfully sent to:",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = email,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        textAlign = TextAlign.Center
                    )
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    
                    Text(
                        text = "⚠️ If not found in your Inbox:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Please check your Spam / Junk folder. Sometimes automated emails are filtered there by mistake.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "Steps to find & move from Spam folder:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        listOf(
                            "1. Open your mail app (e.g., Gmail, Yahoo Mail).",
                            "2. Open the side menu and select the 'Spam' or 'Junk' folder.",
                            "3. Look for an email from 'Survo Quick'.",
                            "4. Open the email and select 'Report Not Spam' or 'Move to Inbox' so you never miss reward updates!"
                        ).forEach { step ->
                            Text(
                                text = step,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.confirmSignup()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(100.dp)
                ) {
                    Text("Continue to Dashboard", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    viewModel: AppViewModel,
    onNavigateToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    val authError by viewModel.authError.collectAsState()
    val authSuccess by viewModel.authSuccess.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(authSuccess) {
        if (authSuccess) {
            Toast.makeText(context, "Password updated successfully! Please login.", Toast.LENGTH_LONG).show()
            viewModel.clearAuthStates()
            onNavigateToLogin()
        }
    }

    AuthBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.LockReset,
                    contentDescription = "Reset",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(56.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Reset Password",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Enter your registered email and a new password below.",
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Update Credentials",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Enter New Password") },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        trailingIcon = {
                            val image = if (showPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            val description = if (showPassword) "Hide password" else "Show password"
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(imageVector = image, contentDescription = description, tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true
                    )

                    authError?.let {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.forgotPassword(email, newPassword) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Update Password", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "Back to Login",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.clickable { onNavigateToLogin() }
            )
        }
    }
}


// --- ADMIN PANEL SCREEN ---

@Composable
fun AdminDashboardScreen(viewModel: AppViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Users", "Config", "Withdrawals", "Offers & Redeems")

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Custom Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.AdminPanelSettings,
                    contentDescription = "Admin",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("Admin Panel", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text("kumayan7488@gmail.com", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                }
            }
            IconButton(
                onClick = { viewModel.logout() }
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = Color.White)
            }
        }

        // Tab Navigation
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                0 -> AdminUsersTab(viewModel)
                1 -> AdminConfigTab(viewModel)
                2 -> AdminWithdrawalsTab(viewModel)
                3 -> AdminOffersAndRedeemsTab(viewModel)
            }
        }
    }
}

@Composable
fun AdminUsersTab(viewModel: AppViewModel) {
    val users by viewModel.allUsers.collectAsState()
    var userToReward by remember { mutableStateOf<User?>(null) }
    var rewardCoinsString by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    if (users.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No registered users found.", style = MaterialTheme.typography.bodyMedium)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(users) { user ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                val avatarIcon = AVATAR_LIST.getOrNull(user.avatarId)?.first ?: Icons.Filled.Person
                                Icon(avatarIcon, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("${user.name} ${user.surname}", fontWeight = FontWeight.Bold, fontSize = 16.dp.value.sp)
                                Text(user.email, fontSize = 12.sp, color = Color.Gray)
                                Text("DOB: ${user.dob}", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.MonetizationOn, "Coins", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${user.coins} Coins", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    userToReward = user
                                    rewardCoinsString = ""
                                    showDialog = true
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Reward", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog && userToReward != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Reward Coins") },
            text = {
                Column {
                    Text("Give or subtract coins for ${userToReward!!.name}.", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Use negative sign (-) to deduct coins.", fontSize = 12.sp, color = Color.Red)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = rewardCoinsString,
                        onValueChange = { rewardCoinsString = it },
                        label = { Text("Amount of Coins") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = rewardCoinsString.toIntOrNull()
                        if (amt != null) {
                            viewModel.rewardUserCoins(userToReward!!.email, amt)
                        }
                        showDialog = false
                    }
                ) {
                    Text("Reward")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminConfigTab(viewModel: AppViewModel) {
    val config by viewModel.adminConfig.collectAsState()
    val context = LocalContext.current

    var geminiApiKey by remember { mutableStateOf("") }
    var cpxSurveyLink by remember { mutableStateOf("") }
    var exchangeCoins by remember { mutableStateOf("") }
    var exchangeInr by remember { mutableStateOf("") }
    var adminEmail by remember { mutableStateOf("") }
    var adminPassword by remember { mutableStateOf("") }
    var showAdminPassword by remember { mutableStateOf(false) }

    // Synchronize inputs with saved config
    LaunchedEffect(config) {
        config?.let {
            geminiApiKey = it.geminiApiKey
            cpxSurveyLink = it.cpxSurveyLink
            exchangeCoins = it.exchangeCoins.toString()
            exchangeInr = it.exchangeInr.toString()
            adminEmail = it.adminEmail
            adminPassword = it.adminPassword
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Config Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = geminiApiKey,
                        onValueChange = { geminiApiKey = it },
                        label = { Text("Gemini AI API Key") },
                        leadingIcon = { Icon(Icons.Filled.Key, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Text("Enables Gemini chatbot assistance for users when configured.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(start = 4.dp, top = 4.dp))

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = cpxSurveyLink,
                        onValueChange = { cpxSurveyLink = it },
                        label = { Text("CPX Research Survey Link") },
                        leadingIcon = { Icon(Icons.Filled.Link, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Text("Your referral link. User email will be appended dynamically.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(start = 4.dp, top = 4.dp))

                    Spacer(modifier = Modifier.height(20.dp))
                    Text("Conversion Rate Settings", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = exchangeCoins,
                            onValueChange = { exchangeCoins = it },
                            label = { Text("Coins") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Box(modifier = Modifier.align(Alignment.CenterVertically)) {
                            Text("=", fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        }
                        OutlinedTextField(
                            value = exchangeInr,
                            onValueChange = { exchangeInr = it },
                            label = { Text("INR (₹)") },
                            keyboardOptions = KeyboardOptionsOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    Text("Example: 10 Coins = 10 INR", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(start = 4.dp, top = 4.dp))

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            val coins = exchangeCoins.toIntOrNull() ?: 10
                            val inr = exchangeInr.toIntOrNull() ?: 10
                            viewModel.saveAdminConfig(
                                geminiApiKey = geminiApiKey,
                                cpxSurveyLink = cpxSurveyLink,
                                exchangeCoins = coins,
                                exchangeInr = inr
                            )
                            Toast.makeText(context, "Configurations updated successfully!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Configurations", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Admin Account Settings 🔒", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = adminEmail,
                        onValueChange = { adminEmail = it },
                        label = { Text("Admin Email Address") },
                        leadingIcon = { Icon(Icons.Filled.Email, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    Text("This email address is used for secure Admin panel logins.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(start = 4.dp, top = 4.dp))

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = adminPassword,
                        onValueChange = { adminPassword = it },
                        label = { Text("Admin Password") },
                        leadingIcon = { Icon(Icons.Filled.Lock, null) },
                        trailingIcon = {
                            val image = if (showAdminPassword) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            val description = if (showAdminPassword) "Hide password" else "Show password"
                            IconButton(onClick = { showAdminPassword = !showAdminPassword }) {
                                Icon(imageVector = image, contentDescription = description)
                            }
                        },
                        visualTransformation = if (showAdminPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                    Text("Secure password used to log in as the Admin.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(start = 4.dp, top = 4.dp))

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            if (adminEmail.isBlank() || adminPassword.isBlank()) {
                                Toast.makeText(context, "Admin email and password cannot be empty!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.saveAdminConfig(
                                geminiApiKey = geminiApiKey,
                                cpxSurveyLink = cpxSurveyLink,
                                exchangeCoins = exchangeCoins.toIntOrNull() ?: 10,
                                exchangeInr = exchangeInr.toIntOrNull() ?: 10,
                                adminEmail = adminEmail,
                                adminPassword = adminPassword
                            )
                            Toast.makeText(context, "Admin credentials updated successfully!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Update Admin Credentials", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Custom Helper to prevent keyboard options confusion in Gradle
private fun KeyboardOptionsOptions(keyboardType: KeyboardType) = KeyboardOptions(keyboardType = keyboardType)

@Composable
fun AdminWithdrawalsTab(viewModel: AppViewModel) {
    val transactions by viewModel.allTransactions.collectAsState()
    var selectedTx by remember { mutableStateOf<Transaction?>(null) }
    var refCode by remember { mutableStateOf("") }
    var showApprovalDialog by remember { mutableStateOf(false) }

    if (transactions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No requested withdrawals found.", style = MaterialTheme.typography.bodyMedium)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(transactions) { tx ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tx.redeemOptionTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            // Status tag
                            val isPending = tx.status == "PENDING"
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isPending) Color(0xFFFFF9C4) else Color(0xFFE0F2F1)
                                )
                            ) {
                                Text(
                                    text = tx.status,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPending) Color(0xFFF57F17) else Color(0xFF00796B),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("User: ${tx.userEmail}", fontSize = 13.sp, color = Color.Gray)
                        Text("Details: ${tx.details}", fontSize = 13.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Amount requested: ${tx.coinsAmount} Coins", fontSize = 12.sp)
                                Text("Equivalent cash: ₹${String.format("%.2f", tx.inrAmount)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                            }
                            if (tx.status == "PENDING") {
                                Button(
                                    onClick = {
                                        selectedTx = tx
                                        refCode = ""
                                        showApprovalDialog = true
                                    },
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text("Approve", fontSize = 13.sp)
                                }
                            }
                        }

                        if (tx.status == "APPROVED" && tx.code.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Transaction Code: ${tx.code}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                        }
                    }
                }
            }
        }
    }

    if (showApprovalDialog && selectedTx != null) {
        AlertDialog(
            onDismissRequest = { showApprovalDialog = false },
            title = { Text("Approve Withdrawal") },
            text = {
                Column {
                    Text("Approve withdrawal of ₹${selectedTx!!.inrAmount} for ${selectedTx!!.userEmail}.", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = refCode,
                        onValueChange = { refCode = it },
                        label = { Text("Enter Reference / Voucher Code") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("This code will be visible to the user below the transaction history card, and they can copy-paste it directly.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(start = 4.dp, top = 4.dp))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.approveTransaction(selectedTx!!.id, refCode)
                        showApprovalDialog = false
                    },
                    enabled = refCode.isNotBlank()
                ) {
                    Text("Complete & Approve")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApprovalDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AdminOffersAndRedeemsTab(viewModel: AppViewModel) {
    val tasks by viewModel.allTasks.collectAsState()
    val redeemOptions by viewModel.redeemOptions.collectAsState()

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddRedeemDialog by remember { mutableStateOf(false) }

    var taskTitle by remember { mutableStateOf("") }
    var taskDesc by remember { mutableStateOf("") }
    var taskCoins by remember { mutableStateOf("") }
    var taskCat by remember { mutableStateOf("Hot Offer") }

    var redeemTitle by remember { mutableStateOf("") }
    var redeemCoins by remember { mutableStateOf("") }
    var redeemType by remember { mutableStateOf("UPI Payment") }
    var redeemHint by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Offer Cards / Columns", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                IconButton(onClick = {
                    taskTitle = ""
                    taskDesc = ""
                    taskCoins = ""
                    taskCat = "Hot Offer"
                    showAddTaskDialog = true
                }) {
                    Icon(Icons.Filled.AddCircle, "Add Task", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(tasks) { task ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(task.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(task.description, fontSize = 11.sp, color = Color.DarkGray, maxLines = 1)
                        Text("Category: ${task.category} | Reward: ${task.coinsReward} Coins", fontSize = 11.sp, color = Color.Gray)
                    }
                    IconButton(onClick = { viewModel.adminDeleteTask(task) }) {
                        Icon(Icons.Filled.Delete, "Delete", tint = Color.Red, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Redeem Cashout Options", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                IconButton(onClick = {
                    redeemTitle = ""
                    redeemCoins = ""
                    redeemType = "UPI Payment"
                    redeemHint = ""
                    showAddRedeemDialog = true
                }) {
                    Icon(Icons.Filled.AddCircle, "Add Redeem Option", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(redeemOptions) { option ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(option.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Type: ${option.paymentType} | Required: ${option.coinsRequired} Coins", fontSize = 11.sp, color = Color.Gray)
                        Text("Input Label: ${option.inputHint}", fontSize = 11.sp, color = Color.DarkGray)
                    }
                    IconButton(onClick = { viewModel.deleteRedeemOption(option) }) {
                        Icon(Icons.Filled.Delete, "Delete", tint = Color.Red, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }

    // Add Task Dialog
    if (showAddTaskDialog) {
        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = { Text("Add Offer/Task") },
            text = {
                Column {
                    OutlinedTextField(
                        value = taskTitle,
                        onValueChange = { taskTitle = it },
                        label = { Text("Task Heading/Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = taskDesc,
                        onValueChange = { taskDesc = it },
                        label = { Text("Task Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = taskCoins,
                        onValueChange = { taskCoins = it },
                        label = { Text("Reward Coins (e.g. 40)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Category Tag:")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Hot Offer", "Survey", "Play & Earn").forEach { cat ->
                            FilterChip(
                                selected = taskCat == cat,
                                onClick = { taskCat = cat },
                                label = { Text(cat, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val rw = taskCoins.toIntOrNull() ?: 10
                        viewModel.adminAddTask(taskTitle, taskDesc, rw, taskCat)
                        showAddTaskDialog = false
                    },
                    enabled = taskTitle.isNotBlank() && taskDesc.isNotBlank() && taskCoins.isNotBlank()
                ) {
                    Text("Add Offer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Redeem Dialog
    if (showAddRedeemDialog) {
        AlertDialog(
            onDismissRequest = { showAddRedeemDialog = false },
            title = { Text("Add Redeem Option") },
            text = {
                Column {
                    OutlinedTextField(
                        value = redeemTitle,
                        onValueChange = { redeemTitle = it },
                        label = { Text("Option Name (e.g. UPI Express)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = redeemCoins,
                        onValueChange = { redeemCoins = it },
                        label = { Text("Coins Required (e.g. 200)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = redeemHint,
                        onValueChange = { redeemHint = it },
                        label = { Text("User Input Hint (e.g. Enter UPI ID)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Payout Network Type:")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("UPI Payment", "Paytm Wallet", "Amazon Voucher").forEach { type ->
                            FilterChip(
                                selected = redeemType == type,
                                onClick = { redeemType = type },
                                label = { Text(type, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cr = redeemCoins.toIntOrNull() ?: 100
                        viewModel.addRedeemOption(redeemTitle, cr, redeemType, redeemHint)
                        showAddRedeemDialog = false
                    },
                    enabled = redeemTitle.isNotBlank() && redeemCoins.isNotBlank() && redeemHint.isNotBlank()
                ) {
                    Text("Add Option")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddRedeemDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}


// --- USER PANEL AND TABS ---

@Composable
fun UserDashboardScreen(viewModel: AppViewModel, user: User?) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Home", "Redeem", "History", "Help Chat", "Profile")

    val config by viewModel.adminConfig.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Main Bento Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val avatarIcon = AVATAR_LIST.getOrNull(user?.avatarId ?: 0)?.first ?: Icons.Filled.Person
                    Icon(avatarIcon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Survo Quick",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "WELCOME, ${user?.name?.uppercase() ?: "USER"}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Coin Balance Display Bento Pill
            val rupeeVal = if (user != null && config != null) {
                user.coins.toFloat() * config!!.exchangeInr / config!!.exchangeCoins
            } else {
                0.0f
            }
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🪙", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${user?.coins ?: 0} (≈ ₹${String.format("%.2f", rupeeVal)})",
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        modifier = Modifier.testTag("coin_balance_text")
                    )
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (selectedTab) {
                0 -> UserHomeTab(viewModel, user, onTabSelect = { selectedTab = it })
                1 -> UserRedeemTab(viewModel, user)
                2 -> UserHistoryTab(viewModel)
                3 -> UserHelpTab(viewModel)
                4 -> UserProfileTab(viewModel, user)
            }
        }

        // Bottom Navigation Bar
        NavigationBar(
            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            val icons = listOf(
                Icons.Filled.Home,
                Icons.Filled.AccountBalanceWallet,
                Icons.Filled.History,
                Icons.AutoMirrored.Filled.Help,
                Icons.Filled.AccountCircle
            )
            tabs.forEachIndexed { index, label ->
                NavigationBarItem(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    icon = { Icon(icons[index], contentDescription = label) },
                    label = { Text(label, fontSize = 10.sp) }
                )
            }
        }
    }
}

fun Modifier.dashedBorder(width: Dp, color: Color, cornerRadius: Dp) = this.drawWithContent {
    drawContent()
    val stroke = Stroke(
        width = width.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
    )
    drawRoundRect(
        color = color,
        style = stroke,
        cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
    )
}

@Composable
fun UserHomeTab(viewModel: AppViewModel, user: User?, onTabSelect: (Int) -> Unit) {
    val tasks by viewModel.availableTasks.collectAsState()
    val config by viewModel.adminConfig.collectAsState()
    val context = LocalContext.current

    var selectedTask by remember { mutableStateOf<Task?>(null) }
    var showTaskDialog by remember { mutableStateOf(false) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // CPX Survey Section Banner if link exists
        config?.let { adminConf ->
            if (adminConf.cpxSurveyLink.isNotBlank()) {
                item(span = { GridItemSpan(2) }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val emailSuffix = user?.email ?: ""
                                val surveyUrl = "${adminConf.cpxSurveyLink}$emailSuffix"
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(surveyUrl))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast
                                        .makeText(
                                            context,
                                            "Could not open survey link. Invalid URL format.",
                                            Toast.LENGTH_SHORT
                                        )
                                        .show()
                                }
                            },
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    ),
                                    shape = RoundedCornerShape(28.dp)
                                )
                                .padding(24.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color.White.copy(alpha = 0.22f))
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Poll,
                                        contentDescription = "Surveys",
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Surface(
                                    color = Color.White.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(100.dp)
                                ) {
                                    Text(
                                        text = "HOT OFFER",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "CPX Research Panel",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Complete surveys and earn high coin payouts instantly",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    val emailSuffix = user?.email ?: ""
                                    val surveyUrl = "${adminConf.cpxSurveyLink}$emailSuffix"
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(surveyUrl))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Could not open survey link.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.White,
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(100.dp),
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 12.dp)
                            ) {
                                Text(
                                    text = "Start Earning Now",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Available Tasks Title
        item(span = { GridItemSpan(2) }) {
            Text(
                text = "Available Offers & Tasks",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
            )
        }

        if (tasks.isEmpty()) {
            item(span = { GridItemSpan(2) }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Filled.CardGiftcard, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("All tasks completed or failed!", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Text("Check back tomorrow for fresh columns & rewarding offers.", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
                    }
                }
            }
        } else {
            items(tasks) { task ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(155.dp)
                        .clickable {
                            selectedTask = task
                            showTaskDialog = true
                        },
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        val emoji = when {
                            task.category.contains("Game", true) -> "🎮"
                            task.category.contains("App", true) || task.category.contains("Install", true) -> "📱"
                            task.category.contains("Video", true) || task.category.contains("Watch", true) || task.category.contains("Ad", true) -> "📺"
                            task.category.contains("Survey", true) || task.category.contains("Poll", true) -> "📋"
                            else -> "🎁"
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(emoji, fontSize = 28.sp)
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(100.dp)
                            ) {
                                Text(
                                    text = task.category.uppercase(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        
                        Column {
                            Text(
                                text = task.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "🪙",
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${task.coinsReward}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "COINS",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Standard Bento "More Tasks" slot to fill up or enhance
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(145.dp)
                    .dashedBorder(1.5.dp, MaterialTheme.colorScheme.primary, 24.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .clickable {
                        Toast.makeText(context, "Stay tuned for more daily rewards!", Toast.LENGTH_SHORT).show()
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "More Tasks",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "MORE TASKS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // Gemini AI Help Gradient Bento Box
        item(span = { GridItemSpan(2) }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(GeminiBlueStart, GeminiBlueEnd)
                        )
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = "Gemini Help",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Gemini AI Help",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Ask anything to our assistant",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
                Button(
                    onClick = { onTabSelect(3) }, // Switch to Chat Help tab (index 3)
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = GeminiBlueStart
                    ),
                    shape = RoundedCornerShape(100.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "CHAT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }

    if (showTaskDialog && selectedTask != null) {
        AlertDialog(
            onDismissRequest = { showTaskDialog = false },
            title = { Text(selectedTask!!.title, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Instruction:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(selectedTask!!.description, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                            .padding(12.dp)
                    ) {
                        Icon(Icons.Filled.MonetizationOn, null, tint = AmberAccent, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reward amount: upto ${selectedTask!!.coinsReward} coins", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.completeTask(selectedTask!!.id, true) { success ->
                            if (success) {
                                Toast.makeText(context, "Task completed successfully! ${selectedTask!!.coinsReward} coins added.", Toast.LENGTH_SHORT).show()
                            }
                        }
                        showTaskDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Complete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.completeTask(selectedTask!!.id, false) { success ->
                            if (success) {
                                Toast.makeText(context, "Task marked as failed. Offer removed.", Toast.LENGTH_SHORT).show()
                            }
                        }
                        showTaskDialog = false
                    }
                ) {
                    Text("Mark Fail", color = Color.Red)
                }
            }
        )
    }
}

@Composable
fun UserRedeemTab(viewModel: AppViewModel, user: User?) {
    val redeemOptions by viewModel.redeemOptions.collectAsState()
    val context = LocalContext.current

    var selectedOption by remember { mutableStateOf<RedeemOption?>(null) }
    var userInputDetails by remember { mutableStateOf("") }
    var showRedeemDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Redeem Coin Balance",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (redeemOptions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                Text("No redeem options set by Admin yet.", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                items(redeemOptions) { option ->
                    val userHasCoins = (user?.coins ?: 0) >= option.coinsRequired

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                        colors = CardDefaults.cardColors(
                            containerColor = if (userHasCoins) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                val payoutIcon = when {
                                    option.paymentType.contains("UPI", true) -> Icons.Filled.AccountBalance
                                    option.paymentType.contains("Paytm", true) -> Icons.Filled.AccountBalanceWallet
                                    else -> Icons.Filled.CardGiftcard
                                }
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(payoutIcon, null, tint = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(option.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Payout Network: ${option.paymentType}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Cost: ${option.coinsRequired} Coins", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    selectedOption = option
                                    userInputDetails = ""
                                    showRedeemDialog = true
                                },
                                enabled = userHasCoins,
                                shape = RoundedCornerShape(100.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                                modifier = Modifier.height(38.dp)
                            ) {
                                Text("Redeem", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRedeemDialog && selectedOption != null) {
        val config by viewModel.adminConfig.collectAsState()
        val cashVal = selectedOption!!.coinsRequired.toFloat() * (config?.exchangeInr ?: 10) / (config?.exchangeCoins ?: 10)

        AlertDialog(
            onDismissRequest = { showRedeemDialog = false },
            title = { Text(selectedOption!!.title) },
            text = {
                Column {
                    Text("Redeem ${selectedOption!!.coinsRequired} coins for ₹${String.format("%.2f", cashVal)} instantly.", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = userInputDetails,
                        onValueChange = { userInputDetails = it },
                        label = { Text(selectedOption!!.inputHint) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Note: Funds are normally dispatched within 24 hours. Your history status will remain pending until approved by admin.", fontSize = 11.sp, color = Color.Gray)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.requestWithdrawal(selectedOption!!, userInputDetails) { success ->
                            if (success) {
                                Toast.makeText(context, "Withdrawal request submitted! Coins deducted.", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Withdrawal failed. Insufficient coin balance.", Toast.LENGTH_SHORT).show()
                            }
                        }
                        showRedeemDialog = false
                    },
                    enabled = userInputDetails.isNotBlank()
                ) {
                    Text("Confirm Cashout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRedeemDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun UserHistoryTab(viewModel: AppViewModel) {
    val transactions by viewModel.userTransactions.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Withdrawal Transactions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (transactions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                Text("You haven't requested any withdrawals yet.", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                items(transactions) { tx ->
                    val isPending = tx.status == "PENDING"
                    val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
                    val dateStr = sdf.format(Date(tx.timestamp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isPending) Icons.Filled.HourglassEmpty else Icons.Filled.CheckCircle,
                                        contentDescription = tx.status,
                                        tint = if (isPending) Color(0xFFF57F17) else Color(0xFF00796B),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(tx.redeemOptionTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isPending) Color(0xFFFFF9C4) else Color(0xFFE0F2F1)
                                    )
                                ) {
                                    Text(
                                        text = tx.status,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPending) Color(0xFFF57F17) else Color(0xFF00796B),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Payment Details: ${tx.details}", fontSize = 13.sp, color = Color.DarkGray)
                            Text("Requested on: $dateStr", fontSize = 11.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${tx.coinsAmount} Coins", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text("Value: ₹${String.format("%.2f", tx.inrAmount)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 15.sp)
                            }

                            // Copyable reference code/voucher displayed if Approved!
                            if (tx.status == "APPROVED" && tx.code.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text("Approval Reference Code:", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = tx.code,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = SuccessGreen,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = Icons.Filled.ContentCopy,
                                        contentDescription = "Copy Reference Code",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clickable {
                                                val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                val clip = ClipData.newPlainText("Transaction Code", tx.code)
                                                clipboardManager.setPrimaryClip(clip)
                                                Toast.makeText(context, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                                            }
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

@Composable
fun UserHelpTab(viewModel: AppViewModel) {
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isChatLoading by viewModel.isChatLoading.collectAsState()
    val config by viewModel.adminConfig.collectAsState()

    var userMessageText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = "AI",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Live AI Help Assistant",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        val hasKey = !config?.geminiApiKey.isNullOrBlank() || (com.example.BuildConfig.GEMINI_API_KEY.isNotBlank() && com.example.BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY")

        if (!hasKey) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.Warning, null, tint = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Gemini Support Unavailable",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "The AI Help section is deactivated because the Admin has not yet configured the Gemini API Key. Please notify the Administrator.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
        } else {
            // Chat Message List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                reverseLayout = false
            ) {
                items(chatMessages) { message ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Card(
                            shape = RoundedCornerShape(
                                topStart = 20.dp,
                                topEnd = 20.dp,
                                bottomStart = if (message.isUser) 20.dp else 4.dp,
                                bottomEnd = if (message.isUser) 4.dp else 20.dp
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (message.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                            ),
                            border = if (!message.isUser) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)) else null,
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Text(
                                text = message.text,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                color = if (message.isUser) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                if (isChatLoading) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 1.5.dp, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("AI is generating helper response...", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Chat Input Panel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = userMessageText,
                    onValueChange = { userMessageText = it },
                    placeholder = { Text("Ask anything about Survo Quick...", fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        viewModel.sendChatMessage(userMessageText)
                        userMessageText = ""
                    },
                    enabled = userMessageText.isNotBlank() && !isChatLoading,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (userMessageText.isNotBlank() && !isChatLoading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send, 
                        contentDescription = "Send", 
                        tint = if (userMessageText.isNotBlank() && !isChatLoading) Color.White else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileTab(viewModel: AppViewModel, user: User?) {
    var isEditing by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var selectedAvatarIdx by remember { mutableStateOf(0) }
    var showAvatarPicker by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(user, isEditing) {
        user?.let {
            name = it.name
            surname = it.surname
            dob = it.dob
            selectedAvatarIdx = it.avatarId
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable(enabled = isEditing) { showAvatarPicker = true },
                contentAlignment = Alignment.Center
            ) {
                val iconSelected = AVATAR_LIST.getOrNull(selectedAvatarIdx)?.first ?: Icons.Filled.Person
                Icon(
                    imageVector = iconSelected,
                    contentDescription = "Avatar",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(64.dp)
                )
                if (isEditing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Edit, "Edit Avatar", tint = Color.White)
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "${user?.name ?: "User"} ${user?.surname ?: ""}",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
            Text(
                text = user?.email ?: "",
                fontSize = 13.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Profile Details", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    if (isEditing) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("First Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = surname,
                            onValueChange = { surname = it },
                            label = { Text("Surname / Last Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = dob,
                            onValueChange = { dob = it },
                            label = { Text("Date of Birth") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { isEditing = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel")
                            }
                            Button(
                                onClick = {
                                    viewModel.updateProfile(name, surname, dob, selectedAvatarIdx)
                                    isEditing = false
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Save Info")
                            }
                        }
                    } else {
                        // Display view
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                            Text("First Name: ", fontWeight = FontWeight.SemiBold, modifier = Modifier.width(120.dp))
                            Text(user?.name ?: "—")
                        }
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                            Text("Surname: ", fontWeight = FontWeight.SemiBold, modifier = Modifier.width(120.dp))
                            Text(user?.surname ?: "—")
                        }
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                            Text("Date of Birth: ", fontWeight = FontWeight.SemiBold, modifier = Modifier.width(120.dp))
                            Text(user?.dob ?: "—")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { isEditing = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Edit, "Edit")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Edit Profile Details")
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedButton(
                onClick = { viewModel.logout() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.DarkGray)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout Account")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { showDeleteConfirmation = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Filled.DeleteForever, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete Account Permanently")
            }
        }
    }

    // Edit Profile Avatar Selection Dialog
    if (showAvatarPicker) {
        AlertDialog(
            onDismissRequest = { showAvatarPicker = false },
            title = { Text("Select Profile Avatar Icon") },
            text = {
                Column {
                    AVATAR_LIST.forEachIndexed { idx, pair ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedAvatarIdx = idx
                                    showAvatarPicker = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = pair.first,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(pair.second, fontSize = 16.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAvatarPicker = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Permanent Delete Confirmation Dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Account Permanently?") },
            text = {
                Text("Are you absolutely sure you want to permanently delete your account? This action is irreversible, and all your earned coins, tasks, and withdrawal history will be deleted forever.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAccountPermanent()
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
