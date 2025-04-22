package com.example.kotiki

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController, startDestination = "login") {
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("select_user/{currentUsername}") { backStackEntry ->
            val currentUsername = backStackEntry.arguments?.getString("currentUsername") ?: ""
            SelectUserScreen(navController, currentUsername)
        }
        composable("messages/{currentUsername}") { backStackEntry ->
            val currentUsername = backStackEntry.arguments?.getString("currentUsername") ?: return@composable
            MessagesScreen(navController, currentUsername)
        }
    }
}