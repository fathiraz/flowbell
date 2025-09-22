package com.fathiraz.flowbell.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.foundation.shape.RoundedCornerShape

// Modern 2025 Banking-Inspired Color Palette
object ModernColors {
    // Primary teal gradient
    val TealPrimary = Color(0xFF00C896)
    val TealLight = Color(0xFF4DDBB8)
    val TealDark = Color(0xFF00A67E)

    // Secondary red gradient
    val RedPrimary = Color(0xFFE53E3E)
    val RedLight = Color(0xFFEF5A5A)
    val RedDark = Color(0xFFCC2929)

    // Accent colors
    val Orange = Color(0xFFFF9500)
    val Purple = Color(0xFF6366F1)
    val Blue = Color(0xFF3B82F6)
    val Green = Color(0xFF10B981)
    val Red = Color(0xFFEF4444)

    // Neutral colors
    val Gray50 = Color(0xFFFAFAFA)
    val Gray100 = Color(0xFFF5F5F5)
    val Gray200 = Color(0xFFE5E5E5)
    val Gray300 = Color(0xFFD1D5DB)
    val Gray400 = Color(0xFF9CA3AF)
    val Gray500 = Color(0xFF6B7280)
    val Gray600 = Color(0xFF4B5563)
    val Gray700 = Color(0xFF374151)
    val Gray800 = Color(0xFF1F2937)
    val Gray900 = Color(0xFF111827)
}

// 2025 Modern Banking Design & Minimalist Dark Mode
private val DarkColorScheme = darkColorScheme(
    // Primary colors - Modern teal/green (inspired by banking app design)
    primary = Color(0xFF00C896), // Modern teal - Banking app primary
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF1A1A1A), // Modern dark container
    onPrimaryContainer = Color(0xFF00C896), // Primary color for contrast

    // Secondary colors - Vibrant red accent
    secondary = Color(0xFFE53E3E), // Modern red - Banking app secondary
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF1A1A1A), // Modern dark container
    onSecondaryContainer = Color(0xFFE53E3E), // Secondary color for contrast

    // Tertiary colors - Warm orange accent
    tertiary = Color(0xFFFF9500), // Modern orange - Warm accent
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF1A1A1A), // Modern dark container
    onTertiaryContainer = Color(0xFFFF9500), // Tertiary color for contrast
    
    // Background colors - Flat dark theme
    background = Color(0xFF000000), // Pure black - Minimalist
    onBackground = Color(0xFFFFFFFF), // Pure white - Maximum contrast
    surface = Color(0xFF1C1C1E), // iOS dark surface - Flat
    onSurface = Color(0xFFFFFFFF), // Pure white text
    surfaceVariant = Color(0xFF2C2C2E), // Slightly lighter - Flat variant
    onSurfaceVariant = Color(0xFF8E8E93), // iOS secondary label - Subtle
    
    // Error colors - Clean error
    error = Color(0xFFFF3B30), // iOS Red - Clean error
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFF1C1C1E), // Flat dark container
    onErrorContainer = Color(0xFFFF3B30), // Error color for contrast
    
    // Outline colors - Minimal borders
    outline = Color(0xFF3A3A3C), // Subtle border
    outlineVariant = Color(0xFF2C2C2E), // Very subtle border
    
    // Surface tint - Clean tinting
    surfaceTint = Color(0xFF007AFF), // Primary color
    
    // Scrim - Clean overlay
    scrim = Color(0x80000000), // Semi-transparent black
)

private val LightColorScheme = lightColorScheme(
    // Primary colors - Modern teal/green (inspired by banking app design)
    primary = Color(0xFF00C896), // Modern teal - Banking app primary
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFF8FDFC), // Very light teal container
    onPrimaryContainer = Color(0xFF00C896), // Primary color for contrast

    // Secondary colors - Vibrant red accent
    secondary = Color(0xFFE53E3E), // Modern red - Banking app secondary
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFDF8F8), // Very light red container
    onSecondaryContainer = Color(0xFFE53E3E), // Secondary color for contrast

    // Tertiary colors - Warm orange accent
    tertiary = Color(0xFFFF9500), // Modern orange - Warm accent
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFFAF5), // Very light orange container
    onTertiaryContainer = Color(0xFFFF9500), // Tertiary color for contrast
    
    // Background colors - Modern clean theme
    background = Color(0xFFFAFAFA), // Very light gray - Modern background
    onBackground = Color(0xFF1A1A1A), // Near black - Better contrast
    surface = Color(0xFFFFFFFF), // Pure white - Clean cards
    onSurface = Color(0xFF1A1A1A), // Near black text
    surfaceVariant = Color(0xFFF5F5F5), // Light gray - Modern variant
    onSurfaceVariant = Color(0xFF6B7280), // Modern gray - Better accessibility
    
    // Error colors - Clean error
    error = Color(0xFFFF3B30), // iOS Red - Clean error
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF2F2F7), // iOS light surface - Flat
    onErrorContainer = Color(0xFFFF3B30), // Error color for contrast
    
    // Outline colors - Minimal borders
    outline = Color(0xFFC7C7CC), // iOS separator - Subtle border
    outlineVariant = Color(0xFFE5E5EA), // iOS secondary system background - Very subtle border
    
    // Surface tint - Clean tinting
    surfaceTint = Color(0xFF007AFF), // Primary color
    
    // Scrim - Clean overlay
    scrim = Color(0x80000000), // Semi-transparent black
)

// Modern Typography - 2025 Design Trends
private val ModernTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

// Modern Banking-Inspired Shapes - 2025 Design Trends
private val ModernShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),  // Increased from 4dp for modern look
    small = RoundedCornerShape(12.dp),      // Increased from 8dp
    medium = RoundedCornerShape(16.dp),     // Increased from 12dp - cards
    large = RoundedCornerShape(20.dp),      // Increased from 16dp - large cards
    extraLarge = RoundedCornerShape(24.dp)  // Increased from 20dp - modern containers
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ModernTypography,
        shapes = ModernShapes,
        content = content
    )
}
