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
        // Pobieramy aktualną listę klientów z bazy (tylko raz)
        val clients = clientDao.getAllClients().first()
        val currentTime = System.currentTimeMillis()
        
        // 14 dni w milisekundach (14 dni * 24h * 60m * 60s * 1000ms)
        val fourteenDaysInMillis = 14L * 24 * 60 * 60 * 1000

        clients.forEach { client ->
            // Jeśli status to "Nowy" i minęło więcej niż 14 dni od utworzenia
            if (client.status == "Nowy" && (currentTime - client.createdAt) > fourteenDaysInMillis) {
                // Tworzymy kopię klienta ze zmienionym statusem
                val updatedClient = client.copy(status = "Wymaga kontaktu")
                clientDao.insertClient(updatedClient)
            }
        }

        return Result.success()
    }
}
