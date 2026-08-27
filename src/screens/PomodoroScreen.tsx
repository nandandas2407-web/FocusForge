import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';
import { useStore } from '../store/useStore';
import { Colors } from '../theme/colors';
import { GlassCard } from '../components/GlassCard';
import { PremiumButton } from '../components/PremiumButton';
import { PremiumHeader } from '../components/PremiumHeader';
import { TimerDial } from '../components/TimerDial';

const MODES = [
  { key: 'POMODORO' as const, label: 'Focus', minutes: 25, color: Colors.gold },
  { key: 'SHORT_BREAK' as const, label: 'Short', minutes: 5, color: Colors.success },
  { key: 'LONG_BREAK' as const, label: 'Long', minutes: 15, color: Colors.info },
];

export default function PomodoroScreen() {
  const {
    timerSeconds, isTimerRunning, timerMode, pomodoroCount,
    setTimerMode, startTimer, pauseTimer, resetTimer,
  } = useStore();

  const currentMode = MODES.find((m) => m.key === timerMode) || MODES[0];
  const totalSeconds = currentMode.minutes * 60;

  return (
    <View style={styles.container}>
      <PremiumHeader
        title="Pomodoro"
        subtitle={`${pomodoroCount} sessions completed`}
      />

      <View style={styles.dialContainer}>
        <TimerDial
          seconds={timerSeconds}
          totalSeconds={totalSeconds}
          mode={timerMode}
          size={260}
        />
      </View>

      {/* Mode Selector */}
      <View style={styles.modeRow}>
        {MODES.map((m) => (
          <TouchableOpacity
            key={m.key}
            style={[styles.modeBtn, timerMode === m.key && { borderColor: m.color, backgroundColor: m.color + '15' }]}
            onPress={() => setTimerMode(m.key)}
          >
            <Text style={[styles.modeLabel, timerMode === m.key && { color: m.color }]}>
              {m.label}
            </Text>
            <Text style={[styles.modeMinutes, timerMode === m.key && { color: m.color }]}>
              {m.minutes}m
            </Text>
          </TouchableOpacity>
        ))}
      </View>

      {/* Controls */}
      <View style={styles.controls}>
        <PremiumButton
          title="Reset"
          onPress={resetTimer}
          variant="ghost"
          size="md"
        />
        <PremiumButton
          title={isTimerRunning ? 'Pause' : 'Start'}
          onPress={isTimerRunning ? pauseTimer : startTimer}
          variant="primary"
          size="lg"
        />
        <PremiumButton
          title="Skip"
          onPress={() => {
            resetTimer();
          }}
          variant="ghost"
          size="md"
        />
      </View>

      {/* Ambient Sounds */}
      <GlassCard style={styles.ambientCard}>
        <Text style={styles.ambientTitle}>Ambient Sound</Text>
        <View style={styles.soundGrid}>
          {['Rain', 'Lo-Fi', 'Cafe', 'White Noise', 'Forest', 'None'].map((sound) => (
            <TouchableOpacity key={sound} style={styles.soundChip}>
              <Text style={styles.soundText}>{sound}</Text>
            </TouchableOpacity>
          ))}
        </View>
      </GlassCard>

      {/* Session Info */}
      <GlassCard style={styles.sessionInfo}>
        <View style={styles.sessionRow}>
          <View style={styles.sessionStat}>
            <Text style={styles.sessionStatValue}>{pomodoroCount}</Text>
            <Text style={styles.sessionStatLabel}>Sessions</Text>
          </View>
          <View style={styles.sessionDivider} />
          <View style={styles.sessionStat}>
            <Text style={styles.sessionStatValue}>{pomodoroCount * 25}</Text>
            <Text style={styles.sessionStatLabel}>Minutes</Text>
          </View>
          <View style={styles.sessionDivider} />
          <View style={styles.sessionStat}>
            <Text style={styles.sessionStatValue}>{Math.floor(pomodoroCount / 4)}</Text>
            <Text style={styles.sessionStatLabel}>Cycles</Text>
          </View>
        </View>
      </GlassCard>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.background },
  dialContainer: { alignItems: 'center', justifyContent: 'center', paddingVertical: 24 },

  modeRow: { flexDirection: 'row', justifyContent: 'center', gap: 12, paddingHorizontal: 20 },
  modeBtn: {
    flex: 1, alignItems: 'center', paddingVertical: 14, borderRadius: 14,
    backgroundColor: Colors.surface, borderWidth: 1.5, borderColor: Colors.border,
  },
  modeLabel: { fontSize: 14, fontWeight: '600', color: Colors.textSecondary },
  modeMinutes: { fontSize: 12, color: Colors.textMuted, marginTop: 2 },

  controls: {
    flexDirection: 'row', justifyContent: 'center', alignItems: 'center',
    gap: 20, paddingHorizontal: 20, marginTop: 32, marginBottom: 24,
  },

  ambientCard: { marginHorizontal: 16 },
  ambientTitle: { fontSize: 14, fontWeight: '600', color: Colors.textPrimary, marginBottom: 12 },
  soundGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },
  soundChip: {
    paddingHorizontal: 16, paddingVertical: 8, borderRadius: 20,
    backgroundColor: Colors.surfaceElevated, borderWidth: 1, borderColor: Colors.border,
  },
  soundText: { fontSize: 13, color: Colors.textSecondary },

  sessionInfo: { marginHorizontal: 16, marginTop: 12 },
  sessionRow: { flexDirection: 'row', justifyContent: 'space-around', alignItems: 'center' },
  sessionStat: { alignItems: 'center' },
  sessionStatValue: { fontSize: 24, fontWeight: '700', color: Colors.gold },
  sessionStatLabel: { fontSize: 12, color: Colors.textSecondary, marginTop: 4 },
  sessionDivider: { width: 1, height: 32, backgroundColor: Colors.border },
});
