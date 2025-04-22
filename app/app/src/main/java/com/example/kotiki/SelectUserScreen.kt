package com.example.kotiki

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectUserScreen(navController: NavController, currentUser: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedUser by remember { mutableStateOf<String?>(null) }
    var users by remember { mutableStateOf(listOf<String>()) }
    var selectedImage by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }

    val imageList = listOf("waa", "be", "zzz", "mono", "mya", "poker")
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        try {
            val url = URL(url+"users/$currentUser")
            withContext(Dispatchers.IO) {
                val connection = url.openConnection() as HttpURLConnection
                val response = connection.inputStream.bufferedReader().readText()
                val json = JSONArray(response)
                val result = mutableListOf<String>()
                for (i in 0 until json.length()) {
                    result.add(json.getString(i))
                }
                users = result
            }
        } catch (e: Exception) {
            Log.e("SelectUserScreen", "Error: ${e.message}")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Выбор получателя") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("login") {
                            popUpTo("select_user/{$currentUser}") { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate("messages/$currentUser")
                    }) {
                        Icon(painter = painterResource(R.drawable.mail), contentDescription = "Сообщения")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = selectedUser ?: "",
                    onValueChange = {},
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    readOnly = true,
                    label = { Text("Выберите пользователя") }
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    users.forEach { user ->
                        DropdownMenuItem(
                            text = { Text(user) },
                            onClick = {
                                selectedUser = user
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedUser != null) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(imageList.chunked(3)) { rowImages ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            rowImages.forEach { imageName ->
                                val drawableId = when (imageName) {
                                    "waa" -> R.drawable.waa
                                    "be" -> R.drawable.be
                                    "zzz" -> R.drawable.zzz
                                    "mono" -> R.drawable.mono
                                    "mya" -> R.drawable.mya
                                    "poker" -> R.drawable.poker
                                    else -> R.drawable.poker
                                }

                                Card(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clickable { selectedImage = imageName },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selectedImage == imageName)
                                            Color(0xFFBBDEFB) else Color(0xFFF0F0F0)
                                    )
                                ) {
                                    Image(
                                        painter = painterResource(id = drawableId),
                                        contentDescription = imageName,
                                        modifier = Modifier.fillMaxSize().padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        selectedUser?.let { receiver ->
                            selectedImage?.let { image ->
                                scope.launch(Dispatchers.IO) {
                                    try {
                                        val url = URL(url+"send")
                                        val connection = (url.openConnection() as HttpURLConnection).apply {
                                            requestMethod = "POST"
                                            doOutput = true
                                            setRequestProperty("Content-Type", "application/json")
                                        }

                                        val payload = """
                                            {
                                                "sender": "$currentUser",
                                                "receiver": "$receiver",
                                                "message": "$image"
                                            }
                                        """.trimIndent()

                                        connection.outputStream.write(payload.toByteArray())
                                        connection.outputStream.flush()

                                        if (connection.responseCode == 200) {
                                            withContext(Dispatchers.Main) {
                                                selectedImage = null
                                                selectedUser = null
                                                snackbarHostState.showSnackbar("Сообщение отправлено!")
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.e("SendImage", "Error: ${e.message}")
                                    }
                                }
                            }
                        }
                    },
                    enabled = selectedImage != null,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Отправить изображение")
                }
            }
        }
    }
}
