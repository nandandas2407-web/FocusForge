// ============================================================
// FILE: lib/features/theme_customizer/presentation/theme_customizer_screen.dart
// PURPOSE: Live-preview theme customizer — accent color, glass
//          opacity/blur, wallpaper source, presets.
// CREATED: 2026-08-03 | LAST MODIFIED: 2026-08-03
// ============================================================
import 'dart:ui';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:image_picker/image_picker.dart';
import '../../../core/theme/glass_tokens.dart';
import '../../../core/theme/wallpaper_controller.dart';
import '../../../shared/widgets/glass_card.dart';

class ThemeCustomizerScreen extends ConsumerStatefulWidget {
  const ThemeCustomizerScreen({super.key});

  @override
  ConsumerState<ThemeCustomizerScreen> createState() =>
      _ThemeCustomizerScreenState();
}

class _ThemeCustomizerScreenState extends ConsumerState<ThemeCustomizerScreen> {
  double _blurIntensity = 24.0;
  double _opacityIntensity = 0.14;
  Color _selectedAccent = GlassTokens.accentPrimary;
  String _selectedPreset = 'Midnight Glass';

  final _accentColors = [
    const Color(0xFF7C5CFF),
    const Color(0xFF60A5FA),
    const Color(0xFF3DDC97),
    const Color(0xFFFFC857),
    const Color(0xFFFF5C7C),
    const Color(0xFF9B7FFF),
    const Color(0xFFFF6B6B),
    const Color(0xFF4ECDC4),
  ];

  final _presets = {
    'Midnight Glass': _PresetData(
      Color(0xFF7C5CFF),
      24.0,
      0.14,
      'Default dark glass',
    ),
    'Aurora': _PresetData(
      Color(0xFF3DDC97),
      28.0,
      0.12,
      'Green-tinted glass',
    ),
    'Frosted Light': _PresetData(
      Color(0xFF60A5FA),
      32.0,
      0.10,
      'Light and airy',
    ),
    'Sunset': _PresetData(
      Color(0xFFFF5C7C),
      20.0,
      0.16,
      'Warm rose glass',
    ),
  };

  @override
  Widget build(BuildContext context) {
    final wallpaper = ref.watch(wallpaperProvider);

    return Scaffold(
      backgroundColor: GlassTokens.bgBase,
      appBar: AppBar(
        title: const Text('Theme Customizer'),
        leading: IconButton(
          icon: const Icon(Icons.arrow_back_ios_new),
          onPressed: () => Navigator.of(context).pop(),
        ),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Live preview
            Text(
              'Preview',
              style: TextStyle(
                color: GlassTokens.textPrimary,
                fontSize: 18,
                fontWeight: FontWeight.w600,
              ),
            ),
            const SizedBox(height: 12),
            _buildLivePreview(wallpaper),

            const SizedBox(height: 24),

            // Presets
            Text(
              'Presets',
              style: TextStyle(
                color: GlassTokens.textPrimary,
                fontSize: 18,
                fontWeight: FontWeight.w600,
              ),
            ),
            const SizedBox(height: 12),
            ...(_presets.entries.map((entry) {
              final isSelected = _selectedPreset == entry.key;
              return Padding(
                padding: const EdgeInsets.only(bottom: 8),
                child: GlassCard(
                  padding: const EdgeInsets.all(16),
                  onTap: () => _applyPreset(entry.key, entry.value),
                  child: Row(
                    children: [
                      Container(
                        width: 36,
                        height: 36,
                        decoration: BoxDecoration(
                          color: entry.value.accentColor,
                          shape: BoxShape.circle,
                        ),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              entry.key,
                              style: TextStyle(
                                color: GlassTokens.textPrimary,
                                fontSize: 15,
                                fontWeight: FontWeight.w500,
                              ),
                            ),
                            Text(
                              entry.value.description,
                              style: TextStyle(
                                color: GlassTokens.textSecondary,
                                fontSize: 12,
                              ),
                            ),
                          ],
                        ),
                      ),
                      if (isSelected)
                        Icon(
                          Icons.check_circle,
                          color: GlassTokens.accentPrimary,
                        ),
                    ],
                  ),
                ),
              );
            })),

            const SizedBox(height: 24),

            // Accent colors
            Text(
              'Accent Color',
              style: TextStyle(
                color: GlassTokens.textPrimary,
                fontSize: 18,
                fontWeight: FontWeight.w600,
              ),
            ),
            const SizedBox(height: 12),
            Wrap(
              spacing: 12,
              runSpacing: 12,
              children: _accentColors.map((color) {
                final isSelected = _selectedAccent == color;
                return GestureDetector(
                  onTap: () {
                    setState(() {
                      _selectedAccent = color;
                      GlassTokens.accentPrimary = color;
                    });
                  },
                  child: Container(
                    width: 48,
                    height: 48,
                    decoration: BoxDecoration(
                      color: color,
                      shape: BoxShape.circle,
                      border: Border.all(
                        color: isSelected ? Colors.white : Colors.transparent,
                        width: 3,
                      ),
                    ),
                    child: isSelected
                        ? const Icon(Icons.check, color: Colors.white, size: 20)
                        : null,
                  ),
                );
              }).toList(),
            ),

            const SizedBox(height: 24),

            // Glass sliders
            Text(
              'Glass Effects',
              style: TextStyle(
                color: GlassTokens.textPrimary,
                fontSize: 18,
                fontWeight: FontWeight.w600,
              ),
            ),
            const SizedBox(height: 12),
            _buildSlider(
              label: 'Blur Intensity',
              value: _blurIntensity,
              min: 0,
              max: 48,
              onChanged: (value) {
                setState(() {
                  _blurIntensity = value;
                  GlassTokens.glassBlurSigma = value;
                });
              },
            ),
            _buildSlider(
              label: 'Opacity',
              value: _opacityIntensity,
              min: 0.02,
              max: 0.30,
              onChanged: (value) {
                setState(() {
                  _opacityIntensity = value;
                  GlassTokens.glassTintTopOpacity = value;
                });
              },
            ),

            const SizedBox(height: 24),

            // Wallpaper
            Text(
              'Wallpaper',
              style: TextStyle(
                color: GlassTokens.textPrimary,
                fontSize: 18,
                fontWeight: FontWeight.w600,
              ),
            ),
            const SizedBox(height: 12),
            GlassCard(
              onTap: () async {
                final picker = ImagePicker();
                final image =
                    await picker.pickImage(source: ImageSource.gallery);
                if (image != null) {
                  ref.read(wallpaperProvider.notifier).setWallpaper(image.path);
                }
              },
              child: Row(
                children: [
                  Icon(
                    Icons.photo_library_outlined,
                    color: GlassTokens.accentPrimary,
                  ),
                  const SizedBox(width: 12),
                  Text(
                    'Pick from Gallery',
                    style: TextStyle(
                      color: GlassTokens.textPrimary,
                      fontSize: 15,
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 8),
            _buildSwitchTile(
              title: 'Different wallpaper for Focus Timer',
              value: wallpaper.useDifferentFocusWallpaper,
              onChanged: (value) {
                ref
                    .read(wallpaperProvider.notifier)
                    .setUseDifferentFocusWallpaper(value);
              },
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildLivePreview(WallpaperState wallpaper) {
    return ClipRRect(
      borderRadius: BorderRadius.circular(28),
      child: BackdropFilter(
        filter: ImageFilter.blur(
          sigmaX: _blurIntensity,
          sigmaY: _blurIntensity,
        ),
        child: Container(
          height: 200,
          decoration: BoxDecoration(
            gradient: LinearGradient(
              begin: Alignment.topLeft,
              end: Alignment.bottomRight,
              colors: [
                Colors.white.withValues(alpha: _opacityIntensity),
                Colors.white.withValues(alpha: _opacityIntensity * 0.3),
              ],
            ),
            border: Border.all(
              color: Colors.white.withValues(alpha: 0.25),
              width: 1,
            ),
            borderRadius: BorderRadius.circular(28),
          ),
          padding: const EdgeInsets.all(20),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            mainAxisAlignment: MainAxisAlignment.end,
            children: [
              Container(
                padding:
                    const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                decoration: BoxDecoration(
                  color: _selectedAccent.withValues(alpha: 0.2),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Text(
                  'Glass Card Preview',
                  style: TextStyle(
                    color: _selectedAccent,
                    fontSize: 14,
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ),
              const SizedBox(height: 8),
              Text(
                'This is what your glass panels will look like with the current settings.',
                style: TextStyle(
                  color: Colors.white70,
                  fontSize: 12,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildSlider({
    required String label,
    required double value,
    required double min,
    required double max,
    required ValueChanged<double> onChanged,
  }) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: GlassCard(
        padding: const EdgeInsets.all(12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  label,
                  style: TextStyle(
                    color: GlassTokens.textSecondary,
                    fontSize: 13,
                  ),
                ),
                Text(
                  value.toStringAsFixed(1),
                  style: TextStyle(
                    color: GlassTokens.accentPrimary,
                    fontSize: 13,
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ],
            ),
            SliderTheme(
              data: SliderThemeData(
                activeTrackColor: GlassTokens.accentPrimary,
                inactiveTrackColor: Colors.white.withValues(alpha: 0.1),
                thumbColor: GlassTokens.accentPrimary,
                overlayColor: GlassTokens.accentPrimary.withValues(alpha: 0.1),
              ),
              child: Slider(
                value: value,
                min: min,
                max: max,
                onChanged: onChanged,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSwitchTile({
    required String title,
    required bool value,
    required ValueChanged<bool> onChanged,
  }) {
    return GlassCard(
      padding: const EdgeInsets.all(12),
      child: Row(
        children: [
          Expanded(
            child: Text(
              title,
              style: TextStyle(
                color: GlassTokens.textPrimary,
                fontSize: 14,
              ),
            ),
          ),
          Switch(
            value: value,
            onChanged: onChanged,
            activeThumbColor: GlassTokens.accentPrimary,
            activeTrackColor: GlassTokens.accentPrimary.withValues(alpha: 0.3),
            inactiveThumbColor: GlassTokens.textSecondary,
            inactiveTrackColor: Colors.white.withValues(alpha: 0.1),
          ),
        ],
      ),
    );
  }

  void _applyPreset(String name, _PresetData preset) {
    setState(() {
      _selectedPreset = name;
      _selectedAccent = preset.accentColor;
      _blurIntensity = preset.blur;
      _opacityIntensity = preset.opacity;
      GlassTokens.accentPrimary = preset.accentColor;
      GlassTokens.glassBlurSigma = preset.blur;
      GlassTokens.glassTintTopOpacity = preset.opacity;
    });
  }
}

class _PresetData {
  final Color accentColor;
  final double blur;
  final double opacity;
  final String description;

  _PresetData(this.accentColor, this.blur, this.opacity, this.description);
}
