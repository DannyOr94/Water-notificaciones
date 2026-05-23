pluginManagement {
    repositories {
        google()            // <--- Servidor oficial de Google para bajar el plugin de Android
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()            // <--- Servidor para las librerías de la app (Room, Hilt, etc.)
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "Water-notificaciones"
include(":app")             // <--- Obligatorio para que reconozca la carpeta de tu aplicación móvil