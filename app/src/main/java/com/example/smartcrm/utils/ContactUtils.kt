package com.example.smartcrm.utils

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri

// wykorzystujemy mechanizm intent dla wyslania zadania do systemu
fun makeCall(context: Context, phoneNumber: String) {
    // usuwamy wszystko co nie jest cyfra lub znakiem +
    val cleanNumber = phoneNumber.filter { it.isDigit() || it == '+' }

    // tworzymy intencje do wywolania telefonu
    val intent = Intent(Intent.ACTION_DIAL).apply {
        data = "tel:$cleanNumber".toUri()
    }
    // uruchamiamy intencje czyli wywolujemy telefon
    context.startActivity(intent)
}

fun sendEmail(context: Context, email: String) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = "mailto:$email".toUri()
    }
    context.startActivity(intent)
}

fun openWhatsApp(context: Context, phoneNumber: String) {
    val cleanNumber = phoneNumber.replace(" ", "").replace("+", "")
    val url = "https://wa.me/$cleanNumber"
    val intent = Intent(Intent.ACTION_VIEW).apply {
        data = url.toUri()
    }
    try { // sprawdzamy czy aplikacja jest zainstalowana
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "WhatsApp nie jest zainstalowany", Toast.LENGTH_SHORT).show()
    }
}