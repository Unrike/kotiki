package com.example.kotiki

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun LoginScreen(navController: NavController) {
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    BackHandler(enabled = true) {
    }

    fun loginUser(email: String, password: String) {
        isSubmitting = true
        errorMessage = null

        scope.launch(Dispatchers.IO) {
            try {
                val url = URL(url+"login")
                val postData = """{"login":"$login", "password":"$password"}"""

                with(url.openConnection() as HttpURLConnection) {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    outputStream.write(postData.toByteArray())

                    val responseCode = responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Log.d("LOGIN", "Login successful")
                        withContext(Dispatchers.Main){ navController.navigate("select_user/${login}") }
                        // navController.navigate("home")
                    } else {
                        errorMessage = "Не удалось: $responseCode"
                        Log.e("LOGIN", errorMessage ?: "Unknown error")
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Ошибка: ${e.message}"
                Log.e("LOGIN", "Exception: ${e.message}")
            } finally {
                isSubmitting = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Логин", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = login,
            onValueChange = { login = it },
            label = { Text("Логин") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(24.dp))

        errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = {
                if (login.isBlank() || password.isBlank()) {
                    errorMessage = "Поля заполни"
                } else {
                    loginUser(login, password)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSubmitting
        ) {
            Text(if (isSubmitting) "Делаем..." else "Войти")
        }


        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = {
            navController.navigate("register")
        }) {
            Text("Ещё не зарегистрированы? Вам сюда!")
        }
    }
}