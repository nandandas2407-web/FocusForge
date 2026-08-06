# FocusForge — File Manifest

Every file in this project is tagged and tracked below. Run `dart run pigeon --input pigeon_api.dart` after cloning to generate the actual Pigeon bindings.

## Native Android Layer (Kotlin)

| File | Layer | Purpose | Status |
|------|-------|---------|--------|
| `android/app/src/main/kotlin/com/focusforge/app/MainActivity.kt` | Native | Main Flutter activity — initializes native bridges, handles MethodChannel calls | ✅ |
| `android/app/src/main/kotlin/com/focusforge/app/accessibility/FocusAccessibilityService.kt` | Native | Core detection engine — watches foreground app/window, decides block/allow via BlockDecisionEngine | ✅ |
| `android/app/src/main/kotlin/com/focusforge/app/accessibility/AppDetectionRules.kt` | Native | Per-app detection rules for Shorts/Reels sub-screen blocking. Loads bundled JSON with remote update support | ✅ |
| `android/app/src/main/kotlin/com/focusforge/app/accessibility/BlockDecisionEngine.kt` | Native | Evaluates whether the foreground app/sub-screen should be blocked based on current block list and session state | ✅ |
| `android/app/src/main/kotlin/com/focusforge/app/overlay/BlockOverlayService.kt` | Native | Shows a full-screen overlay when an app is blocked, explaining why and providing optional unlock flow | ✅ |
| `android/app/src/main/kotlin/com/focusforge/app/usage/UsageStatsBridge.kt` | Native | Wraps UsageStatsManager to provide app usage data to Flutter for the Stats Dashboard | ✅ |
| `android/app/src/main/kotlin/com/focusforge/app/vpn/FocusVpnService.kt` | Native | Local VPN service for DNS-level website blocking across all browsers | ✅ |
| `android/app/src/main/kotlin/com/focusforge/app/admin/FocusDeviceAdminReceiver.kt` | Native | Device Admin receiver for uninstall protection during Strict Mode | ✅ |
| `android/app/src/main/kotlin/com/focusforge/app/boot/BootCompletedReceiver.kt` | Native | Restarts blocking services after device reboot | ✅ |
| `android/app/src/main/kotlin/com/focusforge/app/channels/PigeonApi.kt` | Native | Pigeon placeholder — run `dart run pigeon` to generate type-safe Dart↔Kotlin bindings | ✅ |

## Android Configuration

| File | Layer | Purpose | Status |
|------|-------|---------|--------|
| `android/app/src/main/AndroidManifest.xml` | Config | Permissions, service declarations, receiver registrations | ✅ |
| `android/app/build.gradle.kts` | Config | App-level Gradle config with R8 minification, Pigeon, target/compile SDK | ✅ |
| `android/build.gradle.kts` | Config | Project-level Gradle config | ✅ |
| `android/app/proguard-rules.pro` | Config | ProGuard/R8 rules — keeps Pigeon classes, accessibility service, blocks obfuscation | ✅ |
| `android/app/src/main/res/xml/accessibility_service_config.xml` | Config | Accessibility service configuration — event types, package filter, flags | ✅ |
| `android/app/src/main/res/xml/device_admin_policies.xml` | Config | Device admin policies for uninstall protection | ✅ |
| `android/app/src/main/res/values/strings.xml` | Config | App strings, service descriptions, notification channels | ✅ |
| `android/app/src/main/res/values/styles.xml` | Config | Launch and normal themes (dark) | ✅ |

## Flutter Core (Dart)

| File | Layer | Purpose | Status |
|------|-------|---------|--------|
| `lib/main.dart` | Flutter | App entry point — initializes Riverpod, sets system UI overlay style | ✅ |
| `lib/app.dart` | Flutter | Root widget — MaterialApp with GoRouter, liquid glass theme | ✅ |
| `lib/core/theme/glass_tokens.dart` | Flutter | Design token constants for the liquid glass system — blur, opacity, colors, radii | ✅ |
| `lib/core/theme/wallpaper_controller.dart` | Flutter | Manages user-selected wallpaper file path, persistence, per-screen overrides | ✅ |
| `lib/core/theme/liquid_glass_theme.dart` | Flutter | Complete ThemeData combining glass tokens for dark and light themes | ✅ |
| `lib/core/native_bridge/accessibility_bridge.dart` | Flutter | Flutter bridge to native Accessibility Service | ✅ |
| `lib/core/native_bridge/usage_stats_bridge.dart` | Flutter | Flutter bridge to native UsageStatsManager | ✅ |
| `lib/core/native_bridge/overlay_bridge.dart` | Flutter | Flutter bridge to native BlockOverlayService | ✅ |
| `lib/core/native_bridge/vpn_bridge.dart` | Flutter | Flutter bridge to native FocusVpnService | ✅ |
| `lib/core/router/app_router.dart` | Flutter | GoRouter configuration with all app routes and bottom nav shell | ✅ |
| `lib/core/di/injection.dart` | Flutter | Dependency injection placeholder (Riverpod providers) | ✅ |

## Flutter Shared Widgets & Models

| File | Layer | Purpose | Status |
|------|-------|---------|--------|
| `lib/shared/widgets/glass_card.dart` | Flutter | Reusable liquid glass card widget — fundamental UI building block | ✅ |
| `lib/shared/widgets/glass_button.dart` | Flutter | Glass-styled button with primary/secondary variants | ✅ |
| `lib/shared/widgets/glass_bottom_sheet.dart` | Flutter | Bottom sheet styled with liquid glass effect | ✅ |
| `lib/shared/widgets/glass_dialog.dart` | Flutter | Dialog styled with liquid glass effect and fade transition | ✅ |
| `lib/shared/models/app_info.dart` | Flutter | Models for AppInfo, BlockRule, BlockMode, FocusSession | ✅ |

## Flutter Features

| File | Layer | Purpose | Status |
|------|-------|---------|--------|
| `lib/features/onboarding/presentation/onboarding_screen.dart` | Flutter | First-launch onboarding — permissions, goal selection, wallpaper, accent color | ✅ |
| `lib/features/home_dashboard/presentation/home_dashboard.dart` | Flutter | Main dashboard — hero card, quick actions, today timeline, streak | ✅ |
| `lib/features/app_blocker/presentation/app_blocker_screen.dart` | Flutter | Searchable app list with toggle switches and preset categories | ✅ |
| `lib/features/shorts_reels_blocker/presentation/shorts_reels_blocker_screen.dart` | Flutter | Dedicated Shorts/Reels blocking toggles with live block counters | ✅ |
| `lib/features/youtube_study_mode/presentation/youtube_study_mode_screen.dart` | Flutter | YouTube Study Mode — whitelist-only access with channel management | ✅ |
| `lib/features/website_blocker/presentation/website_blocker_screen.dart` | Flutter | Domain-level website blocking with add/remove and category presets | ✅ |
| `lib/features/focus_timer/presentation/focus_timer_screen.dart` | Flutter | Pomodoro/Focus timer with wallpaper-behind-glass UI, ambient sounds | ✅ |
| `lib/features/todo/presentation/todo_screen.dart` | Flutter | To-do list with tasks, priorities, groups, add task flow | ✅ |
| `lib/features/calendar/presentation/calendar_screen.dart` | Flutter | Calendar with month view, exam countdown, merged agenda | ✅ |
| `lib/features/stats_dashboard/presentation/stats_dashboard_screen.dart` | Flutter | Screen time dashboard with weekly bar chart, top apps, streak heatmap | ✅ |
| `lib/features/streaks_goals/presentation/streaks_goals_screen.dart` | Flutter | Focus streaks, daily goals, milestone badges, gentle motivation | ✅ |
| `lib/features/settings/presentation/settings_screen.dart` | Flutter | Settings — appearance, permissions, blocking, data & privacy, about | ✅ |
| `lib/features/theme_customizer/presentation/theme_customizer_screen.dart` | Flutter | Live-preview theme customizer — colors, glass effects, wallpaper, presets | ✅ |

## Configuration

| File | Layer | Purpose | Status |
|------|-------|---------|--------|
| `pubspec.yaml` | Config | Flutter dependencies and project configuration | ✅ |
| `analysis_options.yaml` | Config | Dart linting rules | ✅ |
| `.gitignore` | Config | Git ignore rules | ✅ |
| `README.md` | Docs | Project overview, architecture, features, getting started | ✅ |
| `docs/FILES_CREATED.md` | Docs | This file — running manifest of all created files | ✅ |

## Pigeon API (Codegen Input)

| File | Layer | Purpose | Status |
|------|-------|---------|--------|
| `pigeon_api.dart` | Config | Pigeon API definitions — run `dart run pigeon --input pigeon_api.dart` to generate | ✅ |

---

**Total: 45 files** across Native Android (10), Android Config (8), Flutter Core (11), Shared (5), Features (13), Config/Docs (5)

Generated: 2026-08-03
