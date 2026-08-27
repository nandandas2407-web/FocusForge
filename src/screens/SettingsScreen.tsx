import React from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, Switch, Alert } from 'react-native';
import { useStore } from '../store/useStore';
import { Colors } from '../theme/colors';
import { GlassCard } from '../components/GlassCard';
import { PremiumHeader } from '../components/PremiumHeader';

const ACCENT_COLORS = [
  { name: 'Gold', color: '#C9A84C' },
  { name: 'Bronze', color: '#8B6914' },
  { name: 'Copper', color: '#B87333' },
  { name: 'Champagne', color: '#F7E7CE' },
  { name: 'Emerald', color: '#5CB85C' },
  { name: 'Rose', color: '#C75050' },
];

export default function SettingsScreen() {
  const {
    accentColor, setAccentColor,
    globalBlockerEnabled, setGlobalBlocker,
    youtubeStudyMode, setYoutubeStudyMode,
    streakGoal, updateStreak,
  } = useStore();

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <PremiumHeader title="Settings" subtitle="Customize your experience" />

      {/* Theme */}
      <Text style={styles.sectionTitle}>Theme</Text>
      <GlassCard>
        <Text style={styles.settingLabel}>Accent Color</Text>
        <View style={styles.colorGrid}>
          {ACCENT_COLORS.map((c) => (
            <TouchableOpacity
              key={c.name}
              style={[
                styles.colorSwatch,
                { backgroundColor: c.color },
                accentColor === c.color && styles.colorSwatchActive,
              ]}
              onPress={() => setAccentColor(c.color)}
            >
              {accentColor === c.color && <Text style={styles.colorCheck}>✓</Text>}
            </TouchableOpacity>
          ))}
        </View>
      </GlassCard>

      {/* Protection */}
      <Text style={styles.sectionTitle}>Protection</Text>
      <GlassCard>
        <View style={styles.settingRow}>
          <View style={{ flex: 1 }}>
            <Text style={styles.settingLabel}>Global Blocker</Text>
            <Text style={styles.settingDesc}>Master switch for all blocking features</Text>
          </View>
          <Switch
            value={globalBlockerEnabled}
            onValueChange={setGlobalBlocker}
            trackColor={{ false: Colors.surfaceElevated, true: Colors.goldGlow }}
            thumbColor={globalBlockerEnabled ? Colors.gold : Colors.textMuted}
          />
        </View>
      </GlassCard>

      <GlassCard>
        <View style={styles.settingRow}>
          <View style={{ flex: 1 }}>
            <Text style={styles.settingLabel}>YouTube Study Mode</Text>
            <Text style={styles.settingDesc}>Only allow whitelisted educational channels</Text>
          </View>
          <Switch
            value={youtubeStudyMode}
            onValueChange={setYoutubeStudyMode}
            trackColor={{ false: Colors.surfaceElevated, true: Colors.goldGlow }}
            thumbColor={youtubeStudyMode ? Colors.gold : Colors.textMuted}
          />
        </View>
      </GlassCard>

      {/* Streak Goal */}
      <Text style={styles.sectionTitle}>Goals</Text>
      <GlassCard>
        <Text style={styles.settingLabel}>Daily Screen Time Goal</Text>
        <Text style={styles.settingDesc}>Current: {streakGoal.dailyScreenTimeGoalMinutes} minutes</Text>
        <View style={styles.goalButtons}>
          {[60, 90, 120, 180, 240].map((mins) => (
            <TouchableOpacity
              key={mins}
              style={[
                styles.goalBtn,
                streakGoal.dailyScreenTimeGoalMinutes === mins && styles.goalBtnActive,
              ]}
              onPress={() => updateStreak(mins)}
            >
              <Text style={[
                styles.goalBtnText,
                streakGoal.dailyScreenTimeGoalMinutes === mins && styles.goalBtnTextActive,
              ]}>
                {mins >= 60 ? `${mins / 60}h` : `${mins}m`}
              </Text>
            </TouchableOpacity>
          ))}
        </View>
      </GlassCard>

      {/* About */}
      <Text style={styles.sectionTitle}>About</Text>
      <GlassCard>
        <Text style={styles.settingLabel}>FocusForge v2.0</Text>
        <Text style={styles.settingDesc}>Premium Focus & Study Suite</Text>
        <Text style={[styles.settingDesc, { marginTop: 8, color: Colors.textMuted }]}>
          Built with React Native. Blocks distractions at the system level using Android Accessibility Services.
        </Text>
      </GlassCard>

      {/* Reset */}
      <TouchableOpacity
        style={styles.resetBtn}
        onPress={() => Alert.alert('Reset', 'This would clear all data.')}
      >
        <Text style={styles.resetText}>Reset All Data</Text>
      </TouchableOpacity>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.background },
  content: { paddingBottom: 100 },

  sectionTitle: { fontSize: 16, fontWeight: '600', color: Colors.textSecondary, paddingHorizontal: 20, marginTop: 24, marginBottom: 12, textTransform: 'uppercase', letterSpacing: 0.5 },

  settingLabel: { fontSize: 15, fontWeight: '600', color: Colors.textPrimary },
  settingDesc: { fontSize: 13, color: Colors.textSecondary, marginTop: 2 },
  settingRow: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' },

  colorGrid: { flexDirection: 'row', gap: 12, marginTop: 12 },
  colorSwatch: {
    width: 44, height: 44, borderRadius: 22, alignItems: 'center', justifyContent: 'center',
    borderWidth: 2, borderColor: 'transparent',
  },
  colorSwatchActive: { borderColor: Colors.textPrimary, transform: [{ scale: 1.1 }] },
  colorCheck: { fontSize: 16, color: Colors.background, fontWeight: '700' },

  goalButtons: { flexDirection: 'row', gap: 8, marginTop: 12 },
  goalBtn: {
    flex: 1, paddingVertical: 10, borderRadius: 10, backgroundColor: Colors.surfaceElevated,
    alignItems: 'center', borderWidth: 1, borderColor: Colors.border,
  },
  goalBtnActive: { backgroundColor: Colors.goldSubtle, borderColor: Colors.borderGold },
  goalBtnText: { fontSize: 14, color: Colors.textSecondary, fontWeight: '500' },
  goalBtnTextActive: { color: Colors.gold },

  resetBtn: { marginHorizontal: 16, marginTop: 24, alignItems: 'center', padding: 16 },
  resetText: { fontSize: 14, color: Colors.danger, fontWeight: '600' },
});
