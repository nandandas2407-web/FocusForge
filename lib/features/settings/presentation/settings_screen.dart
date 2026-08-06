// ============================================================
// FILE: lib/features/settings/presentation/settings_screen.dart
// PURPOSE: App settings — appearance, permissions, blocking,
//          data & privacy, about. Permission tiles show real
//          granted/denied status; Strict Mode is wired to the
//          real persisted block-rules state instead of a local
//          no-op toggle; placeholder buttons with no behavior
//          have been removed rather than left as dead taps.
// CREATED: 2026-08-03 | LAST MODIFIED: 2026-08-05
// ============================================================
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../core/theme/glass_tokens.dart';
import '../../../core/state/block_rules_controller.dart';
import '../../../shared/widgets/glass_card.dart';
import '../../../core/native_bridge/accessibility_bridge.dart';
import '../../../core/native_bridge/usage_stats_bridge.dart';
import '../../../core/native_bridge/overlay_bridge.dart';

class SettingsScreen extends ConsumerStatefulWidget {
  const SettingsScreen({super.key});

  @override
  ConsumerState<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends ConsumerState<SettingsScreen> {
  bool? _accessibilityGranted;
  bool? _usageStatsGranted;
  bool? _overlayGranted;

  @override
  void initState() {
    super.initState();
    _loadPermissionStatus();
  }

  Future<void> _loadPermissionStatus() async {
    final accessibility = await ref.read(accessibilityBridgeProvider).isEnabled();
    final usageStats = await ref.read(usageStatsBridgeProvider).isEnabled();
    final overlay = await ref.read(overlayBridgeProvider).isEnabled();
    if (!mounted) return;
    setState(() {
      _accessibilityGranted = accessibility;
      _usageStatsGranted = usageStats;
      _overlayGranted = overlay;
    });
  }

  @override
  Widget build(BuildContext context) {
    final rules = ref.watch(blockRulesProvider);

    return Scaffold(
      backgroundColor: GlassTokens.bgBase,
      appBar: AppBar(
        title: const Text('Settings'),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back_ios_new),
          onPressed: () => Navigator.of(context).pop(),
        ),
      ),
      body: RefreshIndicator(
        onRefresh: _loadPermissionStatus,
        child: SingleChildScrollView(
          physics: const AlwaysScrollableScrollPhysics(),
          padding: const EdgeInsets.all(20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _buildSectionHeader('Appearance'),
              _buildAppearanceSection(),

              const SizedBox(height: 24),
              _buildSectionHeader('Permissions'),
              _buildPermissionsSection(),

              const SizedBox(height: 24),
              _buildSectionHeader('Blocking'),
              _buildBlockingSection(rules),

              const SizedBox(height: 24),
              _buildSectionHeader('Data & Privacy'),
              _buildPrivacySection(),

              const SizedBox(height: 24),
              _buildSectionHeader('About'),
              _buildAboutSection(),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildSectionHeader(String title) {
    return Text(
      title,
      style: TextStyle(
        color: GlassTokens.textPrimary,
        fontSize: 18,
        fontWeight: FontWeight.w600,
      ),
    );
  }

  Widget _buildAppearanceSection() {
    return Column(
      children: [
        _buildNavigationTile(
          icon: Icons.palette_outlined,
          title: 'Theme Customizer',
          subtitle: 'Colors, glass effects, wallpapers',
          onTap: () => context.go('/theme-customizer'),
        ),
      ],
    );
  }

  Widget _buildPermissionsSection() {
    return Column(
      children: [
        _buildPermissionTile(
          icon: Icons.accessibility_new,
          title: 'Accessibility Service',
          subtitle: 'Required to detect and block distracting apps',
          granted: _accessibilityGranted,
          bridge: ref.read(accessibilityBridgeProvider),
        ),
        _buildPermissionTile(
          icon: Icons.history,
          title: 'Usage Stats',
          subtitle: 'Required for accurate screen time tracking',
          granted: _usageStatsGranted,
          bridge: ref.read(usageStatsBridgeProvider),
        ),
        _buildPermissionTile(
          icon: Icons.layers_outlined,
          title: 'Draw Over Apps',
          subtitle: 'Required to show block screens',
          granted: _overlayGranted,
          bridge: ref.read(overlayBridgeProvider),
        ),
      ],
    );
  }

  Widget _buildPermissionTile({
    required IconData icon,
    required String title,
    required String subtitle,
    required bool? granted,
    required dynamic bridge,
  }) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: GlassCard(
        padding: const EdgeInsets.all(12),
        onTap: () async {
          await bridge.openSettings();
          // Re-check status once the user returns from system settings.
          _loadPermissionStatus();
        },
        child: Row(
          children: [
            Icon(icon, color: GlassTokens.accentPrimary, size: 22),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: TextStyle(
                      color: GlassTokens.textPrimary,
                      fontSize: 14,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                  Text(
                    subtitle,
                    style: TextStyle(color: GlassTokens.textSecondary, fontSize: 12),
                  ),
                ],
              ),
            ),
            _buildStatusChip(granted),
          ],
        ),
      ),
    );
  }

  Widget _buildStatusChip(bool? granted) {
    if (granted == null) {
      return SizedBox(
        width: 16,
        height: 16,
        child: CircularProgressIndicator(
          strokeWidth: 2,
          color: GlassTokens.textSecondary,
        ),
      );
    }
    final color = granted ? GlassTokens.success : GlassTokens.danger;
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.15),
        borderRadius: BorderRadius.circular(8),
      ),
      child: Text(
        granted ? 'Granted' : 'Off',
        style: TextStyle(color: color, fontSize: 11, fontWeight: FontWeight.w600),
      ),
    );
  }

  Widget _buildBlockingSection(BlockRulesState rules) {
    return Column(
      children: [
        _buildNavigationTile(
          icon: Icons.block_outlined,
          title: 'App Blocker',
          subtitle: '${rules.blockedApps.length} app(s) blocked',
          onTap: () => context.go('/app-blocker'),
        ),
        _buildNavigationTile(
          icon: Icons.shortcut_outlined,
          title: 'Shorts/Reels Blocker',
          subtitle: '${rules.blockedShortsApps.length} app(s) targeted',
          onTap: () => context.go('/shorts-reels-blocker'),
        ),
        _buildNavigationTile(
          icon: Icons.school_outlined,
          title: 'YouTube Study Mode',
          subtitle: rules.youtubeStudyMode ? 'On' : 'Off',
          onTap: () => context.go('/youtube-study-mode'),
        ),
        _buildNavigationTile(
          icon: Icons.language_outlined,
          title: 'Website Blocker',
          subtitle: 'Block distracting websites',
          onTap: () => context.go('/website-blocker'),
        ),
        _buildSwitchTile(
          icon: Icons.lock_outline,
          title: 'Strict Mode',
          subtitle: 'No early exit from focus sessions',
          value: rules.strictMode,
          onChanged: (value) =>
              ref.read(blockRulesProvider.notifier).setStrictMode(value),
        ),
      ],
    );
  }

  Widget _buildPrivacySection() {
    return Column(
      children: [
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
                  'All accessibility data is processed on-device only and never leaves your device.',
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
    );
  }

  Widget _buildAboutSection() {
    return Column(
      children: [
        GlassCard(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'FocusForge',
                style: TextStyle(
                  color: GlassTokens.textPrimary,
                  fontSize: 18,
                  fontWeight: FontWeight.w600,
                ),
              ),
              const SizedBox(height: 4),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildNavigationTile({
    required IconData icon,
    required String title,
    String? subtitle,
    required VoidCallback onTap,
    bool isDestructive = false,
  }) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: GlassCard(
        padding: const EdgeInsets.all(12),
        onTap: onTap,
        child: Row(
          children: [
            Icon(
              icon,
              color: isDestructive ? GlassTokens.danger : GlassTokens.accentPrimary,
              size: 22,
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: TextStyle(
                      color: isDestructive
                          ? GlassTokens.danger
                          : GlassTokens.textPrimary,
                      fontSize: 14,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                  if (subtitle != null)
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
            Icon(Icons.chevron_right, color: GlassTokens.textSecondary, size: 20),
          ],
        ),
      ),
    );
  }

  Widget _buildSwitchTile({
    required IconData icon,
    required String title,
    String? subtitle,
    required bool value,
    required ValueChanged<bool> onChanged,
  }) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: GlassCard(
        padding: const EdgeInsets.all(12),
        child: Row(
          children: [
            Icon(icon, color: GlassTokens.accentPrimary, size: 22),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: TextStyle(
                      color: GlassTokens.textPrimary,
                      fontSize: 14,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                  if (subtitle != null)
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
            Switch(
              value: value,
              onChanged: onChanged,
              activeThumbColor: GlassTokens.accentPrimary,
              activeTrackColor: GlassTokens.accentPrimary.withValues(alpha: 0.3),
              inactiveThumbColor: GlassTokens.textSecondary,
              inactiveTrackColor: Colors.white.withValues(alpha: 0.1),
            ),
          ],
        ),
      ),
    );
  }
}
