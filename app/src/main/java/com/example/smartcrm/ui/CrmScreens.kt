package com.example.smartcrm.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartcrm.data.Client
import com.example.smartcrm.data.Interaction
import com.example.smartcrm.data.Note
import com.example.smartcrm.utils.makeCall
import com.example.smartcrm.utils.openWhatsApp
import com.example.smartcrm.utils.sendEmail
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrmScreen(viewModel: CrmViewModel, onNavigateToSearch: () -> Unit, onNavigateToDetails: (Client) -> Unit) {
    val state by viewModel.uiState.collectAsState()
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(state.editingClientId) {
        if (state.editingClientId != null) {
            showSheet = true
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Smart CRM", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Default.Search, contentDescription = "Szukaj")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.onNameChange("")
                    viewModel.onEmailChange("")
                    viewModel.onPhoneChange("")
                    showSheet = true
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj klienta", tint = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Twoje Kontakty",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (state.clients.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Brak klientów. Dodaj pierwszego!", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp)
                ) {
                    items(state.clients) { client ->
                        ClientItem(
                            client = client,
                            onEdit = { viewModel.onEditClick(client) },
                            onDelete = { viewModel.onDeleteClick(client.id) },
                            onClick = { onNavigateToDetails(client) },
                            onAction = { type -> viewModel.logInteraction(client.id, type) }
                        )
                    }
                }
            }
        }

        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = sheetState
            ) {
                ClientFormSheet(
                    state = state,
                    onNameChange = viewModel::onNameChange,
                    onEmailChange = viewModel::onEmailChange,
                    onPhoneChange = viewModel::onPhoneChange,
                    onSave = {
                        viewModel.saveClient()
                        showSheet = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientDetailScreen(viewModel: CrmViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val client = state.selectedClient ?: return
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(client.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wstecz")
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
            // Sekcja Akcji Szybkich
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActionIcon(Icons.Default.Call, "Zadzwoń", MaterialTheme.colorScheme.primary) {
                    makeCall(context, client.phone)
                    viewModel.logInteraction(client.id, "CALL")
                }
                ActionIcon(Icons.Default.Email, "Email", MaterialTheme.colorScheme.secondary) {
                    sendEmail(context, client.email)
                    viewModel.logInteraction(client.id, "EMAIL")
                }
                ActionIcon(Icons.AutoMirrored.Filled.Send, "WhatsApp", Color(0xFF25D366)) {
                    openWhatsApp(context, client.phone)
                    viewModel.logInteraction(client.id, "WHATSAPP")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sekcja Notatek
            Text("Notatki", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = state.noteInput,
                    onValueChange = { viewModel.onNoteInputChange(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Dodaj nową notatkę...") },
                    maxLines = 2
                )
                IconButton(onClick = { viewModel.addNote() }) {
                    Icon(Icons.Default.Add, contentDescription = "Dodaj")
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(state.notes) { note ->
                    NoteItem(note) { viewModel.deleteNote(note.id) }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sekcja Historii
            Text("Historia kontaktu", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            LazyColumn(
                modifier = Modifier.height(200.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(state.interactions) { interaction ->
                    InteractionItem(interaction)
                }
            }
        }
    }
}

@Composable
fun ActionIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        FilledIconButton(onClick = onClick, colors = IconButtonDefaults.filledIconButtonColors(containerColor = color.copy(alpha = 0.1f))) {
            Icon(icon, contentDescription = label, tint = color)
        }
        Text(label, style = MaterialTheme.typography.bodySmall, fontSize = 10.sp)
    }
}

@Composable
fun NoteItem(note: Note, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(note.content, style = MaterialTheme.typography.bodyMedium)
                Text(
                    SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(note.createdAt)),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Usuń", modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun InteractionItem(interaction: Interaction) {
    val (icon, text) = when (interaction.type) {
        "CALL" -> Icons.Default.Call to "Wykonano połączenie"
        "EMAIL" -> Icons.Default.Email to "Wysłano wiadomość e-mail"
        "WHATSAPP" -> Icons.AutoMirrored.Filled.Send to "Wiadomość WhatsApp"
        else -> Icons.Default.Info to "Interakcja"
    }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Gray)
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.weight(1f))
        Text(
            SimpleDateFormat("dd.MM HH:mm", Locale.getDefault()).format(Date(interaction.timestamp)),
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
fun ClientFormSheet(
    state: CrmUiState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp).navigationBarsPadding().imePadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (state.editingClientId != null) "Edytuj dane klienta" else "Dodaj nowego klienta",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        OutlinedTextField(value = state.nameInput, onValueChange = onNameChange, label = { Text("Imię i nazwisko") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Person, null) }, shape = RoundedCornerShape(12.dp))
        OutlinedTextField(value = state.emailInput, onValueChange = onEmailChange, label = { Text("Adres Email") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Email, null) }, shape = RoundedCornerShape(12.dp))
        OutlinedTextField(value = state.phoneInput, onValueChange = onPhoneChange, label = { Text("Numer telefonu") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Call, null) }, shape = RoundedCornerShape(12.dp))
        Button(onClick = onSave, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(12.dp), enabled = state.nameInput.isNotBlank() && state.emailInput.isNotBlank() && state.phoneInput.isNotBlank()) {
            Icon(Icons.Default.Check, null)
            Spacer(Modifier.width(8.dp))
            Text("Zatwierdź i zapisz")
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: CrmViewModel, onBack: () -> Unit, onNavigateToDetails: (Client) -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val filteredClients by viewModel.filteredClients.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(value = state.searchQuery, onValueChange = { viewModel.onSearchQueryChange(it) }, placeholder = { Text("Wyszukaj klienta...") }, modifier = Modifier.fillMaxWidth(), colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, disabledContainerColor = Color.Transparent, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent), singleLine = true)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wstecz")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(vertical = 16.dp)) {
            items(filteredClients) { client ->
                ClientItem(client = client, onEdit = { viewModel.onEditClick(client); onBack() }, onDelete = { viewModel.onDeleteClick(client.id) }, onClick = { onNavigateToDetails(client) }, onAction = { type -> viewModel.logInteraction(client.id, type) })
            }
        }
    }
}

@Composable
fun ClientItem(client: Client, onEdit: () -> Unit, onDelete: () -> Unit, onClick: () -> Unit, onAction: (String) -> Unit) {
    val context = LocalContext.current
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = client.name.take(1).uppercase(), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = client.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(text = client.status, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                    }
                }
                Row {
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Edytuj", tint = MaterialTheme.colorScheme.outline) }
                    IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Usuń", tint = MaterialTheme.colorScheme.error) }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                        Spacer(Modifier.width(4.dp))
                        Text(client.email, style = MaterialTheme.typography.bodySmall, fontSize = 12.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Call, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                        Spacer(Modifier.width(4.dp))
                        Text(client.phone, style = MaterialTheme.typography.bodySmall, fontSize = 12.sp)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledIconButton(onClick = { makeCall(context, client.phone); onAction("CALL") }, modifier = Modifier.size(40.dp), colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) { Icon(Icons.Default.Call, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary) }
                    FilledIconButton(onClick = { sendEmail(context, client.email); onAction("EMAIL") }, modifier = Modifier.size(40.dp), colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) { Icon(Icons.Default.Email, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.secondary) }
                    FilledIconButton(onClick = { openWhatsApp(context, client.phone); onAction("WHATSAPP") }, modifier = Modifier.size(40.dp), colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFFE8F5E9))) { Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(18.dp), tint = Color(0xFF2E7D32)) }
                }
            }
        }
    }
}
