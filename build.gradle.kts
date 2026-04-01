// Plik: build.gradle.kts (Project Level)
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // TO JEST KLUCZOWE: Rejestracja pluginu Hilt z Twojego TOML
    alias(libs.plugins.dagger.hilt.android) apply false
}