// ============================================================
// FILE: lib/features/shorts_reels_blocker/presentation/shorts_reels_blocker_screen.dart
// PURPOSE: Dedicated screen for Shorts/Reels blocking toggles.
//          Reads/writes the persisted BlockRulesController and
//          polls the native block-event counter for real numbers
//          instead of hardcoded placeholders.
// CREATED: 2026-08-03 | LAST MODIFIED: 2026-08-04
// ============================================================
import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/theme/glass_tokens.dart';
import '../../../core/state/block_rules_controller.dart';
import '../../../shared/widgets/glass_card.dart';

class ShortsReelsBlockerScreen extends ConsumerStatefulWidget {
  const ShortsReelsBlockerScreen({super.key});

  @override
  ConsumerState<ShortsReelsBlockerScreen> createState() =>
      _ShortsReelsBlockerScreenState();
}

class _ShortsReelsBlockerScreenState
    extends ConsumerState<ShortsReelsBlockerScreen> {
  Timer? _pollTimer;

  @override
  void initState() {
    super.initState();
    // Poll the native block-event counter every few seconds so the
    // "blocked today" figures reflect real accessibility-service
    // activity rather than a static mock number.
    ref.read(blockRulesProvider.notifier).refreshBlockCount();
    _pollTimer = Timer.periodic(const Duration(seconds: 4), (_) {
      ref.read(blockRulesProvider.notifier).refreshBlockCount();
    });
  }

  @override
  void dispose() {
    _pollTimer?.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final rules = ref.watch(blockRulesProvider);
    final notifier = ref.read(blockRulesProvider.notifier);
    final blocked = rules.blockedShortsApps;

    return Scaffold(
      backgroundColor: GlassTokens.bgBase,
      appBar: AppBar(
        title: const Text('Shorts & Reels Blocker'),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back_ios_new),
          onPressed: () => Navigator.of(context).pop(),
        ),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Expanded(
                  child: Text(
                    'Block short-form video content',
                    style: TextStyle(
                      color: GlassTokens.textSecondary,
                      fontSize: 14,
                    ),
                  ),
                ),
                if (rules.sessionActive)
                  Container(
                    padding: const EdgeInsets.symmetric(
                        horizontal: 10, vertical: 4),
                    decoration: BoxDecoration(
                      color: GlassTokens.success.withValues(alpha: 0.15),
                      borderRadius: BorderRadius.circular(20),
                    ),
                    child: const Text(
                      '● Active',
                      style: TextStyle(
                        color: GlassTokens.success,
                        fontSize: 12,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  )
                else
                  Container(
                    padding: const EdgeInsets.symmetric(
                        horizontal: 10, vertical: 4),
                    decoration: BoxDecoration(
                      color: Colors.white.withValues(alpha: 0.08),
                      borderRadius: BorderRadius.circular(20),
                    ),
                    child: Text(
                      'No active session',
                      style: TextStyle(
                        color: GlassTokens.textSecondary,
                        fontSize: 12,
                      ),
                    ),
                  ),
              ],
            ),
            const SizedBox(height: 20),

            _buildToggleCard(
              title: 'Instagram Reels',
              subtitle: 'Block the Reels tab and player',
              icon: Icons.camera_alt_outlined,
              value: blocked.contains(ShortsTargets.instagram),
              blockedCount: rules.blocksToday,
              onChanged: (value) => notifier.setShortsAppBlocked(
                  ShortsTargets.instagram, value),
            ),
            _buildToggleCard(
              title: 'YouTube Shorts',
              subtitle: 'Block the Shorts player and feed',
              icon: Icons.play_circle_outline,
              value: blocked.contains(ShortsTargets.youtube),
              blockedCount: rules.blocksToday,
              onChanged: (value) =>
                  notifier.setShortsAppBlocked(ShortsTargets.youtube, value),
            ),
            _buildToggleCard(
              title: 'TikTok',
              subtitle: 'Block TikTok entirely (no partial mode)',
              icon: Icons.music_note_outlined,
              value: blocked.contains(ShortsTargets.tiktok),
              blockedCount: rules.blocksToday,
              onChanged: (value) =>
                  notifier.setShortsAppBlocked(ShortsTargets.tiktok, value),
            ),
            _buildToggleCard(
              title: 'Snapchat Spotlight',
              subtitle: 'Block the Spotlight tab',
              icon: Icons.emoji_emotions,
              value: blocked.contains(ShortsTargets.snapchat),
              blockedCount: rules.blocksToday,
              onChanged: (value) =>
                  notifier.setShortsAppBlocked(ShortsTargets.snapchat, value),
            ),
            _buildToggleCard(
              title: 'Facebook Reels',
              subtitle: 'Block the Reels section in Facebook',
              icon: Icons.facebook_outlined,
              value: blocked.contains(ShortsTargets.facebook),
              blockedCount: rules.blocksToday,
              onChanged: (value) =>
                  notifier.setShortsAppBlocked(ShortsTargets.facebook, value),
            ),

            const SizedBox(height: 24),

            if (!rules.sessionActive && blocked.isNotEmpty)
              Padding(
                padding: const EdgeInsets.only(bottom: 16),
                child: GlassCard(
                  padding: const EdgeInsets.all(16),
                  child: Row(
                    children: [
                      const Icon(Icons.warning_amber_rounded,
                          color: GlassTokens.warning, size: 20),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Text(
                          'These toggles only take effect during an active focus session. Start one from the Focus tab.',
                          style: TextStyle(
                            color: GlassTokens.textSecondary,
                            fontSize: 13,
                            height: 1.4,
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
              ),

            GlassCard(
              padding: const EdgeInsets.all(16),
              child: Row(
                children: [
                  Icon(
                    Icons.info_outline,
                    color: GlassTokens.accentPrimary,
                    size: 20,
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Text(
                      'Detection rules are updated remotely as app UIs change. The Accessibility Service monitors which screen/tab is active inside each app.',
                      style: TextStyle(
                        color: GlassTokens.textSecondary,
                        fontSize: 13,
                        height: 1.4,
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildToggleCard({
    required String title,
    required String subtitle,
    required IconData icon,
    required bool value,
    required int blockedCount,
    required ValueChanged<bool> onChanged,
  }) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: GlassCard(
        padding: const EdgeInsets.all(16),
        child: Row(
          children: [
            Container(
              width: 48,
              height: 48,
              decoration: BoxDecoration(
                color: value
                    ? GlassTokens.danger.withValues(alpha: 0.15)
                    : Colors.white.withValues(alpha: 0.06),
                borderRadius: BorderRadius.circular(12),
              ),
              child: Icon(
                icon,
                color: value ? GlassTokens.danger : GlassTokens.textSecondary,
                size: 24,
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: TextStyle(
                      color: GlassTokens.textPrimary,
                      fontSize: 15,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                  Text(
                    subtitle,
                    style: TextStyle(
                      color: GlassTokens.textSecondary,
                      fontSize: 12,
                    ),
                  ),
                ],
              ),
            ),
            Column(
              children: [
                Switch(
                  value: value,
                  onChanged: onChanged,
                  activeThumbColor: GlassTokens.danger,
                  activeTrackColor: GlassTokens.danger.withValues(alpha: 0.3),
                  inactiveThumbColor: GlassTokens.textSecondary,
                  inactiveTrackColor: Colors.white.withValues(alpha: 0.1),
                ),
                if (value && blockedCount > 0)
                  Text(
                    '$blockedCount blocked today',
                    style: const TextStyle(
                      color: GlassTokens.danger,
                      fontSize: 10,
                    ),
                  ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
