// ============================================================
// FILE: lib/core/native_bridge/overlay_bridge.dart
// PURPOSE: Flutter bridge to the native BlockOverlayService
//          for showing/hiding block overlays.
// CREATED: 2026-08-03 | LAST MODIFIED: 2026-08-03
// ============================================================
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

class OverlayBridge {
  static const _channel = MethodChannel('com.focusforge.app/native');

  Future<bool> isEnabled() async {
    try {
      return await _channel.invokeMethod('isOverlayEnabled') ?? false;
    } catch (_) {
      return false;
    }
  }

  Future<void> openSettings() async {
    try {
      await _channel.invokeMethod('openOverlaySettings');
    } catch (_) {}
  }
}

final overlayBridgeProvider = Provider<OverlayBridge>((ref) {
  return OverlayBridge();
});
