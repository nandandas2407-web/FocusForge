// ============================================================
// FILE: lib/core/theme/liquid_glass_theme.dart
// PURPOSE: Complete theme configuration combining glass tokens,
//          wallpaper state, and Material ThemeData for the app.
// CREATED: 2026-08-03 | LAST MODIFIED: 2026-08-03
// ============================================================
import 'package:flutter/material.dart';
import 'glass_tokens.dart';

class LiquidGlassTheme {
  LiquidGlassTheme._();

  static ThemeData get darkTheme {
    return ThemeData(
      brightness: Brightness.dark,
      scaffoldBackgroundColor: GlassTokens.bgBase,
      primaryColor: GlassTokens.accentPrimary,
      colorScheme: ColorScheme.dark(
        primary: GlassTokens.accentPrimary,
        secondary: GlassTokens.accentPrimaryLight,
        surface: GlassTokens.bgBase,
        error: GlassTokens.danger,
        onPrimary: Colors.white,
        onSecondary: Colors.white,
        onSurface: GlassTokens.textPrimary,
        onError: Colors.white,
      ),
      appBarTheme: AppBarTheme(
        backgroundColor: Colors.transparent,
        elevation: 0,
        centerTitle: true,
        titleTextStyle: TextStyle(
          color: GlassTokens.textPrimary,
          fontSize: 20,
          fontWeight: FontWeight.w600,
        ),
        iconTheme: IconThemeData(color: GlassTokens.textPrimary),
      ),
      bottomNavigationBarTheme: BottomNavigationBarThemeData(
        backgroundColor: Colors.transparent,
        elevation: 0,
        selectedItemColor: GlassTokens.accentPrimary,
        unselectedItemColor: GlassTokens.textSecondary,
        type: BottomNavigationBarType.fixed,
      ),
      cardTheme: CardThemeData(
        color: Colors.white.withValues(alpha: GlassTokens.glassTintTopOpacity),
        elevation: 0,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(GlassTokens.glassBorderRadius),
          side: BorderSide(
            color: Colors.white.withValues(alpha: GlassTokens.glassBorderOpacity),
            width: 1,
          ),
        ),
      ),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: GlassTokens.accentPrimary,
          foregroundColor: Colors.white,
          padding: const EdgeInsets.symmetric(horizontal: 32, vertical: 16),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(16),
          ),
          textStyle: const TextStyle(
            fontSize: 16,
            fontWeight: FontWeight.w600,
          ),
        ),
      ),
      textButtonTheme: TextButtonThemeData(
        style: TextButton.styleFrom(
          foregroundColor: GlassTokens.accentPrimary,
          textStyle: const TextStyle(
            fontSize: 14,
            fontWeight: FontWeight.w500,
          ),
        ),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: Colors.white.withValues(alpha: 0.06),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(16),
          borderSide: BorderSide(
            color: Colors.white.withValues(alpha: 0.15),
          ),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(16),
          borderSide: BorderSide(
            color: Colors.white.withValues(alpha: 0.15),
          ),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(16),
          borderSide: BorderSide(
            color: GlassTokens.accentPrimary,
            width: 2,
          ),
        ),
        hintStyle: TextStyle(color: GlassTokens.textSecondary),
        labelStyle: TextStyle(color: GlassTokens.textSecondary),
      ),
      dividerTheme: DividerThemeData(
        color: Colors.white.withValues(alpha: 0.1),
        thickness: 1,
      ),
      iconTheme: IconThemeData(
        color: GlassTokens.textSecondary,
        size: 24,
      ),
      textTheme: TextTheme(
        headlineLarge: TextStyle(
          color: GlassTokens.textPrimary,
          fontSize: 32,
          fontWeight: FontWeight.bold,
        ),
        headlineMedium: TextStyle(
          color: GlassTokens.textPrimary,
          fontSize: 24,
          fontWeight: FontWeight.w600,
        ),
        headlineSmall: TextStyle(
          color: GlassTokens.textPrimary,
          fontSize: 20,
          fontWeight: FontWeight.w600,
        ),
        bodyLarge: TextStyle(
          color: GlassTokens.textPrimary,
          fontSize: 16,
        ),
        bodyMedium: TextStyle(
          color: GlassTokens.textSecondary,
          fontSize: 14,
        ),
        bodySmall: const TextStyle(
          color: GlassTokens.textTertiary,
          fontSize: 12,
        ),
        labelLarge: TextStyle(
          color: GlassTokens.textPrimary,
          fontSize: 14,
          fontWeight: FontWeight.w600,
        ),
      ),
    );
  }

  static ThemeData get lightTheme {
    return ThemeData(
      brightness: Brightness.light,
      scaffoldBackgroundColor: const Color(0xFFF5F5F5),
      primaryColor: GlassTokens.accentPrimary,
      colorScheme: ColorScheme.light(
        primary: GlassTokens.accentPrimary,
        secondary: GlassTokens.accentPrimaryLight,
        surface: const Color(0xFFF5F5F5),
        error: GlassTokens.danger,
        onPrimary: Colors.white,
        onSecondary: Colors.white,
        onSurface: const Color(0xFF1A1A2E),
        onError: Colors.white,
      ),
      appBarTheme: const AppBarTheme(
        backgroundColor: Colors.transparent,
        elevation: 0,
        centerTitle: true,
        titleTextStyle: TextStyle(
          color: Color(0xFF1A1A2E),
          fontSize: 20,
          fontWeight: FontWeight.w600,
        ),
        iconTheme: IconThemeData(color: Color(0xFF1A1A2E)),
      ),
    );
  }
}
