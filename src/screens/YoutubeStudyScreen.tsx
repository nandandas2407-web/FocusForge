import React, { useState } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, Switch, TextInput, Alert } from 'react-native';
import { useStore } from '../store/useStore';
import { Colors } from '../theme/colors';
import { GlassCard } from '../components/GlassCard';
import { PremiumButton } from '../components/PremiumButton';
import { PremiumHeader } from '../components/PremiumHeader';

export default function YoutubeStudyScreen() {
  const {
    youtubeStudyMode, setYoutubeStudyMode,
    youtubeWhitelist, addYoutubeChannel, removeYoutubeChannel,
  } = useStore();

  const [newChannelTitle, setNewChannelTitle] = useState('');
  const [newChannelId, setNewChannelId] = useState('');
  const [showAddForm, setShowAddForm] = useState(false);

  const handleAddChannel = () => {
    const title = newChannelTitle.trim();
    const id = newChannelId.trim();
    if (!title || !id) {
      Alert.alert('Missing info', 'Both channel name and channel ID are required.');
      return;
    }
    if (youtubeWhitelist.some((c) => c.channelId === id)) {
      Alert.alert('Already exists', 'This channel is already in your whitelist.');
      return;
    }
    addYoutubeChannel({ channelId: id, channelTitle: title, category: 'Education' });
    setNewChannelTitle('');
    setNewChannelId('');
    setShowAddForm(false);
  };

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <PremiumHeader
        title="YouTube Study"
        subtitle="Only watch what matters"
      />

      {/* Master Toggle */}
      <GlassCard variant={youtubeStudyMode ? 'gold' : 'default'}>
        <View style={styles.toggleRow}>
          <View style={{ flex: 1 }}>
            <Text style={styles.toggleTitle}>Study Mode</Text>
            <Text style={styles.toggleSub}>
              {youtubeStudyMode
                ? 'Only whitelisted channels are allowed'
                : 'All YouTube content is accessible'}
            </Text>
          </View>
          <Switch
            value={youtubeStudyMode}
            onValueChange={setYoutubeStudyMode}
            trackColor={{ false: Colors.surfaceElevated, true: Colors.goldGlow }}
            thumbColor={youtubeStudyMode ? Colors.gold : Colors.textMuted}
          />
        </View>
      </GlassCard>

      {/* How It Works */}
      <GlassCard style={styles.infoCard}>
        <Text style={styles.infoTitle}>How Study Mode Works</Text>
        <View style={styles.infoStep}>
          <View style={[styles.stepDot, { backgroundColor: Colors.gold }]} />
          <Text style={styles.infoText}>Opens YouTube → builds a screen snapshot from accessibility tree</Text>
        </View>
        <View style={styles.infoStep}>
          <View style={[styles.stepDot, { backgroundColor: Colors.gold }]} />
          <Text style={styles.infoText}>Detects screen type: Watch, Shorts, Home, Search, Channel</Text>
        </View>
        <View style={styles.infoStep}>
          <View style={[styles.stepDot, { backgroundColor: Colors.gold }]} />
          <Text style={styles.infoText}>On Watch page: extracts channel name/handle/ID</Text>
        </View>
        <View style={styles.infoStep}>
          <View style={[styles.stepDot, { backgroundColor: Colors.success }]} />
          <Text style={styles.infoText}>Matches against your whitelist → allows or blocks</Text>
        </View>
        <View style={styles.infoStep}>
          <View style={[styles.stepDot, { backgroundColor: Colors.danger }]} />
          <Text style={styles.infoText}>Fail-closed: unknown channels are blocked by default</Text>
        </View>
      </GlassCard>

      {/* Whitelisted Channels */}
      <View style={styles.sectionHeader}>
        <Text style={styles.sectionTitle}>Whitelisted Channels</Text>
        <TouchableOpacity onPress={() => setShowAddForm(!showAddForm)}>
          <Text style={styles.addBtn}>{showAddForm ? 'Cancel' : '+ Add'}</Text>
        </TouchableOpacity>
      </View>

      {showAddForm && (
        <GlassCard variant="gold" style={styles.addForm}>
          <Text style={styles.addFormTitle}>Add Channel to Whitelist</Text>
          <TextInput
            style={styles.input}
            placeholder="Channel name (e.g. MIT OpenCourseWare)"
            placeholderTextColor={Colors.textMuted}
            value={newChannelTitle}
            onChangeText={setNewChannelTitle}
          />
          <TextInput
            style={styles.input}
            placeholder="Channel ID (e.g. UC_x5XG1OV2P6uZZ5FSM9Ttw)"
            placeholderTextColor={Colors.textMuted}
            value={newChannelId}
            onChangeText={setNewChannelId}
            autoCapitalize="none"
          />
          <Text style={styles.hint}>
            Find your channel ID at youtube.com/account_advanced
          </Text>
          <PremiumButton title="Add Channel" onPress={handleAddChannel} size="sm" />
        </GlassCard>
      )}

      {youtubeWhitelist.map((ch) => (
        <GlassCard key={ch.channelId}>
          <View style={styles.channelRow}>
            <View style={styles.channelIcon}>
              <Text style={styles.channelIconText}>{ch.channelTitle.charAt(0)}</Text>
            </View>
            <View style={{ flex: 1 }}>
              <Text style={styles.channelName}>{ch.channelTitle}</Text>
              <Text style={styles.channelId}>{ch.channelId}</Text>
            </View>
            <TouchableOpacity onPress={() => removeYoutubeChannel(ch.channelId)}>
              <Text style={styles.removeBtn}>Remove</Text>
            </TouchableOpacity>
          </View>
        </GlassCard>
      ))}

      {youtubeWhitelist.length === 0 && (
        <GlassCard>
          <Text style={styles.emptyText}>
            No channels whitelisted. Add channels to allow them in Study Mode.
          </Text>
        </GlassCard>
      )}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.background },
  content: { paddingBottom: 100 },

  toggleRow: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' },
  toggleTitle: { fontSize: 16, fontWeight: '600', color: Colors.textPrimary },
  toggleSub: { fontSize: 13, color: Colors.textSecondary, marginTop: 2 },

  infoCard: { marginHorizontal: 16, marginBottom: 16 },
  infoTitle: { fontSize: 14, fontWeight: '600', color: Colors.gold, marginBottom: 12 },
  infoStep: { flexDirection: 'row', alignItems: 'center', marginBottom: 8, gap: 10 },
  stepDot: { width: 6, height: 6, borderRadius: 3 },
  infoText: { fontSize: 13, color: Colors.textSecondary, flex: 1 },

  sectionHeader: {
    flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center',
    paddingHorizontal: 20, marginBottom: 12,
  },
  sectionTitle: { fontSize: 18, fontWeight: '600', color: Colors.textPrimary },
  addBtn: { fontSize: 14, color: Colors.gold, fontWeight: '600' },

  addForm: { marginHorizontal: 16, marginBottom: 12 },
  addFormTitle: { fontSize: 14, fontWeight: '600', color: Colors.gold, marginBottom: 12 },
  input: {
    backgroundColor: Colors.surfaceElevated, borderRadius: 10,
    paddingHorizontal: 14, paddingVertical: 10, color: Colors.textPrimary, fontSize: 14,
    borderWidth: 1, borderColor: Colors.border, marginBottom: 8,
  },
  hint: { fontSize: 11, color: Colors.textMuted, marginBottom: 12 },

  channelRow: { flexDirection: 'row', alignItems: 'center', gap: 12 },
  channelIcon: {
    width: 40, height: 40, borderRadius: 20, backgroundColor: Colors.goldSubtle,
    alignItems: 'center', justifyContent: 'center',
  },
  channelIconText: { fontSize: 18, fontWeight: '700', color: Colors.gold },
  channelName: { fontSize: 15, fontWeight: '600', color: Colors.textPrimary },
  channelId: { fontSize: 12, color: Colors.textMuted, marginTop: 2 },
  removeBtn: { fontSize: 13, color: Colors.danger, fontWeight: '600' },

  emptyText: { fontSize: 14, color: Colors.textSecondary, textAlign: 'center', padding: 16 },
});
