package com.example.smartcrm.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "interactions",
    foreignKeys = [
        ForeignKey(
            entity = Client::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["clientId"])]
)
data class Interaction(
    @PrimaryKey val id: String,
    val clientId: String,
    val type: String, // "CALL", "EMAIL", "WHATSAPP"
    val timestamp: Long = System.currentTimeMillis()
)
