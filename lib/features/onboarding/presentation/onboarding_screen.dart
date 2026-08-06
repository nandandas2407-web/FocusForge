// ============================================================
// FILE: lib/features/onboarding/presentation/onboarding_screen.dart
// PURPOSE: First-launch onboarding flow — permission setup,
//          goal selection, wallpaper picker, accent color.
// CREATED: 2026-08-03 | LAST MODIFIED: 2026-08-03
// ============================================================
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:image_picker/image_picker.dart';
import '../../../core/theme/glass_tokens.dart';
import '../../../shared/widgets/glass_card.dart';
import '../../../shared/widgets/glass_button.dart';
import '../../../core/native_bridge/accessibility_bridge.dart';
import '../../../core/native_bridge/usage_stats_bridge.dart';
import '../../../core/native_bridge/overlay_bridge.dart';
import '../../../core/theme/wallpaper_controller.dart';
import '../../../core/state/onboarding_controller.dart';

class OnboardingScreen extends ConsumerStatefulWidget {
  const OnboardingScreen({super.key});

  @override
  ConsumerState<OnboardingScreen> createState() => _OnboardingScreenState();
}

class _OnboardingScreenState extends ConsumerState<OnboardingScreen> {
  final PageController _pageController = PageController();
  int _currentPage = 0;

  final _goals = [
    _GoalOption('Study & Exam Prep', Icons.school_outlined, 'Block distractions during study sessions'),
    _GoalOption('General Focus', Icons.center_focus_strong, 'Build better focus habits'),
    _GoalOption('Digital Detox', Icons.phone_android_outlined, 'Reduce screen time significantly'),
  ];

  String _selectedGoal = '';
  Color _selectedAccent = GlassTokens.accentPrimary;
  final _accentColors = [
    const Color(0xFF7C5CFF), // Violet
    const Color(0xFF60A5FA), // Blue
    const Color(0xFF3DDC97), // Green
    const Color(0xFFFFC857), // Amber
    const Color(0xFFFF5C7C), // Rose
    const Color(0xFF9B7FFF), // Lavender
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: GlassTokens.bgBase,
      body: SafeArea(
        child: Column(
          children: [
            // Page indicator
            Padding(
              padding: const EdgeInsets.symmetric(vertical: 16),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: List.generate(5, (i) => _buildIndicator(i)),
              ),
            ),
            // Page view
            Expanded(
              child: PageView(
                controller: _pageController,
                onPageChanged: (page) => setState(() => _currentPage = page),
                physics: const NeverScrollableScrollPhysics(),
                children: [
                  _buildWelcomePage(),
                  _buildPermissionsPage(),
                  _buildGoalPage(),
                  _buildWallpaperPage(),
                  _buildThemePage(),
                ],
              ),
            ),
            // Navigation
            Padding(
              padding: const EdgeInsets.all(24),
              child: Row(
                children: [
                  if (_currentPage > 0)
                    Expanded(
                      child: GlassButton(
                        label: 'Back',
                        isPrimary: false,
                        onPressed: () {
                          _pageController.previousPage(
                            duration: const Duration(milliseconds: 300),
                            curve: Curves.easeInOut,
                          );
                        },
                      ),
                    ),
                  if (_currentPage > 0) const SizedBox(width: 12),
                  Expanded(
                    flex: 2,
                    child: GlassButton(
                      label: _currentPage == 4 ? 'Get Started' : 'Next',
                      icon: _currentPage == 4
                          ? Icons.check
                          : Icons.arrow_forward,
                      isPrimary: true,
                      onPressed: () async {
                        if (_currentPage == 4) {
                          await ref
                              .read(onboardingCompletedProvider.notifier)
                              .markCompleted();
                          if (context.mounted) context.go('/');
                        } else {
                          _pageController.nextPage(
                            duration: const Duration(milliseconds: 300),
                            curve: Curves.easeInOut,
                          );
                        }
                      },
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

  Widget _buildIndicator(int index) {
    final isActive = index == _currentPage;
    return AnimatedContainer(
      duration: const Duration(milliseconds: 300),
      margin: const EdgeInsets.symmetric(horizontal: 4),
      width: isActive ? 24 : 8,
      height: 8,
      decoration: BoxDecoration(
        color: isActive
            ? GlassTokens.accentPrimary
            : Colors.white.withValues(alpha: 0.2),
        borderRadius: BorderRadius.circular(4),
      ),
    );
  }

  Widget _buildWelcomePage() {
    return Padding(
      padding: const EdgeInsets.all(32),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(
            Icons.shield_outlined,
            size: 80,
            color: GlassTokens.accentPrimary,
          ),
          const SizedBox(height: 32),
          Text(
            'Welcome to FocusForge',
            style: TextStyle(
              color: GlassTokens.textPrimary,
              fontSize: 28,
              fontWeight: FontWeight.bold,
            ),
            textAlign: TextAlign.center,
          ),
          const SizedBox(height: 16),
          Text(
            'Your focus companion with liquid glass design.\nBlock distractions. Study smarter. Stay in flow.',
            style: TextStyle(
              color: GlassTokens.textSecondary,
              fontSize: 16,
              height: 1.5,
            ),
            textAlign: TextAlign.center,
          ),
        ],
      ),
    );
  }

  Widget _buildPermissionsPage() {
    return Padding(
      padding: const EdgeInsets.all(24),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Set Up Permissions',
            style: TextStyle(
              color: GlassTokens.textPrimary,
              fontSize: 24,
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            'FocusForge needs these to protect your focus.',
            style: TextStyle(color: GlassTokens.textSecondary),
          ),
          const SizedBox(height: 24),
          _PermissionTile(
            icon: Icons.accessibility_new,
            title: 'Accessibility Service',
            subtitle: 'Detects which app is in the foreground',
            onTap: () => ref.read(accessibilityBridgeProvider).openSettings(),
          ),
          _PermissionTile(
            icon: Icons.history,
            title: 'Usage Access',
            subtitle: 'Tracks screen time for your dashboard',
            onTap: () => ref.read(usageStatsBridgeProvider).openSettings(),
          ),
          _PermissionTile(
            icon: Icons.layers_outlined,
            title: 'Draw Over Apps',
            subtitle: 'Shows block overlay screens',
            onTap: () => ref.read(overlayBridgeProvider).openSettings(),
          ),
        ],
      ),
    );
  }

  Widget _buildGoalPage() {
    return Padding(
      padding: const EdgeInsets.all(24),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'What\'s your goal?',
            style: TextStyle(
              color: GlassTokens.textPrimary,
              fontSize: 24,
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 24),
          ...(_goals.map((goal) => _buildGoalCard(goal))),
        ],
      ),
    );
  }

  Widget _buildGoalCard(_GoalOption goal) {
    final isSelected = _selectedGoal == goal.title;
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: GlassCard(
        padding: const EdgeInsets.all(16),
        onTap: () => setState(() => _selectedGoal = goal.title),
        child: Row(
          children: [
            Container(
              width: 48,
              height: 48,
              decoration: BoxDecoration(
                color: isSelected
                    ? GlassTokens.accentPrimary.withValues(alpha: 0.2)
                    : Colors.white.withValues(alpha: 0.06),
                borderRadius: BorderRadius.circular(12),
              ),
              child: Icon(
                goal.icon,
                color: isSelected
                    ? GlassTokens.accentPrimary
                    : GlassTokens.textSecondary,
              ),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    goal.title,
                    style: TextStyle(
                      color: GlassTokens.textPrimary,
                      fontSize: 16,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                  Text(
                    goal.subtitle,
                    style: TextStyle(
                      color: GlassTokens.textSecondary,
                      fontSize: 13,
                    ),
                  ),
                ],
              ),
            ),
            if (isSelected)
              Icon(Icons.check_circle, color: GlassTokens.accentPrimary),
          ],
        ),
      ),
    );
  }

  Widget _buildWallpaperPage() {
    return Padding(
      padding: const EdgeInsets.all(24),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Choose Your Wallpaper',
            style: TextStyle(
              color: GlassTokens.textPrimary,
              fontSize: 24,
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            'Your wallpaper shows behind the glass panels.',
            style: TextStyle(color: GlassTokens.textSecondary),
          ),
          const SizedBox(height: 24),
          GlassCard(
            onTap: () async {
              final picker = ImagePicker();
              final image = await picker.pickImage(source: ImageSource.gallery);
              if (image != null) {
                ref.read(wallpaperProvider.notifier).setWallpaper(image.path);
              }
            },
            child: Column(
              children: [
                Icon(
                  Icons.photo_library_outlined,
                  size: 48,
                  color: GlassTokens.accentPrimary,
                ),
                const SizedBox(height: 12),
                Text(
                  'Pick from Gallery',
                  style: TextStyle(
                    color: GlassTokens.textPrimary,
                    fontSize: 16,
                    fontWeight: FontWeight.w500,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  'Or use the default glass background',
                  style: TextStyle(
                    color: GlassTokens.textSecondary,
                    fontSize: 13,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildThemePage() {
    return Padding(
      padding: const EdgeInsets.all(24),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Pick Accent Color',
            style: TextStyle(
              color: GlassTokens.textPrimary,
              fontSize: 24,
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 24),
          Wrap(
            spacing: 16,
            runSpacing: 16,
            children: _accentColors.map((color) {
              final isSelected = _selectedAccent == color;
              return GestureDetector(
                onTap: () => setState(() {
                  _selectedAccent = color;
                  GlassTokens.accentPrimary = color;
                }),
                child: Container(
                  width: 56,
                  height: 56,
                  decoration: BoxDecoration(
                    color: color,
                    shape: BoxShape.circle,
                    border: Border.all(
                      color: isSelected ? Colors.white : Colors.transparent,
                      width: 3,
                    ),
                    boxShadow: isSelected
                        ? [
                            BoxShadow(
                              color: color.withValues(alpha: 0.5),
                              blurRadius: 16,
                              spreadRadius: 2,
                            )
                          ]
                        : null,
                  ),
                  child: isSelected
                      ? const Icon(Icons.check, color: Colors.white)
                      : null,
                ),
              );
            }).toList(),
          ),
          const SizedBox(height: 32),
          GlassCard(
            child: Text(
              'Preview: The glass panels will use this accent color for buttons, progress rings, and active states.',
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
}

class _GoalOption {
  final String title;
  final IconData icon;
  final String subtitle;

  _GoalOption(this.title, this.icon, this.subtitle);
}

class _PermissionTile extends StatelessWidget {
  final IconData icon;
  final String title;
  final String subtitle;
  final VoidCallback onTap;

  const _PermissionTile({
    required this.icon,
    required this.title,
    required this.subtitle,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: GlassCard(
        padding: const EdgeInsets.all(16),
        onTap: onTap,
        child: Row(
          children: [
            Icon(icon, color: GlassTokens.accentPrimary, size: 28),
            const SizedBox(width: 16),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: TextStyle(
                      color: GlassTokens.textPrimary,
                      fontSize: 15,
                      fontWeight: FontWeight.w600,
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
            Icon(Icons.chevron_right, color: GlassTokens.textSecondary),
          ],
        ),
      ),
    );
  }
}
