package com.example.smartcrm.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Model danych dla Historii Interakcji.
 * Przechowuje informacje o wykonanych akcjach, takich jak połączenia czy e-maile.
 * Prowadzący: Tabela połączona relacją z tabelą 'clients'.
 */
@Entity(
    tableName = "interactions",
    foreignKeys = [
        ForeignKey(
            entity = Client::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.CASCADE // Usunięcie klienta czyści całą jego historię kontaktu.
        )
    ],
    indices = [Index(value = ["clientId"])]
)
data class Interaction(
    @PrimaryKey val id: String, // Unikalne ID zdarzenia
    val clientId: String,       // ID klienta powiązanego z akcją
    val type: String,           // Typ akcji: "CALL", "EMAIL", "WHATSAPP"
    val timestamp: Long = System.currentTimeMillis() // Kiedy akcja miała miejsce
)
