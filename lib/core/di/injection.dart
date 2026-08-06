// ============================================================
// FILE: lib/core/di/injection.dart
// PURPOSE: Dependency injection setup using Riverpod providers.
// CREATED: 2026-08-03 | LAST MODIFIED: 2026-08-03
// ============================================================
import 'package:flutter_riverpod/flutter_riverpod.dart';

// Re-export providers for convenient access
export '../native_bridge/accessibility_bridge.dart';
export '../native_bridge/usage_stats_bridge.dart';
export '../native_bridge/overlay_bridge.dart';
export '../native_bridge/vpn_bridge.dart';
export '../theme/wallpaper_controller.dart';

// Session state provider
class SessionState {
  final bool isActive;
  final Set<String> blockedPackages;
  final bool strictMode;
  final bool youtubeStudyMode;
  final List<String> youtubeWhitelist;

  const SessionState({
    this.isActive = false,
    this.blockedPackages = const {},
    this.strictMode = false,
    this.youtubeStudyMode = false,
    this.youtubeWhitelist = const [],
  });

  SessionState copyWith({
    bool? isActive,
    Set<String>? blockedPackages,
    bool? strictMode,
    bool? youtubeStudyMode,
    List<String>? youtubeWhitelist,
  }) {
    return SessionState(
      isActive: isActive ?? this.isActive,
      blockedPackages: blockedPackages ?? this.blockedPackages,
      strictMode: strictMode ?? this.strictMode,
      youtubeStudyMode: youtubeStudyMode ?? this.youtubeStudyMode,
      youtubeWhitelist: youtubeWhitelist ?? this.youtubeWhitelist,
    );
  }
}

class SessionController extends StateNotifier<SessionState> {
  SessionController() : super(const SessionState());

  void startSession({
    required Set<String> blockedPackages,
    bool strictMode = false,
    bool youtubeStudyMode = false,
    List<String> youtubeWhitelist = const [],
  }) {
    state = SessionState(
      isActive: true,
      blockedPackages: blockedPackages,
      strictMode: strictMode,
      youtubeStudyMode: youtubeStudyMode,
      youtubeWhitelist: youtubeWhitelist,
    );
  }

  void endSession() {
    state = const SessionState();
  }
}

final sessionProvider =
    StateNotifierProvider<SessionController, SessionState>((ref) {
  return SessionController();
});
