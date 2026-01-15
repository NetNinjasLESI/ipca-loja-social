package com.ipca.lojasocial.presentation.ui.theme

import androidx.compose.ui.graphics.Color

/* =====================================================
 * PALETA MODERNA COM IDENTIDADE VERDE (IPCA)
 * Verde usado com intenção, não em excesso
 * ===================================================== */

/* ---------- PRIMARY (Identidade IPCA) ---------- */

val Primary = Color(0xFF1B5E20)              // Verde escuro institucional
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFFC8E6C9)     // Verde muito claro
val OnPrimaryContainer = Color(0xFF0B2E13)

/* ---------- SECONDARY (Neutro moderno) ---------- */

val Secondary = Color(0xFF455A64)            // Cinza azulado
val OnSecondary = Color(0xFFFFFFFF)
val SecondaryContainer = Color(0xFFCFD8DC)
val OnSecondaryContainer = Color(0xFF1C313A)

/* ---------- TERTIARY (Atenção / Pendente) ---------- */

val Tertiary = Color(0xFFFF9800)             // Âmbar
val OnTertiary = Color(0xFF000000)
val TertiaryContainer = Color(0xFFFFE0B2)
val OnTertiaryContainer = Color(0xFF2E1500)

/* ---------- ERROR ---------- */

val Error = Color(0xFFD32F2F)
val OnError = Color(0xFFFFFFFF)
val ErrorContainer = Color(0xFFFFCDD2)
val OnErrorContainer = Color(0xFF5F1A1A)

/* ---------- BACKGROUND & SURFACE ---------- */

val Background = Color(0xFFFAFAFA)
val OnBackground = Color(0xFF1A1C1E)

val Surface = Color(0xFFFFFFFF)
val OnSurface = Color(0xFF1A1C1E)

val SurfaceVariant = Color(0xFFE7E8EC)
val OnSurfaceVariant = Color(0xFF44464E)

/* ---------- OUTLINES ---------- */

val Outline = Color(0xFFCAC4D0)
val OutlineVariant = Color(0xFFE0E0E0)

/* ---------- SUCCESS / WARNING / INFO ---------- */

val Success = Color(0xFF2E7D32)               // Verde sucesso (diferente do primary)
val OnSuccess = Color(0xFFFFFFFF)
val SuccessContainer = Color(0xFFC8E6C9)

val Warning = Color(0xFFFFA726)
val OnWarning = Color(0xFF000000)
val WarningContainer = Color(0xFFFFE0B2)

val Info = Color(0xFF0288D1)                  // Azul informativo
val OnInfo = Color(0xFFFFFFFF)
val InfoContainer = Color(0xFFB3E5FC)

/* ---------- ESTADOS DA APLICAÇÃO ---------- */

// Campanhas
val CampaignActive = Success
val CampaignDraft = Color(0xFF9E9E9E)
val CampaignCompleted = Info
val CampaignCancelled = Error

// Candidaturas
val ApplicationPending = Warning
val ApplicationApproved = Success
val ApplicationRejected = Error

// Entregas
val DeliveryPending = Warning
val DeliveryDelivered = Success
val DeliveryCancelled = Color(0xFF9E9E9E)
