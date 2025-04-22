package com.example.kotiki

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale

data class Message(
    val sender: String,
    val receiver: String,
    val message: String,
    val date: String
)

@Composable
fun MessageImageCard(
    message: Message,
    isSentByMe: Boolean,
    showSent: Boolean
) {
    val drawableId = when (message.message.lowercase(Locale.getDefault())) {
        "waa" -> R.drawable.waa
        "be" -> R.drawable.be
        "zzz" -> R.drawable.zzz
        "mono" -> R.drawable.mono
        "mya" -> R.drawable.mya
        "poker" -> R.drawable.poker
        else -> R.drawable.poker
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.widthIn(max = 300.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSentByMe) Color(0xFFD0E8FF) else Color(0xFFF5F5F5)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {

                // 🔹 Верхняя строка: имя и дата
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (showSent) message.receiver else message.sender,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Text(
                        text = formatDate(message.date),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 🔹 Изображение
                Image(
                    painter = painterResource(id = drawableId),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(
    navController: NavController,
    currentUsername: String
) {
    val scope = rememberCoroutineScope()
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var showSent by remember { mutableStateOf(false) } // <- переключатель
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            try {
                val url = URL(url+"receive/$currentUsername")
                with(url.openConnection() as HttpURLConnection) {
                    requestMethod = "GET"
                    val response = inputStream.bufferedReader().readText()
                    val jsonArray = JSONArray(response)
                    val loaded = mutableListOf<Message>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        loaded.add(
                            Message(
                                sender = obj.getString("sender"),
                                receiver = obj.getString("receiver"),
                                message = obj.getString("message"),
                                date = obj.getString("date")
                            )
                        )
                    }
                    messages = loaded
                }
            } catch (e: Exception) {
                error = "Не удалось загрузить сообщения"
            }
        }
    }

    val filtered = messages.filter {
        if (showSent) it.sender == currentUsername else it.receiver == currentUsername
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (showSent) "Отправленные" else "Входящие")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("select_user/$currentUsername") {
                            popUpTo("messages/$currentUsername") { inclusive = true }
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = { showSent = !showSent }) {
                        Icon(
                            painter = painterResource(R.drawable.swap_horiz),
                            contentDescription = "Переключить входящие/отправленные"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filtered) { msg ->
                        MessageImageCard(
                            message = msg,
                            isSentByMe = msg.sender == currentUsername,
                            showSent = showSent
                        )
                    }
                }
            }
        }
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("HH:mm dd.MM.yy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        if (date != null) outputFormat.format(date) else dateString
    } catch (e: Exception) {
        dateString
    }
}
