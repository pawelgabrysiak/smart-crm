package com.example.smartcrm.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
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
fun CrmScreen(viewModel: CrmViewModel, onNavigateToSearch: () -> Unit, onNavigateToDetails: (Client) -> Unit, onOpenDrawer: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(state.editingClientId) {
        if (state.editingClientId != null) {
            showSheet = true
        }
    }

    // Okno potwierdzenia interakcji
    if (state.pendingInteraction != null) {
        val pending = state.pendingInteraction!!
        val actionName = when(pending.type) {
            "CALL" -> "rozmowa telefoniczna"
            "EMAIL" -> "wysłanie e-maila"
            "WHATSAPP" -> "wiadomość WhatsApp"
            else -> "kontakt"
        }
        AlertDialog(
            onDismissRequest = { viewModel.cancelInteraction() },
            title = { Text("Potwierdzenie kontaktu") },
            text = { Text("Czy ${actionName} z ${pending.clientName} zakończyła się sukcesem?") },
            confirmButton = {
                Button(onClick = { viewModel.confirmInteraction() }) {
                    Text("Tak, zapisz w historii")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelInteraction() }) {
                    Text("Nie / Anuluj")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Smart CRM", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
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
                    onImageChange = viewModel::onImageChange,
                    onDeadlineChange = viewModel::onDeadlineChange,
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

    // Okno potwierdzenia interakcji (także na ekranie detali)
    if (state.pendingInteraction != null) {
        val pending = state.pendingInteraction!!
        AlertDialog(
            onDismissRequest = { viewModel.cancelInteraction() },
            title = { Text("Potwierdź kontakt") },
            text = { Text("Czy udało Ci się skontaktować z ${pending.clientName}?") },
            confirmButton = {
                Button(onClick = { viewModel.confirmInteraction() }) {
                    Text("Tak, odnotuj w historii")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelInteraction() }) {
                    Text("Anuluj")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil klienta") },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Nagłówek profilu
            ClientAvatar(client = client, size = 120)
            Spacer(modifier = Modifier.height(16.dp))
            Text(client.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(client.status, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
            
            // Dodajemy wyświetlanie deadline'u w profilu
            client.deadline?.let { DeadlineTag(it) }

            Spacer(modifier = Modifier.height(24.dp))

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
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))

            // Sekcja Notatek
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Notatki", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = state.noteInput,
                        onValueChange = { viewModel.onNoteInputChange(it) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Dodaj nową notatkę...") },
                        maxLines = 2,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { viewModel.addNote() },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Dodaj", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                state.notes.forEach { note ->
                    NoteItem(note) { viewModel.deleteNote(note.id) }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sekcja Historii
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Historia kontaktu", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                state.interactions.forEach { interaction ->
                    InteractionItem(interaction)
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(viewModel: CrmViewModel, onNavigateToDetails: (Client) -> Unit) {
    val activeChats by viewModel.activeChats.collectAsState()
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Czaty", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        if (activeChats.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Email, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(Modifier.height(8.dp))
                    Text("Brak aktywnych rozmów", color = Color.Gray)
                    Text("Wyślij Email lub WhatsApp, aby zacząć", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                items(activeChats) { (client, lastInteraction) ->
                    val timeString = formatShortTime(lastInteraction.timestamp)
                    val lastActionText = when(lastInteraction.type) {
                        "EMAIL" -> "Wysłano E-mail"
                        "WHATSAPP" -> "Wiadomość WhatsApp"
                        else -> "Kontakt"
                    }

                    ListItem(
                        headlineContent = { Text(client.name, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text(lastActionText, maxLines = 1) },
                        leadingContent = { ClientAvatar(client) },
                        trailingContent = { Text(timeString, style = MaterialTheme.typography.labelSmall, color = Color.Gray) },
                        modifier = Modifier.clickable { onNavigateToDetails(client) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                }
            }
        }
    }
}

/**
 * Pomocnicza funkcja do ładnego formatowania czasu (np. "12:30" lub "Wczoraj")
 */
fun formatShortTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val oneDay = 24 * 60 * 60 * 1000L

    return when {
        diff < oneDay -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
        diff < 2 * oneDay -> "Wczoraj"
        else -> SimpleDateFormat("dd.MM", Locale.getDefault()).format(Date(timestamp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallsScreen(viewModel: CrmViewModel, onNavigateToDetails: (Client) -> Unit) {
    val state by viewModel.uiState.collectAsState()
    
    // Filtrujemy tylko interakcje typu "CALL" ze wszystkich dostępnych interakcji w stanie
    val callInteractions = state.interactions.filter { it.type == "CALL" }
        .mapNotNull { interaction ->
            val client = state.clients.find { it.id == interaction.clientId }
            if (client != null) interaction to client else null
        }.sortedByDescending { it.first.timestamp }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Połączenia", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        if (callInteractions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Brak historii połączeń", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                items(callInteractions) { (interaction, client) ->
                    ListItem(
                        headlineContent = { Text(client.name, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Call, null, modifier = Modifier.size(14.dp), tint = Color.Green)
                                Spacer(Modifier.width(4.dp))
                                Text(SimpleDateFormat("dd.MM, HH:mm", Locale.getDefault()).format(Date(interaction.timestamp)))
                            }
                        },
                        leadingContent = { ClientAvatar(client) },
                        trailingContent = { 
                            IconButton(onClick = { onNavigateToDetails(client) }) {
                                Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        modifier = Modifier.clickable { onNavigateToDetails(client) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: CrmViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    
    val callCount = state.interactions.count { it.type == "CALL" }
    val emailCount = state.interactions.count { it.type == "EMAIL" }
    val whatsappCount = state.interactions.count { it.type == "WHATSAPP" }
    val frozenCount = state.clients.count { it.status == "Zamrożony" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statystyki") },
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Liczniki
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard("Połączenia", callCount.toString(), Icons.Default.Call, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                StatCard("Zamrożone", frozenCount.toString(), Icons.Default.Warning, MaterialTheme.colorScheme.error, Modifier.weight(1f))
            }
            Spacer(Modifier.height(24.dp))
            
            Text("Aktywność (Wykres)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            
            // Prosty wykres słupkowy
            SimpleBarChart(
                data = listOf(
                    "Tel" to callCount,
                    "Mail" to emailCount,
                    "WA" to whatsappCount
                )
            )
            
            Spacer(Modifier.height(32.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))
            
            // O autorze (w statystykach dla dodatkowego efektu)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("O projekcie", fontWeight = FontWeight.Bold)
                    Text("Aplikacja Smart CRM v1.5.0", style = MaterialTheme.typography.bodySmall)
                    Text("Autor: Paweł Gabrysiak", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: androidx.compose.ui.graphics.Color, modifier: Modifier) {
    ElevatedCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun SimpleBarChart(data: List<Pair<String, Int>>) {
    val maxVal = data.maxOfOrNull { it.second }?.takeIf { it > 0 } ?: 1
    Row(
        modifier = Modifier.fillMaxWidth().height(150.dp).padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { (label, value) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(value.toString(), style = MaterialTheme.typography.labelSmall)
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height((100 * value / maxVal).dp)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
                Spacer(Modifier.height(4.dp))
                Text(label, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("O aplikacji") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wstecz")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Info, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
            Text("Smart CRM", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Wersja 1.5.0 (Enterprise Edition)", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(32.dp))
            Text("Projekt zrealizowany w celach edukacyjnych.", style = MaterialTheme.typography.bodySmall, color = androidx.compose.ui.graphics.Color.Gray)
            Text("Autor: Paweł Gabrysiak", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientFormSheet(
    state: CrmUiState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onImageChange: (String?) -> Unit,
    onDeadlineChange: (Long?) -> Unit,
    onSave: () -> Unit
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            try {
                // Uzyskanie trwałych uprawnień do URI (kluczowe, żeby zdjęcia nie znikały po restarcie)
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Niektóre aplikacje/źródła mogą nie wspierać persistable URI, 
                // ale dla standardowej galerii to zadziała
            }
        }
        onImageChange(uri?.toString())
    }

    // Okno wyboru daty
    val calendar = Calendar.getInstance()
    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, year, month, day ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, day)
            onDeadlineChange(selectedCalendar.timeInMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (state.editingClientId != null) "Edytuj dane klienta" else "Dodaj nowego klienta",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        // Wybór zdjęcia
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .clickable { launcher.launch(arrayOf("image/*")) }
        ) {
            if (state.imageInput != null) {
                Image(
                    painter = rememberAsyncImagePainter(state.imageInput),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(24.dp)
                            .align(Alignment.Center),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
        Text("Zmień zdjęcie", style = MaterialTheme.typography.labelSmall)

        OutlinedTextField(value = state.nameInput, onValueChange = onNameChange, label = { Text("Imię i nazwisko") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Person, null) }, shape = RoundedCornerShape(12.dp))
        
        // Wybór Deadline'u
        OutlinedCard(
            modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() },
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.DateRange, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Termin (Deadline)", style = MaterialTheme.typography.labelSmall)
                    Text(
                        text = if (state.deadlineInput != null) 
                            SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(state.deadlineInput))
                            else "Kliknij, aby ustawić termin",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

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
fun ClientAvatar(client: Client, size: Int = 48) {
    if (client.imageUri != null) {
        Image(
            painter = rememberAsyncImagePainter(client.imageUri),
            contentDescription = null,
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        Surface(
            modifier = Modifier.size(size.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = client.name.take(1).uppercase(),
                    style = if (size > 50) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun DeadlineTag(deadline: Long) {
    val currentTime = System.currentTimeMillis()
    val diff = deadline - currentTime
    val daysLeft = diff / (24 * 60 * 60 * 1000)

    val (containerColor, contentColor, text) = when {
        diff < 0 -> Triple(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.error, "Zaległe!")
        daysLeft < 2 -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), "Termin: Dzisiaj/Jutro")
        else -> Triple(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer, "Zostało: $daysLeft dni")
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(14.dp), tint = contentColor)
            Spacer(Modifier.width(4.dp))
            Text(text, style = MaterialTheme.typography.labelSmall, color = contentColor, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ClientItem(client: Client, onEdit: () -> Unit, onDelete: () -> Unit, onClick: () -> Unit, onAction: (String) -> Unit) {
    val context = LocalContext.current
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ClientAvatar(client = client)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = client.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(text = client.status, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                        // Wyświetlamy tag deadline'u jeśli istnieje
                        client.deadline?.let { DeadlineTag(it) }
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
