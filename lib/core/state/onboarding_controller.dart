// ============================================================
// FILE: lib/core/state/onboarding_controller.dart
// PURPOSE: Tracks whether first-launch onboarding has been
//          completed, persisted so the welcome flow is shown
//          exactly once, ever (not on every app open).
// CREATED: 2026-08-04 | LAST MODIFIED: 2026-08-04
// ============================================================
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';

class OnboardingController extends StateNotifier<bool?> {
  OnboardingController() : super(null) {
    _load();
  }

  static const _kCompleted = 'onboarding_completed';

  Future<void> _load() async {
    final prefs = await SharedPreferences.getInstance();
    state = prefs.getBool(_kCompleted) ?? false;
  }

  Future<void> markCompleted() async {
    state = true;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(_kCompleted, true);
  }

  /// Exposed only for a future "reset app" settings action — never
  /// triggered automatically.
  Future<void> reset() async {
    state = false;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(_kCompleted, false);
  }
}

/// null = still loading from disk, false = show onboarding,
/// true = onboarding already completed, go straight to home.
final onboardingCompletedProvider =
    StateNotifierProvider<OnboardingController, bool?>((ref) {
  return OnboardingController();
});
