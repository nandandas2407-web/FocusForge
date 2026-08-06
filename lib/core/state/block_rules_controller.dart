// ============================================================
// FILE: lib/core/state/block_rules_controller.dart
// PURPOSE: Single persisted source of truth for everything that
//          decides what gets blocked: manually blocked apps,
//          shorts/reels per-app toggles, YouTube Study Mode +
//          whitelist, and strict mode. Every mutation here is
//          (a) saved to SharedPreferences and (b) pushed to the
//          native accessibility service immediately, so toggles
//          in the UI actually change blocking behavior in real time.
// CREATED: 2026-08-04 | LAST MODIFIED: 2026-08-04
// ============================================================
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../native_bridge/session_bridge.dart';
import 'default_study_channels.dart';

/// Well-known short-form-video sub-screens we can detect natively.
/// Keys match the native AppDetectionRules package names.
class ShortsTargets {
  static const instagram = 'com.instagram.android';
  static const youtube = 'com.google.android.youtube';
  static const tiktok = 'com.zhiliaoapp.musically';
  static const snapchat = 'com.snapchat.android';
  static const facebook = 'com.facebook.katana';
}

class BlockRulesState {
  /// Apps fully blocked whenever a focus session is active.
  final Set<String> blockedApps;

  /// Per-app short-form (Reels/Shorts/Spotlight) blocking toggles.
  final Set<String> blockedShortsApps;

  /// Whether a focus session is currently running (drives whether
  /// any of the above rules are actually enforced).
  final bool sessionActive;

  final bool strictMode;
  final bool youtubeStudyMode;
  final List<String> youtubeWhitelist;

  /// Local counters, incremented by the block-event listener so the
  /// dashboard shows real numbers instead of hardcoded ones.
  final int blocksToday;

  const BlockRulesState({
    this.blockedApps = const {},
    this.blockedShortsApps = const {},
    this.sessionActive = false,
    this.strictMode = false,
    this.youtubeStudyMode = false,
    this.youtubeWhitelist = const [],
    this.blocksToday = 0,
  });

  BlockRulesState copyWith({
    Set<String>? blockedApps,
    Set<String>? blockedShortsApps,
    bool? sessionActive,
    bool? strictMode,
    bool? youtubeStudyMode,
    List<String>? youtubeWhitelist,
    int? blocksToday,
  }) {
    return BlockRulesState(
      blockedApps: blockedApps ?? this.blockedApps,
      blockedShortsApps: blockedShortsApps ?? this.blockedShortsApps,
      sessionActive: sessionActive ?? this.sessionActive,
      strictMode: strictMode ?? this.strictMode,
      youtubeStudyMode: youtubeStudyMode ?? this.youtubeStudyMode,
      youtubeWhitelist: youtubeWhitelist ?? this.youtubeWhitelist,
      blocksToday: blocksToday ?? this.blocksToday,
    );
  }

  /// Union of every package the accessibility service should watch —
  /// used only to size the native event-filter superset, not to decide
  /// full-block vs sub-screen-block (that distinction is preserved by
  /// sending blockedApps and blockedShortsApps separately to native).
  Set<String> get watchedPackages => {
        ...blockedApps,
        ...blockedShortsApps,
        if (youtubeStudyMode) ShortsTargets.youtube,
      };
}

class BlockRulesController extends StateNotifier<BlockRulesState> {
  BlockRulesController(this._bridge) : super(const BlockRulesState()) {
    _load();
  }

  final SessionBridge _bridge;

  static const _kBlockedApps = 'rules_blocked_apps';
  static const _kBlockedShorts = 'rules_blocked_shorts_apps';
  static const _kStrictMode = 'rules_strict_mode';
  static const _kStudyMode = 'rules_youtube_study_mode';
  static const _kWhitelist = 'rules_youtube_whitelist';
  static const _kSessionActive = 'rules_session_active';
  static const _kBlocksToday = 'rules_blocks_today';
  static const _kBlocksTodayDate = 'rules_blocks_today_date';

  Future<void> _load() async {
    final prefs = await SharedPreferences.getInstance();

    // Reset the daily counter if it's a new day.
    final todayKey = DateTime.now().toIso8601String().substring(0, 10);
    final savedDate = prefs.getString(_kBlocksTodayDate);
    final blocksToday =
        savedDate == todayKey ? (prefs.getInt(_kBlocksToday) ?? 0) : 0;

    final whitelist =
        prefs.getStringList(_kWhitelist) ?? kDefaultStudyChannels;

    // Seed defaults on first-ever load.
    if (!prefs.containsKey(_kWhitelist)) {
      await prefs.setStringList(_kWhitelist, kDefaultStudyChannels);
    }

    state = BlockRulesState(
      blockedApps: (prefs.getStringList(_kBlockedApps) ?? []).toSet(),
      blockedShortsApps:
          (prefs.getStringList(_kBlockedShorts) ?? []).toSet(),
      sessionActive: prefs.getBool(_kSessionActive) ?? false,
      strictMode: prefs.getBool(_kStrictMode) ?? false,
      youtubeStudyMode: prefs.getBool(_kStudyMode) ?? false,
      youtubeWhitelist: whitelist,
      blocksToday: blocksToday,
    );

    await _pushToNative();
  }

  Future<void> _persist() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setStringList(_kBlockedApps, state.blockedApps.toList());
    await prefs.setStringList(
        _kBlockedShorts, state.blockedShortsApps.toList());
    await prefs.setBool(_kStrictMode, state.strictMode);
    await prefs.setBool(_kStudyMode, state.youtubeStudyMode);
    await prefs.setStringList(_kWhitelist, state.youtubeWhitelist);
    await prefs.setBool(_kSessionActive, state.sessionActive);
  }

  Future<void> _pushToNative() async {
    await _bridge.updateSessionState(
      active: state.sessionActive,
      blockedPackages: state.blockedApps,
      shortsBlockedPackages: state.blockedShortsApps,
      strictMode: state.strictMode,
      youtubeStudyMode: state.youtubeStudyMode,
      youtubeWhitelist: state.youtubeWhitelist,
    );
  }

  // ─── App Blocker ────────────────────────────────────────────
  Future<void> setAppBlocked(String packageName, bool blocked) async {
    final updated = Set<String>.from(state.blockedApps);
    blocked ? updated.add(packageName) : updated.remove(packageName);
    state = state.copyWith(blockedApps: updated);
    await _persist();
    await _pushToNative();
  }

  Future<void> setBlockedApps(Set<String> packages) async {
    state = state.copyWith(blockedApps: packages);
    await _persist();
    await _pushToNative();
  }

  // ─── Shorts/Reels Blocker ───────────────────────────────────
  Future<void> setShortsAppBlocked(String packageName, bool blocked) async {
    final updated = Set<String>.from(state.blockedShortsApps);
    blocked ? updated.add(packageName) : updated.remove(packageName);
    state = state.copyWith(blockedShortsApps: updated);
    await _persist();
    await _pushToNative();
  }

  // ─── YouTube Study Mode ─────────────────────────────────────
  Future<void> setStudyMode(bool enabled) async {
    state = state.copyWith(youtubeStudyMode: enabled);
    await _persist();
    await _pushToNative();
  }

  Future<void> addWhitelistChannel(String name) async {
    if (name.trim().isEmpty) return;
    if (state.youtubeWhitelist
        .any((c) => c.toLowerCase() == name.trim().toLowerCase())) {
      return;
    }
    final updated = [...state.youtubeWhitelist, name.trim()];
    state = state.copyWith(youtubeWhitelist: updated);
    await _persist();
    await _pushToNative();
  }

  Future<void> removeWhitelistChannel(String name) async {
    final updated =
        state.youtubeWhitelist.where((c) => c != name).toList();
    state = state.copyWith(youtubeWhitelist: updated);
    await _persist();
    await _pushToNative();
  }

  Future<void> resetWhitelistToDefaults() async {
    state = state.copyWith(youtubeWhitelist: List.of(kDefaultStudyChannels));
    await _persist();
    await _pushToNative();
  }

  // ─── Session lifecycle (called by the Focus Timer) ─────────
  Future<void> startSession({bool? strictMode}) async {
    state = state.copyWith(
      sessionActive: true,
      strictMode: strictMode ?? state.strictMode,
    );
    await _persist();
    await _pushToNative();
  }

  Future<void> endSession() async {
    state = state.copyWith(sessionActive: false);
    await _persist();
    await _pushToNative();
  }

  Future<void> setStrictMode(bool value) async {
    state = state.copyWith(strictMode: value);
    await _persist();
    await _pushToNative();
  }

  // ─── Block event counter (polled from native) ──────────────
  Future<void> refreshBlockCount() async {
    final count = await _bridge.getBlockEventCount();
    if (count == state.blocksToday) return;
    state = state.copyWith(blocksToday: count);
    final prefs = await SharedPreferences.getInstance();
    await prefs.setInt(_kBlocksToday, count);
    await prefs.setString(
      _kBlocksTodayDate,
      DateTime.now().toIso8601String().substring(0, 10),
    );
  }
}

final blockRulesProvider =
    StateNotifierProvider<BlockRulesController, BlockRulesState>((ref) {
  return BlockRulesController(ref.watch(sessionBridgeProvider));
});
