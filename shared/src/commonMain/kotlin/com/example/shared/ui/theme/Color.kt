package com.example.shared.ui.theme

import androidx.compose.ui.graphics.Color

// ── Premium Neutral Base ──────────────────────────────────────────────────────
val PremiumBlack      = Color(0xFF111827)   // Quiet ink
val PremiumInk        = Color(0xFF111118)   // Deep ink card
val PremiumCharcoal   = Color(0xFF1A1A27)   // Slightly lighter dark surface
val PremiumSlate      = Color(0xFF4B5563)   // Muted label text — readable on porcelain

val PremiumBg         = Color(0xFFFFFDFC)   // Warm porcelain
val PremiumSurface    = Color(0xFFFFFFFF)   // Pure white card
val PremiumBorder     = Color(0xFFEAE9E6)   // Very subtle warm border

// ── Primary Accent ─────────────────────────────────────────────────────────
val PremiumViolet     = Color(0xFF123242)   // Deep navy brand accent
val PremiumVioletSoft = Color(0xFFE8F0F2)   // Soft ink tint
val PremiumVioletMid  = Color(0xFFCCE0E4)   // Mid ink tint

// ── Semantic Accent Colors ────────────────────────────────────────────────
val PremiumEmerald    = Color(0xFF168466)   // Positive / income green
val PremiumEmeraldBg  = Color(0xFFE3F4EE)   // Emerald tint background
val PremiumRose       = Color(0xFFC55355)   // Expense / negative red
val PremiumRoseBg     = Color(0xFFF9E8E7)   // Rose tint background
val PremiumAmber      = Color(0xFFFFA827)   // Warning / pending amber
val PremiumAmberBg    = Color(0xFFFFF5E6)   // Amber tint background

// ── Hero Dark Card (Payday) ───────────────────────────────────────────────
val PremiumHeroCard   = Color(0xFF111118)   // Deep dark hero card
val PremiumHeroAccent = Color(0xFF6C52EF)   // Violet accent on dark

// ── Legacy aliases for component compatibility ─────────────────────────────
val BentoIndigoPrimary   = PremiumViolet
val BentoIndigoDark      = Color(0xFF3D2DB5)
val BentoIndigoLight     = PremiumVioletSoft
val BentoIndigoContainer = PremiumVioletMid

val BentoSlateBg         = PremiumBg
val BentoSlateCard       = PremiumSurface
val BentoDarkCard        = PremiumInk
val BentoCardBorder      = PremiumBorder

val BentoEmerald         = PremiumEmerald
val BentoEmeraldLight    = PremiumEmeraldBg
val BentoAmber           = PremiumAmber
val BentoAmberLight      = PremiumAmberBg
val BentoRose            = PremiumRose
val BentoRoseLight       = PremiumRoseBg

// Legacy
val Emerald80        = Color(0xFFA7F3D0)
val EmeraldGreen     = PremiumEmerald
val EmeraldDark      = Color(0xFF005C40)
val TealSecondary    = Color(0xFF0D9488)
val TealLight        = Color(0xFF99F6E4)
val AmberTertiary    = PremiumAmber
val AmberLight       = PremiumAmberBg
val CoralExpense     = PremiumRose
val MintIncome       = PremiumEmerald
val SoftBackgroundLight = PremiumBg
val CardSurfaceLight    = PremiumSurface
val SoftBackgroundDark  = Color(0xFF0F172A)
val CardSurfaceDark     = Color(0xFF1E293B)
