## Prezentacja śródsemestralna – wykonane funkcjonalności

Na obecnym etapie projektu zrealizowano kluczowe elementy MVP aplikacji **Smart CRM**, obejmujące zarówno warstwę interfejsu użytkownika, jak i podstawową logikę biznesową oraz trwałe przechowywanie danych.

###  Zrealizowane funkcje

**1. Interfejs użytkownika (UI)**
- Utworzono główny ekran listy klientów w technologii Jetpack Compose.
- Zaimplementowano formularze dodawania i edycji klienta.
- Wdrożono podstawową nawigację między ekranami.

**2. Zarządzanie klientami**
- Dodawanie nowych klientów.
- Edycja danych klienta.
- Usuwanie klientów z listy.

**3. Komunikacja**
- Integracja z systemowymi intentami:
  - wykonywanie połączeń telefonicznych,
  - wysyłanie e-maili,
  - szybki kontakt przez komunikatory (np. WhatsApp).

**4. Baza danych**
- Wdrożono lokalną bazę danych Room.
- Zapewniono trwałe przechowywanie danych klientów.
- Dodano mechanizm wyszukiwania klientów.

---

###  Aktualny stan projektu

Projekt znajduje się na etapie ukończonego **MVP (Minimum Viable Product)**, co oznacza:
- aplikacja umożliwia podstawowe zarządzanie klientami,
- dane są zapisywane lokalnie,
- użytkownik może szybko kontaktować się z klientami.

---

###  Kolejne kroki

W następnych etapach planowane jest wdrożenie funkcji:
- automatycznych statusów klientów (WorkManager),
- systemu powiadomień,
- ulepszeń UX oraz testów użytkowników,
- opcjonalnie: integracji z AI.
