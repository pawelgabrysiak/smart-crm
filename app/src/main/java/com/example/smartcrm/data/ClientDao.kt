package com.example.smartcrm.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) - Serce operacji na bazie danych.
 * Zawiera zapytania SQL i instrukcje, które pozwalają aplikacji rozmawiać z bazą SQLite.
 * Prowadzący: Tutaj można dodać nowe zapytania raportowe lub filtrujące.
 */
@Dao
interface ClientDao {
    
    // --- Zarządzanie Klientami ---

    /**
     * Pobiera wszystkich klientów z bazy. 
     * Flow sprawia, że interfejs użytkownika odświeży się sam, gdy tylko dane w bazie ulegną zmianie.
     */
    @Query("SELECT * FROM clients")
    fun getAllClients(): Flow<List<Client>>

    /**
     * Dodaje nowego klienta lub aktualizuje istniejącego (jeśli ID się zgadza).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: Client)

    /**
     * Usuwa klienta na podstawie jego unikalnego identyfikatora ID.
     */
    @Query("DELETE FROM clients WHERE id = :clientId")
    suspend fun deleteById(clientId: String)

    // --- Zarządzanie Notatkami ---

    /**
     * Pobiera wszystkie notatki przypisane do konkretnego klienta.
     * Wyniki są sortowane od najnowszych (DESC).
     */
    @Query("SELECT * FROM notes WHERE clientId = :clientId ORDER BY createdAt DESC")
    fun getNotesForClient(clientId: String): Flow<List<Note>>

    /**
     * Zapisuje nową notatkę w bazie danych.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    /**
     * Usuwa wybraną notatkę.
     */
    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNote(noteId: String)

    // --- Zarządzanie Historią (Interakcje) ---

    /**
     * Pobiera całą historię interakcji ze wszystkimi klientami.
     */
    @Query("SELECT * FROM interactions ORDER BY timestamp DESC")
    fun getAllInteractions(): Flow<List<Interaction>>

    /**
     * Pobiera historię działań (połączenia, maile) dla konkretnego klienta.
     */
    @Query("SELECT * FROM interactions WHERE clientId = :clientId ORDER BY timestamp DESC")
    fun getInteractionsForClient(clientId: String): Flow<List<Interaction>>

    /**
     * Rejestruje nową interakcję (np. fakt wykonania telefonu) w historii.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInteraction(interaction: Interaction)
}
