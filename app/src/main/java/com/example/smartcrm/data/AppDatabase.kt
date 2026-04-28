package com.example.smartcrm.data

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Główna klasa bazy danych. 
 * Tutaj rejestrujemy tabele (entities) oraz wersję bazy.
 */
@Database(entities = [Client::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    
    // Funkcja dająca dostęp do operacji na tabeli klientów (DAO)
    abstract fun clientDao(): ClientDao 
}
