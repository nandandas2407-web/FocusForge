// ============================================================
// FILE: lib/features/stats_dashboard/presentation/stats_dashboard_screen.dart
// PURPOSE: Screen time dashboard — weekly bar chart, per-app
//          breakdown, and streak heatmap, all built from real
//          UsageStatsManager data and persisted session history.
//          Shows "No data available" instead of fake numbers when
//          Usage Access hasn't been granted or there's no history yet.
// CREATED: 2026-08-03 | LAST MODIFIED: 2026-08-04
// ============================================================
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:fl_chart/fl_chart.dart';
import '../../../core/theme/glass_tokens.dart';
import '../../../core/native_bridge/usage_stats_bridge.dart';
import '../../../core/state/session_history_controller.dart';
import '../../../shared/widgets/glass_card.dart';

class StatsDashboardScreen extends ConsumerStatefulWidget {
  const StatsDashboardScreen({super.key});

  @override
  ConsumerState<StatsDashboardScreen> createState() =>
      _StatsDashboardScreenState();
}

class _StatsDashboardScreenState extends ConsumerState<StatsDashboardScreen> {
  bool _loading = true;
  bool _permissionGranted = false;
  List<DayUsage> _weeklyBreakdown = [];
  List<AppUsageStat> _topApps = [];
  int _weeklyTotalMs = -1;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    final bridge = ref.read(usageStatsBridgeProvider);
    final granted = await bridge.isEnabled();
    if (!granted) {
      setState(() {
        _permissionGranted = false;
        _loading = false;
      });
      return;
    }

    final breakdown = await bridge.getDailyBreakdown(days: 7);
    final topApps = await bridge.getTopApps(limit: 5);
    final weeklyTotal = await bridge.getWeeklyScreenTime();

    if (!mounted) return;
    setState(() {
      _permissionGranted = true;
      _weeklyBreakdown = breakdown;
      _topApps = topApps;
      _weeklyTotalMs = weeklyTotal;
      _loading = false;
    });
  }

  String _formatDuration(int ms) {
    if (ms < 0) return '—';
    final totalMinutes = ms ~/ 60000;
    final hours = totalMinutes ~/ 60;
    final minutes = totalMinutes % 60;
    if (hours == 0) return '${minutes}m';
    return '${hours}h ${minutes}m';
  }

  @override
  Widget build(BuildContext context) {
    final history = ref.watch(sessionHistoryProvider);

    return Scaffold(
      backgroundColor: Colors.transparent,
      body: SafeArea(
        child: RefreshIndicator(
          onRefresh: _load,
          child: SingleChildScrollView(
            physics: const AlwaysScrollableScrollPhysics(),
            padding: const EdgeInsets.all(20),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Stats',
                  style: TextStyle(
                    color: GlassTokens.textPrimary,
                    fontSize: 24,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 20),

                if (_loading)
                  const Padding(
                    padding: EdgeInsets.symmetric(vertical: 80),
                    child: Center(child: CircularProgressIndicator()),
                  )
                else if (!_permissionGranted)
                  _buildPermissionPrompt()
                else ...[
                  _buildWeeklyTotalCard(),
                  const SizedBox(height: 20),
                  Text(
                    'Weekly Screen Time',
                    style: TextStyle(
                      color: GlassTokens.textPrimary,
                      fontSize: 18,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                  const SizedBox(height: 12),
                  GlassCard(
                    height: 200,
                    padding: const EdgeInsets.all(16),
                    child: _buildWeeklyChart(),
                  ),
                  const SizedBox(height: 20),
                  Text(
                    'Top Apps Today',
                    style: TextStyle(
                      color: GlassTokens.textPrimary,
                      fontSize: 18,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                  const SizedBox(height: 12),
                  if (_topApps.isEmpty)
                    _buildNoDataCard('No app usage recorded yet today')
                  else
                    ..._buildAppBars(),
                ],

                const SizedBox(height: 20),
                Text(
                  'Focus Session History',
                  style: TextStyle(
                    color: GlassTokens.textPrimary,
                    fontSize: 18,
                    fontWeight: FontWeight.w600,
                  ),
                ),
                const SizedBox(height: 12),
                GlassCard(
                  padding: const EdgeInsets.all(16),
                  child: history.records.isEmpty
                      ? Center(
                          child: Text(
                            'No focus sessions completed yet',
                            style: TextStyle(color: GlassTokens.textSecondary),
                          ),
                        )
                      : _buildStreakHeatmap(history),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildPermissionPrompt() {
    return GlassCard(
      padding: const EdgeInsets.all(24),
      child: Column(
        children: [
          Icon(Icons.bar_chart_outlined, size: 40, color: GlassTokens.textSecondary),
          const SizedBox(height: 12),
          Text(
            'No data available',
            style: TextStyle(
              color: GlassTokens.textPrimary,
              fontSize: 16,
              fontWeight: FontWeight.w600,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            'Grant Usage Access in Settings to see real screen time and app usage stats.',
            textAlign: TextAlign.center,
            style: TextStyle(color: GlassTokens.textSecondary),
          ),
          const SizedBox(height: 16),
          ElevatedButton(
            onPressed: () async {
              await ref.read(usageStatsBridgeProvider).openSettings();
            },
            child: const Text('Open Settings'),
          ),
        ],
      ),
    );
  }

  Widget _buildNoDataCard(String message) {
    return GlassCard(
      padding: const EdgeInsets.all(16),
      child: Center(
        child: Text(message, style: TextStyle(color: GlassTokens.textSecondary)),
      ),
    );
  }

  Widget _buildWeeklyTotalCard() {
    return GlassCard(
      padding: const EdgeInsets.all(16),
      child: Row(
        children: [
          Container(
            width: 48,
            height: 48,
            decoration: BoxDecoration(
              color: GlassTokens.accentPrimary.withValues(alpha: 0.15),
              borderRadius: BorderRadius.circular(12),
            ),
            child: Icon(Icons.calendar_view_week, color: GlassTokens.accentPrimary),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'This Week',
                  style: TextStyle(color: GlassTokens.textSecondary, fontSize: 13),
                ),
                Text(
                  _weeklyTotalMs < 0 ? 'No data available' : _formatDuration(_weeklyTotalMs),
                  style: TextStyle(
                    color: GlassTokens.textPrimary,
                    fontSize: 20,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildWeeklyChart() {
    if (_weeklyBreakdown.isEmpty) {
      return Center(
        child: Text('No data available', style: TextStyle(color: GlassTokens.textSecondary)),
      );
    }

    final maxMs = _weeklyBreakdown.map((d) => d.totalTimeMs).fold<int>(0, (a, b) => a > b ? a : b);
    final maxHours = maxMs <= 0 ? 4.0 : (maxMs / 3600000).ceilToDouble();
    final labels = _weeklyBreakdown.map((d) {
      final date = DateTime.parse(d.dateKey);
      const names = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
      return names[date.weekday - 1];
    }).toList();

    return BarChart(
      BarChartData(
        alignment: BarChartAlignment.spaceAround,
        maxY: maxHours,
        barTouchData: BarTouchData(
          touchTooltipData: BarTouchTooltipData(
            getTooltipItem: (group, groupIndex, rod, rodIndex) {
              return BarTooltipItem(
                '${rod.toY.toStringAsFixed(1)}h',
                const TextStyle(color: Colors.white, fontSize: 12, fontWeight: FontWeight.w600),
              );
            },
          ),
        ),
        titlesData: FlTitlesData(
          show: true,
          bottomTitles: AxisTitles(
            sideTitles: SideTitles(
              showTitles: true,
              getTitlesWidget: (value, meta) {
                final idx = value.toInt();
                if (idx >= 0 && idx < labels.length) {
                  return Text(labels[idx], style: TextStyle(color: GlassTokens.textSecondary, fontSize: 11));
                }
                return const Text('');
              },
            ),
          ),
          leftTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)),
          topTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)),
          rightTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)),
        ),
        gridData: const FlGridData(show: false),
        borderData: FlBorderData(show: false),
        barGroups: _weeklyBreakdown.asMap().entries.map((entry) {
          final isToday = entry.key == _weeklyBreakdown.length - 1;
          final hours = entry.value.totalTimeMs / 3600000;
          return BarChartGroupData(
            x: entry.key,
            barRods: [
              BarChartRodData(
                toY: hours,
                color: isToday ? GlassTokens.accentPrimary : GlassTokens.accentPrimary.withValues(alpha: 0.4),
                width: 20,
                borderRadius: const BorderRadius.vertical(top: Radius.circular(6)),
              ),
            ],
          );
        }).toList(),
      ),
    );
  }

  List<Widget> _buildAppBars() {
    final maxMs = _topApps.first.totalTimeMs;
    return _topApps.map((app) {
      final fraction = maxMs == 0 ? 0.0 : app.totalTimeMs / maxMs;
      return Padding(
        padding: const EdgeInsets.only(bottom: 12),
        child: GlassCard(
          padding: const EdgeInsets.all(12),
          child: Row(
            children: [
              SizedBox(
                width: 100,
                child: Text(
                  app.appLabel,
                  overflow: TextOverflow.ellipsis,
                  style: TextStyle(color: GlassTokens.textPrimary, fontSize: 13, fontWeight: FontWeight.w500),
                ),
              ),
              Expanded(
                child: ClipRRect(
                  borderRadius: BorderRadius.circular(4),
                  child: LinearProgressIndicator(
                    value: fraction,
                    backgroundColor: Colors.white.withValues(alpha: 0.06),
                    valueColor: AlwaysStoppedAnimation<Color>(
                      GlassTokens.accentPrimary.withValues(alpha: 0.4 + (fraction * 0.6)),
                    ),
                    minHeight: 8,
                  ),
                ),
              ),
              const SizedBox(width: 12),
              Text(_formatDuration(app.totalTimeMs), style: TextStyle(color: GlassTokens.textSecondary, fontSize: 13)),
            ],
          ),
        ),
      );
    }).toList();
  }

  Widget _buildStreakHeatmap(SessionHistoryState history) {
    // Real 12-week x 7-day contribution grid based on actual logged
    // focus-session minutes per day, most recent day last.
    final secondsByDay = history.secondsByDay;
    final today = DateTime.now();
    final days = List.generate(84, (i) => today.subtract(Duration(days: 83 - i)));

    return Column(
      children: List.generate(7, (weekIndex) {
        return Padding(
          padding: const EdgeInsets.only(bottom: 4),
          child: Row(
            children: List.generate(12, (dayIndex) {
              final idx = dayIndex * 7 + weekIndex;
              if (idx >= days.length) return const SizedBox(width: 22, height: 22);
              final date = days[idx];
              final key = '${date.year.toString().padLeft(4, '0')}-${date.month.toString().padLeft(2, '0')}-${date.day.toString().padLeft(2, '0')}';
              final seconds = secondsByDay[key] ?? 0;
              final intensity = seconds == 0 ? 0.0 : (seconds / 3600).clamp(0.15, 1.0);
              return Container(
                width: 20,
                height: 20,
                margin: const EdgeInsets.all(1),
                decoration: BoxDecoration(
                  color: seconds == 0
                      ? Colors.white.withValues(alpha: 0.05)
                      : GlassTokens.success.withValues(alpha: intensity),
                  borderRadius: BorderRadius.circular(4),
                ),
              );
            }),
          ),
        );
      }),
    );
  }
}
