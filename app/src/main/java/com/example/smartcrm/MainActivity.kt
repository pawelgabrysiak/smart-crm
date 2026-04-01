package com.example.smartcrm

import androidx.hilt.navigation.compose.hiltViewModel
import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@HiltAndroidApp
class SmartCRMApp : Application()

// tak wyglada pojedynczy klient w naszym systemie
data class Client(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val status: String
)

// miejsce gdzie przechowujemy liste klientow w pamieci ram telefonu
@Singleton
class ClientRepository @Inject constructor() {

    private val _clients = MutableStateFlow<List<Client>>(emptyList())
    val clients = _clients.asStateFlow()

    init {
        _clients.value = listOf(
            Client(UUID.randomUUID().toString(), "Jan Kowalski", "jan.kowalski@example.com", "923 212 121", "Nowy"),
            Client(UUID.randomUUID().toString(), "Anna Nowak", "anna@firma.pl", "221331121", "Oczekuje na kontakt")
        )
    }

    // tworzy nowego klienta i dodaje go na sam poczatek listy
    fun addClient(name: String, email: String, phone: String) {
        val newClient = Client(
            id = UUID.randomUUID().toString(),
            name = name,
            email = email,
            phone = phone,
            status = "Nowy"
        )
        _clients.value = listOf(newClient) + _clients.value
    }

    // znajduje klienta po id i nadpisuje go nowymi danymi
    fun updateClient(updatedClient: Client) {
        _clients.value = _clients.value.map { oldClient ->
            if (oldClient.id == updatedClient.id) updatedClient else oldClient
        }
    }
}

// pobiera klientow z naszego repozytorium
class GetClientsUseCase @Inject constructor(
    private val repository: ClientRepository
) {
    operator fun invoke() = repository.clients
}

// dodaje klienta po sprawdzeniu czy pola nie sa puste
class AddClientUseCase @Inject constructor(
    private val repository: ClientRepository
) {
    operator fun invoke(name: String, email: String, phone: String) {
        if (name.isNotBlank() && email.isNotBlank() && phone.isNotBlank()) {
            repository.addClient(name.trim(), email.trim(), phone.trim())
        }
    }
}

// aktualizuje dane klienta w repozytorium
class UpdateClientUseCase @Inject constructor(
    private val repository: ClientRepository
) {
    operator fun invoke(client: Client) {
        if (client.name.isNotBlank() && client.email.isNotBlank() && client.phone.isNotBlank()) {
            repository.updateClient(client)
        }
    }
}

// przechowuje aktualny stan ekranu - teksty i tryb edycji
data class CrmUiState(
    val clients: List<Client> = emptyList(),
    val nameInput: String = "",
    val emailInput: String = "",
    val phoneInput: String = "",
    val editingClientId: String? = null // jesli nie jest nullem, to edytujemy tego klienta
)

// zarzadza danymi na ekranie i reaguje na akcje uzytkownika
@HiltViewModel
class CrmViewModel @Inject constructor(
    private val getClientsUseCase: GetClientsUseCase,
    private val addClientUseCase: AddClientUseCase,
    private val updateClientUseCase: UpdateClientUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CrmUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getClientsUseCase().collect { clientList ->
                _uiState.update { it.copy(clients = clientList) }
            }
        }
    }

    // aktualizuje wpisane imie w stanie aplikacji
    fun onNameChange(newName: String) {
        _uiState.update { it.copy(nameInput = newName) }
    }

    // aktualizuje wpisany email w stanie aplikacji
    fun onEmailChange(newEmail: String) {
        _uiState.update { it.copy(emailInput = newEmail) }
    }

    // aktualizuje wpisany telefon w stanie aplikacji
    fun onPhoneChange(newPhone: String) {
        _uiState.update { it.copy(phoneInput = newPhone) }
    }

    // dodaje nowego klienta lub zapisuje edytowanego
    fun saveClient() {
        val state = _uiState.value
        if (state.editingClientId != null) {
            val updated = Client(
                id = state.editingClientId,
                name = state.nameInput,
                email = state.emailInput,
                phone = state.phoneInput,
                status = "Zaktualizowany"
            )
            updateClientUseCase(updated)
        } else {
            addClientUseCase(state.nameInput, state.emailInput, state.phoneInput)
        }
        // czysci pola tekstowe i resetuje tryb edycji
        _uiState.update { it.copy(nameInput = "", emailInput = "", phoneInput = "", editingClientId = null) }
    }

    // laduje dane wybranego klienta do pol tekstowych zeby mozna bylo je edytowac
    fun onEditClick(client: Client) {
        _uiState.update { it.copy(
            nameInput = client.name,
            emailInput = client.email,
            phoneInput = client.phone,
            editingClientId = client.id
        ) }
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // POPRAWKA TUTAJ: Zamiast .viewModel() używamy hiltViewModel()
                    val viewModel: CrmViewModel = hiltViewModel()
                    CrmScreen(viewModel)
                }
            }
        }
    }
}

// glowny widok aplikacji z formularzem i lista
@Composable
fun CrmScreen(viewModel: CrmViewModel) {
    val state by viewModel.uiState.collectAsState()
    val isEditing = state.editingClientId != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Smart CRM", style = MaterialTheme.typography.headlineMedium)

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // zmienia naglowek zaleznie od tego czy edytujemy czy dodajemy
                Text(
                    text = if (isEditing) "Edytuj klienta" else "Nowy klient",
                    style = MaterialTheme.typography.titleMedium
                )

                OutlinedTextField(
                    value = state.nameInput,
                    onValueChange = { viewModel.onNameChange(it) },
                    label = { Text("Imię i nazwisko") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = state.emailInput,
                    onValueChange = { viewModel.onEmailChange(it) },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = state.phoneInput,
                    onValueChange = { viewModel.onPhoneChange(it) },
                    label = { Text("Numer telefonu") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // przycisk zmienia tekst i funkcje zaleznie od trybu edycji
                Button(
                    onClick = { viewModel.saveClient() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.nameInput.isNotBlank() && state.emailInput.isNotBlank() && state.phoneInput.isNotBlank()
                ) {
                    Text(if (isEditing) "Zapisz zmiany" else "Dodaj Klienta")
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Text(
            text = "Baza kontaktów",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.Start)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.clients) { client ->
                // przekazujemy funkcje edycji do kazdego kafelka
                ClientItem(client, onEdit = { viewModel.onEditClick(client) })
            }
        }
    }
}

// pojedynczy kafelek klienta z danymi i przyciskiem edycji
@Composable
fun ClientItem(client: Client, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        ListItem(
            headlineContent = { Text(client.name, style = MaterialTheme.typography.bodyLarge) },
            supportingContent = {
                Column {
                    Text("✉️ ${client.email}", style = MaterialTheme.typography.bodyMedium)
                    Text("📞 ${client.phone}", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "Status: ${client.status}",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            },
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Ikona klienta",
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            // dodaje przycisk edycji po prawej stronie kafelka
            trailingContent = {
                IconButton(onClick = onEdit) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edytuj klienta")
                }
            },
            colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
        )
    }
}