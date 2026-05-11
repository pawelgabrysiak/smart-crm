package com.example.smartcrm

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.*
import com.example.smartcrm.ui.ClientDetailScreen
import com.example.smartcrm.ui.CrmScreen
import com.example.smartcrm.ui.CrmViewModel
import com.example.smartcrm.ui.SearchScreen
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
        
        // Uruchamiamy automat do sprawdzania statusów
        scheduleStatusUpdates(this)

        setContent {
            MaterialTheme {
                // Kontroler nawigacji zarządza przełączaniem ekranów
                val navController = rememberNavController()

                // NavHost definiuje mapę ekranów w aplikacji
                NavHost(
                    navController = navController,
                    startDestination = "home" // Startujemy od listy kontaktów
                ) {
                    // Ekran główny (lista klientów)
                    composable("home") {
                        val viewModel: CrmViewModel = hiltViewModel()
                        CrmScreen(
                            viewModel = viewModel,
                            onNavigateToSearch = { navController.navigate("search") },
                            onNavigateToDetails = { client ->
                                viewModel.onClientClick(client)
                                navController.navigate("details")
                            }
                        )
                    }
                    // Ekran wyszukiwarki
                    composable("search") {
                        val viewModel: CrmViewModel = hiltViewModel()
                        SearchScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                            onNavigateToDetails = { client ->
                                viewModel.onClientClick(client)
                                navController.navigate("details")
                            }
                        )
                    }
                    // Ekran detali klienta
                    composable("details") {
                        val viewModel: CrmViewModel = hiltViewModel()
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

/**
 * Funkcja planująca cykliczne sprawdzanie statusów (raz na dobę).
 */
fun scheduleStatusUpdates(context: android.content.Context) {
    val workRequest = PeriodicWorkRequestBuilder<StatusUpdateWorker>(24, TimeUnit.HOURS)
        .setBackoffCriteria(BackoffPolicy.LINEAR, 15, TimeUnit.MINUTES)
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "StatusUpdateWork",
        ExistingPeriodicWorkPolicy.KEEP, // Jeśli zadanie już jest zaplanowane, nie nadpisuj go
        workRequest
    )
}
