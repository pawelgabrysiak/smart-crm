package com.example.smartcrm.ui

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
import com.example.smartcrm.utils.makeCall
import com.example.smartcrm.utils.openWhatsApp
import com.example.smartcrm.utils.sendEmail

/**
 * Główny ekran aplikacji wyświetlający listę kontaktów.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrmScreen(viewModel: CrmViewModel, onNavigateToSearch: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    var showSheet by remember { mutableStateOf(false) } // Stan kontrolujący widoczność dolnego arkusza (formularza)
    val sheetState = rememberModalBottomSheetState()

    // Efekt uruchamiany przy zmianie klienta do edycji - automatycznie otwiera formularz
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
            // Przycisk (+) do dodawania nowego klienta
            FloatingActionButton(
                onClick = {
                    viewModel.onNameChange("") // Czyścimy pola przed otwarciem
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

            // Wyświetlanie listy lub komunikatu o jej braku
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
                            onDelete = { viewModel.onDeleteClick(client.id) }
                        )
                    }
                }
            }
        }

        // Dolny arkusz (BottomSheet) z formularzem dodawania/edycji
        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showSheet = false
                    // Resetowanie stanu w ViewModelu po zamknięciu arkusza
                    if (state.editingClientId != null) {
                        viewModel.saveClient() 
                    }
                },
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

/**
 * Komponent formularza wewnątrz dolnego arkusza.
 */
@Composable
fun ClientFormSheet(
    state: CrmUiState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .navigationBarsPadding() // Uwzględnienie paska nawigacji systemu
            .imePadding(), // Automatyczne przesuwanie formularza nad klawiaturę
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (state.editingClientId != null) "Edytuj dane klienta" else "Dodaj nowego klienta",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = state.nameInput,
            onValueChange = onNameChange,
            label = { Text("Imię i nazwisko") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Person, null) },
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = state.emailInput,
            onValueChange = onEmailChange,
            label = { Text("Adres Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Email, null) },
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = state.phoneInput,
            onValueChange = onPhoneChange,
            label = { Text("Numer telefonu") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Call, null) },
            shape = RoundedCornerShape(12.dp)
        )

        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = state.nameInput.isNotBlank() && state.emailInput.isNotBlank() && state.phoneInput.isNotBlank()
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Zatwierdź i zapisz")
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * Ekran wyszukiwania klientów.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: CrmViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val filteredClients by viewModel.filteredClients.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = state.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        placeholder = { Text("Wyszukaj klienta...") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Wstecz")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(filteredClients) { client ->
                ClientItem(
                    client = client,
                    onEdit = {
                        viewModel.onEditClick(client)
                        onBack() // Po kliknięciu edycji wracamy do głównego ekranu
                    },
                    onDelete = { viewModel.onDeleteClick(client.id) }
                )
            }
        }
    }
}

/**
 * Komponent pojedynczego wiersza (karty) klienta.
 */
@Composable
fun ClientItem(client: Client, onEdit: () -> Unit, onDelete: () -> Unit) {
    val context = LocalContext.current
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Górna część karty: Avatar, Dane i Przyciski zarządzania
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar z pierwszą literą imienia
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = client.name.take(1).uppercase(),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = client.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = client.status,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edytuj", tint = MaterialTheme.colorScheme.outline)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Usuń", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            // Dolna część karty: Dane kontaktowe i przyciski akcji
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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

                // Szybkie akcje: Telefon, Email, WhatsApp
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledIconButton(
                        onClick = { makeCall(context, client.phone) },
                        modifier = Modifier.size(40.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(Icons.Default.Call, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                    FilledIconButton(
                        onClick = { sendEmail(context, client.email) },
                        modifier = Modifier.size(40.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Icon(Icons.Default.Email, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.secondary)
                    }
                    FilledIconButton(
                        onClick = { openWhatsApp(context, client.phone) },
                        modifier = Modifier.size(40.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFFE8F5E9))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, null, modifier = Modifier.size(18.dp), tint = Color(0xFF2E7D32))
                    }
                }
            }
        }
    }
}
