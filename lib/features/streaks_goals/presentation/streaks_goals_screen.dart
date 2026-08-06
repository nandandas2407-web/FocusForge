// ============================================================
// FILE: lib/features/streaks_goals/presentation/streaks_goals_screen.dart
// PURPOSE: Focus streaks, daily goals, milestone badges — all
//          computed from the persisted SessionHistoryController
//          (real completed focus sessions), never hardcoded.
// CREATED: 2026-08-03 | LAST MODIFIED: 2026-08-04
// ============================================================
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/theme/glass_tokens.dart';
import '../../../core/state/session_history_controller.dart';
import '../../../shared/widgets/glass_card.dart';

class StreaksGoalsScreen extends ConsumerWidget {
  const StreaksGoalsScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final history = ref.watch(sessionHistoryProvider);
    final notifier = ref.read(sessionHistoryProvider.notifier);

    final hasAnyData = history.records.isNotEmpty;
    final currentStreak = history.currentStreak;
    final longestStreak = history.longestStreak;
    final totalDays = history.totalActiveDays;
    final todayMinutes = history.todaySeconds ~/ 60;
    final goalMinutes = history.dailyGoalMinutes;
    final progress =
        goalMinutes == 0 ? 0.0 : (todayMinutes / goalMinutes).clamp(0.0, 1.0);
    final goalMet = todayMinutes >= goalMinutes;

    return Scaffold(
      backgroundColor: GlassTokens.bgBase,
      appBar: AppBar(
        title: const Text('Streaks & Goals'),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back_ios_new),
          onPressed: () => Navigator.of(context).pop(),
        ),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(20),
        child: Column(
          children: [
            // Streak hero
            GlassCard(
              child: Column(
                children: [
                  Text(
                    hasAnyData ? '🔥' : '🌱',
                    style: const TextStyle(fontSize: 48),
                  ),
                  const SizedBox(height: 12),
                  Text(
                    hasAnyData
                        ? '$currentStreak Day Streak'
                        : 'No streak yet',
                    style: TextStyle(
                      color: GlassTokens.textPrimary,
                      fontSize: 28,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    !hasAnyData
                        ? 'Complete a focus session to start your streak'
                        : goalMet
                            ? 'You\'re building great habits!'
                            : 'Keep going — you\'ve got this!',
                    textAlign: TextAlign.center,
                    style: TextStyle(
                      color: GlassTokens.textSecondary,
                      fontSize: 14,
                    ),
                  ),
                  const SizedBox(height: 16),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceAround,
                    children: [
                      _buildStatItem(
                          hasAnyData ? '$currentStreak' : '—', 'Current'),
                      _buildStatItem(
                          hasAnyData ? '$longestStreak' : '—', 'Best'),
                      _buildStatItem(
                          hasAnyData ? '$totalDays' : '—', 'Active Days'),
                    ],
                  ),
                ],
              ),
            ),

            const SizedBox(height: 20),

            // Daily goal
            GlassCard(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Daily Focus Goal',
                    style: TextStyle(
                      color: GlassTokens.textPrimary,
                      fontSize: 16,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                  const SizedBox(height: 16),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      _buildGoalButton(
                        icon: Icons.remove,
                        onTap: () =>
                            notifier.setDailyGoalMinutes(goalMinutes - 15),
                      ),
                      Text(
                        '${goalMinutes ~/ 60}h ${goalMinutes % 60}m',
                        style: TextStyle(
                          color: GlassTokens.accentPrimary,
                          fontSize: 24,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      _buildGoalButton(
                        icon: Icons.add,
                        onTap: () =>
                            notifier.setDailyGoalMinutes(goalMinutes + 15),
                      ),
                    ],
                  ),
                  const SizedBox(height: 16),
                  ClipRRect(
                    borderRadius: BorderRadius.circular(6),
                    child: LinearProgressIndicator(
                      value: progress,
                      backgroundColor: Colors.white.withValues(alpha: 0.06),
                      valueColor: AlwaysStoppedAnimation<Color>(
                        goalMet ? GlassTokens.success : GlassTokens.accentPrimary,
                      ),
                      minHeight: 12,
                    ),
                  ),
                  const SizedBox(height: 8),
                  Text(
                    '${todayMinutes ~/ 60}h ${todayMinutes % 60}m of ${goalMinutes ~/ 60}h ${goalMinutes % 60}m goal',
                    style: TextStyle(
                      color: GlassTokens.textSecondary,
                      fontSize: 13,
                    ),
                  ),
                ],
              ),
            ),

            const SizedBox(height: 20),

            // Milestones — unlocked strictly from the real longest streak.
            Text(
              'Milestones',
              style: TextStyle(
                color: GlassTokens.textPrimary,
                fontSize: 18,
                fontWeight: FontWeight.w600,
              ),
            ),
            const SizedBox(height: 12),
            _buildMilestone('3 Day Streak', Icons.local_fire_department,
                GlassTokens.success, longestStreak >= 3),
            _buildMilestone('7 Day Streak', Icons.emoji_events_outlined,
                GlassTokens.warning, longestStreak >= 7),
            _buildMilestone('30 Day Streak', Icons.military_tech_outlined,
                GlassTokens.accentPrimary, longestStreak >= 30),
            _buildMilestone('100 Day Streak', Icons.stars_outlined,
                GlassTokens.danger, longestStreak >= 100),
          ],
        ),
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
            fontSize: 22,
            fontWeight: FontWeight.bold,
          ),
        ),
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

  Widget _buildGoalButton({
    required IconData icon,
    required VoidCallback onTap,
  }) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        width: 40,
        height: 40,
        decoration: BoxDecoration(
          color: Colors.white.withValues(alpha: 0.06),
          borderRadius: BorderRadius.circular(12),
          border: Border.all(
            color: Colors.white.withValues(alpha: 0.15),
          ),
        ),
        child: Icon(icon, color: GlassTokens.accentPrimary, size: 20),
      ),
    );
  }

  Widget _buildMilestone(
    String title,
    IconData icon,
    Color color,
    bool achieved,
  ) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: GlassCard(
        padding: const EdgeInsets.all(16),
        child: Row(
          children: [
            Container(
              width: 48,
              height: 48,
              decoration: BoxDecoration(
                color: achieved
                    ? color.withValues(alpha: 0.15)
                    : Colors.white.withValues(alpha: 0.06),
                borderRadius: BorderRadius.circular(12),
              ),
              child: Icon(
                icon,
                color: achieved ? color : GlassTokens.textSecondary,
                size: 24,
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Text(
                title,
                style: TextStyle(
                  color: achieved ? GlassTokens.textPrimary : GlassTokens.textSecondary,
                  fontSize: 15,
                  fontWeight: FontWeight.w500,
                ),
              ),
            ),
            if (achieved)
              Icon(Icons.check_circle, color: color, size: 20)
            else
              Icon(Icons.lock_outline, color: GlassTokens.textTertiary, size: 20),
          ],
        ),
      ),
    );
  }
}
