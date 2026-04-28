package com.example.smartcrm.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcrm.data.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// Klasa przechowująca aktualny stan widoku (dane wpisane w pola, lista klientów)
data class CrmUiState(
    val clients: List<Client> = emptyList(),
    val searchQuery: String = "",
    val nameInput: String = "",
    val emailInput: String = "",
    val phoneInput: String = "",
    val editingClientId: String? = null
)

@HiltViewModel
class CrmViewModel @Inject constructor(
    private val getClientsUseCase: GetClientsUseCase,
    private val addClientUseCase: AddClientUseCase,
    private val updateClientUseCase: UpdateClientUseCase,
    private val deleteClientUseCase: DeleteClientUseCase
) : ViewModel() {

    // Główny strumień stanu UI
    private val _uiState = MutableStateFlow(CrmUiState())
    val uiState = _uiState.asStateFlow()

    // Dynamicznie filtrowana lista klientów dla wyszukiwarki
    val filteredClients = uiState.map { state ->
        if (state.searchQuery.isBlank()) {
            state.clients
        } else {
            state.clients.filter {
                it.name.contains(state.searchQuery, ignoreCase = true) ||
                        it.email.contains(state.searchQuery, ignoreCase = true) ||
                        it.phone.contains(state.searchQuery)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        // Automatyczne pobieranie listy klientów z bazy przy starcie
        viewModelScope.launch {
            getClientsUseCase().collect { clientList ->
                _uiState.update { it.copy(clients = clientList) }
            }
        }
    }

    // Funkcje aktualizujące stan po wpisaniu tekstu przez użytkownika
    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onNameChange(newName: String) {
        _uiState.update { it.copy(nameInput = newName) }
    }

    fun onEmailChange(newEmail: String) {
        _uiState.update { it.copy(emailInput = newEmail) }
    }

    fun onPhoneChange(newPhone: String) {
        _uiState.update { it.copy(phoneInput = newPhone) }
    }

    // Zapisywanie nowego lub edytowanego klienta
    fun saveClient() {
        val state = _uiState.value
        viewModelScope.launch {
            if (state.editingClientId != null) {
                // Jeśli edytujemy istniejącego klienta
                val updated = Client(
                    id = state.editingClientId,
                    name = state.nameInput,
                    email = state.emailInput,
                    phone = state.phoneInput,
                    status = "Zaktualizowany"
                )
                updateClientUseCase(updated)
            } else {
                // Jeśli dodajemy nowego klienta
                addClientUseCase(state.nameInput, state.emailInput, state.phoneInput)
            }
            // Wyczyszczenie pól po zapisie
            _uiState.update {
                it.copy(
                    nameInput = "",
                    emailInput = "",
                    phoneInput = "",
                    editingClientId = null
                )
            }
        }
    }

    // Przygotowanie pól do edycji po kliknięciu ikonki ołówka
    fun onEditClick(client: Client) {
        _uiState.update { it.copy(
            nameInput = client.name,
            emailInput = client.email,
            phoneInput = client.phone,
            editingClientId = client.id
        ) }
    }

    // Usuwanie klienta z bazy
    fun onDeleteClick(clientId: String) {
        viewModelScope.launch {
            deleteClientUseCase(clientId)
        }
    }
}
