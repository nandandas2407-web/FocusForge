// ============================================================
// FILE: lib/core/state/calendar_controller.dart
// PURPOSE: Persisted calendar events (exams, study blocks, etc.)
//          so the Calendar screen shows the user's real entries
//          instead of hardcoded seed data.
// CREATED: 2026-08-05 | LAST MODIFIED: 2026-08-05
// ============================================================
import 'dart:convert';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';

enum CalendarEventType { exam, study, other }

class CalendarEventRecord {
  final String id;
  final String title;
  final DateTime date;
  final CalendarEventType type;

  const CalendarEventRecord({
    required this.id,
    required this.title,
    required this.date,
    required this.type,
  });

  Map<String, dynamic> toJson() => {
        'id': id,
        'title': title,
        'date': date.toIso8601String(),
        'type': type.name,
      };

  factory CalendarEventRecord.fromJson(Map<String, dynamic> json) {
    return CalendarEventRecord(
      id: json['id'] as String,
      title: json['title'] as String,
      date: DateTime.parse(json['date'] as String),
      type: CalendarEventType.values.firstWhere(
        (t) => t.name == json['type'],
        orElse: () => CalendarEventType.other,
      ),
    );
  }
}

class CalendarController extends StateNotifier<List<CalendarEventRecord>> {
  CalendarController() : super([]) {
    _load();
  }

  static const _key = 'calendar_events_v1';

  Future<void> _load() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final raw = prefs.getString(_key);
      if (raw == null) return;
      final list = (jsonDecode(raw) as List)
          .map((e) => CalendarEventRecord.fromJson(e as Map<String, dynamic>))
          .toList();
      state = list;
    } catch (_) {
      state = [];
    }
  }

  Future<void> _persist() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_key, jsonEncode(state.map((e) => e.toJson()).toList()));
  }

  Future<void> addEvent(String title, DateTime date, CalendarEventType type) async {
    if (title.trim().isEmpty) return;
    final record = CalendarEventRecord(
      id: '${DateTime.now().microsecondsSinceEpoch}',
      title: title.trim(),
      date: date,
      type: type,
    );
    state = [...state, record];
    await _persist();
  }

  Future<void> deleteEvent(String id) async {
    state = state.where((e) => e.id != id).toList();
    await _persist();
  }

  /// The soonest upcoming exam-type event (today or later), or null
  /// if there isn't one — used for the exam countdown card.
  CalendarEventRecord? get nextExam {
    final now = DateTime.now();
    final today = DateTime(now.year, now.month, now.day);
    final exams = state.where((e) =>
        e.type == CalendarEventType.exam &&
        !DateTime(e.date.year, e.date.month, e.date.day).isBefore(today));
    if (exams.isEmpty) return null;
    final sorted = exams.toList()..sort((a, b) => a.date.compareTo(b.date));
    return sorted.first;
  }
}

final calendarProvider =
    StateNotifierProvider<CalendarController, List<CalendarEventRecord>>((ref) {
  return CalendarController();
});
