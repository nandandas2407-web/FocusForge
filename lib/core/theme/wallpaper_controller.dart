// ============================================================
// FILE: lib/core/theme/wallpaper_controller.dart
// PURPOSE: Manages user-selected wallpaper file path, persistence,
//          and per-screen overrides (e.g. different wallpaper for
//          the Focus Timer screen).
// CREATED: 2026-08-03 | LAST MODIFIED: 2026-08-03
// ============================================================
import 'dart:io';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:path_provider/path_provider.dart';
import 'package:shared_preferences/shared_preferences.dart';

class WallpaperState {
  final String? wallpaperPath;
  final String? focusTimerWallpaperPath;
  final bool useDifferentFocusWallpaper;

  const WallpaperState({
    this.wallpaperPath,
    this.focusTimerWallpaperPath,
    this.useDifferentFocusWallpaper = false,
  });

  WallpaperState copyWith({
    String? wallpaperPath,
    String? focusTimerWallpaperPath,
    bool? useDifferentFocusWallpaper,
  }) {
    return WallpaperState(
      wallpaperPath: wallpaperPath ?? this.wallpaperPath,
      focusTimerWallpaperPath:
          focusTimerWallpaperPath ?? this.focusTimerWallpaperPath,
      useDifferentFocusWallpaper:
          useDifferentFocusWallpaper ?? this.useDifferentFocusWallpaper,
    );
  }

  String? get effectiveFocusWallpaper =>
      useDifferentFocusWallpaper ? focusTimerWallpaperPath : wallpaperPath;
}

class WallpaperController extends StateNotifier<WallpaperState> {
  WallpaperController() : super(const WallpaperState()) {
    _load();
  }

  static const _keyWallpaper = 'wallpaper_path';
  static const _keyFocusWallpaper = 'focus_timer_wallpaper_path';
  static const _keyUseDifferent = 'use_different_focus_wallpaper';

  Future<void> _load() async {
    final prefs = await SharedPreferences.getInstance();
    state = WallpaperState(
      wallpaperPath: prefs.getString(_keyWallpaper),
      focusTimerWallpaperPath: prefs.getString(_keyFocusWallpaper),
      useDifferentFocusWallpaper: prefs.getBool(_keyUseDifferent) ?? false,
    );
  }

  Future<void> setWallpaper(String sourcePath) async {
    final appDir = await getApplicationDocumentsDirectory();
    final fileName =
        'wallpaper_${DateTime.now().millisecondsSinceEpoch}.jpg';
    final destFile = File('${appDir.path}/$fileName');
    await File(sourcePath).copy(destFile.path);

    state = state.copyWith(wallpaperPath: destFile.path);

    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_keyWallpaper, destFile.path);
  }

  Future<void> setFocusTimerWallpaper(String sourcePath) async {
    final appDir = await getApplicationDocumentsDirectory();
    final fileName =
        'focus_wallpaper_${DateTime.now().millisecondsSinceEpoch}.jpg';
    final destFile = File('${appDir.path}/$fileName');
    await File(sourcePath).copy(destFile.path);

    state = state.copyWith(focusTimerWallpaperPath: destFile.path);

    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_keyFocusWallpaper, destFile.path);
  }

  Future<void> setUseDifferentFocusWallpaper(bool value) async {
    state = state.copyWith(useDifferentFocusWallpaper: value);
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(_keyUseDifferent, value);
  }

  Future<void> clearWallpaper() async {
    state = state.copyWith(wallpaperPath: null);
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_keyWallpaper);
  }

  Future<void> clearFocusTimerWallpaper() async {
    state = state.copyWith(focusTimerWallpaperPath: null);
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_keyFocusWallpaper);
  }

  File? get wallpaperFile =>
      state.wallpaperPath != null ? File(state.wallpaperPath!) : null;

  File? get focusTimerWallpaperFile =>
      state.effectiveFocusWallpaper != null
          ? File(state.effectiveFocusWallpaper!)
          : null;
}

final wallpaperProvider =
    StateNotifierProvider<WallpaperController, WallpaperState>((ref) {
  return WallpaperController();
});
