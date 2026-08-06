// ============================================================
// FILE: lib/core/state/website_blocker_controller.dart
// PURPOSE: Persisted blocked-domains list + VPN/enforcement toggles
//          for the Website Blocker screen. Previously all local
//          setState — reset to the seed list every app restart.
// CREATED: 2026-08-05 | LAST MODIFIED: 2026-08-05
// ============================================================
import 'dart:convert';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../native_bridge/vpn_bridge.dart';

class WebsiteBlockerState {
  final Set<String> blockedDomains;
  final bool vpnEnabled;
  final bool enforceAlways;

  const WebsiteBlockerState({
    this.blockedDomains = const {},
    this.vpnEnabled = false,
    this.enforceAlways = false,
  });

  WebsiteBlockerState copyWith({
    Set<String>? blockedDomains,
    bool? vpnEnabled,
    bool? enforceAlways,
  }) {
    return WebsiteBlockerState(
      blockedDomains: blockedDomains ?? this.blockedDomains,
      vpnEnabled: vpnEnabled ?? this.vpnEnabled,
      enforceAlways: enforceAlways ?? this.enforceAlways,
    );
  }
}

class WebsiteBlockerController extends StateNotifier<WebsiteBlockerState> {
  WebsiteBlockerController(this._vpnBridge) : super(const WebsiteBlockerState()) {
    _load();
  }

  final VpnBridge _vpnBridge;

  static const _domainsKey = 'website_blocked_domains_v1';
  static const _enforceAlwaysKey = 'website_enforce_always';

  Future<void> _load() async {
    final prefs = await SharedPreferences.getInstance();
    final raw = prefs.getStringList(_domainsKey);
    final enforceAlways = prefs.getBool(_enforceAlwaysKey) ?? false;
    // Real running status of the native VPN service, not a locally
    // remembered guess — so the toggle reflects reality after a
    // process restart (e.g. the OS killed the VPN service).
    final vpnRunning = await _vpnBridge.isRunning();
    state = WebsiteBlockerState(
      blockedDomains: raw?.toSet() ?? {},
      vpnEnabled: vpnRunning,
      enforceAlways: enforceAlways,
    );
    if (state.blockedDomains.isNotEmpty) {
      await _vpnBridge.setBlockedDomains(state.blockedDomains.toList());
    }
  }

  Future<void> _persistDomains() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setStringList(_domainsKey, state.blockedDomains.toList());
  }

  Future<void> addDomain(String domain) async {
    final trimmed = domain.trim().toLowerCase();
    if (trimmed.isEmpty) return;
    state = state.copyWith(blockedDomains: {...state.blockedDomains, trimmed});
    await _persistDomains();
    await _vpnBridge.setBlockedDomains(state.blockedDomains.toList());
  }

  Future<void> addDomains(List<String> domains) async {
    state = state.copyWith(blockedDomains: {...state.blockedDomains, ...domains});
    await _persistDomains();
    await _vpnBridge.setBlockedDomains(state.blockedDomains.toList());
  }

  Future<void> removeDomain(String domain) async {
    final updated = {...state.blockedDomains}..remove(domain);
    state = state.copyWith(blockedDomains: updated);
    await _persistDomains();
    await _vpnBridge.setBlockedDomains(state.blockedDomains.toList());
  }

  Future<void> setVpnEnabled(bool enabled) async {
    state = state.copyWith(vpnEnabled: enabled);
    if (enabled) {
      await _vpnBridge.setBlockedDomains(state.blockedDomains.toList());
      await _vpnBridge.start();
    } else {
      await _vpnBridge.stop();
    }
  }

  Future<void> setEnforceAlways(bool value) async {
    state = state.copyWith(enforceAlways: value);
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(_enforceAlwaysKey, value);
  }
}

final websiteBlockerProvider =
    StateNotifierProvider<WebsiteBlockerController, WebsiteBlockerState>((ref) {
  return WebsiteBlockerController(ref.read(vpnBridgeProvider));
});
