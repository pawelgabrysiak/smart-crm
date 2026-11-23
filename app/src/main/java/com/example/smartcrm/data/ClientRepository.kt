package com.example.smartcrm.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repozytorium - jedyne źródło prawdy o danych w aplikacji.
 * Pośredniczy między bazą danych Room, chmurą Firebase a resztą aplikacji.
 */
@Singleton
class ClientRepository @Inject constructor(
    private val clientDao: ClientDao,
    private val firestore: FirebaseFirestore
) {
    // Strumień wszystkich klientów z bazy
    val clients = clientDao.getAllClients()

    // init wykonuje sie dookladnie raz, w momencie tworzenia obeiktu klasy
    init {
        // 1. Otwieramy nasłuchiwanie w czasie rzeczywistym na kolekcji "clients" w Firebase
        firestore.collection("clients")
            .addSnapshotListener { snapshot, error ->
                // Jeśli wystąpił błąd lub brak danych – ignorujemy
                if (error != null || snapshot == null) return@addSnapshotListener

                try {
                    // 2. Mapujemy dokumenty z Firebase na obiekty Kotlinowe `Client`
                    val clientsFromCloud = snapshot.toObjects(Client::class.java)

                    // 3. W osobnym wątku zapisujemy pobranych klientów do lokalnej bazy Room
                    CoroutineScope(Dispatchers.IO).launch {
                        for (client in clientsFromCloud) {
                            clientDao.insertClient(client)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
    }

    // Tworzy nowego klienta z unikalnym ID i zapisuje go w bazie lokalnej oraz w chmurze
    suspend fun addClient(name: String, email: String, phone: String, imageUri: String? = null, deadline: Long? = null) {
        val newClient = Client(
            id = UUID.randomUUID().toString(), // Generowanie unikalnego ciągu znaków jako ID
            name = name,
            email = email,
            phone = phone,
            status = "Nowy", // Domyślny status dla każdego nowego klienta
            imageUri = imageUri,
            deadline = deadline
        )
        // 1. Zapis w lokalnej bazie danych Room
        clientDao.insertClient(newClient)

        // 2. Synchronizacja z chmurą Firebase Firestore
        firestore.collection("clients")
            .document(newClient.id)
            .set(newClient)
    }

    // Aktualizuje dane istniejącego klienta lokalnie i w chmurze
    suspend fun updateClient(updatedClient: Client) {
        clientDao.insertClient(updatedClient)
        firestore.collection("clients")
            .document(updatedClient.id)
            .set(updatedClient)
    }

    // Usuwa klienta z bazy po jego identyfikatorze
    suspend fun deleteClient(clientId: String) {
        clientDao.deleteById(clientId)
        firestore.collection("clients")
            .document(clientId)
            .delete()
    }

    // --- Notatki ---
    fun getNotesForClient(clientId: String) = clientDao.getNotesForClient(clientId)

    suspend fun addNote(clientId: String, content: String) {
        val note = Note(
            id = UUID.randomUUID().toString(),
            clientId = clientId,
            content = content
        )
        clientDao.insertNote(note)
    }

    suspend fun deleteNote(noteId: String) {
        clientDao.deleteNote(noteId)
    }

    // --- Historia ---
    fun getAllInteractions() = clientDao.getAllInteractions()

    fun getInteractionsForClient(clientId: String) = clientDao.getInteractionsForClient(clientId)

    suspend fun addInteraction(clientId: String, type: String) {
        val interaction = Interaction(
            id = UUID.randomUUID().toString(),
            clientId = clientId,
            type = type
        )
        clientDao.insertInteraction(interaction)
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
    suspend operator fun invoke(name: String, email: String, phone: String, imageUri: String? = null, deadline: Long? = null) {
        if (name.isNotBlank() && email.isNotBlank() && phone.isNotBlank()) {
            repository.addClient(name.trim(), email.trim(), phone.trim(), imageUri, deadline)
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
