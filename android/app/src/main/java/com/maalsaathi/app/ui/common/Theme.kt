package com.maalsaathi.app.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maalsaathi.app.R
import java.text.NumberFormat
import java.util.Locale

private val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

val Poppins = FontFamily(
    Font(googleFont = GoogleFont("Poppins"), fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Poppins"), fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Poppins"), fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = GoogleFont("Poppins"), fontProvider = fontProvider, weight = FontWeight.Bold),
)

val NotoSans = FontFamily(
    Font(googleFont = GoogleFont("Noto Sans"), fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Noto Sans"), fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Noto Sans"), fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = GoogleFont("Noto Sans"), fontProvider = fontProvider, weight = FontWeight.Bold),
)

val MsCrimson = Color(0xFFDC143C)
val MsCrimsonDark = Color(0xFFB01030)
val MsCrimsonTint = Color(0xFFFFF0F0)
val MsGreen = Color(0xFF25D366)
val MsCream = Color(0xFFF5F5F0)
val MsWhite = Color(0xFFFFFFFF)
val MsTextPrimary = Color(0xFF1A1D24)
val MsTextSecondary = Color(0xFF6B7280)
val MsLoss = Color(0xFFDC4C4C)
val MsWarning = Color(0xFFF59E0B)
val MsBorder = Color(0xFFE5E2D9)
val MsDivider = Color(0xFFF0EDE8)
val MsStatBg = Color(0xFFF5F3EE)
val MsShadow = Color(0x0F000000)

@Immutable
data class MsColors(
    val primary: Color = MsCrimson,
    val primaryDark: Color = MsCrimsonDark,
    val primaryTint: Color = MsCrimsonTint,
    val onPrimary: Color = MsWhite,
    val background: Color = MsCream,
    val card: Color = MsWhite,
    val textPrimary: Color = MsTextPrimary,
    val textSecondary: Color = MsTextSecondary,
    val profit: Color = MsGreen,
    val loss: Color = MsLoss,
    val warning: Color = MsWarning,
    val border: Color = MsBorder,
    val divider: Color = MsDivider,
    val statBg: Color = MsStatBg,
    val shadow: Color = MsShadow,
)

val LocalMsColors = staticCompositionLocalOf { MsColors() }

object Ms {
    val colors: MsColors @Composable get() = LocalMsColors.current
}

object MsType {
    val displayLarge = TextStyle(fontFamily = Poppins, fontSize = 28.sp, fontWeight = FontWeight.Bold, lineHeight = 36.sp, color = MsTextPrimary)
    val headlineLarge = TextStyle(fontFamily = Poppins, fontSize = 24.sp, fontWeight = FontWeight.Bold, lineHeight = 32.sp, color = MsTextPrimary)
    val headlineMedium = TextStyle(fontFamily = Poppins, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, lineHeight = 28.sp, color = MsTextPrimary)
    val titleLarge = TextStyle(fontFamily = Poppins, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, lineHeight = 26.sp, color = MsTextPrimary)
    val titleMedium = TextStyle(fontFamily = Poppins, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, lineHeight = 24.sp, color = MsTextPrimary)
    val bodyLarge = TextStyle(fontFamily = NotoSans, fontSize = 16.sp, fontWeight = FontWeight.Normal, lineHeight = 24.sp, color = MsTextPrimary)
    val bodyMedium = TextStyle(fontFamily = NotoSans, fontSize = 14.sp, fontWeight = FontWeight.Normal, lineHeight = 21.sp, color = MsTextSecondary)
    val labelLarge = TextStyle(fontFamily = NotoSans, fontSize = 13.sp, fontWeight = FontWeight.Medium, lineHeight = 18.sp, color = MsTextSecondary)
    val labelSmall = TextStyle(fontFamily = NotoSans, fontSize = 11.sp, fontWeight = FontWeight.Normal, lineHeight = 16.sp, color = MsTextSecondary)
    val amountLarge = TextStyle(fontFamily = Poppins, fontSize = 26.sp, fontWeight = FontWeight.Bold, lineHeight = 32.sp, letterSpacing = 0.sp)
    val amountMedium = TextStyle(fontFamily = Poppins, fontSize = 18.sp, fontWeight = FontWeight.Bold, lineHeight = 24.sp, letterSpacing = 0.sp)
    val button = TextStyle(fontFamily = Poppins, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, lineHeight = 22.sp)
    val sectionHeader = TextStyle(fontFamily = NotoSans, fontSize = 13.sp, fontWeight = FontWeight.Medium, lineHeight = 18.sp, letterSpacing = 0.sp, color = MsTextSecondary)
}

object Sp {
    val screenHPad = 20.dp
    val sectionGap = 20.dp
    val cardGap = 10.dp
    val cardPad = 16.dp
    val cardRadius = 16.dp
    val buttonRadius = 14.dp
    val inputRadius = 12.dp
    val primaryCta = 60.dp
    val secondaryCta = 52.dp
    val cardMinH = 72.dp
    val bottomNavH = 64.dp
    val iconButton = 48.dp
    val micButton = 64.dp
    val cardBorder = 0.5.dp
    val cardShadowElevation = 2.dp
}

@Composable
fun MaalSaathiTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalMsColors provides MsColors()) { content() }
}

private val indianFmt: NumberFormat by lazy {
    NumberFormat.getNumberInstance(Locale("en", "IN")).apply { maximumFractionDigits = 0 }
}

fun formatRupees(amount: Long): String {
    if (amount == 0L) return "₹0"
    val abs = kotlin.math.abs(amount)
    return if (amount < 0) "-₹${indianFmt.format(abs)}" else "₹${indianFmt.format(abs)}"
}

fun formatDuration(millis: Long): String {
    val totalMin = millis / 60_000
    val h = totalMin / 60; val m = totalMin % 60
    return when { h == 0L -> "$m minute"; m == 0L -> "$h ghante"; else -> "$h ghante $m minute" }
}

fun formatWeight(tons: Double): String =
    if (tons == tons.toLong().toDouble()) "${tons.toLong()} ton" else "$tons ton"

fun capWords(s: String): String =
    s.split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
