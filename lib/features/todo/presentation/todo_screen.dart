// ============================================================
// FILE: lib/features/todo/presentation/todo_screen.dart
// PURPOSE: To-Do list with tasks, priorities, and grouping.
//          Backed by the persisted TodoController so tasks survive
//          app restarts instead of resetting to sample data.
// CREATED: 2026-08-03 | LAST MODIFIED: 2026-08-04
// ============================================================
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/theme/glass_tokens.dart';
import '../../../core/state/todo_controller.dart';
import '../../../shared/widgets/glass_card.dart';

class TodoScreen extends ConsumerStatefulWidget {
  const TodoScreen({super.key});

  @override
  ConsumerState<TodoScreen> createState() => _TodoScreenState();
}

class _TodoScreenState extends ConsumerState<TodoScreen> {
  final _titleController = TextEditingController();
  String _selectedPriority = 'Medium';
  String _addGroup = 'Today';
  String _selectedGroup = 'Today';

  @override
  void dispose() {
    _titleController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final tasks = ref.watch(todoProvider);

    return Scaffold(
      backgroundColor: Colors.transparent,
      body: SafeArea(
        child: Column(
          children: [
            // Header
            Padding(
              padding: const EdgeInsets.fromLTRB(20, 16, 20, 12),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(
                    'Tasks',
                    style: TextStyle(
                      color: GlassTokens.textPrimary,
                      fontSize: 24,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  IconButton(
                    onPressed: _showAddTaskSheet,
                    icon: Icon(
                      Icons.add_circle,
                      color: GlassTokens.accentPrimary,
                      size: 28,
                    ),
                  ),
                ],
              ),
            ),

            // Group tabs
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20),
              child: Row(
                children: ['Today', 'Upcoming', 'Someday'].map((group) {
                  final isSelected = _selectedGroup == group;
                  final count = tasks.where((t) => t.group == group).length;
                  return Padding(
                    padding: const EdgeInsets.only(right: 8),
                    child: GestureDetector(
                      onTap: () => setState(() => _selectedGroup = group),
                      child: Container(
                        padding: const EdgeInsets.symmetric(
                            horizontal: 16, vertical: 8),
                        decoration: BoxDecoration(
                          color: isSelected
                              ? GlassTokens.accentPrimary.withValues(alpha: 0.2)
                              : Colors.transparent,
                          borderRadius: BorderRadius.circular(20),
                          border: Border.all(
                            color: isSelected
                                ? GlassTokens.accentPrimary
                                : Colors.white.withValues(alpha: 0.15),
                          ),
                        ),
                        child: Text(
                          '$group ($count)',
                          style: TextStyle(
                            color: isSelected
                                ? GlassTokens.accentPrimary
                                : GlassTokens.textSecondary,
                            fontSize: 13,
                            fontWeight: FontWeight.w500,
                          ),
                        ),
                      ),
                    ),
                  );
                }).toList(),
              ),
            ),
            const SizedBox(height: 12),

            // Task list
            Expanded(
              child: Builder(builder: (context) {
                final grouped =
                    tasks.where((t) => t.group == _selectedGroup).toList();
                if (grouped.isEmpty) {
                  return Center(
                    child: Text(
                      'No tasks yet — tap + to add one',
                      style: TextStyle(color: GlassTokens.textSecondary),
                    ),
                  );
                }
                return ListView.builder(
                  padding: const EdgeInsets.symmetric(horizontal: 20),
                  itemCount: grouped.length,
                  itemBuilder: (context, index) =>
                      _buildTaskTile(grouped[index]),
                );
              }),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildTaskTile(TodoTask task) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: GlassCard(
        padding: const EdgeInsets.all(12),
        child: Row(
          children: [
            GestureDetector(
              onTap: () =>
                  ref.read(todoProvider.notifier).toggleCompleted(task.id),
              child: Container(
                width: 24,
                height: 24,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  color: task.completed
                      ? GlassTokens.success
                      : Colors.transparent,
                  border: Border.all(
                    color: task.completed
                        ? GlassTokens.success
                        : GlassTokens.textSecondary,
                    width: 2,
                  ),
                ),
                child: task.completed
                    ? const Icon(Icons.check, color: Colors.white, size: 16)
                    : null,
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Text(
                task.title,
                style: TextStyle(
                  color: task.completed
                      ? GlassTokens.textSecondary
                      : GlassTokens.textPrimary,
                  fontSize: 15,
                  fontWeight: FontWeight.w500,
                  decoration:
                      task.completed ? TextDecoration.lineThrough : null,
                ),
              ),
            ),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
              decoration: BoxDecoration(
                color: _getPriorityColor(task.priority).withValues(alpha: 0.15),
                borderRadius: BorderRadius.circular(8),
              ),
              child: Text(
                task.priority,
                style: TextStyle(
                  color: _getPriorityColor(task.priority),
                  fontSize: 11,
                  fontWeight: FontWeight.w600,
                ),
              ),
            ),
            IconButton(
              onPressed: () =>
                  ref.read(todoProvider.notifier).deleteTask(task.id),
              icon: Icon(Icons.close, size: 16, color: GlassTokens.textTertiary),
              padding: EdgeInsets.zero,
              constraints: const BoxConstraints(minWidth: 32, minHeight: 32),
            ),
          ],
        ),
      ),
    );
  }

  Color _getPriorityColor(String priority) {
    switch (priority) {
      case 'High':
        return GlassTokens.danger;
      case 'Medium':
        return GlassTokens.warning;
      case 'Low':
        return GlassTokens.success;
      default:
        return GlassTokens.textSecondary;
    }
  }

  void _showAddTaskSheet() {
    _addGroup = _selectedGroup;
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (sheetContext) => StatefulBuilder(
        builder: (sheetContext, setSheetState) => Padding(
          padding: EdgeInsets.only(
            bottom: MediaQuery.of(sheetContext).viewInsets.bottom,
          ),
          child: Container(
            padding: const EdgeInsets.all(24),
            decoration: BoxDecoration(
              color: GlassTokens.bgBase,
              borderRadius:
                  const BorderRadius.vertical(top: Radius.circular(28)),
              border: Border.all(
                color: Colors.white.withValues(alpha: 0.15),
              ),
            ),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Center(
                  child: Container(
                    width: 40,
                    height: 4,
                    decoration: BoxDecoration(
                      color: Colors.white.withValues(alpha: 0.3),
                      borderRadius: BorderRadius.circular(2),
                    ),
                  ),
                ),
                const SizedBox(height: 20),
                Text(
                  'New Task',
                  style: TextStyle(
                    color: GlassTokens.textPrimary,
                    fontSize: 20,
                    fontWeight: FontWeight.w600,
                  ),
                ),
                const SizedBox(height: 16),
                TextField(
                  controller: _titleController,
                  autofocus: true,
                  decoration: const InputDecoration(
                    hintText: 'Task title...',
                  ),
                  style: TextStyle(color: GlassTokens.textPrimary),
                ),
                const SizedBox(height: 16),
                Text(
                  'List',
                  style: TextStyle(
                    color: GlassTokens.textSecondary,
                    fontSize: 13,
                  ),
                ),
                const SizedBox(height: 8),
                Row(
                  children: ['Today', 'Upcoming', 'Someday'].map((group) {
                    final isSelected = _addGroup == group;
                    return Padding(
                      padding: const EdgeInsets.only(right: 8),
                      child: GestureDetector(
                        onTap: () =>
                            setSheetState(() => _addGroup = group),
                        child: Container(
                          padding: const EdgeInsets.symmetric(
                              horizontal: 16, vertical: 8),
                          decoration: BoxDecoration(
                            color: isSelected
                                ? GlassTokens.accentPrimary.withValues(alpha: 0.2)
                                : Colors.transparent,
                            borderRadius: BorderRadius.circular(12),
                            border: Border.all(
                              color: isSelected
                                  ? GlassTokens.accentPrimary
                                  : Colors.white.withValues(alpha: 0.15),
                            ),
                          ),
                          child: Text(
                            group,
                            style: TextStyle(
                              color: isSelected
                                  ? GlassTokens.accentPrimary
                                  : GlassTokens.textSecondary,
                              fontSize: 13,
                              fontWeight: FontWeight.w500,
                            ),
                          ),
                        ),
                      ),
                    );
                  }).toList(),
                ),
                const SizedBox(height: 16),
                Text(
                  'Priority',
                  style: TextStyle(
                    color: GlassTokens.textSecondary,
                    fontSize: 13,
                  ),
                ),
                const SizedBox(height: 8),
                Row(
                  children: ['Low', 'Medium', 'High'].map((priority) {
                    final isSelected = _selectedPriority == priority;
                    return Padding(
                      padding: const EdgeInsets.only(right: 8),
                      child: GestureDetector(
                        onTap: () =>
                            setSheetState(() => _selectedPriority = priority),
                        child: Container(
                          padding: const EdgeInsets.symmetric(
                              horizontal: 16, vertical: 8),
                          decoration: BoxDecoration(
                            color: isSelected
                                ? _getPriorityColor(priority)
                                    .withValues(alpha: 0.2)
                                : Colors.transparent,
                            borderRadius: BorderRadius.circular(12),
                            border: Border.all(
                              color: isSelected
                                  ? _getPriorityColor(priority)
                                  : Colors.white.withValues(alpha: 0.15),
                            ),
                          ),
                          child: Text(
                            priority,
                            style: TextStyle(
                              color: isSelected
                                  ? _getPriorityColor(priority)
                                  : GlassTokens.textSecondary,
                              fontSize: 13,
                              fontWeight: FontWeight.w500,
                            ),
                          ),
                        ),
                      ),
                    );
                  }).toList(),
                ),
                const SizedBox(height: 24),
                SizedBox(
                  width: double.infinity,
                  child: ElevatedButton(
                    onPressed: () => _addTask(sheetContext),
                    child: const Text('Add Task'),
                  ),
                ),
                const SizedBox(height: 16),
              ],
            ),
          ),
        ),
      ),
    );
  }

  void _addTask(BuildContext sheetContext) {
    final title = _titleController.text.trim();
    if (title.isEmpty) return;

    ref.read(todoProvider.notifier).addTask(
          title: title,
          priority: _selectedPriority,
          group: _addGroup,
        );
    _titleController.clear();
    Navigator.of(sheetContext).pop();
  }
}
