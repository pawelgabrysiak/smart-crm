package com.example.smartcrm.utils

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.smartcrm.data.ClientDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

/**
 * Robot pracujący w tle, który sprawdza wiek kontaktów.
 * Jeśli kontakt ma status "Nowy" i minęło 14 dni, zmienia go na "Wymaga kontaktu".
 */
@HiltWorker
class StatusUpdateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val clientDao: ClientDao
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // Pobieramy aktualną listę klientów i interakcji (tylko raz)
        val clients = clientDao.getAllClients().first()
        val allInteractions = clientDao.getAllInteractions().first()
        val currentTime = System.currentTimeMillis()
        
        // Definicja "zamrożenia": 14 dni bez kontaktu
        val fourteenDaysInMillis = 14L * 24 * 60 * 60 * 1000
        var frozenCount = 0

        clients.forEach { client ->
            // Znajdź ostatnią interakcję dla tego klienta
            val lastInteraction = allInteractions
                .filter { it.clientId == client.id }
                .maxByOrNull { it.timestamp }
            
            // Czas od ostatniego kontaktu (jeśli brak kontaktu, licz od daty utworzenia)
            val timeSinceLastContact = if (lastInteraction != null) {
                currentTime - lastInteraction.timestamp
            } else {
                currentTime - client.createdAt
            }

            // Jeśli minęło więcej niż 14 dni i klient nie ma jeszcze statusu "Zamrożony"
            if (timeSinceLastContact > fourteenDaysInMillis && client.status != "Zamrożony") {
                val updatedClient = client.copy(status = "Zamrożony")
                clientDao.insertClient(updatedClient)
                frozenCount++
            }
        }

        // --- Sprawdzanie Deadline'ów ---
        val soonDeadlineInMillis = 24L * 60 * 60 * 1000 // 24h
        var soonDeadlinesCount = 0
        var overdueDeadlinesCount = 0

        clients.forEach { client ->
            client.deadline?.let { deadline ->
                if (!client.deadlineCompleted) {
                    val timeToDeadline = deadline - currentTime
                    
                    if (timeToDeadline < 0) {
                        // Termin minął, a nie jest oznaczony jako wykonany
                        overdueDeadlinesCount++
                    } else if (timeToDeadline <= soonDeadlineInMillis) {
                        // Termin jest dzisiaj/jutro (do 24h)
                        soonDeadlinesCount++
                    }
                }
            }
        }

        if (overdueDeadlinesCount > 0) {
            NotificationHelper.showNotification(
                applicationContext,
                "Smart CRM: Zaległe zadania! ⚠️",
                "Masz $overdueDeadlinesCount zaległych terminów, które wymagają uwagi!"
            )
        }

        if (soonDeadlinesCount > 0) {
            NotificationHelper.showNotification(
                applicationContext,
                "Smart CRM: Nadchodzące terminy",
                "Masz $soonDeadlinesCount klientów z terminem na dzisiaj lub jutro!"
            )
        }

        if (frozenCount > 0) {
            NotificationHelper.showNotification(
                applicationContext,
                "Smart CRM: Zamrożone kontakty",
                "Masz $frozenCount kontaktów, które nie były obsługiwane od ponad 14 dni!"
            )
        }

        return Result.success()
    }
}
