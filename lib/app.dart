// ============================================================
// FILE: lib/app.dart
// PURPOSE: Root widget — sets up MaterialApp with GoRouter,
//          liquid glass theme, and dark-first design.
// CREATED: 2026-08-03 | LAST MODIFIED: 2026-08-03
// ============================================================
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'core/theme/liquid_glass_theme.dart';
import 'core/theme/glass_tokens.dart';
import 'core/router/app_router.dart';
import 'core/state/onboarding_controller.dart';

class FocusForgeApp extends ConsumerWidget {
  const FocusForgeApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final onboardingResolved = ref.watch(onboardingCompletedProvider) != null;

    if (!onboardingResolved) {
      // Brief splash while the one-time-onboarding flag loads from disk.
      return MaterialApp(
        debugShowCheckedModeBanner: false,
        theme: LiquidGlassTheme.darkTheme,
        home: Scaffold(
          backgroundColor: GlassTokens.bgBase,
          body: Center(
            child: CircularProgressIndicator(
              color: GlassTokens.accentPrimary,
            ),
          ),
        ),
      );
    }

    final router = ref.watch(appRouterProvider);

    return MaterialApp.router(
      title: 'FocusForge',
      debugShowCheckedModeBanner: false,
      theme: LiquidGlassTheme.darkTheme,
      darkTheme: LiquidGlassTheme.darkTheme,
      themeMode: ThemeMode.dark,
      routerConfig: router,
    );
  }
}
