// ============================================================
// FILE: lib/shared/widgets/glass_bottom_sheet.dart
// PURPOSE: Bottom sheet styled with liquid glass effect.
// CREATED: 2026-08-03 | LAST MODIFIED: 2026-08-03
// ============================================================
import 'dart:ui';
import 'package:flutter/material.dart';
import '../../core/theme/glass_tokens.dart';

void showGlassBottomSheet({
  required BuildContext context,
  required Widget child,
  String? title,
  double? height,
}) {
  showModalBottomSheet(
    context: context,
    isScrollControlled: true,
    backgroundColor: Colors.transparent,
    builder: (context) => _GlassBottomSheetContent(
      child: child,
      title: title,
      height: height,
    ),
  );
}

class _GlassBottomSheetContent extends StatelessWidget {
  final Widget child;
  final String? title;
  final double? height;

  const _GlassBottomSheetContent({
    required this.child,
    this.title,
    this.height,
  });

  @override
  Widget build(BuildContext context) {
    return ClipRRect(
      borderRadius: const BorderRadius.vertical(top: Radius.circular(28)),
      child: BackdropFilter(
        filter: ImageFilter.blur(sigmaX: 40, sigmaY: 40),
        child: Container(
          height: height ?? MediaQuery.of(context).size.height * 0.7,
          decoration: BoxDecoration(
            gradient: LinearGradient(
              begin: Alignment.topCenter,
              end: Alignment.bottomCenter,
              colors: [
                Colors.white.withValues(alpha: GlassTokens.glassTintTopOpacity),
                Colors.white.withValues(
                    alpha: GlassTokens.glassTintBottomOpacity),
              ],
            ),
            border: Border.all(
              color: Colors.white.withValues(
                  alpha: GlassTokens.glassBorderOpacity),
              width: 1,
            ),
            borderRadius:
                const BorderRadius.vertical(top: Radius.circular(28)),
          ),
          child: Column(
            children: [
              // Handle
              Container(
                margin: const EdgeInsets.only(top: 12),
                width: 40,
                height: 4,
                decoration: BoxDecoration(
                  color: Colors.white.withValues(alpha: 0.3),
                  borderRadius: BorderRadius.circular(2),
                ),
              ),
              // Title
              if (title != null)
                Padding(
                  padding: const EdgeInsets.fromLTRB(20, 20, 20, 8),
                  child: Text(
                    title!,
                    style: TextStyle(
                      color: GlassTokens.textPrimary,
                      fontSize: 20,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ),
              // Content
              Expanded(child: child),
            ],
          ),
        ),
      ),
    );
  }
}
