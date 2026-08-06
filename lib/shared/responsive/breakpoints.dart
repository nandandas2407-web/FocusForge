// ============================================================
// FILE: lib/shared/responsive/breakpoints.dart
// PURPOSE: Central breakpoint definitions and helpers so every
//          screen adapts consistently between phone, small tablet,
//          and large tablet widths instead of stretching a
//          phone-sized layout across the full screen.
// CREATED: 2026-08-04 | LAST MODIFIED: 2026-08-04
// ============================================================
import 'package:flutter/material.dart';

enum DeviceClass { phone, tablet, largeTablet }

class Breakpoints {
  Breakpoints._();

  /// Below this, treat as a phone regardless of orientation.
  static const double tablet = 600;

  /// Above this, treat as a large tablet (iPad Pro / desktop-ish).
  static const double largeTablet = 1000;

  /// Content on very wide screens is capped at this width and
  /// centered, so text and controls don't stretch uncomfortably.
  static const double maxContentWidth = 720;

  static DeviceClass classify(double width) {
    if (width >= largeTablet) return DeviceClass.largeTablet;
    if (width >= tablet) return DeviceClass.tablet;
    return DeviceClass.phone;
  }

  static bool isTablet(BuildContext context) =>
      MediaQuery.sizeOf(context).shortestSide >= tablet;

  static DeviceClass of(BuildContext context) =>
      classify(MediaQuery.sizeOf(context).width);
}

/// Wraps a screen's scrollable content so it's centered and
/// width-capped on tablets, while staying full-width on phones.
class ResponsiveContent extends StatelessWidget {
  final Widget child;
  final double maxWidth;

  const ResponsiveContent({
    super.key,
    required this.child,
    this.maxWidth = Breakpoints.maxContentWidth,
  });

  @override
  Widget build(BuildContext context) {
    return Center(
      child: ConstrainedBox(
        constraints: BoxConstraints(maxWidth: maxWidth),
        child: child,
      ),
    );
  }
}

/// Picks a grid column count based on available width — used for
/// dashboard quick-action tiles, app-blocker grid, etc.
int responsiveColumns(
  double width, {
  int phone = 2,
  int tablet = 3,
  int largeTablet = 4,
}) {
  switch (Breakpoints.classify(width)) {
    case DeviceClass.phone:
      return phone;
    case DeviceClass.tablet:
      return tablet;
    case DeviceClass.largeTablet:
      return largeTablet;
  }
}
