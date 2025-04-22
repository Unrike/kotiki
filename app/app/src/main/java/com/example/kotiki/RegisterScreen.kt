package com.example.kotiki

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
fun RegisterScreen(navController: NavController) {
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    BackHandler(enabled = true) {
        // Блокируем кнопку назад
    }

    fun registerUser(login: String, password: String) {
        isSubmitting = true
        errorMessage = null

        scope.launch(Dispatchers.IO) {
            try {
                val url = URL(url+"register")
                val postData = """{"login":"$login", "password":"$password"}"""

                with(url.openConnection() as HttpURLConnection) {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    outputStream.write(postData.toByteArray())

                    val responseCode = responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // Успешная регистрация
                        Log.d("REGISTER", "Success")
                        withContext(Dispatchers.Main){ navController.navigate("select_user/${login}") }
                    } else {
                        errorMessage = "Провал: $responseCode"
                        Log.e("REGISTER", errorMessage ?: "Unknown error")
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Ошибка подключения: ${e.message}"
                Log.e("REGISTER", "Exception: ${e.message}")
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
        Text("Регистрация", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = login,
            onValueChange = { login = it },
            label = { Text("Логин") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Пароль") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Повторите пароль") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(24.dp))
        errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(12.dp))
        }
        Button(
            onClick = {
                if (password != confirmPassword) {
                    errorMessage = "Пароли не совпадают"
                } else if (login.isBlank() || password.isBlank()) {
                    errorMessage = "Поля заполни"
                } else {
                    registerUser(login, password)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSubmitting
        ) {
            Text(if (isSubmitting) "Делаем." else "Войти")
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = {
            navController.popBackStack()
        }) {
            Text("Уже есть аккаунт? Вам сюда")
        }
    }
}
