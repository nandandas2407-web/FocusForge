import React, { useEffect, useState } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, Dimensions } from 'react-native';
import { useStore } from '../store/useStore';
import { Colors } from '../theme/colors';
import { GlassCard } from '../components/GlassCard';
import { PremiumButton } from '../components/PremiumButton';
import Svg, { Circle } from 'react-native-svg';

const { width } = Dimensions.get('window');

export default function HomeScreen({ navigation }: any) {
  const {
    blockedApps, youtubeStudyMode, globalBlockerEnabled,
    streakGoal, focusSessions, tasks, timerSeconds, timerMode,
    isTimerRunning, pomodoroCount,
  } = useStore();

  const blockedCount = blockedApps.filter((a) => a.isFullyBlocked).length;
  const reelsBlocked = blockedApps.filter((a) => a.isReelsBlocked).length;
  const shortsBlocked = blockedApps.filter((a) => a.isShortsBlocked).length;
  const completedTasks = tasks.filter((t) => t.isCompleted).length;
  const todaySessions = focusSessions.filter(
    (s) => new Date(s.completedAt).toDateString() === new Date().toDateString()
  ).length;

  const minutes = Math.floor(timerSeconds / 60);
  const secs = timerSeconds % 60;
  const totalSecs = timerMode === 'POMODORO' ? 25 * 60 : timerMode === 'SHORT_BREAK' ? 5 * 60 : 15 * 60;
  const progress = totalSecs > 0 ? (totalSecs - timerSeconds) / totalSecs : 0;
  const radius = 36;
  const circumference = 2 * Math.PI * radius;

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      {/* Hero */}
      <View style={styles.hero}>
        <Text style={styles.greeting}>FocusForge</Text>
        <Text style={styles.tagline}>Discipline is freedom.</Text>
      </View>

      {/* Active Timer Card */}
      <GlassCard variant="elevated" style={styles.timerCard}>
        <View style={styles.timerRow}>
          <View style={styles.timerLeft}>
            <Text style={styles.timerLabel}>
              {isTimerRunning ? 'In Progress' : 'Ready'}
            </Text>
            <Text style={styles.timerTime}>
              {String(minutes).padStart(2, '0')}:{String(secs).padStart(2, '0')}
            </Text>
            <Text style={styles.timerMode}>
              {timerMode === 'POMODORO' ? 'Focus Session' : timerMode === 'SHORT_BREAK' ? 'Short Break' : 'Long Break'}
            </Text>
          </View>
          <View style={styles.timerDialContainer}>
            <Svg width={84} height={84}>
              <Circle cx={42} cy={42} r={radius} stroke={Colors.surfaceElevated} strokeWidth={5} fill="none" />
              <Circle
                cx={42} cy={42} r={radius}
                stroke={isTimerRunning ? Colors.gold : Colors.goldMuted}
                strokeWidth={5} fill="none"
                strokeDasharray={circumference}
                strokeDashoffset={circumference * (1 - progress)}
                strokeLinecap="round"
                transform="rotate(-90 42 42)"
              />
            </Svg>
            <Text style={styles.pomodoroCount}>{pomodoroCount}</Text>
          </View>
        </View>
        <View style={styles.timerActions}>
          <PremiumButton
            title={isTimerRunning ? 'Pause' : 'Start'}
            onPress={() => isTimerRunning ? useStore.getState().pauseTimer() : useStore.getState().startTimer()}
            variant="primary"
            size="sm"
          />
          <PremiumButton
            title="Reset"
            onPress={() => useStore.getState().resetTimer()}
            variant="ghost"
            size="sm"
          />
        </View>
      </GlassCard>

      {/* Stats Grid */}
      <View style={styles.statsGrid}>
        <GlassCard style={styles.statCard}>
          <Text style={styles.statValue}>{blockedCount}</Text>
          <Text style={styles.statLabel}>Apps Blocked</Text>
          <View style={[styles.statDot, { backgroundColor: Colors.danger }]} />
        </GlassCard>
        <GlassCard style={styles.statCard}>
          <Text style={styles.statValue}>{todaySessions}</Text>
          <Text style={styles.statLabel}>Sessions</Text>
          <View style={[styles.statDot, { backgroundColor: Colors.gold }]} />
        </GlassCard>
        <GlassCard style={styles.statCard}>
          <Text style={styles.statValue}>{completedTasks}/{tasks.length}</Text>
          <Text style={styles.statLabel}>Tasks Done</Text>
          <View style={[styles.statDot, { backgroundColor: Colors.success }]} />
        </GlassCard>
        <GlassCard style={styles.statCard}>
          <Text style={styles.statValue}>{streakGoal.currentStreakDays}</Text>
          <Text style={styles.statLabel}>Day Streak</Text>
          <View style={[styles.statDot, { backgroundColor: Colors.warning }]} />
        </GlassCard>
      </View>

      {/* Quick Actions */}
      <Text style={styles.sectionTitle}>Quick Actions</Text>
      <View style={styles.actionsGrid}>
        <TouchableOpacity
          style={[styles.actionBtn, { borderColor: Colors.danger }]}
          onPress={() => navigation.navigate('Blocker')}
        >
          <Text style={styles.actionIcon}>🛡️</Text>
          <Text style={styles.actionText}>Blocker</Text>
          <Text style={styles.actionSub}>{blockedCount} apps</Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={[styles.actionBtn, { borderColor: Colors.gold }]}
          onPress={() => navigation.navigate('Pomodoro')}
        >
          <Text style={styles.actionIcon}>⏱️</Text>
          <Text style={styles.actionText}>Timer</Text>
          <Text style={styles.actionSub}>Focus</Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={[styles.actionBtn, { borderColor: Colors.info }]}
          onPress={() => navigation.navigate('YouTube')}
        >
          <Text style={styles.actionIcon}>📚</Text>
          <Text style={styles.actionText}>Study</Text>
          <Text style={styles.actionSub}>{youtubeStudyMode ? 'ON' : 'OFF'}</Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={[styles.actionBtn, { borderColor: Colors.success }]}
          onPress={() => navigation.navigate('Tasks')}
        >
          <Text style={styles.actionIcon}>✅</Text>
          <Text style={styles.actionText}>Tasks</Text>
          <Text style={styles.actionSub}>{completedTasks} done</Text>
        </TouchableOpacity>
      </View>

      {/* Protection Status */}
      <GlassCard style={styles.statusCard}>
        <View style={styles.statusRow}>
          <View style={styles.statusInfo}>
            <Text style={styles.statusTitle}>Protection Status</Text>
            <Text style={[styles.statusValue, { color: globalBlockerEnabled ? Colors.success : Colors.danger }]}>
              {globalBlockerEnabled ? 'ACTIVE' : 'PAUSED'}
            </Text>
          </View>
          <View style={styles.statusDetails}>
            <View style={styles.statusItem}>
              <View style={[styles.statusDot, { backgroundColor: reelsBlocked > 0 ? Colors.success : Colors.textMuted }]} />
              <Text style={styles.statusItemText}>Reels</Text>
            </View>
            <View style={styles.statusItem}>
              <View style={[styles.statusDot, { backgroundColor: shortsBlocked > 0 ? Colors.success : Colors.textMuted }]} />
              <Text style={styles.statusItemText}>Shorts</Text>
            </View>
            <View style={styles.statusItem}>
              <View style={[styles.statusDot, { backgroundColor: youtubeStudyMode ? Colors.success : Colors.textMuted }]} />
              <Text style={styles.statusItemText}>Study</Text>
            </View>
          </View>
        </View>
      </GlassCard>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.background },
  content: { paddingBottom: 100 },
  hero: { paddingHorizontal: 20, paddingTop: 60, paddingBottom: 8 },
  greeting: { fontSize: 32, fontWeight: '700', color: Colors.textPrimary, letterSpacing: -1 },
  tagline: { fontSize: 15, color: Colors.textSecondary, marginTop: 4 },

  timerCard: { marginHorizontal: 16, marginTop: 16 },
  timerRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  timerLeft: { flex: 1 },
  timerLabel: { fontSize: 12, color: Colors.textMuted, textTransform: 'uppercase', letterSpacing: 1.5 },
  timerTime: { fontSize: 40, fontWeight: '700', color: Colors.textPrimary, fontVariant: ['tabular-nums'], marginVertical: 4 },
  timerMode: { fontSize: 13, color: Colors.gold },
  timerDialContainer: { alignItems: 'center', justifyContent: 'center' },
  pomodoroCount: {
    position: 'absolute', fontSize: 18, fontWeight: '700', color: Colors.gold,
  },
  timerActions: { flexDirection: 'row', gap: 12, marginTop: 16 },

  statsGrid: { flexDirection: 'row', flexWrap: 'wrap', paddingHorizontal: 16, marginTop: 16, gap: 8 },
  statCard: { width: (width - 48) / 2, marginBottom: 0, position: 'relative' },
  statValue: { fontSize: 28, fontWeight: '700', color: Colors.textPrimary },
  statLabel: { fontSize: 12, color: Colors.textSecondary, marginTop: 4 },
  statDot: { width: 6, height: 6, borderRadius: 3, position: 'absolute', top: 16, right: 16 },

  sectionTitle: { fontSize: 18, fontWeight: '600', color: Colors.textPrimary, paddingHorizontal: 20, marginTop: 24, marginBottom: 12 },
  actionsGrid: { flexDirection: 'row', flexWrap: 'wrap', paddingHorizontal: 16, gap: 8 },
  actionBtn: {
    width: (width - 48) / 2, backgroundColor: Colors.card, borderRadius: 16,
    borderWidth: 1, borderColor: Colors.border, padding: 16, alignItems: 'center',
  },
  actionIcon: { fontSize: 28, marginBottom: 8 },
  actionText: { fontSize: 15, fontWeight: '600', color: Colors.textPrimary },
  actionSub: { fontSize: 12, color: Colors.textSecondary, marginTop: 2 },

  statusCard: { marginHorizontal: 16, marginTop: 16 },
  statusRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  statusInfo: { flex: 1 },
  statusTitle: { fontSize: 14, color: Colors.textSecondary },
  statusValue: { fontSize: 18, fontWeight: '700', marginTop: 4 },
  statusDetails: { flexDirection: 'row', gap: 12 },
  statusItem: { alignItems: 'center' },
  statusDot: { width: 8, height: 8, borderRadius: 4, marginBottom: 4 },
  statusItemText: { fontSize: 11, color: Colors.textSecondary },
});
