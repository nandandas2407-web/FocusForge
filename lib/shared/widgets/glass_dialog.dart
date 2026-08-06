// ============================================================
// FILE: lib/shared/widgets/glass_dialog.dart
// PURPOSE: Dialog styled with liquid glass effect.
// CREATED: 2026-08-03 | LAST MODIFIED: 2026-08-03
// ============================================================
import 'dart:ui';
import 'package:flutter/material.dart';
import '../../core/theme/glass_tokens.dart';

Future<T?> showGlassDialog<T>({
  required BuildContext context,
  required Widget child,
  String? title,
  bool barrierDismissible = true,
}) {
  return showGeneralDialog<T>(
    context: context,
    barrierDismissible: barrierDismissible,
    barrierLabel: 'Dismiss',
    barrierColor: Colors.black54,
    transitionDuration: const Duration(milliseconds: 250),
    pageBuilder: (context, animation, secondaryAnimation) {
      return _GlassDialogContent(title: title, child: child);
    },
    transitionBuilder: (context, animation, secondaryAnimation, child) {
      return FadeTransition(
        opacity: animation,
        child: ScaleTransition(
          scale: CurvedAnimation(
            parent: animation,
            curve: Curves.easeOutCubic,
          ),
          child: child,
        ),
      );
    },
  );
}

class _GlassDialogContent extends StatelessWidget {
  final Widget child;
  final String? title;

  const _GlassDialogContent({required this.child, this.title});

  @override
  Widget build(BuildContext context) {
    return Center(
      child: ClipRRect(
        borderRadius: BorderRadius.circular(28),
        child: BackdropFilter(
          filter: ImageFilter.blur(sigmaX: 40, sigmaY: 40),
          child: Container(
            width: MediaQuery.of(context).size.width * 0.85,
            constraints: const BoxConstraints(maxWidth: 400),
            padding: const EdgeInsets.all(28),
            decoration: BoxDecoration(
              gradient: LinearGradient(
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
                colors: [
                  Colors.white
                      .withValues(alpha: GlassTokens.glassTintTopOpacity),
                  Colors.white
                      .withValues(alpha: GlassTokens.glassTintBottomOpacity),
                ],
              ),
              border: Border.all(
                color: Colors.white.withValues(
                    alpha: GlassTokens.glassBorderOpacity),
                width: 1,
              ),
              borderRadius: BorderRadius.circular(28),
              boxShadow: [
                BoxShadow(
                  color: Colors.black
                      .withValues(alpha: GlassTokens.glassShadowOpacity),
                  blurRadius: GlassTokens.glassShadowBlur,
                  offset: const Offset(0, 8),
                ),
              ],
            ),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                if (title != null) ...[
                  Text(
                    title!,
                    style: TextStyle(
                      color: GlassTokens.textPrimary,
                      fontSize: 20,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                  const SizedBox(height: 20),
                ],
                child,
              ],
            ),
          ),
        ),
      ),
    );
  }
}
