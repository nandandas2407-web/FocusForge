// ============================================================
// FILE: lib/features/app_blocker/presentation/app_blocker_screen.dart
// PURPOSE: Searchable list of installed apps with toggle switches
//          and block-mode presets. Reads/writes the persisted
//          BlockRulesController, which pushes changes to the native
//          accessibility service in real time.
// CREATED: 2026-08-03 | LAST MODIFIED: 2026-08-04
// ============================================================
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/theme/glass_tokens.dart';
import '../../../core/state/block_rules_controller.dart';
import '../../../core/native_bridge/usage_stats_bridge.dart';
import '../../../shared/widgets/glass_card.dart';

/// Fallback list shown only when the native installed-apps query is
/// unavailable (e.g. previewing on desktop/web). On a real Android
/// device this is bypassed entirely by getInstalledApps().
const _fallbackApps = [
  _AppEntry('com.instagram.android', 'Instagram'),
  _AppEntry('com.google.android.youtube', 'YouTube'),
  _AppEntry('com.zhiliaoapp.musically', 'TikTok'),
  _AppEntry('com.twitter.android', 'X (Twitter)'),
  _AppEntry('com.snapchat.android', 'Snapchat'),
  _AppEntry('com.facebook.katana', 'Facebook'),
  _AppEntry('com.whatsapp', 'WhatsApp'),
  _AppEntry('com.spotify.music', 'Spotify'),
  _AppEntry('com.reddit.frontpage', 'Reddit'),
];

class _AppEntry {
  final String packageName;
  final String name;
  const _AppEntry(this.packageName, this.name);
}

final _presets = {
  'Social': [
    'com.instagram.android',
    'com.twitter.android',
    'com.facebook.katana',
    'com.snapchat.android',
    'com.zhiliaoapp.musically',
  ],
  'Short-form Video': [
    'com.instagram.android',
    'com.google.android.youtube',
    'com.zhiliaoapp.musically',
  ],
  'Games': [
    'com.supercell.clashofclans',
    'com.supercell.brawlstars',
    'com.epicgames.fortnite',
  ],
  'Shopping': [
    'com.amazon.mShop.android.shopping',
    'com.flipkart.android',
    'com.etsy.android',
  ],
};

class AppBlockerScreen extends ConsumerStatefulWidget {
  const AppBlockerScreen({super.key});

  @override
  ConsumerState<AppBlockerScreen> createState() => _AppBlockerScreenState();
}

class _AppBlockerScreenState extends ConsumerState<AppBlockerScreen> {
  final _searchController = TextEditingController();
  String _searchQuery = '';
  String _selectedPreset = '';

  List<_AppEntry> _apps = _fallbackApps;
  bool _loading = true;

  @override
  void initState() {
    super.initState();
    _loadInstalledApps();
  }

  Future<void> _loadInstalledApps() async {
    final bridge = ref.read(usageStatsBridgeProvider);
    final installed = await bridge.getInstalledApps();
    if (!mounted) return;
    setState(() {
      if (installed.isNotEmpty) {
        _apps = installed
            .map((a) => _AppEntry(a.packageName, a.appLabel))
            .toList();
      }
      _loading = false;
    });
  }

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final blockedApps = ref.watch(blockRulesProvider).blockedApps;

    final filteredApps = _apps.where((app) {
      if (_searchQuery.isEmpty) return true;
      return app.name.toLowerCase().contains(_searchQuery.toLowerCase()) ||
          app.packageName.toLowerCase().contains(_searchQuery.toLowerCase());
    }).toList();

    return Scaffold(
      backgroundColor: GlassTokens.bgBase,
      appBar: AppBar(
        title: const Text('App Blocker'),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back_ios_new),
          onPressed: () => Navigator.of(context).pop(),
        ),
      ),
      body: Column(
        children: [
          // Search bar
          Padding(
            padding: const EdgeInsets.fromLTRB(20, 0, 20, 12),
            child: TextField(
              controller: _searchController,
              onChanged: (value) => setState(() => _searchQuery = value),
              decoration: InputDecoration(
                hintText: 'Search apps...',
                prefixIcon: const Icon(Icons.search),
                suffixIcon: _searchQuery.isNotEmpty
                    ? IconButton(
                        icon: const Icon(Icons.clear),
                        onPressed: () {
                          _searchController.clear();
                          setState(() => _searchQuery = '');
                        },
                      )
                    : null,
              ),
            ),
          ),

          // Preset chips
          SizedBox(
            height: 48,
            child: ListView(
              scrollDirection: Axis.horizontal,
              padding: const EdgeInsets.symmetric(horizontal: 20),
              children: _presets.keys.map((preset) {
                final isSelected = _selectedPreset == preset;
                return Padding(
                  padding: const EdgeInsets.only(right: 8),
                  child: FilterChip(
                    label: Text(preset),
                    selected: isSelected,
                    onSelected: (_) {
                      setState(() {
                        _selectedPreset = isSelected ? '' : preset;
                      });
                      final notifier = ref.read(blockRulesProvider.notifier);
                      if (isSelected) {
                        notifier.setBlockedApps({});
                      } else {
                        notifier.setBlockedApps(_presets[preset]!.toSet());
                      }
                    },
                    selectedColor:
                        GlassTokens.accentPrimary.withValues(alpha: 0.2),
                    checkmarkColor: GlassTokens.accentPrimary,
                    labelStyle: TextStyle(
                      color: isSelected
                          ? GlassTokens.accentPrimary
                          : GlassTokens.textSecondary,
                    ),
                    side: BorderSide(
                      color: isSelected
                          ? GlassTokens.accentPrimary
                          : Colors.white.withValues(alpha: 0.15),
                    ),
                  ),
                );
              }).toList(),
            ),
          ),
          const SizedBox(height: 8),

          // App list
          Expanded(
            child: _loading
                ? Center(
                    child: CircularProgressIndicator(
                      color: GlassTokens.accentPrimary,
                    ),
                  )
                : ListView.builder(
                    padding: const EdgeInsets.symmetric(horizontal: 20),
                    itemCount: filteredApps.length,
                    itemBuilder: (context, index) {
                      final app = filteredApps[index];
                      final isBlocked =
                          blockedApps.contains(app.packageName);
                      return _buildAppTile(app, isBlocked);
                    },
                  ),
          ),
        ],
      ),
    );
  }

  Widget _buildAppTile(_AppEntry app, bool isBlocked) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: GlassCard(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
        child: Row(
          children: [
            Container(
              width: 44,
              height: 44,
              decoration: BoxDecoration(
                color: Colors.white.withValues(alpha: 0.06),
                borderRadius: BorderRadius.circular(12),
              ),
              child: Icon(
                _getAppIcon(app.packageName),
                color: GlassTokens.textSecondary,
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    app.name,
                    style: TextStyle(
                      color: GlassTokens.textPrimary,
                      fontSize: 15,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                  Text(
                    app.packageName,
                    style: TextStyle(
                      color: GlassTokens.textTertiary,
                      fontSize: 11,
                    ),
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                  ),
                ],
              ),
            ),
            Switch(
              value: isBlocked,
              onChanged: (value) {
                ref
                    .read(blockRulesProvider.notifier)
                    .setAppBlocked(app.packageName, value);
              },
              activeThumbColor: GlassTokens.danger,
              activeTrackColor: GlassTokens.danger.withValues(alpha: 0.3),
              inactiveThumbColor: GlassTokens.textSecondary,
              inactiveTrackColor: Colors.white.withValues(alpha: 0.1),
            ),
          ],
        ),
      ),
    );
  }

  IconData _getAppIcon(String packageName) {
    if (packageName.contains('instagram')) return Icons.camera_alt_outlined;
    if (packageName.contains('youtube')) return Icons.play_circle_outline;
    if (packageName.contains('tiktok') || packageName.contains('musically')) {
      return Icons.music_note_outlined;
    }
    if (packageName.contains('twitter')) return Icons.alternate_email;
    if (packageName.contains('snapchat')) return Icons.emoji_emotions;
    if (packageName.contains('facebook')) return Icons.facebook_outlined;
    if (packageName.contains('whatsapp')) return Icons.chat_bubble_outline;
    if (packageName.contains('spotify')) return Icons.music_note;
    if (packageName.contains('discord')) return Icons.headset_mic_outlined;
    if (packageName.contains('reddit')) return Icons.forum_outlined;
    if (packageName.contains('amazon')) return Icons.shopping_bag_outlined;
    return Icons.apps;
  }
}
