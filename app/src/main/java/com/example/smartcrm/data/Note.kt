package com.example.smartcrm.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Model danych dla Notatki.
 * Każda notatka jest powiązana z konkretnym klientem poprzez [clientId].
 * Prowadzący: Zastosowano klucz obcy (ForeignKey) z usuwaniem kaskadowym.
 */
@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = Client::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.CASCADE // Jeśli klient zostanie usunięty, jego notatki również znikną.
        )
    ],
    indices = [Index(value = ["clientId"])]
)
data class Note(
    @PrimaryKey val id: String, // Unikalne ID notatki
    val clientId: String,       // ID klienta, do którego należy notatka
    val content: String,        // Treść notatki
    val createdAt: Long = System.currentTimeMillis() // Data i czas utworzenia
)
