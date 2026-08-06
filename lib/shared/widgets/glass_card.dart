// ============================================================
// FILE: lib/shared/widgets/glass_card.dart
// PURPOSE: Reusable liquid glass card widget — the fundamental
//          building block of the FocusForge UI.
// CREATED: 2026-08-03 | LAST MODIFIED: 2026-08-03
// ============================================================
import 'dart:ui';
import 'package:flutter/material.dart';
import '../../core/theme/glass_tokens.dart';

class GlassCard extends StatelessWidget {
  final Widget child;
  final double blurSigma;
  final double borderRadius;
  final double? width;
  final double? height;
  final EdgeInsetsGeometry? padding;
  final EdgeInsetsGeometry? margin;
  final bool enableTapScale;
  final VoidCallback? onTap;

  const GlassCard({
    super.key,
    required this.child,
    this.blurSigma = 24.0,
    this.borderRadius = 28.0,
    this.width,
    this.height,
    this.padding,
    this.margin,
    this.enableTapScale = false,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final Widget card = ClipRRect(
      borderRadius: BorderRadius.circular(borderRadius),
      child: BackdropFilter(
        filter: ImageFilter.blur(sigmaX: blurSigma, sigmaY: blurSigma),
        child: Container(
          width: width,
          height: height,
          margin: margin,
          decoration: BoxDecoration(
            borderRadius: BorderRadius.circular(borderRadius),
            gradient: LinearGradient(
              begin: Alignment.topLeft,
              end: Alignment.bottomRight,
              colors: [
                Colors.white.withValues(alpha: GlassTokens.glassTintTopOpacity),
                Colors.white.withValues(
                    alpha: GlassTokens.glassTintBottomOpacity),
              ],
            ),
            border: Border.all(
              width: 1,
              color: Colors.white.withValues(
                  alpha: GlassTokens.glassBorderOpacity),
            ),
            boxShadow: [
              BoxShadow(
                color: Colors.black
                    .withValues(alpha: GlassTokens.glassShadowOpacity),
                blurRadius: GlassTokens.glassShadowBlur,
                offset: const Offset(0, 8),
              ),
            ],
          ),
          padding: padding ?? const EdgeInsets.all(20),
          child: child,
        ),
      ),
    );

    if (onTap != null) {
      return GestureDetector(
        onTap: onTap,
        child: enableTapScale
            ? AnimatedContainer(
                duration: const Duration(milliseconds: 150),
                child: card,
              )
            : card,
      );
    }

    return card;
  }
}
