package com.watersf.app.presentation.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// Water-SF · Design Tokens de color — paleta "agua/ASADA", dark-first,
// anclada en el navy de marca #0F172A.
//
// ESTE es el ÚNICO archivo autorizado para literales Color(0x...). [INV-4]
// Contrastes WCAG verificados sobre BackgroundBase (#0F172A) y la superficie
// más clara donde cae texto, SurfaceVariant (#1E294B).
// ============================================================================

// --- Superficies / fondo ---
val BackgroundBase = Color(0xFF0F172A)   // Fondo global (ancla de marca)
val Surface = Color(0xFF131C33)          // Cards, diálogos, app bar scrolled, chips no-sel
val SurfaceVariant = Color(0xFF1E294B)   // Elevado: no-leído, input enfocado, chip sel., skeleton
val SurfaceOffline = Color(0xFF374151)   // Banner offline (slate informativo)

// --- Bordes ---
val Outline = Color(0xFF222D4A)          // Borde default / input no-enfocado
val OutlineStrong = Color(0xFF3B4D80)    // Borde de card no-leída

// --- Marca / acento ---
val Primary = Color(0xFF0284C7)          // Botón primario (container)
val PrimaryBright = Color(0xFF38BDF8)    // Focus, links, "Nueva", punto no-leído, acentos
val Secondary = Color(0xFF06B6D4)        // Cyan secundario
val Tertiary = Color(0xFF0891B2)         // Cyan profundo
val OnPrimary = Color(0xFFFFFFFF)        // Texto/icono sobre Primary

// --- Texto ---
val TextPrimary = Color(0xFFF8FAFC)      // Títulos / texto principal (~17:1)
val TextBody = Color(0xFFCBD5E1)         // Cuerpo de mensaje (~9.6:1 sobre card)
val TextSecondary = Color(0xFF94A3B8)    // Meta, subtítulos, timestamps (~5.6:1 sobre card)
val TextMuted = Color(0xFF64748B)        // Placeholder / iconos de input / empty-state (glifos 3:1)

// --- Severidad (semánticos obligatorios) ---
val SeverityHigh = Color(0xFFF87171)            // CRÍTICO / ALTA  → rojo
val SeverityHighContainer = Color(0xFF3B1E1E)
val SeverityMedium = Color(0xFFFBBF24)          // MEDIA           → ámbar
val SeverityMediumContainer = Color(0xFF3B2A1E)
val SeverityLow = Color(0xFF34D399)             // BAJA / LEVE     → verde
val SeverityLowContainer = Color(0xFF1E3B2E)
