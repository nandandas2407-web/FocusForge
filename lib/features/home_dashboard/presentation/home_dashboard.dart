// ============================================================
// FILE: lib/features/home_dashboard/presentation/home_dashboard.dart
// PURPOSE: Main dashboard — hero card with real screen time (from
//          UsageStatsManager), real today-blocked count, quick
//          actions, and the real next-due task from TodoController.
// CREATED: 2026-08-03 | LAST MODIFIED: 2026-08-04
// ============================================================
import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../core/theme/glass_tokens.dart';
import '../../../core/state/block_rules_controller.dart';
import '../../../core/state/todo_controller.dart';
import '../../../core/native_bridge/usage_stats_bridge.dart';
import '../../../shared/widgets/glass_card.dart';
import '../../../shared/responsive/breakpoints.dart';

class HomeDashboard extends ConsumerStatefulWidget {
  const HomeDashboard({super.key});

  @override
  ConsumerState<HomeDashboard> createState() => _HomeDashboardState();
}

class _HomeDashboardState extends ConsumerState<HomeDashboard> {
  int _screenTimeMs = 0;
  bool _usageStatsGranted = false;
  Timer? _poll;

  @override
  void initState() {
    super.initState();
    _refresh();
    ref.read(blockRulesProvider.notifier).refreshBlockCount();
    _poll = Timer.periodic(const Duration(seconds: 20), (_) => _refresh());
  }

  @override
  void dispose() {
    _poll?.cancel();
    super.dispose();
  }

  Future<void> _refresh() async {
    final bridge = ref.read(usageStatsBridgeProvider);
    final granted = await bridge.isEnabled();
    final ms = granted ? await bridge.getDailyScreenTime() : 0;
    if (!mounted) return;
    setState(() {
      _usageStatsGranted = granted;
      _screenTimeMs = ms;
    });
    ref.read(blockRulesProvider.notifier).refreshBlockCount();
  }

  String _formatDuration(int ms) {
    final totalMinutes = ms ~/ 60000;
    final hours = totalMinutes ~/ 60;
    final minutes = totalMinutes % 60;
    if (hours == 0) return '${minutes}m';
    return '${hours}h ${minutes}m';
  }

  @override
  Widget build(BuildContext context) {
    final rules = ref.watch(blockRulesProvider);
    final tasks = ref.watch(todoProvider);
    final hour = DateTime.now().hour;
    final greeting = hour < 12
        ? 'Good morning'
        : hour < 18
            ? 'Good afternoon'
            : 'Good evening';

    return Scaffold(
      backgroundColor: Colors.transparent,
      body: SafeArea(
        child: RefreshIndicator(
          onRefresh: _refresh,
          child: SingleChildScrollView(
            physics: const AlwaysScrollableScrollPhysics(),
            padding: const EdgeInsets.all(20),
            child: ResponsiveContent(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _buildHeader(greeting, rules),
                  const SizedBox(height: 24),
                  _buildHeroCard(rules),
                  const SizedBox(height: 20),
                  _buildQuickActions(context),
                  const SizedBox(height: 20),
                  _buildUpcomingTasks(context, tasks),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildHeader(String greeting, BlockRulesState rules) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              greeting,
              style: TextStyle(
                color: GlassTokens.textSecondary,
                fontSize: 14,
              ),
            ),
            const SizedBox(height: 4),
            Text(
              'FocusForge',
              style: TextStyle(
                color: GlassTokens.textPrimary,
                fontSize: 24,
                fontWeight: FontWeight.bold,
              ),
            ),
          ],
        ),
        if (rules.sessionActive)
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
            decoration: BoxDecoration(
              color: GlassTokens.success.withValues(alpha: 0.15),
              borderRadius: BorderRadius.circular(20),
              border: Border.all(
                color: GlassTokens.success.withValues(alpha: 0.3),
              ),
            ),
            child: Row(
              children: [
                Icon(Icons.shield, size: 14, color: GlassTokens.success),
                const SizedBox(width: 4),
                Text(
                  'Focus active',
                  style: TextStyle(
                    color: GlassTokens.success,
                    fontSize: 13,
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ],
            ),
          ),
      ],
    );
  }

  Widget _buildHeroCard(BlockRulesState rules) {
    return GlassCard(
      child: Column(
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                'Today\'s Screen Time',
                style: TextStyle(
                  color: GlassTokens.textSecondary,
                  fontSize: 14,
                ),
              ),
              if (!_usageStatsGranted)
                Container(
                  padding:
                      const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                  decoration: BoxDecoration(
                    color: GlassTokens.warning.withValues(alpha: 0.15),
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Text(
                    'Permission needed',
                    style: TextStyle(
                      color: GlassTokens.warning,
                      fontSize: 11,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ),
            ],
          ),
          const SizedBox(height: 24),
          if (!_usageStatsGranted)
            Padding(
              padding: const EdgeInsets.symmetric(vertical: 24),
              child: Column(
                children: [
                  Icon(Icons.bar_chart_outlined,
                      size: 40, color: GlassTokens.textSecondary),
                  const SizedBox(height: 12),
                  Text(
                    'Grant Usage Access in Settings to see real screen time',
                    textAlign: TextAlign.center,
                    style: TextStyle(color: GlassTokens.textSecondary),
                  ),
                ],
              ),
            )
          else
            SizedBox(
              width: 160,
              height: 160,
              child: Stack(
                alignment: Alignment.center,
                children: [
                  SizedBox(
                    width: 160,
                    height: 160,
                    child: CircularProgressIndicator(
                      value: (_screenTimeMs / (4 * 3600000)).clamp(0.0, 1.0),
                      strokeWidth: 12,
                      backgroundColor: Colors.white.withValues(alpha: 0.08),
                      valueColor: AlwaysStoppedAnimation<Color>(
                        GlassTokens.accentPrimary,
                      ),
                      strokeCap: StrokeCap.round,
                    ),
                  ),
                  Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Text(
                        _formatDuration(_screenTimeMs),
                        style: TextStyle(
                          color: GlassTokens.textPrimary,
                          fontSize: 32,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      Text(
                        'today',
                        style: TextStyle(
                          color: GlassTokens.textSecondary,
                          fontSize: 13,
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          const SizedBox(height: 16),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceAround,
            children: [
              _buildStatItem(_formatDuration(_screenTimeMs), 'Screen Time'),
              _buildStatItem(
                  rules.sessionActive ? 'Active' : 'Off', 'Focus Session'),
              _buildStatItem('${rules.blocksToday}', 'Blocked'),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildStatItem(String value, String label) {
    return Column(
      children: [
        Text(
          value,
          style: TextStyle(
            color: GlassTokens.textPrimary,
            fontSize: 18,
            fontWeight: FontWeight.w600,
          ),
        ),
        const SizedBox(height: 4),
        Text(
          label,
          style: TextStyle(
            color: GlassTokens.textSecondary,
            fontSize: 12,
          ),
        ),
      ],
    );
  }

  Widget _buildQuickActions(BuildContext context) {
    final actions = [
      (icon: Icons.play_arrow_rounded, label: 'Start Focus', path: '/focus'),
      (icon: Icons.block_outlined, label: 'Block List', path: '/app-blocker'),
      (icon: Icons.school_outlined, label: 'Study Mode', path: '/youtube-study-mode'),
      (icon: Icons.add_task, label: 'Add Task', path: '/tasks'),
    ];

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          'Quick Actions',
          style: TextStyle(
            color: GlassTokens.textPrimary,
            fontSize: 18,
            fontWeight: FontWeight.w600,
          ),
        ),
        const SizedBox(height: 12),
        LayoutBuilder(
          builder: (context, constraints) {
            final columns = responsiveColumns(
              constraints.maxWidth,
              phone: 2,
              tablet: 4,
              largeTablet: 4,
            );
            return GridView.count(
              crossAxisCount: columns,
              shrinkWrap: true,
              physics: const NeverScrollableScrollPhysics(),
              crossAxisSpacing: 12,
              mainAxisSpacing: 12,
              childAspectRatio: 1.3,
              children: [
                for (final a in actions)
                  _buildActionCard(
                    icon: a.icon,
                    label: a.label,
                    onTap: () => context.go(a.path),
                  ),
              ],
            );
          },
        ),
      ],
    );
  }

  Widget _buildActionCard({
    required IconData icon,
    required String label,
    required VoidCallback onTap,
  }) {
    return GlassCard(
      padding: const EdgeInsets.all(16),
      onTap: onTap,
      child: Column(
        children: [
          Icon(icon, color: GlassTokens.accentPrimary, size: 28),
          const SizedBox(height: 8),
          Text(
            label,
            style: TextStyle(
              color: GlassTokens.textPrimary,
              fontSize: 13,
              fontWeight: FontWeight.w500,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildUpcomingTasks(BuildContext context, List<TodoTask> tasks) {
    final pending = tasks.where((t) => !t.completed).toList();

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(
              'Upcoming Tasks',
              style: TextStyle(
                color: GlassTokens.textPrimary,
                fontSize: 18,
                fontWeight: FontWeight.w600,
              ),
            ),
            TextButton(
              onPressed: () => context.go('/tasks'),
              child: Text(
                'See All',
                style: TextStyle(color: GlassTokens.accentPrimary),
              ),
            ),
          ],
        ),
        const SizedBox(height: 8),
        if (pending.isEmpty)
          GlassCard(
            padding: const EdgeInsets.all(16),
            child: Row(
              children: [
                Icon(Icons.check_circle_outline,
                    color: GlassTokens.success, size: 20),
                const SizedBox(width: 12),
                Text(
                  'All caught up — no pending tasks',
                  style: TextStyle(color: GlassTokens.textSecondary),
                ),
              ],
            ),
          )
        else
          ...pending.take(3).map((task) => Padding(
                padding: const EdgeInsets.only(bottom: 8),
                child: GlassCard(
                  padding: const EdgeInsets.all(16),
                  child: Row(
                    children: [
                      Icon(
                        Icons.circle_outlined,
                        color: GlassTokens.textSecondary,
                        size: 20,
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Text(
                          task.title,
                          style: TextStyle(
                            color: GlassTokens.textPrimary,
                            fontSize: 14,
                            fontWeight: FontWeight.w500,
                          ),
                        ),
                      ),
                      Container(
                        padding: const EdgeInsets.symmetric(
                            horizontal: 8, vertical: 4),
                        decoration: BoxDecoration(
                          color: (task.priority == 'High'
                                  ? GlassTokens.danger
                                  : task.priority == 'Medium'
                                      ? GlassTokens.warning
                                      : GlassTokens.success)
                              .withValues(alpha: 0.15),
                          borderRadius: BorderRadius.circular(8),
                        ),
                        child: Text(
                          task.priority,
                          style: TextStyle(
                            color: task.priority == 'High'
                                ? GlassTokens.danger
                                : task.priority == 'Medium'
                                    ? GlassTokens.warning
                                    : GlassTokens.success,
                            fontSize: 11,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              )),
      ],
    );
  }
}
