package com.example.smartcrm.data

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Główna klasa bazy danych. 
 * Tutaj rejestrujemy tabele (entities) oraz wersję bazy.
 */
@Database(entities = [Client::class, Note::class, Interaction::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    
    // Funkcja dająca dostęp do operacji na tabeli klientów (DAO)
    abstract fun clientDao(): ClientDao 
}
