# FocusForge — File Manifest Ledger

Every file created in the codebase is indexed below with its structural layer, purpose, and implementation status.

| File Path | Layer | Purpose | Status |
|---|---|---|---|
| `metadata.json` | Platform | Project identity (FocusForge) | ✅ |
| `settings.gradle.kts` | Build | Gradle root project configuration | ✅ |
| `app/build.gradle.kts` | Build | App module dependencies & Room/KSP plugins | ✅ |
| `app/src/main/res/values/strings.xml` | Resources | Application string resources (`FocusForge`) | ✅ |
| `app/src/main/res/drawable/ic_launcher_background.xml` | Drawable | Adaptive app icon background vector | ✅ |
| `app/src/main/res/drawable/ic_launcher_foreground.xml` | Drawable | Adaptive app icon foreground focus shield vector | ✅ |
| `app/src/main/res/xml/accessibility_service_config.xml` | Config | AccessibilityService window event configuration | ✅ |
| `app/src/main/res/xml/device_admin_rules.xml` | Config | Device Admin policy rules for Strict Mode | ✅ |
| `app/src/main/AndroidManifest.xml` | Manifest | System permissions, Accessibility & Overlay services | ✅ |
| `app/src/main/java/com/example/data/entity/Entities.kt` | Data / Room | Entities for blocked apps, focus sessions, tasks, calendar, streaks, themes | ✅ |
| `app/src/main/java/com/example/data/dao/FocusDao.kt` | Data / Room | Reactive Flow DAO interface for SQLite queries | ✅ |
| `app/src/main/java/com/example/data/db/FocusDatabase.kt` | Data / Room | Room database holder with singleton instance | ✅ |
| `app/src/main/java/com/example/data/repository/Repositories.kt` | Data | Repositories for Focus, Task, Calendar, and UsageStatsManager | ✅ |
| `app/src/main/java/com/example/service/AppDetectionRules.kt` | Service | Detection logic for Reels (Instagram) and Shorts (YouTube) | ✅ |
| `app/src/main/java/com/example/service/FocusAccessibilityService.kt` | Service | Android AccessibilityService listening for foreground window events | ✅ |
| `app/src/main/java/com/example/service/BlockOverlayService.kt` | Service | Foreground service displaying block alert overlays | ✅ |
| `app/src/main/java/com/example/service/FocusDeviceAdminReceiver.kt` | Service | Device Admin receiver for uninstall protection | ✅ |
| `app/src/main/java/com/example/service/BootCompletedReceiver.kt` | Service | Broadcast receiver resuming focus protection on boot | ✅ |
| `app/src/main/java/com/example/ui/theme/GlassTokens.kt` | UI / Theme | Design tokens (Colors, Blur Sigmas, Glass Alphas) | ✅ |
| `app/src/main/java/com/example/ui/theme/WallpaperBackground.kt` | UI / Theme | Ambient liquid glass background with customizable wallpapers | ✅ |
| `app/src/main/java/com/example/ui/theme/GlassComponents.kt` | UI / Theme | GlassCard, GlassButton, GlassBottomNav, GlassTextField, GlassRingProgress | ✅ |
| `app/src/main/java/com/example/ui/theme/Theme.kt` | UI / Theme | Material 3 Dark theme wrapper | ✅ |
| `app/src/main/java/com/example/ui/components/AmbientSoundPlayer.kt` | UI / Component | Ambient focus sound synthesizer & player | ✅ |
| `app/src/main/java/com/example/ui/viewmodel/ViewModels.kt` | ViewModel | State management for Focus, Tasks, Calendar, Stats, Theme | ✅ |
| `app/src/main/java/com/example/ui/screens/OnboardingScreen.kt` | UI / Screen | First-launch permissions & goal onboarding screen | ✅ |
| `app/src/main/java/com/example/ui/screens/DashboardScreen.kt` | UI / Screen | Main Home Dashboard with hero progress ring & quick actions | ✅ |
| `app/src/main/java/com/example/ui/screens/AppBlockerScreen.kt` | UI / Screen | Searchable app blocker screen with category filters | ✅ |
| `app/src/main/java/com/example/ui/screens/ShortsReelsBlockerScreen.kt` | UI / Screen | Instagram Reels & YouTube Shorts sub-screen blocker | ✅ |
| `app/src/main/java/com/example/ui/screens/YoutubeStudyModeScreen.kt` | UI / Screen | Educational YouTube whitelist manager | ✅ |
| `app/src/main/java/com/example/ui/screens/WebsiteBlockerScreen.kt` | UI / Screen | Website and domain block list manager | ✅ |
| `app/src/main/java/com/example/ui/screens/FocusTimerScreen.kt` | UI / Screen | Pomodoro Timer ("Promodoro") with wallpaper glass UI & ambient audio | ✅ |
| `app/src/main/java/com/example/ui/screens/TodoListScreen.kt` | UI / Screen | Study task manager & priority task binder | ✅ |
| `app/src/main/java/com/example/ui/screens/CalendarScreen.kt` | UI / Screen | Study calendar & exam countdown cards | ✅ |
| `app/src/main/java/com/example/ui/screens/StatsDashboardScreen.kt` | UI / Screen | Screen time analytics & time reclaimed breakdown | ✅ |
| `app/src/main/java/com/example/ui/screens/StreaksGoalsScreen.kt` | UI / Screen | Streak flame tracking & milestone badges | ✅ |
| `app/src/main/java/com/example/ui/screens/ThemeCustomizerScreen.kt` | UI / Screen | Live glass preview, blur/opacity sliders & wallpapers | ✅ |
| `app/src/main/java/com/example/MainActivity.kt` | Entry | Primary Activity hosting Compose navigation & ViewModels | ✅ |
