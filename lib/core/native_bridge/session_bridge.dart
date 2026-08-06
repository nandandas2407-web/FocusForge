// ============================================================
// FILE: lib/core/native_bridge/session_bridge.dart
// PURPOSE: Pushes the current block-rule/session state to the
//          native FocusAccessibilityService so it can actually
//          decide what to block. Without this call, the native
//          BlockDecisionEngine never learns which apps/whitelist
//          are active — this is the bridge that makes blocking work.
// CREATED: 2026-08-04 | LAST MODIFIED: 2026-08-04
// ============================================================
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

class SessionBridge {
  static const _channel = MethodChannel('com.focusforge.app/native');

  Future<void> updateSessionState({
    required bool active,
    required Set<String> blockedPackages,
    required Set<String> shortsBlockedPackages,
    required bool strictMode,
    required bool youtubeStudyMode,
    required List<String> youtubeWhitelist,
  }) async {
    try {
      await _channel.invokeMethod('updateSessionState', {
        'active': active,
        'blockedPackages': blockedPackages.toList(),
        'shortsBlockedPackages': shortsBlockedPackages.toList(),
        'strictMode': strictMode,
        'youtubeStudyMode': youtubeStudyMode,
        'youtubeWhitelist': youtubeWhitelist,
      });
    } catch (_) {
      // Native side unavailable (e.g. running outside Android) — ignore.
    }
  }

  Future<int> getBlockEventCount() async {
    try {
      return await _channel.invokeMethod('getBlockEventCount') ?? 0;
    } catch (_) {
      return 0;
    }
  }
}

final sessionBridgeProvider = Provider<SessionBridge>((ref) {
  return SessionBridge();
});
