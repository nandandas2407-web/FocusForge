// ============================================================
// FILE: lib/core/native_bridge/usage_stats_bridge.dart
// PURPOSE: Flutter bridge to native UsageStatsManager wrapper
//          for the Stats Dashboard charts. Screen-time values of
//          -1 mean "unavailable" (permission not granted) so the
//          UI can show "No data available" instead of a fake 0.
// CREATED: 2026-08-03 | LAST MODIFIED: 2026-08-04
// ============================================================
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

class DayUsage {
  final String dateKey;
  final int totalTimeMs;
  const DayUsage({required this.dateKey, required this.totalTimeMs});

  factory DayUsage.fromMap(Map<dynamic, dynamic> map) => DayUsage(
        dateKey: map['dateKey'] as String,
        totalTimeMs: map['totalTimeMs'] as int,
      );
}

class AppUsageStat {
  final String packageName;
  final int totalTimeMs;
  final int lastTimeUsed;
  final String appLabel;

  const AppUsageStat({
    required this.packageName,
    required this.totalTimeMs,
    required this.lastTimeUsed,
    required this.appLabel,
  });

  factory AppUsageStat.fromMap(Map<dynamic, dynamic> map) {
    return AppUsageStat(
      packageName: map['packageName'] as String,
      totalTimeMs: map['totalTimeMs'] as int,
      lastTimeUsed: map['lastTimeUsed'] as int,
      appLabel: map['appLabel'] as String,
    );
  }

  double get totalTimeHours => totalTimeMs / (1000 * 60 * 60);
  double get totalTimeMinutes => totalTimeMs / (1000 * 60);
}

class UsageStatsBridge {
  static const _channel = MethodChannel('com.focusforge.app/native');

  Future<bool> isEnabled() async {
    try {
      return await _channel.invokeMethod('isUsageStatsEnabled') ?? false;
    } catch (_) {
      return false;
    }
  }

  Future<void> openSettings() async {
    try {
      await _channel.invokeMethod('openUsageStatsSettings');
    } catch (_) {}
  }

  /// Returns -1 when Usage Access hasn't been granted — callers must
  /// treat that as "no data available", never as zero usage.
  Future<int> getDailyScreenTime() async {
    try {
      return await _channel.invokeMethod('getDailyScreenTime') ?? -1;
    } catch (_) {
      return -1;
    }
  }

  Future<int> getWeeklyScreenTime() async {
    try {
      return await _channel.invokeMethod('getWeeklyScreenTime') ?? -1;
    } catch (_) {
      return -1;
    }
  }

  Future<int> getMonthlyScreenTime() async {
    try {
      return await _channel.invokeMethod('getMonthlyScreenTime') ?? -1;
    } catch (_) {
      return -1;
    }
  }

  /// One entry per day for the last [days] days (oldest first). Empty
  /// list means unavailable — callers should show "No data available".
  Future<List<DayUsage>> getDailyBreakdown({int days = 7}) async {
    try {
      final result =
          await _channel.invokeMethod('getDailyBreakdown', {'days': days});
      if (result is List) {
        return result
            .map((e) => DayUsage.fromMap(e as Map<dynamic, dynamic>))
            .toList();
      }
      return [];
    } catch (_) {
      return [];
    }
  }

  Future<List<AppUsageStat>> getTopApps({int limit = 10}) async {
    try {
      final result = await _channel.invokeMethod('getTopApps', {'limit': limit});
      if (result is List) {
        return result
            .map((e) => AppUsageStat.fromMap(e as Map<dynamic, dynamic>))
            .toList();
      }
      return [];
    } catch (_) {
      return [];
    }
  }

  /// All launchable apps on the device (via PackageManager), used to
  /// populate the App Blocker picker. Returns an empty list when running
  /// outside Android (e.g. desktop preview) — callers should fall back
  /// to a small built-in list in that case.
  Future<List<AppUsageStat>> getInstalledApps() async {
    try {
      final result = await _channel.invokeMethod('getInstalledApps');
      if (result is List) {
        return result
            .map((e) => AppUsageStat.fromMap(e as Map<dynamic, dynamic>))
            .toList();
      }
      return [];
    } catch (_) {
      return [];
    }
  }
}

final usageStatsBridgeProvider = Provider<UsageStatsBridge>((ref) {
  return UsageStatsBridge();
});
