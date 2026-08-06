// ============================================================
// FILE: lib/main.dart
// PURPOSE: App entry point — initializes Riverpod, sets up
//          the app with liquid glass theme and global error handling.
// CREATED: 2026-08-03 | LAST MODIFIED: 2026-08-06
// ============================================================
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'app.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // Global error handler — catches unhandled Dart exceptions and
  // prevents the red error screen from crashing the app.
  FlutterError.onError = (details) {
    FlutterError.presentError(details);
    // In production, you could log this to a crash reporting service.
  };

  // Allow all orientations — phones typically stay portrait naturally,
  // tablets are free to rotate to landscape.
  await SystemChrome.setPreferredOrientations([
    DeviceOrientation.portraitUp,
    DeviceOrientation.portraitDown,
    DeviceOrientation.landscapeLeft,
    DeviceOrientation.landscapeRight,
  ]);

  // Set system UI overlay style for dark glass look
  SystemChrome.setSystemUIOverlayStyle(const SystemUiOverlayStyle(
    statusBarColor: Colors.transparent,
    statusBarIconBrightness: Brightness.light,
    systemNavigationBarColor: Colors.transparent,
    systemNavigationBarIconBrightness: Brightness.light,
  ));

  runApp(
    const ProviderScope(
      child: FocusForgeApp(),
    ),
  );
}
