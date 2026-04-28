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
}
