// ============================================================
// FILE: lib/core/native_bridge/vpn_bridge.dart
// PURPOSE: Flutter bridge to the native FocusVpnService
//          for domain-level website blocking.
// CREATED: 2026-08-03 | LAST MODIFIED: 2026-08-03
// ============================================================
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

class VpnBridge {
  static const _channel = MethodChannel('com.focusforge.app/native');

  Future<bool> isRunning() async {
    try {
      return await _channel.invokeMethod('isVpnRunning') ?? false;
    } catch (_) {
      return false;
    }
  }

  Future<void> start() async {
    await _channel.invokeMethod('startVpnService');
  }

  Future<void> stop() async {
    await _channel.invokeMethod('stopVpnService');
  }

  Future<void> setBlockedDomains(List<String> domains) async {
    await _channel.invokeMethod('setBlockedDomains', {'domains': domains});
  }
}

final vpnBridgeProvider = Provider<VpnBridge>((ref) {
  return VpnBridge();
});
