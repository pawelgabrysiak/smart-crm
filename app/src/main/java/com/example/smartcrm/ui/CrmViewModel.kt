package com.example.smartcrm.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartcrm.data.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// Klasa przechowująca aktualny stan widoku
data class CrmUiState(
    val clients: List<Client> = emptyList(),
    val searchQuery: String = "",
    val nameInput: String = "",
    val emailInput: String = "",
    val phoneInput: String = "",
    val editingClientId: String? = null,
    val selectedClient: Client? = null,
    val notes: List<Note> = emptyList(),
    val interactions: List<Interaction> = emptyList(),
    val noteInput: String = ""
)

@HiltViewModel
class CrmViewModel @Inject constructor(
    private val repository: ClientRepository, // Potrzebny do notatek i historii
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

    // --- Nawigacja i Detale ---
    fun loadClientDetails(clientId: String) {
        val client = _uiState.value.clients.find { it.id == clientId }
        _uiState.update { it.copy(selectedClient = client) }

        viewModelScope.launch {
            repository.getNotesForClient(clientId).collect { notes ->
                _uiState.update { it.copy(notes = notes) }
            }
        }
        viewModelScope.launch {
            repository.getInteractionsForClient(clientId).collect { interactions ->
                _uiState.update { it.copy(interactions = interactions) }
            }
        }
    }

    // --- Notatki ---
    fun onNoteInputChange(newValue: String) {
        _uiState.update { it.copy(noteInput = newValue) }
    }

    fun addNote() {
        val clientId = _uiState.value.selectedClient?.id ?: return
        val content = _uiState.value.noteInput
        if (content.isBlank()) return

        viewModelScope.launch {
            repository.addNote(clientId, content)
            _uiState.update { it.copy(noteInput = "") }
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            repository.deleteNote(noteId)
        }
    }

    // --- Historia ---
    fun logInteraction(clientId: String, type: String) {
        viewModelScope.launch {
            repository.addInteraction(clientId, type)
        }
    }

    // --- Funkcje formularza głównego ---
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

    fun saveClient() {
        val state = _uiState.value
        viewModelScope.launch {
            if (state.editingClientId != null) {
                val updated = Client(
                    id = state.editingClientId,
                    name = state.nameInput,
                    email = state.emailInput,
                    phone = state.phoneInput,
                    status = "Zaktualizowany",
                    createdAt = state.clients.find { it.id == state.editingClientId }?.createdAt ?: System.currentTimeMillis()
                )
                updateClientUseCase(updated)
            } else {
                addClientUseCase(state.nameInput, state.emailInput, state.phoneInput)
            }
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

    fun onEditClick(client: Client) {
        _uiState.update { it.copy(
            nameInput = client.name,
            emailInput = client.email,
            phoneInput = client.phone,
            editingClientId = client.id
        ) }
    }

    fun onDeleteClick(clientId: String) {
        viewModelScope.launch {
            deleteClientUseCase(clientId)
        }
    }
}
