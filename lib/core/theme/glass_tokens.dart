// ============================================================
// FILE: lib/core/theme/glass_tokens.dart
// PURPOSE: Design token constants for the liquid glass system.
//          All glass widgets reference these tokens for consistency.
//          Tunable from the Theme Customizer sliders.
// CREATED: 2026-08-03 | LAST MODIFIED: 2026-08-03
// ============================================================
import 'package:flutter/material.dart';

class GlassTokens {
  GlassTokens._();

  // ─── Glass Fill ─────────────────────────────────────────────
  static double glassTintTopOpacity = 0.14;
  static double glassTintBottomOpacity = 0.04;
  static double glassBorderOpacity = 0.25;
  static double glassBlurSigma = 24.0;
  static double glassBorderRadius = 28.0;
  static double glassShadowOpacity = 0.25;
  static double glassShadowBlur = 20.0;

  // ─── Accent ─────────────────────────────────────────────────
  static Color accentPrimary = const Color(0xFF7C5CFF);
  static Color accentPrimaryLight = const Color(0xFF9B7FFF);

  // ─── Text ───────────────────────────────────────────────────
  static const Color textTertiary = Color(0xFF6B7280);

  // ─── Semantic ───────────────────────────────────────────────
  static const Color success = Color(0xFF3DDC97);
  static const Color warning = Color(0xFFFFC857);
  static const Color danger = Color(0xFFFF5C7C);
  static const Color info = Color(0xFF60A5FA);

  // ─── Light Mode Overrides ───────────────────────────────────
  static bool _isDarkMode = true;

  static void setDarkMode(bool isDark) {
    _isDarkMode = isDark;
    if (!isDark) {
      bgBase = const Color(0xFFF5F5F5);
      bgGradientStart = const Color(0xFFFAFAFA);
      bgGradientEnd = const Color(0xFFE8E8E8);
      glassTintTopOpacity = 0.65;
      glassTintBottomOpacity = 0.35;
      glassBorderOpacity = 0.4;
      glassShadowOpacity = 0.1;
      textPrimary = const Color(0xFF1A1A2E);
      textSecondary = const Color(0xFF4A4A6A);
    } else {
      bgBase = const Color(0xFF0B0D12);
      bgGradientStart = const Color(0xFF12141C);
      bgGradientEnd = const Color(0xFF05060A);
      glassTintTopOpacity = 0.14;
      glassTintBottomOpacity = 0.04;
      glassBorderOpacity = 0.25;
      glassShadowOpacity = 0.25;
      textPrimary = const Color(0xFFF5F6FA);
      textSecondary = const Color(0xFFA7ACC0);
    }
  }

  static bool get isDarkMode => _isDarkMode;

  // ─── Light mode mutable colors ──────────────────────────────
  static Color bgBase = const Color(0xFF0B0D12);
  static Color bgGradientStart = const Color(0xFF12141C);
  static Color bgGradientEnd = const Color(0xFF05060A);
  static Color textPrimary = const Color(0xFFF5F6FA);
  static Color textSecondary = const Color(0xFFA7ACC0);
}
