<div align="center">

# FocusForge

### Your phone works for you — not against you.

**The Android focus suite that blocks distractions at the system level.**
Reels. Shorts. Mindless scrolling. Gone.

Liquid Glass UI · Pomodoro Timer · App Blocker · YouTube Study Mode · Tasks & Calendar

[![Build APK](https://github.com/nandandas2407-web/FocusForge/actions/workflows/build-apk.yml/badge.svg)](https://github.com/nandandas2407-web/FocusForge/actions/workflows/build-apk.yml)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)](https://developer.android.com/about/versions/nougat)
[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84.svg)](https://developer.android.com)

</div>

---

## What is FocusForge?

FocusForge is a **native Android focus & study app** that blocks distracting content at the **system accessibility level** — not just a launcher overlay or a VPN trick. It uses Android's `AccessibilityService` to intercept foreground app events in real time and block content before it loads.

If you're a student, knowledge worker, or anyone trying to deep focus, FocusForge kills the scroll loop before it starts.

---

## Features

### Core Blocking Engine

| Feature | How It Works |
|---|---|
| **App Blocker** | Block any installed app entirely. TikTok, Instagram, games — gone. |
| **Instagram Reels Blocker** | Detects Reels sub-screens via accessibility tree analysis. Feed stays, Reels die. |
| **YouTube Shorts Blocker** | Intercepts Shorts player specifically. Long-form YouTube remains usable. |
| **YouTube Study Mode** | Whitelist-first: only approved educational channels play. Everything else is blocked. |
| **Website Blocker** | Block domains in any browser (Chrome, Firefox, Brave, Edge, Samsung, Opera). |
| **Uninstall Protection** | Device Admin receiver prevents casual uninstallation during focus sessions. |

### YouTube Study Mode — Deep Dive

This isn't a keyword filter. FocusForge builds a **screen snapshot** from the full accessibility tree:

```
YouTube opened
    ↓
Study Mode enabled?
    ↓ YES
Screen snapshot built from ALL accessibility properties:
  • text, contentDescription, viewIdResourceName, className
    ↓
Screen type detected: WATCH / SHORTS / HOME / SEARCH / CHANNEL
    ↓
┌─────────────────────────────────┐
│  WATCH page detected            │
│  Channel extracted from nodes   │
│  Matched against whitelist?     │
│                                 │
│  ✅ Whitelisted → ALLOW         │
│  ❌ Not verified → BLOCK        │
│  🏠 Navigation → ALLOW          │
└─────────────────────────────────┘
```

**Fail-closed design:** If a video is detected but the channel can't be verified against the whitelist, it's blocked. No exceptions.

### Productivity Tools

| Feature | Details |
|---|---|
| **Pomodoro Timer** | 25/5/15 min modes with ambient soundscapes (rain, lo-fi, café, white noise) |
| **Task Manager** | Priority-based task list with categories, due dates, and focus session linking |
| **Study Calendar** | Event tracking with exam countdown cards and study block scheduling |
| **Streak Tracker** | Daily screen time goals with streak flame tracking and milestone badges |
| **Screen Time Stats** | Per-app usage analytics with time reclaimed breakdown |
| **Theme Customizer** | Liquid Glass UI with blur/opacity sliders, wallpaper presets, and accent colors |

### UI Design

- **Liquid Glass** dark theme with real-time blur and transparency
- Adaptive layout: phone (bottom nav) + tablet (side rail)
- Wallpaper presets: Cosmic Neon, Lo-Fi Study, Aurora, Custom
- Onboarding flow with permission guidance

---

## Architecture

```
┌──────────────────────────────────────────────────────┐
│                    Presentation                      │
│  Jetpack Compose · Material 3 · Navigation          │
│  ViewModels (StateFlow) · Adaptive Phone/Tablet      │
├──────────────────────────────────────────────────────┤
│                     Domain                           │
│  AppDetectionRules · YoutubeScreenSnapshot           │
│  BlockDecision (sealed class) · Channel Matching     │
├──────────────────────────────────────────────────────┤
│                      Data                           │
│  Room DB · DAO (Flow queries) · Repositories        │
│  Entities: BlockedApp, FocusSession, Task, Calendar  │
├──────────────────────────────────────────────────────┤
│                   Services                          │
│  FocusAccessibilityService  ←→  BlockOverlayService │
│  FocusDeviceAdminReceiver   ←→  BootCompletedRecv   │
└──────────────────────────────────────────────────────┘
```

### Blocking Flow

```
AccessibilityEvent received
    ↓
FocusAccessibilityService.onAccessibilityEvent()
    ↓
AppDetectionRules.evaluate()
    ├─ Full app block?        → GLOBAL_ACTION_HOME
    ├─ Instagram Reels?       → GLOBAL_ACTION_BACK + overlay
    ├─ YouTube Shorts?        → GLOBAL_ACTION_BACK + overlay
    ├─ YouTube Study Mode?    → Screen snapshot → channel match → BACK/HOME
    ├─ Website blocked?       → GLOBAL_ACTION_BACK + overlay
    └─ Allowed?              → no-op
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| **Language** | Kotlin |
| **UI** | Jetpack Compose + Material 3 |
| **Architecture** | MVVM (ViewModel + StateFlow + Room) |
| **Database** | Room (SQLite) with reactive Flows |
| **Navigation** | Jetpack Navigation Compose |
| **DI** | Manual (ViewModel factories) |
| **Networking** | OkHttp + Retrofit + Moshi |
| **Image Loading** | Coil Compose |
| **Firebase** | Firebase AI (Gemini), App Check (reCAPTCHA) |
| **Build** | Gradle 9.3.1, KSP, Version Catalog |
| **CI/CD** | GitHub Actions (build + release) |
| **Min SDK** | 24 (Android 7.0) |
| **Target SDK** | 36 |

---

## Getting Started

### Prerequisites

- [Android Studio](https://developer.android.com/studio) Ladybug or later
- JDK 17+
- Android SDK 36
- A physical device recommended (accessibility services work better than emulators)

### Setup

```bash
# Clone the repo
git clone https://github.com/nandandas2407-web/FocusForge.git
cd FocusForge

# (Optional) Add Gemini API key for AI features
cp .env.example .env
# Edit .env and set GEMINI_API_KEY=your_key_here
```

### Build & Run

1. Open the project in Android Studio
2. Let Gradle sync and resolve dependencies
3. Remove `signingConfig = signingConfigs.getByName("debugConfig")` from `app/build.gradle.kts` (line 49) for local debug builds
4. Run on a device or emulator

### First Launch

1. Grant **Accessibility Service** permission when prompted
2. Grant **Display over other apps** permission
3. (Optional) Enable **Device Admin** for uninstall protection
4. Toggle on the features you want in the Blocker tab

---

## CI/CD

### Automatic Builds

GitHub Actions builds the APK on every push to `main` and on pull requests.

| Workflow | Trigger | Output |
|---|---|---|
| `build-apk.yml` | Push to `main`, PRs, manual | Debug APK artifact |
| `release-apk.yml` | Tag push (`v*`), manual | GitHub Release with APK |

### Creating a Release

```bash
git tag v1.0.0
git push origin v1.0.0
```

This triggers a release build and attaches the APK to a GitHub Release.

### APK Artifact

The debug APK is uploaded as `focusforge-debug-apk` in the Actions artifacts.

---

## Project Structure

```
FocusForge/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/
│   │   │   │   ├── MainActivity.kt              # Entry point, Compose nav
│   │   │   │   ├── data/
│   │   │   │   │   ├── entity/Entities.kt        # Room entities
│   │   │   │   │   ├── dao/FocusDao.kt           # DAO interface
│   │   │   │   │   ├── db/FocusDatabase.kt       # Room database
│   │   │   │   │   └── repository/Repositories.kt
│   │   │   │   ├── service/
│   │   │   │   │   ├── AppDetectionRules.kt      # Blocking logic engine
│   │   │   │   │   ├── FocusAccessibilityService.kt  # Core service
│   │   │   │   │   ├── BlockOverlayService.kt    # Block notification overlay
│   │   │   │   │   ├── FocusDeviceAdminReceiver.kt
│   │   │   │   │   └── BootCompletedReceiver.kt
│   │   │   │   └── ui/
│   │   │   │       ├── screens/                  # 12 screen composables
│   │   │   │       ├── theme/                    # Liquid Glass design system
│   │   │   │       ├── components/               # Ambient sound player
│   │   │   │       └── viewmodel/ViewModels.kt   # State management
│   │   │   ├── res/                              # Resources, icons, configs
│   │   │   └── AndroidManifest.xml
│   │   └── test/                                 # Unit + screenshot tests
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml                        # Version catalog
├── .github/workflows/                            # CI/CD pipelines
├── build.gradle.kts
├── settings.gradle.kts
└── metadata.json
```

---

## Permissions

| Permission | Why |
|---|---|
| `BIND_ACCESSIBILITY_SERVICE` | Core blocking — intercepts foreground app events |
| `SYSTEM_ALERT_WINDOW` | Block overlay notifications |
| `FOREGROUND_SERVICE` | Keeps blocking alive in background |
| `RECEIVE_BOOT_COMPLETED` | Restarts protection after reboot |
| `QUERY_ALL_PACKAGES` | Lists installed apps for the blocker UI |
| `PACKAGE_USAGE_STATS` | Screen time analytics |
| `POST_NOTIFICATIONS` | Block overlay notifications |
| `READ_MEDIA_IMAGES` | Custom wallpaper selection |
| `BIND_DEVICE_ADMIN` | Uninstall protection (optional) |

---

## Configuration

### Environment Variables

| Variable | Required | Description |
|---|---|---|
| `GEMINI_API_KEY` | No | Gemini API key for AI features |
| `KEYSTORE_PATH` | Release builds | Path to upload keystore |
| `STORE_PASSWORD` | Release builds | Keystore password |
| `KEY_PASSWORD` | Release builds | Key password |

### Default Whitelisted YouTube Channels

These channels are pre-loaded in the YouTube Study Mode whitelist:

- MIT OpenCourseWare
- Kurzgesagt - In a Nutshell
- freeCodeCamp.org

Add your own via the YouTube Study Mode screen in the app.

### Default Blocked Websites

- reddit.com
- twitter.com / x.com

Manage via the Website Blocker screen.

---

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

<div align="center">

**Built with focus. Ship with discipline.**

</div>
