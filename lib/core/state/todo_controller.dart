// ============================================================
// FILE: lib/core/state/todo_controller.dart
// PURPOSE: Persisted task list backing the To-Do screen and the
//          Home Dashboard "Upcoming Tasks" card, so tasks survive
//          app restarts instead of resetting to sample data.
// CREATED: 2026-08-04 | LAST MODIFIED: 2026-08-04
// ============================================================
import 'dart:convert';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:uuid/uuid.dart';

class TodoTask {
  final String id;
  final String title;
  final String priority; // Low, Medium, High
  final String group; // Today, Upcoming, Someday
  final bool completed;

  const TodoTask({
    required this.id,
    required this.title,
    required this.priority,
    required this.group,
    this.completed = false,
  });

  TodoTask copyWith({bool? completed}) => TodoTask(
        id: id,
        title: title,
        priority: priority,
        group: group,
        completed: completed ?? this.completed,
      );

  Map<String, dynamic> toJson() => {
        'id': id,
        'title': title,
        'priority': priority,
        'group': group,
        'completed': completed,
      };

  factory TodoTask.fromJson(Map<String, dynamic> json) => TodoTask(
        id: json['id'] as String,
        title: json['title'] as String,
        priority: json['priority'] as String,
        group: json['group'] as String,
        completed: json['completed'] as bool? ?? false,
      );
}

class TodoController extends StateNotifier<List<TodoTask>> {
  TodoController() : super([]) {
    _load();
  }

  static const _key = 'todo_tasks_v1';
  final _uuid = const Uuid();

  Future<void> _load() async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final raw = prefs.getString(_key);
      if (raw == null) {
        state = [];
        return;
      }
      final list = (jsonDecode(raw) as List)
          .map((e) => TodoTask.fromJson(e as Map<String, dynamic>))
          .toList();
      state = list;
    } catch (_) {
      state = [];
    }
  }

  Future<void> _persist() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(
      _key,
      jsonEncode(state.map((t) => t.toJson()).toList()),
    );
  }

  Future<void> addTask({
    required String title,
    required String priority,
    required String group,
  }) async {
    if (title.trim().isEmpty) return;
    state = [
      TodoTask(id: _uuid.v4(), title: title.trim(), priority: priority, group: group),
      ...state,
    ];
    await _persist();
  }

  Future<void> toggleCompleted(String id) async {
    state = [
      for (final t in state)
        if (t.id == id) t.copyWith(completed: !t.completed) else t,
    ];
    await _persist();
  }

  Future<void> deleteTask(String id) async {
    state = state.where((t) => t.id != id).toList();
    await _persist();
  }
}

final todoProvider = StateNotifierProvider<TodoController, List<TodoTask>>((ref) {
  return TodoController();
});
