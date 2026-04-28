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
import com.example.smartcrm.ui.CrmScreen
import com.example.smartcrm.ui.CrmViewModel
import com.example.smartcrm.ui.SearchScreen
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp

// Inicjalizacja biblioteki Hilt - niezbędne do wstrzykiwania bazy danych
@HiltAndroidApp
class SmartCRMApp : Application()

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                            onNavigateToSearch = { navController.navigate("search") }
                        )
                    }
                    // Ekran wyszukiwarki
                    composable("search") {
                        val viewModel: CrmViewModel = hiltViewModel()
                        SearchScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
