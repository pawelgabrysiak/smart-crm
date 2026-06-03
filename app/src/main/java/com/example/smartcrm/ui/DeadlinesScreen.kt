package com.example.smartcrm.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartcrm.data.Client

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeadlinesScreen(
    viewModel: CrmViewModel,
    onBack: () -> Unit,
    onNavigateToDetails: (Client) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val clientsWithDeadlines = state.clients
        .filter { it.deadline != null }
        .sortedBy { it.deadline }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nadchodzące Terminy") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wstecz")
                    }
                }
            )
        }
    ) { padding ->
        if (clientsWithDeadlines.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("Brak ustawionych terminów", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(clientsWithDeadlines) { client ->
                    ClientItem(
                        client = client,
                        onEdit = { viewModel.onEditClick(client); onBack() },
                        onDelete = { viewModel.onDeleteClick(client.id) },
                        onClick = { onNavigateToDetails(client) },
                        onAction = { type -> viewModel.logInteraction(client.id, type) }
                    )
                }
            }
        }
    }
}
