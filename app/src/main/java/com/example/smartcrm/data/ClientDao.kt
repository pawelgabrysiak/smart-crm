package com.example.smartcrm.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// DAO (Data Access Object) - Interfejs definiujący operacje na bazie danych.
@Dao
interface ClientDao {
    
    // Pobiera wszystkich klientów. Flow sprawia, że lista odświeża się automatycznie przy każdej zmianie w bazie.
    @Query("SELECT * FROM clients")
    fun getAllClients(): Flow<List<Client>>

    // Wstawia lub aktualizuje klienta. Jeśli ID już istnieje, stary wiersz zostanie zastąpiony nowym.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: Client)

    // Usuwa klienta o konkretnym ID z bazy danych.
    @Query("DELETE FROM clients WHERE id = :clientId")
    suspend fun deleteById(clientId: String)

    // --- Notatki ---
    @Query("SELECT * FROM notes WHERE clientId = :clientId ORDER BY createdAt DESC")
    fun getNotesForClient(clientId: String): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNote(noteId: String)

    // --- Historia ---
    @Query("SELECT * FROM interactions WHERE clientId = :clientId ORDER BY timestamp DESC")
    fun getInteractionsForClient(clientId: String): Flow<List<Interaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInteraction(interaction: Interaction)
}
