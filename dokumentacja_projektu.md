# Dokumentacja Projektu: Smart CRM - Rozbudowa i Refaktor

Oto podsumowanie dzisiejszych prac nad aplikacją, przygotowane w logicznej kolejności.

## 1. Architektura i Refaktor (Uporządkowanie Kodu)
Przenieśliśmy kod z jednego wielkiego pliku `MainActivity.kt` do odpowiednich folderów (warstw), aby aplikacja była profesjonalna.

*   **`data/`**: Tu leżą dane (Baza Room, Modele).
*   **`ui/`**: Tu leży wygląd (Ekrany, ViewModel).
*   **`di/`**: Konfiguracja Hilt (Wstrzykiwanie bazy danych).
*   **`utils/`**: Narzędzia pomocnicze (Dzwonienie, WorkManager).

---

## 2. Warstwa Danych (Baza danych Room)
Zaimplementowaliśmy relacyjną bazę danych, która przechowuje informacje na stałe.

*   **`data/Client.kt` (Klasa `Client`)**: Szablon klienta (id, imię, email, telefon, status, data utworzenia).
*   **`data/Note.kt` (Klasa `Note`)**: Tabela na notatki przypisane do konkretnego klienta.
*   **`data/Interaction.kt` (Klasa `Interaction`)**: Tabela na historię kontaktów (połączenia, maile).
*   **`data/ClientDao.kt` (Interfejs `ClientDao`)**: Zawiera zapytania SQL (`INSERT`, `DELETE`, `SELECT`).
*   **`data/AppDatabase.kt`**: Główny punkt dostępu do bazy (wersja 3).
*   **`data/ClientRepository.kt`**: "Kierownik danych" – pośredniczy między bazą a resztą aplikacji.

---

## 3. Warstwa Logiki (ViewModel)
*   **`ui/CrmViewModel.kt` (Klasa `CrmViewModel`)**: "Mózg" aplikacji. 
    *   Przechowuje stan ekranu (`CrmUiState`).
    *   **Wyszukiwarka**: Funkcja `filteredClients` dynamicznie filtruje listę w miarę wpisywania tekstu.
    *   **Notatki i Historia**: Funkcje `addNote` i `logInteraction` dbają o zapisywanie dodatkowych informacji.

---

## 4. Warstwa Wyglądu (Jetpack Compose)
Aplikacja stała się wielostronicowa (Nawigacja).

*   **`ui/CrmScreens.kt`**:
    *   **`CrmScreen`**: Ekran główny z listą klientów i nowoczesnym przyciskiem dodawania (+).
    *   **`SearchScreen`**: Dedykowany ekran wyszukiwarki.
    *   **`ClientDetailScreen`**: **NOWOŚĆ.** Ekran szczegółów klienta z sekcją notatek i historią kontaktu.
    *   **`ClientItem`**: Komponent pojedynczej karty klienta na liście.

---

## 5. Funkcje Inteligentne (Praca w tle)
*   **`utils/StatusUpdateWorker.kt` (Klasa `StatusUpdateWorker`)**: Automat, który raz na dobę sprawdza, czy od dodania klienta minęło 14 dni. Jeśli tak, zmienia status na "Wymaga kontaktu".
*   **`MainActivity.kt`**: Funkcja `scheduleStatusUpdates` planuje działanie tego automatu.

---

## 6. Integracje i Narzędzia
*   **`utils/ContactUtils.kt`**: Funkcje wywołujące systemowe aplikacje:
    *   `makeCall`: Otwiera dialer z numerem telefonu.
    *   `sendEmail`: Otwiera aplikację pocztową.
    *   `openWhatsApp`: Przenosi bezpośrednio do czatu WhatsApp.

---

## Podsumowanie "Efektu WOW"
1.  **Automatyzacja**: WorkManager sam zmienia statusy klientów po 14 dniach bez ingerencji użytkownika.
2.  **Auto-Logowanie**: Każde kliknięcie w ikonę kontaktu (telefon/mail) jest automatycznie zapisywane w historii klienta.
3.  **Modern UI**: Wykorzystanie Material 3, BottomSheet (wysuwany formularz) i płynnej nawigacji między ekranami.
