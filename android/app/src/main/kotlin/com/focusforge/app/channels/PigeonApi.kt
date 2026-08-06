// ============================================================
// FILE: android/.../channels/PigeonApi.g.kt
// PURPOSE: Pigeon-generated type-safe Dart↔Kotlin bindings for
//          structured data passing (block lists, usage stats, etc.)
// CREATED: 2026-08-03 | LAST MODIFIED: 2026-08-03
// NOTE: This is a placeholder. Run `dart run pigeon --input pigeon_api.dart`
//       from the Flutter project root to generate the actual Pigeon bindings.
// ============================================================
package com.focusforge.app.channels

// Placeholder Pigeon-generated classes
// In production, run: dart run pigeon --input pigeon_api.dart
// to generate type-safe bindings from the pigeon_api.dart file.

data class PigeonAppUsageStat(
    val packageName: String,
    val totalTimeMs: Long,
    val lastTimeUsed: Long,
    val appLabel: String
)

data class PigeonBlockedApp(
    val packageName: String,
    val appName: String,
    val blocked: Boolean,
    val blockMode: String // "always", "schedule", "session_only"
)

data class PigeonBlockEvent(
    val packageName: String,
    val reason: String,
    val subScreen: String,
    val timestamp: Long
)

data class PigeonSessionState(
    val active: Boolean,
    val startTime: Long,
    val endTime: Long,
    val blockedPackages: List<String>,
    val strictMode: Boolean
)

data class PigeonDetectionRule(
    val packageName: String,
    val name: String,
    val resourceIdContains: List<String>,
    val contentDescContains: List<String>,
    val classNameContains: List<String>
)
