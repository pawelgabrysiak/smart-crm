package com.example.smartcrm

import android.Manifest
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.*
import kotlinx.coroutines.launch
import androidx.work.*
import com.example.smartcrm.ui.*
import com.example.smartcrm.ui.components.BottomNavigationBar
import com.example.smartcrm.ui.components.Screen
import com.example.smartcrm.utils.NotificationHelper
import com.example.smartcrm.utils.StatusUpdateWorker
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit

// Inicjalizacja biblioteki Hilt - niezbędne do wstrzykiwania bazy danych
@HiltAndroidApp
class SmartCRMApp : Application()

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Rejestracja kanału powiadomień w systemie (Android 8.0+)
        createNotificationChannel()
        
        // WYMUSZENIE: Wyślij powiadomienie testowe, aby system "zauważył" kanał
        NotificationHelper.showNotification(this, "Smart CRM", "System powiadomień aktywny")

        // Uruchamiamy automat do sprawdzania statusów
        scheduleStatusUpdates(this)

        setContent {
            val context = LocalContext.current
            
            // Prośba o uprawnienia do powiadomień (Android 13+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val launcher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted -> }
                
                LaunchedEffect(Unit) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) 
                        != PackageManager.PERMISSION_GRANTED) {
                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            MaterialTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val viewModel: CrmViewModel = hiltViewModel()
                val state by viewModel.uiState.collectAsState()

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet {
                            Spacer(Modifier.height(12.dp))
                            // Nagłówek Profilu
                            Box(modifier = Modifier.padding(16.dp)) {
                                Column {
                                    Surface(
                                        modifier = Modifier.size(64.dp),
                                        shape = androidx.compose.foundation.shape.CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Icon(
                                            Icons.Default.Person,
                                            null, 
                                            modifier = Modifier.padding(16.dp),
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = state.userName,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text("Smart CRM Enterprise", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            HorizontalDivider()
                            NavigationDrawerItem(
                                label = { Text("Klienci") },
                                selected = currentRoute == "home",
                                onClick = { 
                                    scope.launch { drawerState.close() }
                                    navController.navigate("home") 
                                },
                                icon = { Icon(Icons.Default.People, null) }
                            )
                            NavigationDrawerItem(
                                label = { Text("Terminy") },
                                selected = currentRoute == "deadlines",
                                onClick = { 
                                    scope.launch { drawerState.close() }
                                    navController.navigate("deadlines") 
                                },
                                icon = { Icon(Icons.Default.Event, null) }
                            )
                            NavigationDrawerItem(
                                label = { Text("Statystyki") },
                                selected = currentRoute == "stats",
                                onClick = { 
                                    scope.launch { drawerState.close() }
                                    navController.navigate("stats") 
                                },
                                icon = { Icon(Icons.Default.BarChart, null) }
                            )
                            NavigationDrawerItem(
                                label = { Text("Profil") },
                                selected = currentRoute == "profile",
                                onClick = { 
                                    scope.launch { drawerState.close() }
                                    navController.navigate("profile") 
                                },
                                icon = { Icon(Icons.Default.Settings, null) }
                            )
                            NavigationDrawerItem(
                                label = { Text("O aplikacji") },
                                selected = false,
                                onClick = { 
                                    scope.launch { drawerState.close() }
                                    navController.navigate("about") 
                                },
                                icon = { Icon(Icons.Default.Info, null) }
                            )
                            Spacer(Modifier.weight(1f))
                            Text(
                                "Wersja 1.5.0",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = androidx.compose.ui.graphics.Color.Gray
                            )
                        }
                    }
                ) {
                    Scaffold(
                        bottomBar = {
                            if (currentRoute in listOf("home", "deadlines", "chats", "calls")) {
                                BottomNavigationBar(
                                    currentRoute = currentRoute,
                                    onNavigate = { route ->
                                        navController.navigate(route) {
                                            popUpTo("home") { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = "home",
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable("home") {
                                CrmScreen(
                                    viewModel = viewModel,
                                    onNavigateToSearch = { navController.navigate("search") },
                                    onNavigateToDetails = { client ->
                                        navController.navigate("details/${client.id}")
                                    },
                                    onOpenDrawer = { scope.launch { drawerState.open() } }
                                )
                            }
                            composable("deadlines") {
                                DeadlinesScreen(
                                    viewModel = viewModel,
                                    onNavigateToDetails = { client ->
                                        navController.navigate("details/${client.id}")
                                    },
                                    onOpenDrawer = { scope.launch { drawerState.open() } }
                                )
                            }
                            composable("profile") {
                                ProfileScreen(
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable("stats") {
                                StatsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                            }
                            composable("about") {
                                AboutScreen(onBack = { navController.popBackStack() })
                            }
                            // ... pozostałe composable bez zmian w strukturze argumentów
                            composable("chats") {
                                ChatsScreen(
                                    viewModel = viewModel,
                                    onNavigateToDetails = { client ->
                                        navController.navigate("details/${client.id}")
                                    }
                                )
                            }
                            composable("calls") {
                                CallsScreen(
                                    viewModel = viewModel,
                                    onNavigateToDetails = { client ->
                                        navController.navigate("details/${client.id}")
                                    }
                                )
                            }
                            composable("search") {
                                SearchScreen(
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() },
                                    onNavigateToDetails = { client ->
                                        navController.navigate("details/${client.id}")
                                    }
                                )
                            }
                            composable("details/{clientId}") { backStackEntry ->
                                val clientId = backStackEntry.arguments?.getString("clientId")
                                LaunchedEffect(clientId) {
                                    clientId?.let { viewModel.loadClientDetails(it) }
                                }
                                ClientDetailScreen(
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = NotificationHelper.CHANNEL_NAME
            val descriptionText = "Powiadomienia o terminach i kontaktach"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NotificationHelper.CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(android.content.Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Funkcja planująca sprawdzenie statusów.
     * Na potrzeby prezentacji: wysyłamy zadanie natychmiastowe (OneTime).
     */
    fun scheduleStatusUpdates(context: android.content.Context) {
        // Tworzymy zadanie jednorazowe, które uruchomi się natychmiast
        val demoWorkRequest = OneTimeWorkRequestBuilder<StatusUpdateWorker>()
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "DemoStatusCheck",
            ExistingWorkPolicy.REPLACE, // REPLACE wymusi ponowne sprawdzenie przy każdym włączeniu apki
            demoWorkRequest
        )
    }
}
