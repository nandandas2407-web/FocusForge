// ============================================================
// FILE: lib/shared/models/app_info.dart
// PURPOSE: Model for installed app information from PackageManager.
// CREATED: 2026-08-03 | LAST MODIFIED: 2026-08-03
// ============================================================

class AppInfo {
  final String packageName;
  final String appName;
  final String? iconPath;
  final bool isSystemApp;

  const AppInfo({
    required this.packageName,
    required this.appName,
    this.iconPath,
    this.isSystemApp = false,
  });

  factory AppInfo.fromMap(Map<dynamic, dynamic> map) {
    return AppInfo(
      packageName: map['package_name'] as String,
      appName: map['app_name'] as String? ?? map['package_name'] as String,
      iconPath: map['icon'] as String?,
      isSystemApp: map['system'] as bool? ?? false,
    );
  }
}

class BlockRule {
  final String packageName;
  final String appName;
  final BlockMode blockMode;
  final bool enabled;
  final String? schedule; // cron-like schedule string
  final int? allowedMinutesPerDay;

  const BlockRule({
    required this.packageName,
    required this.appName,
    this.blockMode = BlockMode.sessionOnly,
    this.enabled = true,
    this.schedule,
    this.allowedMinutesPerDay,
  });

  BlockRule copyWith({
    BlockMode? blockMode,
    bool? enabled,
    String? schedule,
    int? allowedMinutesPerDay,
  }) {
    return BlockRule(
      packageName: packageName,
      appName: appName,
      blockMode: blockMode ?? this.blockMode,
      enabled: enabled ?? this.enabled,
      schedule: schedule ?? this.schedule,
      allowedMinutesPerDay:
          allowedMinutesPerDay ?? this.allowedMinutesPerDay,
    );
  }
}

enum BlockMode {
  always,
  scheduled,
  sessionOnly,
  allowedMinutesPerDay,
}

class FocusSession {
  final String id;
  final DateTime startTime;
  final DateTime? endTime;
  final bool isActive;
  final bool strictMode;
  final int breakIntervalMinutes;
  final int breakDurationMinutes;

  const FocusSession({
    required this.id,
    required this.startTime,
    this.endTime,
    this.isActive = true,
    this.strictMode = false,
    this.breakIntervalMinutes = 25,
    this.breakDurationMinutes = 5,
  });

  Duration get elapsed =>
      (endTime ?? DateTime.now()).difference(startTime);

  Duration? get remaining {
    if (endTime == null) return null;
    final diff = endTime!.difference(DateTime.now());
    return diff.isNegative ? Duration.zero : diff;
  }
}
