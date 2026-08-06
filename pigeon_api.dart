// ============================================================
// FILE: pigeon_api.dart
// PURPOSE: Pigeon API definitions for type-safe Dart↔Kotlin
//          native bridge code generation.
// CREATED: 2026-08-03 | LAST MODIFIED: 2026-08-03
// INSTRUCTIONS: Run `dart run pigeon --input pigeon_api.dart` to generate
//               the actual type-safe bindings.
// ============================================================
import 'package:pigeon/pigeon.dart';

@HostApi()
abstract class NativeBridge {
  @async
  bool isAccessibilityEnabled();

  void openAccessibilitySettings();

  @async
  bool isUsageStatsEnabled();

  void openUsageStatsSettings();

  @async
  bool isOverlayEnabled();

  void openOverlaySettings();

  @async
  int getDailyScreenTime();

  @async
  List<AppUsageStatData> getTopApps(int limit);

  @async
  bool isVpnRunning();

  @async
  void startVpnService();

  @async
  void stopVpnService();

  @async
  void setBlockedDomains(List<String> domains);

  @async
  void updateSessionState(SessionStateData state);

  @async
  int getBlockEventCount();
}

class AppUsageStatData {
  final String packageName;
  final int totalTimeMs;
  final int lastTimeUsed;
  final String appLabel;

  AppUsageStatData({
    required this.packageName,
    required this.totalTimeMs,
    required this.lastTimeUsed,
    required this.appLabel,
  });
}

class BlockedAppData {
  final String packageName;
  final String appName;
  final bool blocked;
  final String blockMode;

  BlockedAppData({
    required this.packageName,
    required this.appName,
    required this.blocked,
    required this.blockMode,
  });
}

class BlockEventData {
  final String packageName;
  final String reason;
  final String subScreen;
  final int timestamp;

  BlockEventData({
    required this.packageName,
    required this.reason,
    required this.subScreen,
    required this.timestamp,
  });
}

class SessionStateData {
  final bool active;
  final int startTime;
  final int endTime;
  final List<String> blockedPackages;
  final List<String> shortsBlockedPackages;
  final bool strictMode;
  final bool youtubeStudyMode;
  final List<String> youtubeWhitelist;

  SessionStateData({
    required this.active,
    required this.startTime,
    required this.endTime,
    required this.blockedPackages,
    required this.shortsBlockedPackages,
    required this.strictMode,
    required this.youtubeStudyMode,
    required this.youtubeWhitelist,
  });
}

class DetectionRuleData {
  final String packageName;
  final String name;
  final List<String> resourceIdContains;
  final List<String> contentDescContains;
  final List<String> classNameContains;

  DetectionRuleData({
    required this.packageName,
    required this.name,
    required this.resourceIdContains,
    required this.contentDescContains,
    required this.classNameContains,
  });
}
