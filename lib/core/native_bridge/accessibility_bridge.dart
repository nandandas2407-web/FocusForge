// ============================================================
// FILE: lib/core/native_bridge/accessibility_bridge.dart
// PURPOSE: Flutter bridge to native Android Accessibility Service
//          for checking/enabling accessibility and querying block state.
// CREATED: 2026-08-03 | LAST MODIFIED: 2026-08-03
// ============================================================
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

class AccessibilityBridge {
  static const _channel = MethodChannel('com.focusforge.app/native');

  Future<bool> isEnabled() async {
    try {
      return await _channel.invokeMethod('isAccessibilityEnabled') ?? false;
    } catch (_) {
      return false;
    }
  }

  Future<void> openSettings() async {
    try {
      await _channel.invokeMethod('openAccessibilitySettings');
    } catch (_) {}
  }
}

final accessibilityBridgeProvider = Provider<AccessibilityBridge>((ref) {
  return AccessibilityBridge();
});
