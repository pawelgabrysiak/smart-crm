package com.example.smartcrm.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Model danych klienta. 
 * Adnotacja @Entity mówi Roomowi, że ma stworzyć tabelę o nazwie "clients" na podstawie tej klasy.
 */
@Entity(tableName = "clients")
data class Client(
    @PrimaryKey val id: String, // unikalny indefikator klienta w baza
    val name: String,
    val email: String,
    val phone: String,
    val status: String,
    val imageUri: String? = null,
    val deadline: Long? = null, // Nowe: data deadline'u (timestamp)
    val deadlineCompleted: Boolean = false, // Czy zadanie zostało wykonane
    val createdAt: Long = System.currentTimeMillis()
)
