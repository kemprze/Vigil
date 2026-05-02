package com.kemprze.vigil.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.kemprze.vigil.R

enum class AppFont {
    PLAYFAIR,
    LORA,
    MONTSERRAT,
    LATO,
    ATKINSON,
    COURIER_PRIME
}

val PlayfairFamily = FontFamily(
    Font(R.font.playfair_display_variable),
    Font(R.font.playfair_display_italic_variable, style = FontStyle.Italic),
)

val LoraFamily = FontFamily(
    Font(R.font.lora_variable),
    Font(R.font.lora_italic_variable, style = FontStyle.Italic),
)

val MontserratFamily = FontFamily(
    Font(R.font.montserrat_variable),
    Font(R.font.montserrat_italic_variable, style = FontStyle.Italic)
)

val LatoFamily = FontFamily(
    Font(R.font.lato_regular, FontWeight.Normal),
    Font(R.font.lato_bold, FontWeight.Bold),
    Font(R.font.lato_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.lato_bold_italic, FontWeight.Bold, FontStyle.Italic),
)

val AtkinsonFamily = FontFamily(
    Font(R.font.atkinson_hyperlegible_regular, FontWeight.Normal),
    Font(R.font.atkinson_hyperlegible_bold, FontWeight.Bold),
    Font(R.font.atkinson_hyperlegible_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.atkinson_hyperlegible_bold_italic, FontWeight.Bold, FontStyle.Italic),
)

val CourierPrimeFamily = FontFamily(
    Font(R.font.courier_prime_regular, FontWeight.Normal),
    Font(R.font.courier_prime_bold, FontWeight.Bold),
    Font(R.font.courier_prime_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.courier_prime_bold_italic, FontWeight.Bold, FontStyle.Italic),
)

fun typographyFor(font: AppFont): Typography {
    val family = when (font) {
        AppFont.PLAYFAIR -> PlayfairFamily
        AppFont.LORA -> LoraFamily
        AppFont.MONTSERRAT -> MontserratFamily
        AppFont.LATO -> LatoFamily
        AppFont.ATKINSON -> AtkinsonFamily
        AppFont.COURIER_PRIME -> CourierPrimeFamily
    }

    return Typography(
        displayLarge = TextStyle(fontFamily = family, fontWeight = FontWeight.W600, fontSize = 57.sp, lineHeight = 64.sp, letterSpacing = (-0.25).sp),
        displayMedium = TextStyle(fontFamily = family, fontWeight = FontWeight.W600, fontSize = 45.sp, lineHeight = 52.sp, letterSpacing = 0.sp),
        displaySmall = TextStyle(fontFamily = family, fontWeight = FontWeight.W600, fontSize = 36.sp, lineHeight = 44.sp, letterSpacing = 0.sp),
        headlineLarge = TextStyle(fontFamily = family, fontWeight = FontWeight.W600, fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = 0.sp),
        headlineMedium = TextStyle(fontFamily = family, fontWeight = FontWeight.W600, fontSize = 28.sp, lineHeight = 36.sp, letterSpacing = 0.sp),
        headlineSmall = TextStyle(fontFamily = family, fontWeight = FontWeight.W600, fontSize = 24.sp, lineHeight = 32.sp, letterSpacing = 0.sp),
        titleLarge = TextStyle(fontFamily = family, fontWeight = FontWeight.W600, fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = 0.sp),
        titleMedium = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp),
        titleSmall = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
        bodyLarge = TextStyle(fontFamily = family, fontWeight = FontWeight.W600, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp),
        bodyMedium = TextStyle(fontFamily = family, fontWeight = FontWeight.W600, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp),
        bodySmall = TextStyle(fontFamily = family, fontWeight = FontWeight.W600, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
        labelLarge = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
        labelMedium = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
        labelSmall = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
    )
}

fun fontFamilyFor(font: AppFont): FontFamily {
    return when (font) {
        AppFont.PLAYFAIR -> PlayfairFamily
        AppFont.LORA -> LoraFamily
        AppFont.MONTSERRAT -> MontserratFamily
        AppFont.LATO -> LatoFamily
        AppFont.ATKINSON -> AtkinsonFamily
        AppFont.COURIER_PRIME -> CourierPrimeFamily
    }
}

val Typography = typographyFor(AppFont.MONTSERRAT)
