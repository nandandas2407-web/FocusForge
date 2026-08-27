<div align="center">

# FocusForge

### Your phone works for you — not against you.

**The Android focus suite that blocks distractions at the system level.**
Reels. Shorts. Mindless scrolling. Gone.

Premium Gold UI · React Native · Pomodoro Timer · App Blocker · YouTube Study Mode · Tasks & Calendar

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)](https://developer.android.com/about/versions/nougat)
[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84.svg)](https://developer.android.com)

</div>

---

## What is FocusForge?

FocusForge is a **React Native focus & study app** that blocks distracting content at the **system accessibility level** — not just a launcher overlay or a VPN trick. It uses Android's `AccessibilityService` via a native bridge module to intercept foreground app events in real time and block content before it loads.

v2.0 features a **premium gold/amber color scheme** with deep obsidian backgrounds, replacing the previous purple theme.

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

### YouTube Shorts Blocking — Fixed

The Shorts detection uses a **multi-signal approach**:

1. **Resource ID detection** — checks for `reel_player`, `shorts_player`, `ytd-reel` containers
2. **UI element detection** — looks for Shorts-specific actions (remix, use this sound, original audio)
3. **Navigation absence** — Shorts screens lack the main YouTube nav bar
4. **Fail-closed** — if detection is ambiguous, content is blocked

### YouTube Study Mode — Fixed

The Study Mode uses a **screen snapshot approach**:

```
YouTube opened
    ↓
Study Mode enabled?
    ↓ YES
Screen snapshot built from ALL accessibility properties:
  • text, contentDescription, viewIdResourceName
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
| **Theme Customizer** | Premium gold theme with accent color options |

---

## Architecture

```
┌──────────────────────────────────────────────────────┐
│                    Presentation                      │
│  React Native · Expo · React Navigation             │
│  Zustand (State) · React Native SVG                 │
├──────────────────────────────────────────────────────┤
│                   Native Bridge                     │
│  FocusForgeModule (React → Android)                 │
│  setBlockedApps() · setYoutubeStudyMode()           │
│  setYoutubeWhitelist() · setBlockedWebsites()       │
├──────────────────────────────────────────────────────┤
│                Native Android Layer                 │
│  FocusForgeAccessibilityService                     │
│  BlockOverlayService · BootCompletedReceiver        │
│  FocusDeviceAdminReceiver                           │
├──────────────────────────────────────────────────────┤
│                   Services                          │
│  AccessibilityEvent → Screen Snapshot → Block决策   │
│  YouTube Study Mode · Shorts Detection              │
│  Reels Detection · Website Blocker                  │
└──────────────────────────────────────────────────────┘
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| **Language** | TypeScript + Java (native modules) |
| **UI** | React Native + Expo |
| **State** | Zustand + AsyncStorage |
| **Navigation** | React Navigation (Bottom Tabs) |
| **Charts** | React Native SVG |
| **Native** | Android AccessibilityService |
| **Build** | Expo + Gradle |
| **Min SDK** | 24 (Android 7.0) |
| **Target SDK** | 35 |

---

## Getting Started

### Prerequisites

- [Node.js](https://nodejs.org/) 18+
- [Android Studio](https://developer.android.com/studio) Ladybug or later
- JDK 17+
- A physical device recommended (accessibility services work better than emulators)

### Setup

```bash
# Clone the repo
git clone https://github.com/nandandas2407-web/FocusForge.git
cd FocusForge

# Install dependencies
npm install

# (Optional) Add Gemini API key for AI features
cp .env.example .env
```

### Build & Run

```bash
# Start Expo
npx expo start

# Or build APK directly
npx expo run:android
```

### First Launch

1. Grant **Accessibility Service** permission when prompted
2. Grant **Display over other apps** permission
3. (Optional) Enable **Device Admin** for uninstall protection
4. Toggle on the features you want in the Blocker tab

---

## Project Structure

```
FocusForge/
├── App.tsx                           # Entry point
├── src/
│   ├── theme/
│   │   ├── colors.ts                 # Premium gold color palette
│   │   └── theme.ts                  # Spacing, typography, shadows
│   ├── navigation/
│   │   └── AppNavigator.tsx          # Bottom tab navigation
│   ├── screens/
│   │   ├── HomeScreen.tsx            # Dashboard with timer + stats
│   │   ├── BlockerScreen.tsx         # App/website blocking
│   │   ├── PomodoroScreen.tsx        # Focus timer
│   │   ├── TasksScreen.tsx           # Task manager
│   │   ├── CalendarScreen.tsx        # Study calendar
│   │   ├── YoutubeStudyScreen.tsx    # YouTube whitelist management
│   │   ├── StatsScreen.tsx           # Analytics + streaks
│   │   └── SettingsScreen.tsx        # Theme + settings
│   ├── components/
│   │   ├── GlassCard.tsx             # Premium card component
│   │   ├── PremiumButton.tsx         # Themed button
│   │   ├── PremiumHeader.tsx         # Screen header
│   │   └── TimerDial.tsx             # Animated timer ring
│   ├── store/
│   │   └── useStore.ts              # Zustand state management
│   └── services/
│       └── blocking.ts              # Blocking logic (unused, native handles it)
├── android/
│   └── app/src/main/
│       ├── AndroidManifest.xml       # Permissions + service declarations
│       ├── java/com/focusforge/native/
│       │   ├── FocusForgeAccessibilityService.java
│       │   ├── FocusForgeModule.java # RN bridge
│       │   ├── FocusForgePackage.java
│       │   ├── BlockOverlayService.java
│       │   ├── BootCompletedReceiver.java
│       │   └── FocusDeviceAdminReceiver.java
│       └── res/xml/
│           ├── accessibility_service_config.xml
│           └── device_admin_policies.xml
├── package.json
└── .env.example
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
| `BIND_DEVICE_ADMIN` | Uninstall protection (optional) |

---

## Default Configuration

### Whitelisted YouTube Channels

- MIT OpenCourseWare
- Kurzgesagt - In a Nutshell
- freeCodeCamp.org

### Blocked Websites

- reddit.com, twitter.com, x.com, tiktok.com

### Default Blocked Apps

- TikTok (fully blocked)
- Twitter/X (fully blocked)
- Reddit (fully blocked)
- Netflix (fully blocked)
- Twitch (fully blocked)
- Instagram (Reels blocked, feed allowed)
- YouTube (Shorts blocked, long-form allowed)

---

## License

MIT License — see [LICENSE](LICENSE) file.

---

<div align="center">

**Built with focus. Ship with discipline.**

</div>
