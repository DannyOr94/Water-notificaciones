plugins {
    id("com.android.application") version "8.3.2" apply false
    id("com.android.library") version "8.3.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false

    // Alínea la versión global de Hilt (2.50) con la que pusimos en las dependencias de la app
    id("com.google.dagger.hilt.android") version "2.50" apply false
}