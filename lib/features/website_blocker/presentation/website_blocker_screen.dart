// ============================================================
// FILE: lib/features/website_blocker/presentation/website_blocker_screen.dart
// PURPOSE: Domain-level website blocking UI. Backed by
//          WebsiteBlockerController (persisted) instead of local
//          setState that reset to seed data every launch.
//
//          NOTE ON THE VPN CLAIM: the native FocusVpnService now
//          actually parses DNS queries and drops ones matching a
//          blocked domain (previously it silently forwarded every
//          packet unmodified and blocked nothing). This code has
//          not been tested on a real device — hand-written packet
//          parsing is easy to get subtly wrong, so the info card
//          below is phrased to reflect "best effort" rather than a
//          guarantee.
// CREATED: 2026-08-03 | LAST MODIFIED: 2026-08-05
// ============================================================
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/theme/glass_tokens.dart';
import '../../../core/state/website_blocker_controller.dart';
import '../../../shared/widgets/glass_card.dart';

class WebsiteBlockerScreen extends ConsumerStatefulWidget {
  const WebsiteBlockerScreen({super.key});

  @override
  ConsumerState<WebsiteBlockerScreen> createState() =>
      _WebsiteBlockerScreenState();
}

class _WebsiteBlockerScreenState extends ConsumerState<WebsiteBlockerScreen> {
  final _domainController = TextEditingController();

  final _presets = {
    'Social Media': ['facebook.com', 'twitter.com', 'instagram.com', 'tiktok.com', 'reddit.com', 'snapchat.com'],
    'Entertainment': ['youtube.com', 'netflix.com', 'twitch.tv', 'tumblr.com', 'dailymotion.com'],
    'News': ['cnn.com', 'bbc.com', 'foxnews.com', 'nytimes.com'],
    'Shopping': ['amazon.com', 'ebay.com', 'walmart.com', 'etsy.com'],
  };

  @override
  void dispose() {
    _domainController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final blocker = ref.watch(websiteBlockerProvider);
    final notifier = ref.read(websiteBlockerProvider.notifier);

    return Scaffold(
      backgroundColor: GlassTokens.bgBase,
      appBar: AppBar(
        title: const Text('Website Blocker'),
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
            GlassCard(
              padding: const EdgeInsets.all(16),
              child: Row(
                children: [
                  Container(
                    width: 48,
                    height: 48,
                    decoration: BoxDecoration(
                      color: blocker.vpnEnabled
                          ? GlassTokens.success.withValues(alpha: 0.15)
                          : Colors.white.withValues(alpha: 0.06),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: Icon(
                      Icons.vpn_lock_outlined,
                      color: blocker.vpnEnabled ? GlassTokens.success : GlassTokens.textSecondary,
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'Website Blocking VPN',
                          style: TextStyle(color: GlassTokens.textPrimary, fontSize: 16, fontWeight: FontWeight.w600),
                        ),
                        Text(
                          'Local VPN — no traffic leaves your device',
                          style: TextStyle(color: GlassTokens.textSecondary, fontSize: 12),
                        ),
                      ],
                    ),
                  ),
                  Switch(
                    value: blocker.vpnEnabled,
                    onChanged: (value) => notifier.setVpnEnabled(value),
                    activeThumbColor: GlassTokens.success,
                    activeTrackColor: GlassTokens.success.withValues(alpha: 0.3),
                    inactiveThumbColor: GlassTokens.textSecondary,
                    inactiveTrackColor: Colors.white.withValues(alpha: 0.1),
                  ),
                ],
              ),
            ),

            const SizedBox(height: 12),

            _buildSwitchTile(
              title: 'Enforce outside focus sessions',
              subtitle: 'Block even when no session is active',
              value: blocker.enforceAlways,
              onChanged: (value) => notifier.setEnforceAlways(value),
            ),

            const SizedBox(height: 24),
            Text('Category Presets', style: TextStyle(color: GlassTokens.textPrimary, fontSize: 18, fontWeight: FontWeight.w600)),
            const SizedBox(height: 12),
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: _presets.keys.map((preset) {
                return ActionChip(
                  label: Text(preset),
                  onPressed: () => notifier.addDomains(_presets[preset]!),
                  backgroundColor: Colors.white.withValues(alpha: 0.06),
                  labelStyle: TextStyle(color: GlassTokens.textSecondary, fontSize: 13),
                  side: BorderSide(color: Colors.white.withValues(alpha: 0.15)),
                );
              }).toList(),
            ),

            const SizedBox(height: 24),
            Text(
              'Blocked Domains (${blocker.blockedDomains.length})',
              style: TextStyle(color: GlassTokens.textPrimary, fontSize: 18, fontWeight: FontWeight.w600),
            ),
            const SizedBox(height: 12),
            GlassCard(
              padding: const EdgeInsets.all(12),
              child: Row(
                children: [
                  Expanded(
                    child: TextField(
                      controller: _domainController,
                      decoration: InputDecoration(
                        hintText: 'e.g. example.com',
                        border: InputBorder.none,
                        enabledBorder: InputBorder.none,
                        focusedBorder: InputBorder.none,
                        hintStyle: TextStyle(color: GlassTokens.textSecondary),
                      ),
                      style: TextStyle(color: GlassTokens.textPrimary),
                      onSubmitted: (_) => _addDomain(notifier),
                    ),
                  ),
                  IconButton(
                    onPressed: () => _addDomain(notifier),
                    icon: Icon(Icons.add_circle, color: GlassTokens.accentPrimary),
                  ),
                ],
              ),
            ),

            const SizedBox(height: 12),

            if (blocker.blockedDomains.isEmpty)
              Padding(
                padding: const EdgeInsets.symmetric(vertical: 12),
                child: Text('No domains blocked yet', style: TextStyle(color: GlassTokens.textSecondary)),
              )
            else
              Wrap(
                spacing: 8,
                runSpacing: 8,
                children: blocker.blockedDomains.map((domain) {
                  return Chip(
                    label: Text(domain),
                    deleteIcon: const Icon(Icons.close, size: 16),
                    onDeleted: () => notifier.removeDomain(domain),
                    backgroundColor: GlassTokens.danger.withValues(alpha: 0.15),
                    labelStyle: const TextStyle(color: GlassTokens.danger, fontSize: 13),
                    side: BorderSide(color: GlassTokens.danger.withValues(alpha: 0.3)),
                  );
                }).toList(),
              ),

            const SizedBox(height: 24),

            GlassCard(
              padding: const EdgeInsets.all(16),
              child: Row(
                children: [
                  Icon(Icons.info_outline, color: GlassTokens.accentPrimary, size: 20),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Text(
                      'Website blocking works by intercepting DNS lookups through a local, on-device VPN — no traffic is routed through an external server. It only affects apps and browsers that use the system DNS resolver.',
                      style: TextStyle(color: GlassTokens.textSecondary, fontSize: 13, height: 1.4),
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

  void _addDomain(WebsiteBlockerController notifier) {
    if (_domainController.text.trim().isEmpty) return;
    notifier.addDomain(_domainController.text);
    _domainController.clear();
  }

  Widget _buildSwitchTile({
    required String title,
    String? subtitle,
    required bool value,
    required ValueChanged<bool> onChanged,
  }) {
    return GlassCard(
      padding: const EdgeInsets.all(12),
      child: Row(
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(title, style: TextStyle(color: GlassTokens.textPrimary, fontSize: 14, fontWeight: FontWeight.w500)),
                if (subtitle != null)
                  Text(subtitle, style: TextStyle(color: GlassTokens.textSecondary, fontSize: 12)),
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
    );
  }
}
