// ============================================================
// FILE: lib/features/focus_timer/presentation/focus_timer_screen.dart
// PURPOSE: Pomodoro/Focus timer screen with wallpaper-behind-glass
//          UI, ambient sound picker, and auto-blocking.
// CREATED: 2026-08-03 | LAST MODIFIED: 2026-08-03
// ============================================================
import 'dart:async';
import 'dart:ui';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/theme/glass_tokens.dart';
import '../../../core/theme/wallpaper_controller.dart';
import '../../../core/state/block_rules_controller.dart';
import '../../../core/state/session_history_controller.dart';
import '../../../shared/widgets/glass_card.dart';
import '../../../shared/widgets/glass_button.dart';

class FocusTimerScreen extends ConsumerStatefulWidget {
  const FocusTimerScreen({super.key});

  @override
  ConsumerState<FocusTimerScreen> createState() => _FocusTimerScreenState();
}

class _FocusTimerScreenState extends ConsumerState<FocusTimerScreen>
    with SingleTickerProviderStateMixin {
  Timer? _timer;
  int _totalSeconds = 25 * 60; // 25 minutes default
  int _remainingSeconds = 25 * 60;
  bool _isRunning = false;
  bool _isBreak = false;

  int _workMinutes = 25;
  int _breakMinutes = 5;
  int _currentPomodoro = 1;
  int _totalPomodoros = 4;

  String _selectedSound = 'None';
  final _sounds = ['None', 'Lo-fi', 'Rain', 'White Noise', 'Forest'];

  /// Wall-clock time the current focus (non-break) phase began running,
  /// used to log accurate elapsed time to session history regardless of
  /// whether the phase ends via natural completion or a manual pause.
  DateTime? _focusPhaseStartedAt;

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }

  void _startTimer() {
    if (_isRunning) {
      _timer?.cancel();
      setState(() => _isRunning = false);
      // Pausing the timer also lifts the block, so the user isn't
      // locked out while the session is genuinely paused.
      if (!_isBreak) {
        ref.read(blockRulesProvider.notifier).endSession();
        _logElapsedFocusTime();
      }
    } else {
      _isRunning = true;
      // A focus (non-break) phase is what actually enforces blocking.
      if (!_isBreak) {
        ref.read(blockRulesProvider.notifier).startSession();
        _focusPhaseStartedAt = DateTime.now();
      }
      _timer = Timer.periodic(const Duration(seconds: 1), (timer) {
        if (_remainingSeconds > 0) {
          setState(() => _remainingSeconds--);
        } else {
          _timer?.cancel();
          _onTimerComplete();
        }
      });
    }
  }

  /// Logs real elapsed seconds for the focus phase that just ended
  /// (whether by completion or pause) to the persisted session history
  /// that streaks/goals are computed from.
  void _logElapsedFocusTime() {
    final startedAt = _focusPhaseStartedAt;
    if (startedAt == null) return;
    final elapsed = DateTime.now().difference(startedAt).inSeconds;
    if (elapsed > 0) {
      ref.read(sessionHistoryProvider.notifier).logCompletedFocusSeconds(elapsed);
    }
    _focusPhaseStartedAt = null;
  }

  void _onTimerComplete() {
    setState(() {
      _isRunning = false;
      if (_isBreak) {
        _isBreak = false;
        _remainingSeconds = _workMinutes * 60;
        _totalSeconds = _workMinutes * 60;
      } else {
        // Focus phase just ended — lift the block for the break and
        // log the completed duration to session history.
        ref.read(blockRulesProvider.notifier).endSession();
        _logElapsedFocusTime();
        if (_currentPomodoro < _totalPomodoros) {
          _currentPomodoro++;
          _isBreak = true;
          _remainingSeconds = _breakMinutes * 60;
          _totalSeconds = _breakMinutes * 60;
        } else {
          _showCompletionDialog();
        }
      }
    });
  }

  void _showCompletionDialog() {
    showModalBottomSheet(
      context: context,
      backgroundColor: Colors.transparent,
      builder: (context) => ClipRRect(
        borderRadius: const BorderRadius.vertical(top: Radius.circular(28)),
        child: BackdropFilter(
          filter: ImageFilter.blur(sigmaX: 40, sigmaY: 40),
          child: Container(
            padding: const EdgeInsets.all(32),
            decoration: BoxDecoration(
              gradient: LinearGradient(
                begin: Alignment.topCenter,
                end: Alignment.bottomCenter,
                colors: [
                  Colors.white.withValues(alpha: 0.12),
                  Colors.white.withValues(alpha: 0.04),
                ],
              ),
              border: Border.all(
                color: Colors.white.withValues(alpha: 0.25),
              ),
            ),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                const Text('🎉', style: TextStyle(fontSize: 48)),
                const SizedBox(height: 16),
                Text(
                  'Session Complete!',
                  style: TextStyle(
                    color: GlassTokens.textPrimary,
                    fontSize: 24,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 8),
                Text(
                  '$_totalPomodoros pomodoros completed',
                  style: TextStyle(
                    color: GlassTokens.textSecondary,
                    fontSize: 16,
                  ),
                ),
                const SizedBox(height: 24),
                GlassButton(
                  label: 'Done',
                  isExpanded: true,
                  onPressed: () {
                    Navigator.of(context).pop();
                    _resetTimer();
                  },
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  void _resetTimer() {
    _timer?.cancel();
    setState(() {
      _isRunning = false;
      _isBreak = false;
      _currentPomodoro = 1;
      _remainingSeconds = _workMinutes * 60;
      _totalSeconds = _workMinutes * 60;
    });
  }

  String _formatTime(int seconds) {
    final mins = seconds ~/ 60;
    final secs = seconds % 60;
    return '${mins.toString().padLeft(2, '0')}:${secs.toString().padLeft(2, '0')}';
  }

  @override
  Widget build(BuildContext context) {
    final wallpaper = ref.watch(wallpaperProvider);
    final progress = 1.0 - (_remainingSeconds / _totalSeconds);

    return Scaffold(
      backgroundColor: Colors.transparent,
      body: Stack(
        fit: StackFit.expand,
        children: [
          // Wallpaper background
          if (wallpaper.effectiveFocusWallpaper != null)
            Image.asset(
              'assets/images/default_wallpaper.jpg',
              fit: BoxFit.cover,
              errorBuilder: (_, __, ___) => _buildDefaultBackground(),
            )
          else
            _buildDefaultBackground(),

          // Blur overlay
          BackdropFilter(
            filter: ImageFilter.blur(sigmaX: 40, sigmaY: 40),
            child: Container(
              color: Colors.black.withValues(alpha: 0.35),
            ),
          ),

          // Content
          SafeArea(
            child: Column(
              children: [
                // Header
                Padding(
                  padding: const EdgeInsets.all(20),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      IconButton(
                        icon: const Icon(Icons.arrow_back_ios_new),
                        color: Colors.white,
                        onPressed: () => Navigator.of(context).pop(),
                      ),
                      Text(
                        _isBreak ? 'Break Time' : 'Focus Session',
                        style: const TextStyle(
                          color: Colors.white,
                          fontSize: 18,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                      IconButton(
                        icon: const Icon(Icons.settings_outlined),
                        color: Colors.white,
                        onPressed: _showSettingsSheet,
                      ),
                    ],
                  ),
                ),

                // Timer
                Expanded(
                  child: Center(
                    child: GlassCard(
                      width: 300,
                      padding: const EdgeInsets.all(40),
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          // Pomodoro indicator
                          Row(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: List.generate(
                              _totalPomodoros,
                              (i) => Container(
                                margin: const EdgeInsets.symmetric(horizontal: 4),
                                width: 12,
                                height: 12,
                                decoration: BoxDecoration(
                                  shape: BoxShape.circle,
                                  color: i < _currentPomodoro
                                      ? GlassTokens.accentPrimary
                                      : Colors.white.withValues(alpha: 0.2),
                                ),
                              ),
                            ),
                          ),
                          const SizedBox(height: 32),

                          // Timer ring
                          SizedBox(
                            width: 200,
                            height: 200,
                            child: Stack(
                              alignment: Alignment.center,
                              children: [
                                SizedBox(
                                  width: 200,
                                  height: 200,
                                  child: CircularProgressIndicator(
                                    value: progress,
                                    strokeWidth: 8,
                                    backgroundColor:
                                        Colors.white.withValues(alpha: 0.1),
                                    valueColor: AlwaysStoppedAnimation<Color>(
                                      _isBreak
                                          ? GlassTokens.success
                                          : GlassTokens.accentPrimary,
                                    ),
                                    strokeCap: StrokeCap.round,
                                  ),
                                ),
                                Column(
                                  mainAxisSize: MainAxisSize.min,
                                  children: [
                                    Text(
                                      _formatTime(_remainingSeconds),
                                      style: const TextStyle(
                                        color: Colors.white,
                                        fontSize: 48,
                                        fontWeight: FontWeight.bold,
                                        fontFeatures: [
                                          FontFeature.tabularFigures()
                                        ],
                                      ),
                                    ),
                                    Text(
                                      _isBreak ? 'Break' : 'Focus',
                                      style: TextStyle(
                                        color: Colors.white.withValues(alpha: 0.7),
                                        fontSize: 14,
                                      ),
                                    ),
                                  ],
                                ),
                              ],
                            ),
                          ),

                          const SizedBox(height: 32),

                          // Controls
                          Row(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: [
                              // Reset
                              _buildControlButton(
                                icon: Icons.refresh,
                                onTap: _resetTimer,
                              ),
                              const SizedBox(width: 24),
                              // Play/Pause
                              GestureDetector(
                                onTap: _startTimer,
                                child: Container(
                                  width: 72,
                                  height: 72,
                                  decoration: BoxDecoration(
                                    shape: BoxShape.circle,
                                    color: _isRunning
                                        ? GlassTokens.danger
                                        : GlassTokens.accentPrimary,
                                    boxShadow: [
                                      BoxShadow(
                                        color: (_isRunning
                                                ? GlassTokens.danger
                                                : GlassTokens.accentPrimary)
                                            .withValues(alpha: 0.4),
                                        blurRadius: 20,
                                        spreadRadius: 2,
                                      ),
                                    ],
                                  ),
                                  child: Icon(
                                    _isRunning ? Icons.pause : Icons.play_arrow,
                                    color: Colors.white,
                                    size: 36,
                                  ),
                                ),
                              ),
                              const SizedBox(width: 24),
                              // Skip
                              _buildControlButton(
                                icon: Icons.skip_next,
                                onTap: () {
                                  _timer?.cancel();
                                  _onTimerComplete();
                                },
                              ),
                            ],
                          ),
                        ],
                      ),
                    ),
                  ),
                ),

                // Sound picker
                Padding(
                  padding: const EdgeInsets.all(20),
                  child: GlassCard(
                    padding: const EdgeInsets.symmetric(
                        horizontal: 16, vertical: 12),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Icon(
                          Icons.volume_up_outlined,
                          color: GlassTokens.textSecondary,
                          size: 20,
                        ),
                        const SizedBox(width: 12),
                        ...(_sounds.map((sound) {
                          final isSelected = _selectedSound == sound;
                          return Padding(
                            padding: const EdgeInsets.symmetric(horizontal: 4),
                            child: GestureDetector(
                              onTap: () =>
                                  setState(() => _selectedSound = sound),
                              child: Container(
                                padding: const EdgeInsets.symmetric(
                                    horizontal: 12, vertical: 6),
                                decoration: BoxDecoration(
                                  color: isSelected
                                      ? GlassTokens.accentPrimary
                                          .withValues(alpha: 0.2)
                                      : Colors.transparent,
                                  borderRadius: BorderRadius.circular(20),
                                  border: Border.all(
                                    color: isSelected
                                        ? GlassTokens.accentPrimary
                                        : Colors.white.withValues(alpha: 0.1),
                                  ),
                                ),
                                child: Text(
                                  sound,
                                  style: TextStyle(
                                    color: isSelected
                                        ? GlassTokens.accentPrimary
                                        : GlassTokens.textSecondary,
                                    fontSize: 12,
                                  ),
                                ),
                              ),
                            ),
                          );
                        })),
                      ],
                    ),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildDefaultBackground() {
    return Container(
      decoration: BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [
            GlassTokens.bgGradientStart,
            GlassTokens.bgGradientEnd,
          ],
        ),
      ),
    );
  }

  Widget _buildControlButton({
    required IconData icon,
    required VoidCallback onTap,
  }) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        width: 48,
        height: 48,
        decoration: BoxDecoration(
          shape: BoxShape.circle,
          color: Colors.white.withValues(alpha: 0.1),
          border: Border.all(
            color: Colors.white.withValues(alpha: 0.2),
          ),
        ),
        child: Icon(icon, color: Colors.white, size: 22),
      ),
    );
  }

  void _showSettingsSheet() {
    showModalBottomSheet(
      context: context,
      backgroundColor: Colors.transparent,
      builder: (context) => ClipRRect(
        borderRadius: const BorderRadius.vertical(top: Radius.circular(28)),
        child: BackdropFilter(
          filter: ImageFilter.blur(sigmaX: 40, sigmaY: 40),
          child: Container(
            padding: const EdgeInsets.all(24),
            decoration: BoxDecoration(
              gradient: LinearGradient(
                begin: Alignment.topCenter,
                end: Alignment.bottomCenter,
                colors: [
                  Colors.white.withValues(alpha: 0.12),
                  Colors.white.withValues(alpha: 0.04),
                ],
              ),
              border: Border.all(
                color: Colors.white.withValues(alpha: 0.25),
              ),
            ),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Container(
                  width: 40,
                  height: 4,
                  decoration: BoxDecoration(
                    color: Colors.white.withValues(alpha: 0.3),
                    borderRadius: BorderRadius.circular(2),
                  ),
                ),
                const SizedBox(height: 20),
                const Text(
                  'Timer Settings',
                  style: TextStyle(
                    color: Colors.white,
                    fontSize: 20,
                    fontWeight: FontWeight.w600,
                  ),
                ),
                const SizedBox(height: 24),
                _buildSettingRow(
                  'Work Duration',
                  '$_workMinutes min',
                  () => _adjustTime(isWork: true, increment: 5),
                  () => _adjustTime(isWork: true, increment: -5),
                ),
                _buildSettingRow(
                  'Break Duration',
                  '$_breakMinutes min',
                  () => _adjustTime(isWork: false, increment: 1),
                  () => _adjustTime(isWork: false, increment: -1),
                ),
                _buildSettingRow(
                  'Pomodoros',
                  '$_totalPomodoros',
                  () => setState(() {
                    if (_totalPomodoros < 8) _totalPomodoros++;
                  }),
                  () => setState(() {
                    if (_totalPomodoros > 1) _totalPomodoros--;
                  }),
                ),
                const SizedBox(height: 24),
                GlassButton(
                  label: 'Done',
                  isExpanded: true,
                  onPressed: () => Navigator.of(context).pop(),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildSettingRow(
    String label,
    String value,
    VoidCallback onIncrement,
    VoidCallback onDecrement,
  ) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(
            label,
            style: const TextStyle(color: Colors.white, fontSize: 16),
          ),
          Row(
            children: [
              IconButton(
                onPressed: onDecrement,
                icon: Icon(
                  Icons.remove_circle_outline,
                  color: GlassTokens.textSecondary,
                ),
              ),
              SizedBox(
                width: 50,
                child: Text(
                  value,
                  textAlign: TextAlign.center,
                  style: const TextStyle(
                    color: Colors.white,
                    fontSize: 16,
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ),
              IconButton(
                onPressed: onIncrement,
                icon: Icon(
                  Icons.add_circle_outline,
                  color: GlassTokens.accentPrimary,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  void _adjustTime({required bool isWork, required int increment}) {
    setState(() {
      if (isWork) {
        _workMinutes = (_workMinutes + increment).clamp(5, 60);
        if (!_isRunning && !_isBreak) {
          _totalSeconds = _workMinutes * 60;
          _remainingSeconds = _workMinutes * 60;
        }
      } else {
        _breakMinutes = (_breakMinutes + increment).clamp(1, 30);
        if (!_isRunning && _isBreak) {
          _totalSeconds = _breakMinutes * 60;
          _remainingSeconds = _breakMinutes * 60;
        }
      }
    });
  }
}
