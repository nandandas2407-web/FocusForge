// ============================================================
// FILE: lib/features/calendar/presentation/calendar_screen.dart
// PURPOSE: Calendar with study blocks, exam countdowns, and
//          agenda — backed by CalendarController (persisted, real
//          user-entered events) instead of hardcoded seed data.
//          The "+" button opens a working add-event dialog; it
//          previously did nothing.
// CREATED: 2026-08-03 | LAST MODIFIED: 2026-08-05
// ============================================================
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:table_calendar/table_calendar.dart';
import '../../../core/theme/glass_tokens.dart';
import '../../../core/state/calendar_controller.dart';
import '../../../shared/widgets/glass_card.dart';

Color _colorForType(CalendarEventType type) {
  switch (type) {
    case CalendarEventType.exam:
      return GlassTokens.danger;
    case CalendarEventType.study:
      return GlassTokens.accentPrimary;
    case CalendarEventType.other:
      return GlassTokens.success;
  }
}

String _labelForType(CalendarEventType type) {
  switch (type) {
    case CalendarEventType.exam:
      return 'Exam';
    case CalendarEventType.study:
      return 'Study Block';
    case CalendarEventType.other:
      return 'Event';
  }
}

class CalendarScreen extends ConsumerStatefulWidget {
  const CalendarScreen({super.key});

  @override
  ConsumerState<CalendarScreen> createState() => _CalendarScreenState();
}

class _CalendarScreenState extends ConsumerState<CalendarScreen> {
  CalendarFormat _calendarFormat = CalendarFormat.month;
  DateTime _focusedDay = DateTime.now();
  DateTime? _selectedDay;

  Map<DateTime, List<CalendarEventRecord>> _groupEvents(List<CalendarEventRecord> events) {
    final map = <DateTime, List<CalendarEventRecord>>{};
    for (final e in events) {
      final key = DateTime(e.date.year, e.date.month, e.date.day);
      map.putIfAbsent(key, () => []).add(e);
    }
    return map;
  }

  Future<void> _showAddEventDialog() async {
    final controller = TextEditingController();
    var selectedDate = _selectedDay ?? DateTime.now();
    var selectedType = CalendarEventType.study;

    await showDialog(
      context: context,
      builder: (context) {
        return StatefulBuilder(
          builder: (context, setDialogState) {
            return AlertDialog(
              backgroundColor: GlassTokens.bgGradientStart,
              title: Text('New Event', style: TextStyle(color: GlassTokens.textPrimary)),
              content: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  TextField(
                    controller: controller,
                    autofocus: true,
                    style: TextStyle(color: GlassTokens.textPrimary),
                    decoration: InputDecoration(
                      hintText: 'Event title',
                      hintStyle: TextStyle(color: GlassTokens.textSecondary),
                    ),
                  ),
                  const SizedBox(height: 12),
                  Row(
                    children: [
                      Icon(Icons.calendar_today, size: 16, color: GlassTokens.textSecondary),
                      const SizedBox(width: 8),
                      TextButton(
                        onPressed: () async {
                          final picked = await showDatePicker(
                            context: context,
                            initialDate: selectedDate,
                            firstDate: DateTime(2020),
                            lastDate: DateTime(2035),
                          );
                          if (picked != null) {
                            setDialogState(() => selectedDate = picked);
                          }
                        },
                        child: Text(
                          '${selectedDate.month}/${selectedDate.day}/${selectedDate.year}',
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 8),
                  Wrap(
                    spacing: 8,
                    children: CalendarEventType.values.map((type) {
                      final selected = type == selectedType;
                      return ChoiceChip(
                        label: Text(_labelForType(type)),
                        selected: selected,
                        onSelected: (_) => setDialogState(() => selectedType = type),
                        selectedColor: _colorForType(type).withValues(alpha: 0.3),
                      );
                    }).toList(),
                  ),
                ],
              ),
              actions: [
                TextButton(
                  onPressed: () => Navigator.of(context).pop(),
                  child: const Text('Cancel'),
                ),
                ElevatedButton(
                  onPressed: () {
                    if (controller.text.trim().isEmpty) return;
                    ref.read(calendarProvider.notifier).addEvent(
                          controller.text,
                          selectedDate,
                          selectedType,
                        );
                    Navigator.of(context).pop();
                  },
                  child: const Text('Add'),
                ),
              ],
            );
          },
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    final events = ref.watch(calendarProvider);
    final grouped = _groupEvents(events);
    final nextExam = ref.watch(calendarProvider.notifier).nextExam;

    return Scaffold(
      backgroundColor: Colors.transparent,
      body: SafeArea(
        child: Column(
          children: [
            Padding(
              padding: const EdgeInsets.fromLTRB(20, 16, 20, 0),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(
                    'Calendar',
                    style: TextStyle(
                      color: GlassTokens.textPrimary,
                      fontSize: 24,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  IconButton(
                    onPressed: _showAddEventDialog,
                    icon: Icon(Icons.add_circle, color: GlassTokens.accentPrimary, size: 28),
                  ),
                ],
              ),
            ),

            if (nextExam != null)
              Padding(
                padding: const EdgeInsets.fromLTRB(20, 12, 20, 12),
                child: GlassCard(
                  padding: const EdgeInsets.all(16),
                  child: Row(
                    children: [
                      Container(
                        width: 56,
                        height: 56,
                        decoration: BoxDecoration(
                          color: GlassTokens.danger.withValues(alpha: 0.15),
                          borderRadius: BorderRadius.circular(16),
                        ),
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Text(
                              '${nextExam.date.difference(DateTime.now()).inDays + 1}',
                              style: const TextStyle(color: GlassTokens.danger, fontSize: 24, fontWeight: FontWeight.bold),
                            ),
                            const Text('days', style: TextStyle(color: GlassTokens.danger, fontSize: 10)),
                          ],
                        ),
                      ),
                      const SizedBox(width: 16),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text('Next Exam', style: TextStyle(color: GlassTokens.textSecondary, fontSize: 13)),
                            Text(
                              nextExam.title,
                              style: TextStyle(color: GlassTokens.textPrimary, fontSize: 16, fontWeight: FontWeight.w600),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
              ),

            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 12),
              child: TableCalendar(
                firstDay: DateTime(2020),
                lastDay: DateTime(2030),
                focusedDay: _focusedDay,
                calendarFormat: _calendarFormat,
                selectedDayPredicate: (day) => isSameDay(_selectedDay, day),
                onDaySelected: (selectedDay, focusedDay) {
                  setState(() {
                    _selectedDay = selectedDay;
                    _focusedDay = focusedDay;
                  });
                },
                onFormatChanged: (format) => setState(() => _calendarFormat = format),
                onPageChanged: (focusedDay) => _focusedDay = focusedDay,
                calendarStyle: CalendarStyle(
                  outsideDaysVisible: false,
                  todayDecoration: BoxDecoration(
                    color: GlassTokens.accentPrimary.withValues(alpha: 0.2),
                    shape: BoxShape.circle,
                  ),
                  selectedDecoration: BoxDecoration(
                    color: GlassTokens.accentPrimary,
                    shape: BoxShape.circle,
                  ),
                  defaultTextStyle: TextStyle(color: GlassTokens.textPrimary),
                  todayTextStyle: TextStyle(color: GlassTokens.accentPrimary),
                  selectedTextStyle: const TextStyle(color: Colors.white),
                  weekendTextStyle: TextStyle(color: GlassTokens.textSecondary),
                ),
                headerStyle: HeaderStyle(
                  formatButtonVisible: false,
                  titleCentered: true,
                  titleTextStyle: TextStyle(color: GlassTokens.textPrimary, fontSize: 16, fontWeight: FontWeight.w600),
                  leftChevronIcon: Icon(Icons.chevron_left, color: GlassTokens.textSecondary),
                  rightChevronIcon: Icon(Icons.chevron_right, color: GlassTokens.textSecondary),
                ),
                daysOfWeekStyle: DaysOfWeekStyle(
                  weekdayStyle: TextStyle(color: GlassTokens.textSecondary, fontSize: 12),
                  weekendStyle: const TextStyle(color: GlassTokens.textTertiary, fontSize: 12),
                ),
                eventLoader: (day) {
                  return grouped[DateTime(day.year, day.month, day.day)] ?? [];
                },
                calendarBuilders: CalendarBuilders(
                  markerBuilder: (context, day, dayEvents) {
                    if (dayEvents.isEmpty) return null;
                    return Row(
                      mainAxisSize: MainAxisSize.min,
                      children: dayEvents.take(3).map((event) {
                        final e = event as CalendarEventRecord;
                        return Container(
                          margin: const EdgeInsets.symmetric(horizontal: 1),
                          width: 6,
                          height: 6,
                          decoration: BoxDecoration(color: _colorForType(e.type), shape: BoxShape.circle),
                        );
                      }).toList(),
                    );
                  },
                ),
              ),
            ),

            const SizedBox(height: 16),

            Expanded(
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 20),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      _selectedDay != null ? 'Agenda — ${_formatDate(_selectedDay!)}' : 'Today\'s Agenda',
                      style: TextStyle(color: GlassTokens.textPrimary, fontSize: 16, fontWeight: FontWeight.w600),
                    ),
                    const SizedBox(height: 8),
                    Expanded(child: _buildAgenda(grouped)),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildAgenda(Map<DateTime, List<CalendarEventRecord>> grouped) {
    final day = _selectedDay ?? DateTime.now();
    final key = DateTime(day.year, day.month, day.day);
    final dayEvents = grouped[key] ?? [];

    if (dayEvents.isEmpty) {
      return Center(
        child: Text('No events for this day', style: TextStyle(color: GlassTokens.textSecondary)),
      );
    }

    return ListView.builder(
      itemCount: dayEvents.length,
      itemBuilder: (context, index) {
        final event = dayEvents[index];
        return Padding(
          padding: const EdgeInsets.only(bottom: 8),
          child: GlassCard(
            padding: const EdgeInsets.all(12),
            child: Row(
              children: [
                Container(
                  width: 4,
                  height: 40,
                  decoration: BoxDecoration(color: _colorForType(event.type), borderRadius: BorderRadius.circular(2)),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Text(
                    event.title,
                    style: TextStyle(color: GlassTokens.textPrimary, fontSize: 15, fontWeight: FontWeight.w500),
                  ),
                ),
                IconButton(
                  icon: Icon(Icons.close, size: 18, color: GlassTokens.textSecondary),
                  onPressed: () => ref.read(calendarProvider.notifier).deleteEvent(event.id),
                ),
              ],
            ),
          ),
        );
      },
    );
  }

  String _formatDate(DateTime date) {
    const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    return '${months[date.month - 1]} ${date.day}';
  }
}
