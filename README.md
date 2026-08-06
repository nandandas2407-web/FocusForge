# FocusForge

A Regain-style, Flutter-native, Android-first Focus & Study App with "Liquid Glass" Dark UI, Accessibility-Service App/Content Blocking, and a full Study Suite.

## Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│                          FLUTTER LAYER (Dart)                    │
│  Presentation:  Screens, Liquid Glass widgets, Theming engine     │
│  State:         Riverpod — feature-first structure                │
│  Domain:        Use-cases (StartFocusSession, BlockApp, etc.)     │
│  Data:          Repositories → Drift(SQLite) + SharedPrefs       │
└───────────────────────────▲──────────────────────────────────────┘
                             │ MethodChannel / EventChannel / Pigeon
┌───────────────────────────▼──────────────────────────────────────┐
│                    NATIVE ANDROID LAYER (Kotlin)                 │
│  • FocusAccessibilityService (extends AccessibilityService)       │
│  • UsageStatsRepository (UsageStatsManager wrapper)                │
│  • BlockOverlayService (SYSTEM_ALERT_WINDOW, foreground Service)   │
│  • FocusVpnService (extends VpnService, for domain-level blocking) │
│  • FocusDeviceAdminReceiver (DevicePolicyManager, uninstall guard) │
│  • BootReceiver (restarts services after reboot)                  │
└──────────────────────────────────────────────────────────────────┘
```

## Features

- **App Blocker** — pick any installed app, block entirely or on schedule
- **Short-form / Reels / Shorts Blocker** — detect and close Reels/Shorts tabs
- **YouTube Study Mode** — whitelist-only YouTube access
- **Website Blocker** — DNS/VPN-level domain blocking
- **Focus Timer / Pomodoro** — with ambient sounds and auto-blocking
- **To-Do List** — tasks, subtasks, priorities, recurring
- **Calendar** — month/week/day view with exam countdowns
- **Screen Time Dashboard** — daily/weekly charts
- **Focus Streaks & Goals** — streak counter, milestone badges
- **Liquid Glass Design** — frosted translucent panels, dark-first
- **User Wallpaper Theming** — gallery-picked wallpaper behind glass
- **Strict Mode** — unskippable blocking sessions
- **Uninstall Protection** — Device Admin API guard

## Getting Started

1. Ensure Flutter SDK ≥ 3.2.0 is installed
2. `flutter pub get`
3. `flutter run` (on Android device/emulator)
4. Follow onboarding to grant Accessibility, Usage Stats, and Overlay permissions

## Permissions

This app requires the following special permissions:
- **Accessibility Service** — detects foreground app for blocking
- **Usage Stats** — provides screen time data for dashboard
- **Draw Over Other Apps** — shows block overlay screens
- **Device Admin** — optional uninstall protection in Strict Mode

All accessibility data is processed on-device only and never leaves the device.

## Build

```bash
flutter build apk --release
flutter build appbundle --release
```

## License

MIT
