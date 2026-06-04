package com.example.smartcrm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartcrm.data.Client
import com.example.smartcrm.utils.makeCall
import com.example.smartcrm.utils.sendEmail
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeadlinesScreen(
    viewModel: CrmViewModel,
    onNavigateToDetails: (Client) -> Unit,
    onOpenDrawer: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val currentTime = System.currentTimeMillis()
    
    // Grupowanie terminów
    val groupedDeadlines = remember(state.clients) {
        val deadlines = state.clients.filter { it.deadline != null }
        val overdue = deadlines.filter { !it.deadlineCompleted && it.deadline!! < currentTime }
            .sortedBy { it.deadline }
        val today = deadlines.filter { !it.deadlineCompleted && isToday(it.deadline!!) }
            .sortedBy { it.deadline }
        val upcoming = deadlines.filter { !it.deadlineCompleted && it.deadline!! > currentTime && !isToday(it.deadline!!) }
            .sortedBy { it.deadline }
        val completed = deadlines.filter { it.deadlineCompleted }
            .sortedByDescending { it.deadline }
            
        listOf(
            "🔴 Zaległe" to overdue,
            "🟡 Na dzisiaj" to today,
            "🟢 Nadchodzące" to upcoming,
            "⚪ Zrealizowane" to completed
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Planowane Terminy", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            groupedDeadlines.forEach { (header, clients) ->
                if (clients.isNotEmpty()) {
                    item {
                        Text(
                            text = "$header (${clients.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(clients) { client ->
                        DeadlineCard(
                            client = client,
                            onToggleComplete = { viewModel.toggleDeadlineCompletion(client) },
                            onClick = { onNavigateToDetails(client) },
                            onCall = { viewModel.logInteraction(client.id, "CALL") },
                            onEmail = { viewModel.logInteraction(client.id, "EMAIL") }
                        )
                    }
                }
            }
            
            if (groupedDeadlines.all { it.second.isEmpty() }) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Brak zaplanowanych zadań", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun DeadlineCard(
    client: Client,
    onToggleComplete: () -> Unit,
    onClick: () -> Unit,
    onCall: () -> Unit,
    onEmail: () -> Unit
) {
    val context = LocalContext.current
    val isCompleted = client.deadlineCompleted
    val deadlineDate = client.deadline?.let { 
        SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(it))
    } ?: ""

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick,
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isCompleted) Color(0xFFF9F9F9) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Lewa strona: Status i Avatar
                Box(contentAlignment = Alignment.Center) {
                    ClientAvatar(client = client, size = 44)
                    IconButton(
                        onClick = onToggleComplete,
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = 4.dp, y = 4.dp)
                            .background(Color.White, RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (isCompleted) Color(0xFF4CAF50) else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Spacer(Modifier.width(16.dp))
                
                // Środek: Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = client.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) Color.Gray else MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime, 
                            contentDescription = null, 
                            modifier = Modifier.size(14.dp), 
                            tint = if (isCompleted) Color.Gray else MaterialTheme.colorScheme.secondary
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = deadlineDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isCompleted) Color.Gray else MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            if (!isCompleted) {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onToggleComplete,
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Ukończone", fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.weight(1f))
                    FilledIconButton(
                        onClick = { makeCall(context, client.phone); onCall() },
                        modifier = Modifier.size(40.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(Icons.Default.Call, null, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    FilledIconButton(
                        onClick = { sendEmail(context, client.email); onEmail() },
                        modifier = Modifier.size(40.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(Icons.Default.Email, null, modifier = Modifier.size(20.dp))
                    }
                }
            } else {
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = onToggleComplete, 
                    modifier = Modifier.align(Alignment.Start),
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
                ) {
                    Icon(Icons.Default.History, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Przywróć do zrobienia", fontSize = 12.sp)
                }
            }
        }
    }
}

private fun isToday(timestamp: Long): Boolean {
    val cal1 = Calendar.getInstance()
    val cal2 = Calendar.getInstance().apply { timeInMillis = timestamp }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
