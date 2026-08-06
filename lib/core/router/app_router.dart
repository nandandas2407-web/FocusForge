// ============================================================
// FILE: lib/core/router/app_router.dart
// PURPOSE: Go_router configuration with all app routes.
// CREATED: 2026-08-03 | LAST MODIFIED: 2026-08-03
// ============================================================
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../state/onboarding_controller.dart';
import '../theme/glass_tokens.dart';
import '../../shared/responsive/breakpoints.dart';
import '../../features/onboarding/presentation/onboarding_screen.dart';
import '../../features/home_dashboard/presentation/home_dashboard.dart';
import '../../features/app_blocker/presentation/app_blocker_screen.dart';
import '../../features/shorts_reels_blocker/presentation/shorts_reels_blocker_screen.dart';
import '../../features/youtube_study_mode/presentation/youtube_study_mode_screen.dart';
import '../../features/website_blocker/presentation/website_blocker_screen.dart';
import '../../features/focus_timer/presentation/focus_timer_screen.dart';
import '../../features/todo/presentation/todo_screen.dart';
import '../../features/calendar/presentation/calendar_screen.dart';
import '../../features/stats_dashboard/presentation/stats_dashboard_screen.dart';
import '../../features/streaks_goals/presentation/streaks_goals_screen.dart';
import '../../features/settings/presentation/settings_screen.dart';
import '../../features/theme_customizer/presentation/theme_customizer_screen.dart';

/// Notifies GoRouter to re-evaluate redirects whenever the onboarding
/// completion flag changes (e.g. once SharedPreferences finishes loading).
class _OnboardingRefreshListenable extends ChangeNotifier {
  _OnboardingRefreshListenable(this._ref) {
    _ref.listen<bool?>(
      onboardingCompletedProvider,
      (_, __) => notifyListeners(),
    );
  }
  final Ref _ref;
}

final appRouterProvider = Provider<GoRouter>((ref) {
  final refreshListenable = _OnboardingRefreshListenable(ref);

  return GoRouter(
    initialLocation: '/',
    refreshListenable: refreshListenable,
    redirect: (context, state) {
      final completed = ref.read(onboardingCompletedProvider);
      final onOnboarding = state.matchedLocation == '/onboarding';

      // Still loading persisted flag from disk — stay put, no redirect.
      if (completed == null) return null;

      if (!completed && !onOnboarding) return '/onboarding';
      if (completed && onOnboarding) return '/';
      return null;
    },
    routes: _routes,
  );
});

final _routes = <RouteBase>[
    GoRoute(
      path: '/onboarding',
      builder: (context, state) => const OnboardingScreen(),
    ),
    ShellRoute(
      builder: (context, state, child) => ScaffoldWithNav(child: child),
      routes: [
        GoRoute(
          path: '/',
          builder: (context, state) => const HomeDashboard(),
        ),
        GoRoute(
          path: '/focus',
          builder: (context, state) => const FocusTimerScreen(),
        ),
        GoRoute(
          path: '/tasks',
          builder: (context, state) => const TodoScreen(),
        ),
        GoRoute(
          path: '/calendar',
          builder: (context, state) => const CalendarScreen(),
        ),
        GoRoute(
          path: '/stats',
          builder: (context, state) => const StatsDashboardScreen(),
        ),
      ],
    ),
    GoRoute(
      path: '/app-blocker',
      builder: (context, state) => const AppBlockerScreen(),
    ),
    GoRoute(
      path: '/shorts-reels-blocker',
      builder: (context, state) => const ShortsReelsBlockerScreen(),
    ),
    GoRoute(
      path: '/youtube-study-mode',
      builder: (context, state) => const YoutubeStudyModeScreen(),
    ),
    GoRoute(
      path: '/website-blocker',
      builder: (context, state) => const WebsiteBlockerScreen(),
    ),
    GoRoute(
      path: '/streaks-goals',
      builder: (context, state) => const StreaksGoalsScreen(),
    ),
    GoRoute(
      path: '/settings',
      builder: (context, state) => const SettingsScreen(),
    ),
    GoRoute(
      path: '/theme-customizer',
      builder: (context, state) => const ThemeCustomizerScreen(),
    ),
];

class ScaffoldWithNav extends StatelessWidget {
  final Widget child;
  const ScaffoldWithNav({super.key, required this.child});

  static const _destinations = [
    (icon: Icons.home_rounded, outlineIcon: Icons.home_outlined, label: 'Home', path: '/'),
    (icon: Icons.timer_rounded, outlineIcon: Icons.timer_outlined, label: 'Focus', path: '/focus'),
    (icon: Icons.check_circle_rounded, outlineIcon: Icons.check_circle_outline, label: 'Tasks', path: '/tasks'),
    (icon: Icons.calendar_today_rounded, outlineIcon: Icons.calendar_today_outlined, label: 'Calendar', path: '/calendar'),
    (icon: Icons.bar_chart_rounded, outlineIcon: Icons.bar_chart_outlined, label: 'Stats', path: '/stats'),
  ];

  @override
  Widget build(BuildContext context) {
    final isTablet = Breakpoints.isTablet(context);
    final index = _calculateIndex(context);

    if (isTablet) {
      // Tablets get a persistent side nav rail instead of a bottom
      // bar squashed under a full-width phone layout.
      return Scaffold(
        body: Row(
          children: [
            Container(
              width: 96,
              decoration: BoxDecoration(
                color: Colors.white.withValues(alpha: 0.05),
                border: Border(
                  right: BorderSide(
                    color: Colors.white.withValues(alpha: 0.1),
                    width: 0.5,
                  ),
                ),
              ),
              child: SafeArea(
                child: Column(
                  children: [
                    const SizedBox(height: 24),
                    for (var i = 0; i < _destinations.length; i++)
                      _RailItem(
                        destination: _destinations[i],
                        selected: i == index,
                        onTap: () => context.go(_destinations[i].path),
                      ),
                  ],
                ),
              ),
            ),
            Expanded(child: child),
          ],
        ),
      );
    }

    return Scaffold(
      body: child,
      bottomNavigationBar: Container(
        decoration: BoxDecoration(
          color: Colors.white.withValues(alpha: 0.08),
          border: Border(
            top: BorderSide(
              color: Colors.white.withValues(alpha: 0.12),
              width: 0.5,
            ),
          ),
        ),
        child: SafeArea(
          child: BottomNavigationBar(
            currentIndex: index,
            onTap: (i) => context.go(_destinations[i].path),
            backgroundColor: Colors.transparent,
            elevation: 0,
            items: [
              for (final d in _destinations)
                BottomNavigationBarItem(icon: Icon(d.outlineIcon), label: d.label),
            ],
          ),
        ),
      ),
    );
  }

  int _calculateIndex(BuildContext context) {
    final location = GoRouterState.of(context).uri.path;
    if (location == '/') return 0;
    if (location.startsWith('/focus')) return 1;
    if (location.startsWith('/tasks')) return 2;
    if (location.startsWith('/calendar')) return 3;
    if (location.startsWith('/stats')) return 4;
    return 0;
  }
}

class _RailItem extends StatelessWidget {
  final ({IconData icon, IconData outlineIcon, String label, String path}) destination;
  final bool selected;
  final VoidCallback onTap;

  const _RailItem({
    required this.destination,
    required this.selected,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 6),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(16),
        child: Container(
          width: 72,
          padding: const EdgeInsets.symmetric(vertical: 10),
          decoration: BoxDecoration(
            color: selected
                ? GlassTokens.accentPrimary.withValues(alpha: 0.18)
                : Colors.transparent,
            borderRadius: BorderRadius.circular(16),
          ),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Icon(
                selected ? destination.icon : destination.outlineIcon,
                color: selected
                    ? GlassTokens.accentPrimary
                    : GlassTokens.textSecondary,
                size: 24,
              ),
              const SizedBox(height: 4),
              Text(
                destination.label,
                style: TextStyle(
                  fontSize: 11,
                  fontWeight: selected ? FontWeight.w600 : FontWeight.w400,
                  color: selected
                      ? GlassTokens.accentPrimary
                      : GlassTokens.textSecondary,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
