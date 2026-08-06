# FocusForge ProGuard Rules

# Keep Pigeon-generated classes
-keep class com.focusforge.app.channels.** { *; }
-keep class io.flutter.plugins.** { *; }

# Keep Accessibility Service
-keep class com.focusforge.app.accessibility.** { *; }

# Keep Block Overlay
-keep class com.focusforge.app.overlay.** { *; }

# Keep VPN Service
-keep class com.focusforge.app.vpn.** { *; }

# Keep Device Admin
-keep class com.focusforge.app.admin.** { *; }

# General Android rules
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Flutter
-keep class io.flutter.app.** { *; }
-keep class io.flutter.plugin.** { *; }
-keep class io.flutter.util.** { *; }
-keep class io.flutter.view.** { *; }
-keep class io.flutter.** { *; }
-keep class io.flutter.plugins.** { *; }

# Don't obfuscate exception names
-dontwarn javax.annotation.**

# Suppress R8 warnings for missing Google Play Core classes (unused deferred components)
-dontwarn com.google.android.play.core.splitcompat.SplitCompatApplication
-dontwarn com.google.android.play.core.splitinstall.**
-dontwarn com.google.android.play.core.tasks.**
