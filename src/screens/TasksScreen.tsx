import React, { useState } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, TextInput, Switch, Alert } from 'react-native';
import { useStore, Task } from '../store/useStore';
import { Colors } from '../theme/colors';
import { GlassCard } from '../components/GlassCard';
import { PremiumButton } from '../components/PremiumButton';
import { PremiumHeader } from '../components/PremiumHeader';

const PRIORITIES = ['LOW', 'MEDIUM', 'HIGH'] as const;
const CATEGORIES = ['Study', 'Math', 'Physics', 'Chemistry', 'Code', 'Work', 'Personal'];

const priorityColors: Record<string, string> = {
  LOW: Colors.success,
  MEDIUM: Colors.warning,
  HIGH: Colors.danger,
};

export default function TasksScreen() {
  const { tasks, addTask, toggleTask, deleteTask } = useStore();
  const [showForm, setShowForm] = useState(false);
  const [title, setTitle] = useState('');
  const [notes, setNotes] = useState('');
  const [priority, setPriority] = useState<Task['priority']>('MEDIUM');
  const [category, setCategory] = useState('Study');
  const [estMinutes, setEstMinutes] = useState('25');

  const incompleteTasks = tasks.filter((t) => !t.isCompleted);
  const completedTasks = tasks.filter((t) => t.isCompleted);

  const handleAdd = () => {
    if (!title.trim()) {
      Alert.alert('Missing title', 'Please enter a task title.');
      return;
    }
    addTask({
      title: title.trim(),
      notes: notes.trim(),
      priority,
      category,
      estimatedMinutes: parseInt(estMinutes) || 25,
      dueDate: new Date(Date.now() + 86400000).toISOString(),
    });
    setTitle(''); setNotes(''); setPriority('MEDIUM'); setCategory('Study'); setEstMinutes('25');
    setShowForm(false);
  };

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <PremiumHeader
        title="Tasks"
        subtitle={`${incompleteTasks.length} remaining`}
        right={
          <TouchableOpacity onPress={() => setShowForm(!showForm)}>
            <Text style={styles.addBtn}>{showForm ? 'Cancel' : '+ New'}</Text>
          </TouchableOpacity>
        }
      />

      {showForm && (
        <GlassCard variant="gold" style={styles.formCard}>
          <TextInput
            style={styles.input}
            placeholder="Task title"
            placeholderTextColor={Colors.textMuted}
            value={title}
            onChangeText={setTitle}
          />
          <TextInput
            style={[styles.input, styles.notesInput]}
            placeholder="Notes (optional)"
            placeholderTextColor={Colors.textMuted}
            value={notes}
            onChangeText={setNotes}
            multiline
          />

          <Text style={styles.formLabel}>Priority</Text>
          <View style={styles.chipRow}>
            {PRIORITIES.map((p) => (
              <TouchableOpacity
                key={p}
                style={[styles.chip, priority === p && { backgroundColor: priorityColors[p] + '20', borderColor: priorityColors[p] }]}
                onPress={() => setPriority(p)}
              >
                <Text style={[styles.chipText, priority === p && { color: priorityColors[p] }]}>{p}</Text>
              </TouchableOpacity>
            ))}
          </View>

          <Text style={styles.formLabel}>Category</Text>
          <View style={styles.chipRow}>
            {CATEGORIES.map((c) => (
              <TouchableOpacity
                key={c}
                style={[styles.chip, category === c && styles.chipActive]}
                onPress={() => setCategory(c)}
              >
                <Text style={[styles.chipText, category === c && styles.chipTextActive]}>{c}</Text>
              </TouchableOpacity>
            ))}
          </View>

          <Text style={styles.formLabel}>Estimated Minutes</Text>
          <TextInput
            style={styles.input}
            placeholder="25"
            placeholderTextColor={Colors.textMuted}
            value={estMinutes}
            onChangeText={setEstMinutes}
            keyboardType="numeric"
          />

          <PremiumButton title="Add Task" onPress={handleAdd} size="md" />
        </GlassCard>
      )}

      {/* Incomplete Tasks */}
      {incompleteTasks.map((task) => (
        <GlassCard key={task.id}>
          <View style={styles.taskRow}>
            <TouchableOpacity onPress={() => toggleTask(task.id)} style={styles.checkbox}>
              <View style={[styles.checkboxInner, { borderColor: priorityColors[task.priority] }]} />
            </TouchableOpacity>
            <View style={{ flex: 1 }}>
              <Text style={styles.taskTitle}>{task.title}</Text>
              <View style={styles.taskMeta}>
                <Text style={[styles.taskPriority, { color: priorityColors[task.priority] }]}>{task.priority}</Text>
                <Text style={styles.taskCategory}>{task.category}</Text>
                <Text style={styles.taskEst}>{task.estimatedMinutes}m</Text>
              </View>
            </View>
            <TouchableOpacity onPress={() => deleteTask(task.id)}>
              <Text style={styles.deleteBtn}>✕</Text>
            </TouchableOpacity>
          </View>
        </GlassCard>
      ))}

      {/* Completed */}
      {completedTasks.length > 0 && (
        <>
          <Text style={styles.sectionTitle}>Completed ({completedTasks.length})</Text>
          {completedTasks.map((task) => (
            <GlassCard key={task.id} style={{ opacity: 0.5 }}>
              <View style={styles.taskRow}>
                <TouchableOpacity onPress={() => toggleTask(task.id)} style={styles.checkbox}>
                  <View style={[styles.checkboxInner, styles.checkboxDone]}>
                    <Text style={styles.checkmark}>✓</Text>
                  </View>
                </TouchableOpacity>
                <View style={{ flex: 1 }}>
                  <Text style={[styles.taskTitle, styles.taskDone]}>{task.title}</Text>
                </View>
                <TouchableOpacity onPress={() => deleteTask(task.id)}>
                  <Text style={styles.deleteBtn}>✕</Text>
                </TouchableOpacity>
              </View>
            </GlassCard>
          ))}
        </>
      )}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.background },
  content: { paddingBottom: 100 },
  addBtn: { fontSize: 14, color: Colors.gold, fontWeight: '600' },

  formCard: { marginHorizontal: 16, marginBottom: 12 },
  input: {
    backgroundColor: Colors.surfaceElevated, borderRadius: 10,
    paddingHorizontal: 14, paddingVertical: 10, color: Colors.textPrimary, fontSize: 14,
    borderWidth: 1, borderColor: Colors.border, marginBottom: 10,
  },
  notesInput: { height: 60, textAlignVertical: 'top' },
  formLabel: { fontSize: 12, color: Colors.textSecondary, marginBottom: 6, textTransform: 'uppercase', letterSpacing: 0.5 },
  chipRow: { flexDirection: 'row', flexWrap: 'wrap', gap: 6, marginBottom: 12 },
  chip: {
    paddingHorizontal: 12, paddingVertical: 6, borderRadius: 8,
    backgroundColor: Colors.surfaceElevated, borderWidth: 1, borderColor: Colors.border,
  },
  chipActive: { backgroundColor: Colors.goldSubtle, borderColor: Colors.borderGold },
  chipText: { fontSize: 12, color: Colors.textSecondary },
  chipTextActive: { color: Colors.gold },

  taskRow: { flexDirection: 'row', alignItems: 'center', gap: 12 },
  checkbox: { width: 24, height: 24, borderRadius: 6, borderWidth: 2, borderColor: Colors.gold, alignItems: 'center', justifyContent: 'center' },
  checkboxInner: {},
  checkboxDone: { backgroundColor: Colors.gold },
  checkmark: { fontSize: 14, color: Colors.background, fontWeight: '700' },
  taskTitle: { fontSize: 15, fontWeight: '600', color: Colors.textPrimary },
  taskDone: { textDecorationLine: 'line-through', color: Colors.textMuted },
  taskMeta: { flexDirection: 'row', gap: 8, marginTop: 4 },
  taskPriority: { fontSize: 11, fontWeight: '600', textTransform: 'uppercase' },
  taskCategory: { fontSize: 11, color: Colors.textMuted },
  taskEst: { fontSize: 11, color: Colors.textMuted },
  deleteBtn: { fontSize: 16, color: Colors.textMuted, padding: 4 },

  sectionTitle: { fontSize: 16, fontWeight: '600', color: Colors.textSecondary, paddingHorizontal: 20, marginTop: 20, marginBottom: 12 },
});
