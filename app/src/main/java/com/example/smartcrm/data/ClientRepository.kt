package com.example.smartcrm.data

import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repozytorium - jedyne źródło prawdy o danych w aplikacji.
 * Pośredniczy między bazą danych Room a resztą aplikacji.
 */
@Singleton
class ClientRepository @Inject constructor(
    private val clientDao: ClientDao
) {
    // Strumień wszystkich klientów z bazy
    val clients = clientDao.getAllClients()

    // Tworzy nowego klienta z unikalnym ID i zapisuje go w bazie
    suspend fun addClient(name: String, email: String, phone: String) {
        val newClient = Client(
            id = UUID.randomUUID().toString(), // Generowanie unikalnego ciągu znaków jako ID
            name = name,
            email = email,
            phone = phone,
            status = "Nowy" // Domyślny status dla każdego nowego klienta
        )
        clientDao.insertClient(newClient)
    }

    // Aktualizuje dane istniejącego klienta
    suspend fun updateClient(updatedClient: Client) {
        clientDao.insertClient(updatedClient)
    }

    // Usuwa klienta z bazy po jego identyfikatorze
    suspend fun deleteClient(clientId: String) {
        clientDao.deleteById(clientId)
    }
}

/* --- USE CASES (PRZYPADKI UŻYCIA) --- 
   Każda klasa poniżej odpowiada za jedną konkretną akcję, którą może wykonać użytkownik.
   Dzięki temu logika nie miesza się w ViewModelu.
*/

// Pobieranie listy klientów
class GetClientsUseCase @Inject constructor(
    private val repository: ClientRepository
) {
    operator fun invoke() = repository.clients
}

// Dodawanie klienta z podstawową walidacją (sprawdzenie czy pola nie są puste)
class AddClientUseCase @Inject constructor(
    private val repository: ClientRepository
) {
    suspend operator fun invoke(name: String, email: String, phone: String) {
        if (name.isNotBlank() && email.isNotBlank() && phone.isNotBlank()) {
            repository.addClient(name.trim(), email.trim(), phone.trim())
        }
    }
}

// Aktualizacja danych klienta z walidacją
class UpdateClientUseCase @Inject constructor(
    private val repository: ClientRepository
) {
   suspend operator fun invoke(client: Client) {
        if (client.name.isNotBlank() && client.email.isNotBlank() && client.phone.isNotBlank()) {
            repository.updateClient(client)
        }
    }
}

// Usuwanie klienta
class DeleteClientUseCase @Inject constructor(
    private val repository: ClientRepository
) {
   suspend operator fun invoke(clientId: String) = repository.deleteClient(clientId)
}
