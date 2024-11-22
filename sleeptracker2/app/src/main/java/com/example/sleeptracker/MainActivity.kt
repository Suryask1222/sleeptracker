package com.example.sleeptracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SleepTrackerAppTheme {
                SleepTrackerApp()
            }
        }
    }
}

// Main App Composable
@Composable
fun SleepTrackerApp() {
    var isLoggedIn by remember { mutableStateOf(false) }
    var isRegistering by remember { mutableStateOf(false) }

    when {
        isLoggedIn -> SleepTrackerDashboard(onLogout = { isLoggedIn = false })
        isRegistering -> RegisterScreen(
            onRegisterSuccess = { isRegistering = false },
            onBackClick = { isRegistering = false }
        )
        else -> LoginScreen(
            onLoginSuccess = { isLoggedIn = true },
            onRegisterClick = { isRegistering = true }
        )
    }
}

// Login Screen
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, onRegisterClick: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    val auth = FirebaseAuth.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🌙 Sleep Tracker",
            fontSize = 32.sp,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                keyboardController?.hide()
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onLoginSuccess()
                        }
                    }
            })
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onLoginSuccess()
                    }
                }
        }) {
            Text("Login")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = onRegisterClick, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
            Text("Register")
        }
    }
}

// Register Screen
@Composable
fun RegisterScreen(onRegisterSuccess: () -> Unit, onBackClick: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val auth = FirebaseAuth.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Register", fontSize = 24.sp, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        onRegisterSuccess()
                    }
                }
        }) {
            Text("Register")
        }
        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onBackClick) {
            Text("Back to Login")
        }
    }
}

// Sleep Tracker Dashboard
@Composable
fun SleepTrackerDashboard(onLogout: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance().reference

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to Sleep Tracker!",
            fontSize = 28.sp,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            val sleepData = mapOf("startTime" to System.currentTimeMillis())
            database.child("users").child(auth.currentUser?.uid ?: "unknown").setValue(sleepData)
        }) {
            Text("Start Sleep Tracking")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            val wakeUpData = mapOf("endTime" to System.currentTimeMillis())
            database.child("users").child(auth.currentUser?.uid ?: "unknown").updateChildren(wakeUpData)
        }) {
            Text("Stop Sleep Tracking")
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            auth.signOut()
            onLogout()
        }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
            Text("Logout")
        }
    }
}

// Theme
@Composable
fun SleepTrackerAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF6200EE),
            onPrimary = Color.White,
            secondary = Color(0xFF03DAC6),
            background = Color(0xFFF6F6F6),
            surface = Color.White
        ),
        content = content
    )
}
