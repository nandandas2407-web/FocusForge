import React, { useState } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, TextInput, Alert } from 'react-native';
import { useStore, CalendarEvent } from '../store/useStore';
import { Colors } from '../theme/colors';
import { GlassCard } from '../components/GlassCard';
import { PremiumButton } from '../components/PremiumButton';
import { PremiumHeader } from '../components/PremiumHeader';

const EVENT_TYPES = ['STUDY_BLOCK', 'EXAM', 'CLASS', 'DEADLINE'] as const;

const eventTypeColors: Record<string, string> = {
  STUDY_BLOCK: Colors.gold,
  EXAM: Colors.danger,
  CLASS: Colors.info,
  DEADLINE: Colors.warning,
};

const eventTypeLabels: Record<string, string> = {
  STUDY_BLOCK: 'Study',
  EXAM: 'Exam',
  CLASS: 'Class',
  DEADLINE: 'Deadline',
};

function formatDate(ms: number): string {
  return new Date(ms).toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
}

function formatTime(ms: number): string {
  return new Date(ms).toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
}

function daysUntil(ms: number): number {
  return Math.max(0, Math.ceil((ms - Date.now()) / 86400000));
}

export default function CalendarScreen() {
  const { calendarEvents, addEvent, deleteEvent } = useStore();
  const [showForm, setShowForm] = useState(false);
  const [title, setTitle] = useState('');
  const [eventType, setEventType] = useState<CalendarEvent['eventType']>('STUDY_BLOCK');
  const [dateStr, setDateStr] = useState(new Date(Date.now() + 86400000).toISOString().split('T')[0]);
  const [isExam, setIsExam] = useState(false);

  const sortedEvents = [...calendarEvents].sort((a, b) => a.startTime - b.startTime);
  const upcomingEvents = sortedEvents.filter((e) => e.startTime > Date.now());
  const examEvents = upcomingEvents.filter((e) => e.isExamCountdown);

  const handleAdd = () => {
    if (!title.trim()) { Alert.alert('Missing title', 'Enter an event title.'); return; }
    const [year, month, day] = dateStr.split('-').map(Number);
    const start = new Date(year, month - 1, day, 9, 0).getTime();
    const end = start + 3600000;
    addEvent({
      title: title.trim(),
      eventType,
      startTime: start,
      endTime: end,
      dateString: dateStr,
      notes: '',
      isExamCountdown: isExam,
    });
    setTitle(''); setShowForm(false);
  };

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <PremiumHeader
        title="Calendar"
        subtitle={`${upcomingEvents.length} upcoming events`}
        right={
          <TouchableOpacity onPress={() => setShowForm(!showForm)}>
            <Text style={styles.addBtn}>{showForm ? 'Cancel' : '+ New'}</Text>
          </TouchableOpacity>
        }
      />

      {/* Exam Countdowns */}
      {examEvents.length > 0 && (
        <>
          <Text style={styles.sectionTitle}>Exam Countdowns</Text>
          {examEvents.map((event) => {
            const days = daysUntil(event.startTime);
            return (
              <GlassCard key={event.id} variant="gold" style={styles.countdownCard}>
                <View style={styles.countdownRow}>
                  <View style={styles.countdownLeft}>
                    <Text style={styles.countdownTitle}>{event.title}</Text>
                    <Text style={styles.countdownDate}>{formatDate(event.startTime)}</Text>
                  </View>
                  <View style={styles.countdownBadge}>
                    <Text style={styles.countdownDays}>{days}</Text>
                    <Text style={styles.countdownLabel}>days</Text>
                  </View>
                </View>
              </GlassCard>
            );
          })}
        </>
      )}

      {/* Add Form */}
      {showForm && (
        <GlassCard variant="gold" style={styles.formCard}>
          <TextInput
            style={styles.input}
            placeholder="Event title"
            placeholderTextColor={Colors.textMuted}
            value={title}
            onChangeText={setTitle}
          />
          <TextInput
            style={styles.input}
            placeholder="Date (YYYY-MM-DD)"
            placeholderTextColor={Colors.textMuted}
            value={dateStr}
            onChangeText={setDateStr}
          />

          <Text style={styles.formLabel}>Type</Text>
          <View style={styles.chipRow}>
            {EVENT_TYPES.map((t) => (
              <TouchableOpacity
                key={t}
                style={[styles.chip, eventType === t && { backgroundColor: eventTypeColors[t] + '20', borderColor: eventTypeColors[t] }]}
                onPress={() => setEventType(t)}
              >
                <Text style={[styles.chipText, eventType === t && { color: eventTypeColors[t] }]}>
                  {eventTypeLabels[t]}
                </Text>
              </TouchableOpacity>
            ))}
          </View>

          <View style={styles.examToggle}>
            <Text style={styles.examLabel}>Countdown</Text>
            <TouchableOpacity
              style={[styles.examBtn, isExam && styles.examBtnActive]}
              onPress={() => setIsExam(!isExam)}
            >
              <Text style={[styles.examBtnText, isExam && styles.examBtnTextActive]}>
                {isExam ? 'ON' : 'OFF'}
              </Text>
            </TouchableOpacity>
          </View>

          <PremiumButton title="Add Event" onPress={handleAdd} size="md" />
        </GlassCard>
      )}

      {/* Events List */}
      <Text style={styles.sectionTitle}>Upcoming</Text>
      {sortedEvents.map((event) => {
        const color = eventTypeColors[event.eventType] || Colors.gold;
        const days = daysUntil(event.startTime);
        return (
          <GlassCard key={event.id}>
            <View style={styles.eventRow}>
              <View style={[styles.eventTypeBar, { backgroundColor: color }]} />
              <View style={{ flex: 1 }}>
                <Text style={styles.eventTitle}>{event.title}</Text>
                <View style={styles.eventMeta}>
                  <Text style={[styles.eventType, { color }]}>{eventTypeLabels[event.eventType]}</Text>
                  <Text style={styles.eventDate}>{formatDate(event.startTime)} at {formatTime(event.startTime)}</Text>
                </View>
              </View>
              {days > 0 && (
                <View style={styles.daysBadge}>
                  <Text style={styles.daysText}>{days}d</Text>
                </View>
              )}
              <TouchableOpacity onPress={() => deleteEvent(event.id)}>
                <Text style={styles.deleteBtn}>✕</Text>
              </TouchableOpacity>
            </View>
          </GlassCard>
        );
      })}

      {sortedEvents.length === 0 && (
        <GlassCard>
          <Text style={styles.emptyText}>No upcoming events. Add one to get started.</Text>
        </GlassCard>
      )}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.background },
  content: { paddingBottom: 100 },
  addBtn: { fontSize: 14, color: Colors.gold, fontWeight: '600' },

  sectionTitle: { fontSize: 18, fontWeight: '600', color: Colors.textPrimary, paddingHorizontal: 20, marginTop: 20, marginBottom: 12 },

  countdownCard: { marginHorizontal: 16 },
  countdownRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  countdownLeft: { flex: 1 },
  countdownTitle: { fontSize: 16, fontWeight: '600', color: Colors.textPrimary },
  countdownDate: { fontSize: 13, color: Colors.textSecondary, marginTop: 2 },
  countdownBadge: { alignItems: 'center' },
  countdownDays: { fontSize: 32, fontWeight: '700', color: Colors.gold },
  countdownLabel: { fontSize: 11, color: Colors.goldMuted, textTransform: 'uppercase' },

  formCard: { marginHorizontal: 16, marginBottom: 12 },
  input: {
    backgroundColor: Colors.surfaceElevated, borderRadius: 10,
    paddingHorizontal: 14, paddingVertical: 10, color: Colors.textPrimary, fontSize: 14,
    borderWidth: 1, borderColor: Colors.border, marginBottom: 10,
  },
  formLabel: { fontSize: 12, color: Colors.textSecondary, marginBottom: 6, textTransform: 'uppercase', letterSpacing: 0.5 },
  chipRow: { flexDirection: 'row', flexWrap: 'wrap', gap: 6, marginBottom: 12 },
  chip: {
    paddingHorizontal: 12, paddingVertical: 6, borderRadius: 8,
    backgroundColor: Colors.surfaceElevated, borderWidth: 1, borderColor: Colors.border,
  },
  chipText: { fontSize: 12, color: Colors.textSecondary },

  examToggle: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 },
  examLabel: { fontSize: 14, color: Colors.textPrimary },
  examBtn: { paddingHorizontal: 16, paddingVertical: 6, borderRadius: 8, backgroundColor: Colors.surfaceElevated },
  examBtnActive: { backgroundColor: Colors.goldSubtle },
  examBtnText: { fontSize: 13, color: Colors.textSecondary },
  examBtnTextActive: { color: Colors.gold, fontWeight: '600' },

  eventRow: { flexDirection: 'row', alignItems: 'center', gap: 12 },
  eventTypeBar: { width: 4, height: 40, borderRadius: 2 },
  eventTitle: { fontSize: 15, fontWeight: '600', color: Colors.textPrimary },
  eventMeta: { flexDirection: 'row', gap: 8, marginTop: 2 },
  eventType: { fontSize: 12, fontWeight: '600' },
  eventDate: { fontSize: 12, color: Colors.textSecondary },
  daysBadge: {
    paddingHorizontal: 8, paddingVertical: 4, borderRadius: 8,
    backgroundColor: Colors.goldSubtle,
  },
  daysText: { fontSize: 12, fontWeight: '600', color: Colors.gold },
  deleteBtn: { fontSize: 16, color: Colors.textMuted, padding: 4 },
  emptyText: { fontSize: 14, color: Colors.textSecondary, textAlign: 'center', padding: 16 },
});
