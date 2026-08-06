// ============================================================
// FILE: lib/features/youtube_study_mode/presentation/youtube_study_mode_screen.dart
// PURPOSE: YouTube Study Mode — whitelist-only YouTube access with
//          channel management. Reads/writes the persisted
//          BlockRulesController, pre-seeded with default study
//          channels (GFG, CodeWithHarry, Apna College, etc.).
// CREATED: 2026-08-03 | LAST MODIFIED: 2026-08-04
// ============================================================
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/theme/glass_tokens.dart';
import '../../../core/state/block_rules_controller.dart';
import '../../../shared/widgets/glass_card.dart';

class YoutubeStudyModeScreen extends ConsumerStatefulWidget {
  const YoutubeStudyModeScreen({super.key});

  @override
  ConsumerState<YoutubeStudyModeScreen> createState() =>
      _YoutubeStudyModeScreenState();
}

class _YoutubeStudyModeScreenState
    extends ConsumerState<YoutubeStudyModeScreen> {
  final _channelController = TextEditingController();

  @override
  void dispose() {
    _channelController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final rules = ref.watch(blockRulesProvider);
    final notifier = ref.read(blockRulesProvider.notifier);
    final whitelist = rules.youtubeWhitelist;

    return Scaffold(
      backgroundColor: GlassTokens.bgBase,
      appBar: AppBar(
        title: const Text('YouTube Study Mode'),
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
            // Toggle
            GlassCard(
              padding: const EdgeInsets.all(16),
              child: Row(
                children: [
                  Container(
                    width: 48,
                    height: 48,
                    decoration: BoxDecoration(
                      color: rules.youtubeStudyMode
                          ? GlassTokens.success.withValues(alpha: 0.15)
                          : Colors.white.withValues(alpha: 0.06),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: Icon(
                      Icons.school_outlined,
                      color: rules.youtubeStudyMode
                          ? GlassTokens.success
                          : GlassTokens.textSecondary,
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'Study Mode',
                          style: TextStyle(
                            color: GlassTokens.textPrimary,
                            fontSize: 16,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                        Text(
                          'Only allow whitelisted channels on YouTube',
                          style: TextStyle(
                            color: GlassTokens.textSecondary,
                            fontSize: 13,
                          ),
                        ),
                      ],
                    ),
                  ),
                  Switch(
                    value: rules.youtubeStudyMode,
                    onChanged: notifier.setStudyMode,
                    activeThumbColor: GlassTokens.success,
                    activeTrackColor:
                        GlassTokens.success.withValues(alpha: 0.3),
                    inactiveThumbColor: GlassTokens.textSecondary,
                    inactiveTrackColor: Colors.white.withValues(alpha: 0.1),
                  ),
                ],
              ),
            ),

            const SizedBox(height: 24),

            // How it works
            Text(
              'How it works',
              style: TextStyle(
                color: GlassTokens.textPrimary,
                fontSize: 18,
                fontWeight: FontWeight.w600,
              ),
            ),
            const SizedBox(height: 12),
            GlassCard(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _buildInfoRow(
                    '1.',
                    'Only channels on your whitelist will be accessible',
                  ),
                  _buildInfoRow(
                    '2.',
                    'YouTube Shorts are always blocked in Study Mode',
                  ),
                  _buildInfoRow(
                    '3.',
                    'Non-whitelisted videos show an interstitial block screen',
                  ),
                  _buildInfoRow(
                    '4.',
                    'A default set of popular study channels is pre-loaded below — remove any you don\'t want',
                  ),
                ],
              ),
            ),

            const SizedBox(height: 24),

            // Whitelist
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  'Whitelisted Channels',
                  style: TextStyle(
                    color: GlassTokens.textPrimary,
                    fontSize: 18,
                    fontWeight: FontWeight.w600,
                  ),
                ),
                Text(
                  '${whitelist.length} channels',
                  style: TextStyle(
                    color: GlassTokens.textSecondary,
                    fontSize: 14,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),

            // Add channel
            GlassCard(
              padding: const EdgeInsets.all(12),
              child: Row(
                children: [
                  Expanded(
                    child: TextField(
                      controller: _channelController,
                      onSubmitted: (_) => _addChannel(notifier),
                      decoration: InputDecoration(
                        hintText: 'Add channel name...',
                        border: InputBorder.none,
                        enabledBorder: InputBorder.none,
                        focusedBorder: InputBorder.none,
                        hintStyle: TextStyle(
                          color: GlassTokens.textSecondary,
                        ),
                      ),
                      style: TextStyle(color: GlassTokens.textPrimary),
                    ),
                  ),
                  IconButton(
                    onPressed: () => _addChannel(notifier),
                    icon: Icon(
                      Icons.add_circle,
                      color: GlassTokens.accentPrimary,
                    ),
                  ),
                ],
              ),
            ),

            const SizedBox(height: 12),

            // Channel list
            ...whitelist.map(
              (channel) => _buildChannelTile(channel, notifier),
            ),

            if (whitelist.isEmpty)
              Padding(
                padding: const EdgeInsets.symmetric(vertical: 24),
                child: Center(
                  child: TextButton.icon(
                    onPressed: notifier.resetWhitelistToDefaults,
                    icon: Icon(Icons.restore, color: GlassTokens.accentPrimary),
                    label: Text(
                      'Restore default study channels',
                      style: TextStyle(color: GlassTokens.accentPrimary),
                    ),
                  ),
                ),
              ),
          ],
        ),
      ),
    );
  }

  Widget _buildInfoRow(String number, String text) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            number,
            style: TextStyle(
              color: GlassTokens.accentPrimary,
              fontWeight: FontWeight.w600,
            ),
          ),
          const SizedBox(width: 8),
          Expanded(
            child: Text(
              text,
              style: TextStyle(
                color: GlassTokens.textSecondary,
                fontSize: 14,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildChannelTile(String channel, BlockRulesController notifier) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: GlassCard(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
        child: Row(
          children: [
            Container(
              width: 40,
              height: 40,
              decoration: BoxDecoration(
                color: GlassTokens.accentPrimary.withValues(alpha: 0.15),
                borderRadius: BorderRadius.circular(10),
              ),
              child: Icon(
                Icons.play_circle_outline,
                color: GlassTokens.accentPrimary,
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Text(
                channel,
                style: TextStyle(
                  color: GlassTokens.textPrimary,
                  fontSize: 15,
                  fontWeight: FontWeight.w500,
                ),
              ),
            ),
            IconButton(
              onPressed: () => notifier.removeWhitelistChannel(channel),
              icon: const Icon(
                Icons.remove_circle_outline,
                color: GlassTokens.danger,
                size: 20,
              ),
            ),
          ],
        ),
      ),
    );
  }

  void _addChannel(BlockRulesController notifier) {
    final name = _channelController.text.trim();
    if (name.isEmpty) return;
    notifier.addWhitelistChannel(name);
    _channelController.clear();
  }
}
