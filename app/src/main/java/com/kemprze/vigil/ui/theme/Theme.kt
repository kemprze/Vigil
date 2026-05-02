package com.kemprze.vigil.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

enum class AppTheme {
    SYSTEM,
    SCARLET,
    MONSTERA,
    AMBER,
    INK_BLUE
}

private val ScarletLightColorScheme = lightColorScheme(
    primary = ScarletPrimaryLight,
    onPrimary = ScarletOnPrimaryLight,
    primaryContainer = ScarletPrimaryContainerLight,
    onPrimaryContainer = ScarletOnPrimaryContainerLight,
    secondary = ScarletSecondaryLight,
    onSecondary = ScarletOnSecondaryLight,
    secondaryContainer = ScarletSecondaryContainerLight,
    onSecondaryContainer = ScarletOnSecondaryContainerLight,
    tertiary = ScarletTertiaryLight,
    onTertiary = ScarletOnTertiaryLight,
    tertiaryContainer = ScarletTertiaryContainerLight,
    onTertiaryContainer = ScarletOnTertiaryContainerLight,
    error = ScarletErrorLight,
    onError = ScarletOnErrorLight,
    errorContainer = ScarletErrorContainerLight,
    onErrorContainer = ScarletOnErrorContainerLight,
    background = ScarletBackgroundLight,
    onBackground = ScarletOnBackgroundLight,
    surface = ScarletSurfaceLight,
    onSurface = ScarletOnSurfaceLight,
    surfaceVariant = ScarletSurfaceVariantLight,
    onSurfaceVariant = ScarletOnSurfaceVariantLight,
    outline = ScarletOutlineLight,
    outlineVariant = ScarletOutlineVariantLight,
    scrim = ScarletScrimLight,
    inverseSurface = ScarletInverseSurfaceLight,
    inverseOnSurface = ScarletInverseOnSurfaceLight,
    inversePrimary = ScarletInversePrimaryLight,
    surfaceDim = ScarletSurfaceDimLight,
    surfaceBright = ScarletSurfaceBrightLight,
    surfaceContainerLowest = ScarletSurfaceContainerLowestLight,
    surfaceContainerLow = ScarletSurfaceContainerLowLight,
    surfaceContainer = ScarletSurfaceContainerLight,
    surfaceContainerHigh = ScarletSurfaceContainerHighLight,
    surfaceContainerHighest = ScarletSurfaceContainerHighestLight,
)

private val ScarletDarkColorScheme = darkColorScheme(
    primary = ScarletPrimaryDark,
    onPrimary = ScarletOnPrimaryDark,
    primaryContainer = ScarletPrimaryContainerDark,
    onPrimaryContainer = ScarletOnPrimaryContainerDark,
    secondary = ScarletSecondaryDark,
    onSecondary = ScarletOnSecondaryDark,
    secondaryContainer = ScarletSecondaryContainerDark,
    onSecondaryContainer = ScarletOnSecondaryContainerDark,
    tertiary = ScarletTertiaryDark,
    onTertiary = ScarletOnTertiaryDark,
    tertiaryContainer = ScarletTertiaryContainerDark,
    onTertiaryContainer = ScarletOnTertiaryContainerDark,
    error = ScarletErrorDark,
    onError = ScarletOnErrorDark,
    errorContainer = ScarletErrorContainerDark,
    onErrorContainer = ScarletOnErrorContainerDark,
    background = ScarletBackgroundDark,
    onBackground = ScarletOnBackgroundDark,
    surface = ScarletSurfaceDark,
    onSurface = ScarletOnSurfaceDark,
    surfaceVariant = ScarletSurfaceVariantDark,
    onSurfaceVariant = ScarletOnSurfaceVariantDark,
    outline = ScarletOutlineDark,
    outlineVariant = ScarletOutlineVariantDark,
    scrim = ScarletScrimDark,
    inverseSurface = ScarletInverseSurfaceDark,
    inverseOnSurface = ScarletInverseOnSurfaceDark,
    inversePrimary = ScarletInversePrimaryDark,
    surfaceDim = ScarletSurfaceDimDark,
    surfaceBright = ScarletSurfaceBrightDark,
    surfaceContainerLowest = ScarletSurfaceContainerLowestDark,
    surfaceContainerLow = ScarletSurfaceContainerLowDark,
    surfaceContainer = ScarletSurfaceContainerDark,
    surfaceContainerHigh = ScarletSurfaceContainerHighDark,
    surfaceContainerHighest = ScarletSurfaceContainerHighestDark,
)

private val MonsteraLightColorScheme = lightColorScheme(
    primary = MonsteraPrimaryLight,
    onPrimary = MonsteraOnPrimaryLight,
    primaryContainer = MonsteraPrimaryContainerLight,
    onPrimaryContainer = MonsteraOnPrimaryContainerLight,
    secondary = MonsteraSecondaryLight,
    onSecondary = MonsteraOnSecondaryLight,
    secondaryContainer = MonsteraSecondaryContainerLight,
    onSecondaryContainer = MonsteraOnSecondaryContainerLight,
    tertiary = MonsteraTertiaryLight,
    onTertiary = MonsteraOnTertiaryLight,
    tertiaryContainer = MonsteraTertiaryContainerLight,
    onTertiaryContainer = MonsteraOnTertiaryContainerLight,
    error = MonsteraErrorLight,
    onError = MonsteraOnErrorLight,
    errorContainer = MonsteraErrorContainerLight,
    onErrorContainer = MonsteraOnErrorContainerLight,
    background = MonsteraBackgroundLight,
    onBackground = MonsteraOnBackgroundLight,
    surface = MonsteraSurfaceLight,
    onSurface = MonsteraOnSurfaceLight,
    surfaceVariant = MonsteraSurfaceVariantLight,
    onSurfaceVariant = MonsteraOnSurfaceVariantLight,
    outline = MonsteraOutlineLight,
    outlineVariant = MonsteraOutlineVariantLight,
    scrim = MonsteraScrimLight,
    inverseSurface = MonsteraInverseSurfaceLight,
    inverseOnSurface = MonsteraInverseOnSurfaceLight,
    inversePrimary = MonsteraInversePrimaryLight,
    surfaceDim = MonsteraSurfaceDimLight,
    surfaceBright = MonsteraSurfaceBrightLight,
    surfaceContainerLowest = MonsteraSurfaceContainerLowestLight,
    surfaceContainerLow = MonsteraSurfaceContainerLowLight,
    surfaceContainer = MonsteraSurfaceContainerLight,
    surfaceContainerHigh = MonsteraSurfaceContainerHighLight,
    surfaceContainerHighest = MonsteraSurfaceContainerHighestLight,
)

private val MonsteraDarkColorScheme = darkColorScheme(
    primary = MonsteraPrimaryDark,
    onPrimary = MonsteraOnPrimaryDark,
    primaryContainer = MonsteraPrimaryContainerDark,
    onPrimaryContainer = MonsteraOnPrimaryContainerDark,
    secondary = MonsteraSecondaryDark,
    onSecondary = MonsteraOnSecondaryDark,
    secondaryContainer = MonsteraSecondaryContainerDark,
    onSecondaryContainer = MonsteraOnSecondaryContainerDark,
    tertiary = MonsteraTertiaryDark,
    onTertiary = MonsteraOnTertiaryDark,
    tertiaryContainer = MonsteraTertiaryContainerDark,
    onTertiaryContainer = MonsteraOnTertiaryContainerDark,
    error = MonsteraErrorDark,
    onError = MonsteraOnErrorDark,
    errorContainer = MonsteraErrorContainerDark,
    onErrorContainer = MonsteraOnErrorContainerDark,
    background = MonsteraBackgroundDark,
    onBackground = MonsteraOnBackgroundDark,
    surface = MonsteraSurfaceDark,
    onSurface = MonsteraOnSurfaceDark,
    surfaceVariant = MonsteraSurfaceVariantDark,
    onSurfaceVariant = MonsteraOnSurfaceVariantDark,
    outline = MonsteraOutlineDark,
    outlineVariant = MonsteraOutlineVariantDark,
    scrim = MonsteraScrimDark,
    inverseSurface = MonsteraInverseSurfaceDark,
    inverseOnSurface = MonsteraInverseOnSurfaceDark,
    inversePrimary = MonsteraInversePrimaryDark,
    surfaceDim = MonsteraSurfaceDimDark,
    surfaceBright = MonsteraSurfaceBrightDark,
    surfaceContainerLowest = MonsteraSurfaceContainerLowestDark,
    surfaceContainerLow = MonsteraSurfaceContainerLowDark,
    surfaceContainer = MonsteraSurfaceContainerDark,
    surfaceContainerHigh = MonsteraSurfaceContainerHighDark,
    surfaceContainerHighest = MonsteraSurfaceContainerHighestDark,
)

private val AmberLightColorScheme = lightColorScheme(
    primary = AmberPrimaryLight,
    onPrimary = AmberOnPrimaryLight,
    primaryContainer = AmberPrimaryContainerLight,
    onPrimaryContainer = AmberOnPrimaryContainerLight,
    secondary = AmberSecondaryLight,
    onSecondary = AmberOnSecondaryLight,
    secondaryContainer = AmberSecondaryContainerLight,
    onSecondaryContainer = AmberOnSecondaryContainerLight,
    tertiary = AmberTertiaryLight,
    onTertiary = AmberOnTertiaryLight,
    tertiaryContainer = AmberTertiaryContainerLight,
    onTertiaryContainer = AmberOnTertiaryContainerLight,
    error = AmberErrorLight,
    onError = AmberOnErrorLight,
    errorContainer = AmberErrorContainerLight,
    onErrorContainer = AmberOnErrorContainerLight,
    background = AmberBackgroundLight,
    onBackground = AmberOnBackgroundLight,
    surface = AmberSurfaceLight,
    onSurface = AmberOnSurfaceLight,
    surfaceVariant = AmberSurfaceVariantLight,
    onSurfaceVariant = AmberOnSurfaceVariantLight,
    outline = AmberOutlineLight,
    outlineVariant = AmberOutlineVariantLight,
    scrim = AmberScrimLight,
    inverseSurface = AmberInverseSurfaceLight,
    inverseOnSurface = AmberInverseOnSurfaceLight,
    inversePrimary = AmberInversePrimaryLight,
    surfaceDim = AmberSurfaceDimLight,
    surfaceBright = AmberSurfaceBrightLight,
    surfaceContainerLowest = AmberSurfaceContainerLowestLight,
    surfaceContainerLow = AmberSurfaceContainerLowLight,
    surfaceContainer = AmberSurfaceContainerLight,
    surfaceContainerHigh = AmberSurfaceContainerHighLight,
    surfaceContainerHighest = AmberSurfaceContainerHighestLight,
)

private val AmberDarkColorScheme = darkColorScheme(
    primary = AmberPrimaryDark,
    onPrimary = AmberOnPrimaryDark,
    primaryContainer = AmberPrimaryContainerDark,
    onPrimaryContainer = AmberOnPrimaryContainerDark,
    secondary = AmberSecondaryDark,
    onSecondary = AmberOnSecondaryDark,
    secondaryContainer = AmberSecondaryContainerDark,
    onSecondaryContainer = AmberOnSecondaryContainerDark,
    tertiary = AmberTertiaryDark,
    onTertiary = AmberOnTertiaryDark,
    tertiaryContainer = AmberTertiaryContainerDark,
    onTertiaryContainer = AmberOnTertiaryContainerDark,
    error = AmberErrorDark,
    onError = AmberOnErrorDark,
    errorContainer = AmberErrorContainerDark,
    onErrorContainer = AmberOnErrorContainerDark,
    background = AmberBackgroundDark,
    onBackground = AmberOnBackgroundDark,
    surface = AmberSurfaceDark,
    onSurface = AmberOnSurfaceDark,
    surfaceVariant = AmberSurfaceVariantDark,
    onSurfaceVariant = AmberOnSurfaceVariantDark,
    outline = AmberOutlineDark,
    outlineVariant = AmberOutlineVariantDark,
    scrim = AmberScrimDark,
    inverseSurface = AmberInverseSurfaceDark,
    inverseOnSurface = AmberInverseOnSurfaceDark,
    inversePrimary = AmberInversePrimaryDark,
    surfaceDim = AmberSurfaceDimDark,
    surfaceBright = AmberSurfaceBrightDark,
    surfaceContainerLowest = AmberSurfaceContainerLowestDark,
    surfaceContainerLow = AmberSurfaceContainerLowDark,
    surfaceContainer = AmberSurfaceContainerDark,
    surfaceContainerHigh = AmberSurfaceContainerHighDark,
    surfaceContainerHighest = AmberSurfaceContainerHighestDark,
)

private val InkBlueLightColorScheme = lightColorScheme(
    primary = InkBluePrimaryLight,
    onPrimary = InkBlueOnPrimaryLight,
    primaryContainer = InkBluePrimaryContainerLight,
    onPrimaryContainer = InkBlueOnPrimaryContainerLight,
    secondary = InkBlueSecondaryLight,
    onSecondary = InkBlueOnSecondaryLight,
    secondaryContainer = InkBlueSecondaryContainerLight,
    onSecondaryContainer = InkBlueOnSecondaryContainerLight,
    tertiary = InkBlueTertiaryLight,
    onTertiary = InkBlueOnTertiaryLight,
    tertiaryContainer = InkBlueTertiaryContainerLight,
    onTertiaryContainer = InkBlueOnTertiaryContainerLight,
    error = InkBlueErrorLight,
    onError = InkBlueOnErrorLight,
    errorContainer = InkBlueErrorContainerLight,
    onErrorContainer = InkBlueOnErrorContainerLight,
    background = InkBlueBackgroundLight,
    onBackground = InkBlueOnBackgroundLight,
    surface = InkBlueSurfaceLight,
    onSurface = InkBlueOnSurfaceLight,
    surfaceVariant = InkBlueSurfaceVariantLight,
    onSurfaceVariant = InkBlueOnSurfaceVariantLight,
    outline = InkBlueOutlineLight,
    outlineVariant = InkBlueOutlineVariantLight,
    scrim = InkBlueScrimLight,
    inverseSurface = InkBlueInverseSurfaceLight,
    inverseOnSurface = InkBlueInverseOnSurfaceLight,
    inversePrimary = InkBlueInversePrimaryLight,
    surfaceDim = InkBlueSurfaceDimLight,
    surfaceBright = InkBlueSurfaceBrightLight,
    surfaceContainerLowest = InkBlueSurfaceContainerLowestLight,
    surfaceContainerLow = InkBlueSurfaceContainerLowLight,
    surfaceContainer = InkBlueSurfaceContainerLight,
    surfaceContainerHigh = InkBlueSurfaceContainerHighLight,
    surfaceContainerHighest = InkBlueSurfaceContainerHighestLight,
)

private val InkBlueDarkColorScheme = darkColorScheme(
    primary = InkBluePrimaryDark,
    onPrimary = InkBlueOnPrimaryDark,
    primaryContainer = InkBluePrimaryContainerDark,
    onPrimaryContainer = InkBlueOnPrimaryContainerDark,
    secondary = InkBlueSecondaryDark,
    onSecondary = InkBlueOnSecondaryDark,
    secondaryContainer = InkBlueSecondaryContainerDark,
    onSecondaryContainer = InkBlueOnSecondaryContainerDark,
    tertiary = InkBlueTertiaryDark,
    onTertiary = InkBlueOnTertiaryDark,
    tertiaryContainer = InkBlueTertiaryContainerDark,
    onTertiaryContainer = InkBlueOnTertiaryContainerDark,
    error = InkBlueErrorDark,
    onError = InkBlueOnErrorDark,
    errorContainer = InkBlueErrorContainerDark,
    onErrorContainer = InkBlueOnErrorContainerDark,
    background = InkBlueBackgroundDark,
    onBackground = InkBlueOnBackgroundDark,
    surface = InkBlueSurfaceDark,
    onSurface = InkBlueOnSurfaceDark,
    surfaceVariant = InkBlueSurfaceVariantDark,
    onSurfaceVariant = InkBlueOnSurfaceVariantDark,
    outline = InkBlueOutlineDark,
    outlineVariant = InkBlueOutlineVariantDark,
    scrim = InkBlueScrimDark,
    inverseSurface = InkBlueInverseSurfaceDark,
    inverseOnSurface = InkBlueInverseOnSurfaceDark,
    inversePrimary = InkBlueInversePrimaryDark,
    surfaceDim = InkBlueSurfaceDimDark,
    surfaceBright = InkBlueSurfaceBrightDark,
    surfaceContainerLowest = InkBlueSurfaceContainerLowestDark,
    surfaceContainerLow = InkBlueSurfaceContainerLowDark,
    surfaceContainer = InkBlueSurfaceContainerDark,
    surfaceContainerHigh = InkBlueSurfaceContainerHighDark,
    surfaceContainerHighest = InkBlueSurfaceContainerHighestDark,
)

@Composable
fun TODOPrototypingTheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    appFont: AppFont = AppFont.LATO,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
            when (appTheme) {
                AppTheme.SYSTEM -> if (darkTheme) ScarletDarkColorScheme else ScarletLightColorScheme
                AppTheme.SCARLET -> if (darkTheme) ScarletDarkColorScheme else ScarletLightColorScheme
                AppTheme.MONSTERA -> if (darkTheme) MonsteraDarkColorScheme else MonsteraLightColorScheme
                AppTheme.AMBER -> if (darkTheme) AmberDarkColorScheme else AmberLightColorScheme
                AppTheme.INK_BLUE -> if (darkTheme) InkBlueDarkColorScheme else InkBlueLightColorScheme
            }
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typographyFor(appFont),
        content = content
    )
}

fun themePrimaryColor(theme: AppTheme, darkTheme: Boolean): Color {
    return when (theme) {
        AppTheme.SYSTEM -> Color.Gray
        AppTheme.SCARLET -> if (darkTheme) ScarletPrimaryDark else ScarletPrimaryLight
        AppTheme.MONSTERA -> if (darkTheme) MonsteraPrimaryDark else MonsteraPrimaryLight
        AppTheme.AMBER -> if (darkTheme) AmberPrimaryDark else AmberPrimaryLight
        AppTheme.INK_BLUE -> if (darkTheme) InkBluePrimaryDark else InkBluePrimaryLight
    }
}

