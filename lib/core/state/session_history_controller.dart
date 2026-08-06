// ============================================================
// FILE: lib/core/state/session_history_controller.dart
// PURPOSE: Logs each completed focus session (date + duration) so
//          streaks, "days active", and daily-goal progress can be
//          computed from real history instead of hardcoded numbers.
//          Android has no API for "focus session" — that's a concept
//          this app owns, so it must keep its own durable log.
// CREATED: 2026-08-04 | LAST MODIFIED: 2026-08-04
// ============================================================
import 'dart:convert';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';

class FocusSessionRecord {
  final String dateKey; // yyyy-MM-dd, local
  final int durationSeconds;

  const FocusSessionRecord({
    required this.dateKey,
    required this.durationSeconds,
  });

  Map<String, dynamic> toJson() => {
        'dateKey': dateKey,
        'durationSeconds': durationSeconds,
      };

  factory FocusSessionRecord.fromJson(Map<String, dynamic> json) =>
      FocusSessionRecord(
        dateKey: json['dateKey'] as String,
        durationSeconds: json['durationSeconds'] as int,
      );
}

String _dateKey(DateTime d) =>
    '${d.year.toString().padLeft(4, '0')}-${d.month.toString().padLeft(2, '0')}-${d.day.toString().padLeft(2, '0')}';

class SessionHistoryState {
  final List<FocusSessionRecord> records;
  final int dailyGoalMinutes;
  const SessionHistoryState({this.records = const [], this.dailyGoalMinutes = 120});

  /// Total completed-focus seconds grouped by day.
  Map<String, int> get secondsByDay {
    final map = <String, int>{};
    for (final r in records) {
      map[r.dateKey] = (map[r.dateKey] ?? 0) + r.durationSeconds;
    }
    return map;
  }

  /// Consecutive-day streak ending today (or yesterday, if today has
  /// no session yet but yesterday did — so the streak doesn't reset
  /// to 0 the moment the clock passes midnight before they've focused).
  int get currentStreak {
    final days = secondsByDay.keys.toSet();
    if (days.isEmpty) return 0;
    var cursor = DateTime.now();
    var streak = 0;

    // If no session today yet, start counting from yesterday instead.
    if (!days.contains(_dateKey(cursor))) {
      cursor = cursor.subtract(const Duration(days: 1));
      if (!days.contains(_dateKey(cursor))) return 0;
    }

    while (days.contains(_dateKey(cursor))) {
      streak++;
      cursor = cursor.subtract(const Duration(days: 1));
    }
    return streak;
  }

  int get longestStreak {
    final days = secondsByDay.keys.toList()..sort();
    if (days.isEmpty) return 0;
    var longest = 1;
    var current = 1;
    for (var i = 1; i < days.length; i++) {
      final prev = DateTime.parse(days[i - 1]);
      final curr = DateTime.parse(days[i]);
      if (curr.difference(prev).inDays == 1) {
        current++;
      } else {
        current = 1;
      }
      if (current > longest) longest = current;
    }
    return longest;
  }

  int get totalActiveDays => secondsByDay.length;

  int get todaySeconds => secondsByDay[_dateKey(DateTime.now())] ?? 0;
}

class SessionHistoryController extends StateNotifier<SessionHistoryState> {
  SessionHistoryController() : super(const SessionHistoryState()) {
    _load();
  }

  static const _key = 'focus_session_history_v1';
  static const _goalKey = 'daily_focus_goal_minutes';

  Future<void> _load() async {
    final prefs = await SharedPreferences.getInstance();
    final raw = prefs.getString(_key);
    final goal = prefs.getInt(_goalKey) ?? 120;
    final records = raw == null
        ? <FocusSessionRecord>[]
        : (jsonDecode(raw) as List)
            .map((e) => FocusSessionRecord.fromJson(e as Map<String, dynamic>))
            .toList();
    state = SessionHistoryState(records: records, dailyGoalMinutes: goal);
  }

  Future<void> _persist() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(
      _key,
      jsonEncode(state.records.map((r) => r.toJson()).toList()),
    );
  }

  /// Call when a focus (non-break) timer phase completes or is stopped
  /// with meaningful elapsed time — logs it against today's date.
  Future<void> logCompletedFocusSeconds(int seconds) async {
    if (seconds <= 0) return;
    final today = _dateKey(DateTime.now());
    state = SessionHistoryState(
      records: [...state.records, FocusSessionRecord(dateKey: today, durationSeconds: seconds)],
      dailyGoalMinutes: state.dailyGoalMinutes,
    );
    await _persist();
  }

  Future<void> setDailyGoalMinutes(int minutes) async {
    final clamped = minutes.clamp(15, 480);
    state = SessionHistoryState(records: state.records, dailyGoalMinutes: clamped);
    final prefs = await SharedPreferences.getInstance();
    await prefs.setInt(_goalKey, clamped);
  }
}

final sessionHistoryProvider =
    StateNotifierProvider<SessionHistoryController, SessionHistoryState>((ref) {
  return SessionHistoryController();
});
