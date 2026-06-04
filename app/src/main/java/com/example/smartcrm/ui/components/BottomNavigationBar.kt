package com.example.smartcrm.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Contacts : Screen("home", "Klienci", Icons.Default.People)
    object Deadlines : Screen("deadlines", "Terminy", Icons.Default.Event)
    object Chats : Screen("chats", "Czaty", Icons.Default.Email)
    object Calls : Screen("calls", "Połączenia", Icons.Default.Call)
}

@Composable
fun BottomNavigationBar(currentRoute: String?, onNavigate: (String) -> Unit) {
    val items = listOf(Screen.Contacts, Screen.Deadlines, Screen.Chats, Screen.Calls)
    NavigationBar {
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                selected = currentRoute == screen.route,
                onClick = {
                    if (currentRoute != screen.route) {
                        onNavigate(screen.route)
                    }
                }
            )
        }
    }
}
