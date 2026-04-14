# Smart CRM

**Smart CRM** to nowoczesna aplikacja mobilna stworzona z myślą o freelancerach. Nie jest to zwykła książka adresowa, ale inteligentny asystent, który pomaga w utrzymywaniu relacji biznesowych, sam pilnuje terminów kontaktu i pomaga w analizie zysków.


## Kluczowe funkcjonalności

**1. Zarządzanie klientami i Analiza**
* Dodawanie, edycja i usuwanie klientów.
* Przechowywane dane: imię, email, telefon, status relacji oraz całkowity przychód (wartość klienta).

**2. Szybka komunikacja i Lista kontaktów**
* Przegląd wszystkich klientów z opcją wyszukiwania i filtrowania.
* Szybki kontakt z poziomu aplikacji (jedno kliknięcie, by zadzwonić, napisać SMS lub e-mail przez systemowe aplikacje).

**3. Notatki i Zarządzanie zadaniami**
* Dodawanie notatek i historii kontaktu do profilu klienta.
* Przypisywanie terminów (deadlines) do konkretnych projektów lub ustaleń.

**4. Automatyzacja i Przypomnienia (Funkcja SMART)**
* Ustawianie przypomnień i powiadomienia systemowe na telefon.
* **Inteligentne statusy:** system sam zmienia status klienta na "Zimny kontakt", jeśli wykryje brak interakcji przez 14 dni.

**5. Inteligentny Asystent (Opcjonalnie)**
* Możliwość wygenerowania treści wiadomości do klienta przy pomocy sztucznej inteligencji na podstawie wcześniejszych notatek.

---

## Wykorzystane technologie

Aplikacja została napisana w najnowszych standardach tworzenia aplikacji na system Android:
* **Język:** Kotlin
* **Interfejs użytkownika (UI):** Jetpack Compose (Material Design 3)
* **Architektura:** MVVM (Model-View-ViewModel)
* **Wstrzykiwanie zależności:** Dagger Hilt
* **Baza danych:** Room (lokalny zapis danych w telefonie)
* **Zadania w tle:** WorkManager (do automatyzacji statusów i powiadomień)

---

## Harmonogram prac (Roadmap)

**MARZEC (UI i MVP)**
- [x] Sprint 1: Konfiguracja (Hilt, Compose), główna lista.
- [x] Sprint 2: Formularze i Edycja klienta (pamięć tymczasowa).

**KWIECIEŃ (Komunikacja i Baza) - *Aktualny etap***
- [x] Sprint 3: Usuwanie klienta i szybki kontakt (Intenty systemowe: Zadzwoń / E-mail / WhatsApp).
- [ ] Sprint 4: Wdrożenie bazy Room (trwały zapis) i wyszukiwarka.

**MAJ (Automatyzacja - CORE)**
- [ ] Sprint 5: Inteligentne Statusy (WorkManager) – automat po 14 dniach.
- [ ] Sprint 6: Lokalne powiadomienia i szlify UX.

**CZERWIEC (Finał i Opcje)**
- [ ] Sprint 7: Testy z użytkownikami (UAT), łatanie błędów, dokumentacja.
- [ ] Sprint 8 (Opcjonalnie): Generowanie maili przez AI, wskaźnik zysków (LTV).

---

## Jak uruchomić projekt lokalnie?

1. Sklonuj to repozytorium na swój komputer: `git clone [TUTAJ_WKLEJ_LINK_DO_SWOJEGO_REPO]`
2. Otwórz projekt w programie **Android Studio**.
3. Poczekaj, aż Gradle pobierze wszystkie potrzebne biblioteki.
4. Uruchom aplikację na fizycznym urządzeniu z Androidem lub na emulatorze.
