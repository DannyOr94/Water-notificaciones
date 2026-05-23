package com.watersf.app.domain.model

enum class UserType {
    ABONADO,
    FONTANERO,
    ADMINISTRADOR,
    ADMINISTRATIVO,
    SECRETARIO,
    JUNTA;

    companion object {
        fun fromString(value: String?): UserType {
            return try {
                value?.let { valueOf(it.uppercase()) } ?: ABONADO
            } catch (e: IllegalArgumentException) {
                ABONADO // Default fallback
            }
        }
    }
}
