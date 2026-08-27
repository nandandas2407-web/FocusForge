import React from 'react';
import { View, Text, StyleSheet, ScrollView } from 'react-native';
import { useStore } from '../store/useStore';
import { Colors } from '../theme/colors';
import { GlassCard } from '../components/GlassCard';
import { PremiumHeader } from '../components/PremiumHeader';

export default function StatsScreen() {
  const { streakGoal, focusSessions, tasks, blockedApps } = useStore();

  const todaySessions = focusSessions.filter(
    (s) => new Date(s.completedAt).toDateString() === new Date().toDateString()
  );
  const todayMinutes = todaySessions.reduce((sum, s) => sum + s.durationMinutes, 0);
  const completedTasks = tasks.filter((t) => t.isCompleted).length;
  const blockedCount = blockedApps.filter((a) => a.isFullyBlocked).length;

  const weeklyData = Array.from({ length: 7 }, (_, i) => {
    const d = new Date();
    d.setDate(d.getDate() - (6 - i));
    const dayStr = d.toDateString();
    const sessions = focusSessions.filter((s) => new Date(s.completedAt).toDateString() === dayStr);
    return {
      label: d.toLocaleDateString('en-US', { weekday: 'short' }),
      minutes: sessions.reduce((sum, s) => sum + s.durationMinutes, 0),
    };
  });

  const maxMinutes = Math.max(...weeklyData.map((d) => d.minutes), 1);

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <PremiumHeader title="Stats" subtitle="Your focus journey" />

      {/* Today's Summary */}
      <GlassCard variant="elevated" style={styles.todayCard}>
        <Text style={styles.todayTitle}>Today</Text>
        <View style={styles.todayGrid}>
          <View style={styles.todayStat}>
            <Text style={styles.todayValue}>{todayMinutes}</Text>
            <Text style={styles.todayLabel}>Minutes</Text>
          </View>
          <View style={styles.todayDivider} />
          <View style={styles.todayStat}>
            <Text style={styles.todayValue}>{todaySessions.length}</Text>
            <Text style={styles.todayLabel}>Sessions</Text>
          </View>
          <View style={styles.todayDivider} />
          <View style={styles.todayStat}>
            <Text style={styles.todayValue}>{completedTasks}</Text>
            <Text style={styles.todayLabel}>Tasks</Text>
          </View>
        </View>
      </GlassCard>

      {/* Streak */}
      <GlassCard variant="gold" style={styles.streakCard}>
        <View style={styles.streakRow}>
          <View style={styles.streakFlame}>
            <Text style={styles.flameEmoji}>🔥</Text>
          </View>
          <View style={{ flex: 1 }}>
            <Text style={styles.streakTitle}>Current Streak</Text>
            <Text style={styles.streakValue}>{streakGoal.currentStreakDays} days</Text>
          </View>
          <View style={styles.streakRight}>
            <Text style={styles.bestLabel}>Best</Text>
            <Text style={styles.bestValue}>{streakGoal.bestStreakDays}</Text>
          </View>
        </View>
      </GlassCard>

      {/* Weekly Chart */}
      <GlassCard style={styles.chartCard}>
        <Text style={styles.chartTitle}>This Week</Text>
        <View style={styles.chart}>
          {weeklyData.map((day, i) => (
            <View key={i} style={styles.barContainer}>
              <View style={styles.barTrack}>
                <View
                  style={[
                    styles.bar,
                    {
                      height: `${Math.max((day.minutes / maxMinutes) * 100, 4)}%`,
                      backgroundColor: day.minutes > 0 ? Colors.gold : Colors.surfaceElevated,
                    },
                  ]}
                />
              </View>
              <Text style={styles.barLabel}>{day.label}</Text>
              {day.minutes > 0 && <Text style={styles.barValue}>{day.minutes}m</Text>}
            </View>
          ))}
        </View>
      </GlassCard>

      {/* Lifetime Stats */}
      <Text style={styles.sectionTitle}>All Time</Text>
      <View style={styles.lifetimeGrid}>
        <GlassCard style={styles.lifetimeCard}>
          <Text style={styles.lifetimeValue}>{streakGoal.totalFocusMinutesAllTime}</Text>
          <Text style={styles.lifetimeLabel}>Total Minutes</Text>
        </GlassCard>
        <GlassCard style={styles.lifetimeCard}>
          <Text style={styles.lifetimeValue}>{focusSessions.length}</Text>
          <Text style={styles.lifetimeLabel}>Total Sessions</Text>
        </GlassCard>
        <GlassCard style={styles.lifetimeCard}>
          <Text style={styles.lifetimeValue}>{blockedCount}</Text>
          <Text style={styles.lifetimeLabel}>Apps Blocked</Text>
        </GlassCard>
        <GlassCard style={styles.lifetimeCard}>
          <Text style={styles.lifetimeValue}>{Math.floor(streakGoal.totalFocusMinutesAllTime / 60)}</Text>
          <Text style={styles.lifetimeLabel}>Hours Focused</Text>
        </GlassCard>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.background },
  content: { paddingBottom: 100 },

  todayCard: { marginHorizontal: 16 },
  todayTitle: { fontSize: 14, color: Colors.textSecondary, marginBottom: 12 },
  todayGrid: { flexDirection: 'row', justifyContent: 'space-around' },
  todayStat: { alignItems: 'center' },
  todayValue: { fontSize: 28, fontWeight: '700', color: Colors.gold },
  todayLabel: { fontSize: 12, color: Colors.textSecondary, marginTop: 4 },
  todayDivider: { width: 1, backgroundColor: Colors.border, height: 40 },

  streakCard: { marginHorizontal: 16, marginTop: 12 },
  streakRow: { flexDirection: 'row', alignItems: 'center', gap: 16 },
  streakFlame: { width: 48, height: 48, borderRadius: 24, backgroundColor: Colors.warningBg, alignItems: 'center', justifyContent: 'center' },
  flameEmoji: { fontSize: 24 },
  streakTitle: { fontSize: 13, color: Colors.textSecondary },
  streakValue: { fontSize: 22, fontWeight: '700', color: Colors.textPrimary },
  streakRight: { alignItems: 'center' },
  bestLabel: { fontSize: 11, color: Colors.textMuted },
  bestValue: { fontSize: 18, fontWeight: '700', color: Colors.goldMuted },

  chartCard: { marginHorizontal: 16, marginTop: 12 },
  chartTitle: { fontSize: 14, fontWeight: '600', color: Colors.textPrimary, marginBottom: 16 },
  chart: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-end', height: 120 },
  barContainer: { alignItems: 'center', flex: 1 },
  barTrack: { height: 80, width: 20, justifyContent: 'flex-end', backgroundColor: Colors.surfaceElevated, borderRadius: 4 },
  bar: { width: 20, borderRadius: 4, minHeight: 4 },
  barLabel: { fontSize: 10, color: Colors.textMuted, marginTop: 6 },
  barValue: { fontSize: 9, color: Colors.goldMuted, marginTop: 2 },

  sectionTitle: { fontSize: 18, fontWeight: '600', color: Colors.textPrimary, paddingHorizontal: 20, marginTop: 24, marginBottom: 12 },
  lifetimeGrid: { flexDirection: 'row', flexWrap: 'wrap', paddingHorizontal: 16, gap: 8 },
  lifetimeCard: { width: '48%', flex: 0 },
  lifetimeValue: { fontSize: 24, fontWeight: '700', color: Colors.textPrimary },
  lifetimeLabel: { fontSize: 12, color: Colors.textSecondary, marginTop: 4 },
});
