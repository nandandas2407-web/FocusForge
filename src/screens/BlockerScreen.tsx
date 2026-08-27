import React, { useState } from 'react';
import { View, Text, StyleSheet, ScrollView, TouchableOpacity, Switch, TextInput, Alert } from 'react-native';
import { useStore, BlockedApp } from '../store/useStore';
import { Colors } from '../theme/colors';
import { GlassCard } from '../components/GlassCard';
import { PremiumButton } from '../components/PremiumButton';
import { PremiumHeader } from '../components/PremiumHeader';

const CATEGORIES = ['All', 'Social', 'Video', 'Games', 'Shopping'];

export default function BlockerScreen() {
  const {
    blockedApps, globalBlockerEnabled,
    toggleAppBlocked, toggleReelsBlocked, toggleShortsBlocked,
    setGlobalBlocker,
    websiteBlocks, addWebsiteBlock, removeWebsiteBlock,
  } = useStore();

  const [selectedCategory, setSelectedCategory] = useState('All');
  const [newDomain, setNewDomain] = useState('');
  const [showWebsites, setShowWebsites] = useState(false);

  const filteredApps = selectedCategory === 'All'
    ? blockedApps
    : blockedApps.filter((a) => a.category === selectedCategory);

  const handleAddDomain = () => {
    const domain = newDomain.trim().toLowerCase();
    if (!domain) return;
    if (websiteBlocks.some((b) => b.domain === domain)) {
      Alert.alert('Already blocked', `${domain} is already in the block list.`);
      return;
    }
    addWebsiteBlock({ domain, category: 'Custom' });
    setNewDomain('');
  };

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <PremiumHeader
        title="Blocker"
        subtitle="Control what distracts you"
        right={
          <View style={styles.masterToggle}>
            <Text style={[styles.masterLabel, { color: globalBlockerEnabled ? Colors.success : Colors.danger }]}>
              {globalBlockerEnabled ? 'ON' : 'OFF'}
            </Text>
            <Switch
              value={globalBlockerEnabled}
              onValueChange={setGlobalBlocker}
              trackColor={{ false: Colors.surfaceElevated, true: Colors.goldGlow }}
              thumbColor={globalBlockerEnabled ? Colors.gold : Colors.textMuted}
            />
          </View>
        }
      />

      {/* Tab Toggle */}
      <View style={styles.tabRow}>
        <TouchableOpacity
          style={[styles.tab, !showWebsites && styles.tabActive]}
          onPress={() => setShowWebsites(false)}
        >
          <Text style={[styles.tabText, !showWebsites && styles.tabTextActive]}>Apps</Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={[styles.tab, showWebsites && styles.tabActive]}
          onPress={() => setShowWebsites(true)}
        >
          <Text style={[styles.tabText, showWebsites && styles.tabTextActive]}>Websites</Text>
        </TouchableOpacity>
      </View>

      {!showWebsites ? (
        <>
          {/* Category Filter */}
          <ScrollView horizontal showsHorizontalScrollIndicator={false} style={styles.categoryScroll}>
            {CATEGORIES.map((cat) => (
              <TouchableOpacity
                key={cat}
                style={[styles.categoryChip, selectedCategory === cat && styles.categoryChipActive]}
                onPress={() => setSelectedCategory(cat)}
              >
                <Text style={[styles.categoryText, selectedCategory === cat && styles.categoryTextActive]}>
                  {cat}
                </Text>
              </TouchableOpacity>
            ))}
          </ScrollView>

          {/* Global Block All */}
          <GlassCard variant="gold" style={styles.globalBlockCard}>
            <View style={styles.globalBlockRow}>
              <View style={{ flex: 1 }}>
                <Text style={styles.globalBlockTitle}>Block All Social Media</Text>
                <Text style={styles.globalBlockSub}>Blocks Instagram, TikTok, Twitter, Reddit entirely</Text>
              </View>
              <PremiumButton
                title="Block All"
                onPress={() => {
                  blockedApps.forEach((app) => {
                    if (['Social', 'Video'].includes(app.category) && !app.isFullyBlocked) {
                      toggleAppBlocked(app.packageName);
                    }
                  });
                }}
                variant="danger"
                size="sm"
              />
            </View>
          </GlassCard>

          {/* App List */}
          {filteredApps.map((app) => (
            <AppBlockItem key={app.packageName} app={app} />
          ))}
        </>
      ) : (
        <>
          {/* Website Blocker */}
          <GlassCard style={styles.addDomainCard}>
            <Text style={styles.addDomainTitle}>Add Website to Block</Text>
            <View style={styles.domainInputRow}>
              <TextInput
                style={styles.domainInput}
                placeholder="e.g. facebook.com"
                placeholderTextColor={Colors.textMuted}
                value={newDomain}
                onChangeText={setNewDomain}
                autoCapitalize="none"
                autoCorrect={false}
              />
              <PremiumButton title="Add" onPress={handleAddDomain} size="sm" />
            </View>
          </GlassCard>

          {websiteBlocks.map((wb) => (
            <GlassCard key={wb.domain} style={styles.websiteItem}>
              <View style={styles.websiteRow}>
                <View style={{ flex: 1 }}>
                  <Text style={styles.websiteDomain}>{wb.domain}</Text>
                  <Text style={styles.websiteCategory}>{wb.category}</Text>
                </View>
                <TouchableOpacity onPress={() => removeWebsiteBlock(wb.domain)}>
                  <Text style={styles.removeBtn}>Remove</Text>
                </TouchableOpacity>
              </View>
            </GlassCard>
          ))}
        </>
      )}
    </ScrollView>
  );
}

function AppBlockItem({ app }: { app: BlockedApp }) {
  const { toggleAppBlocked, toggleReelsBlocked, toggleShortsBlocked } = useStore();
  const isYouTube = app.packageName === 'com.google.android.youtube';
  const isInstagram = app.packageName === 'com.instagram.android';

  return (
    <GlassCard>
      <View style={styles.appRow}>
        <View style={styles.appInfo}>
          <Text style={styles.appName}>{app.appName}</Text>
          <Text style={styles.appCategory}>{app.category}</Text>
        </View>
        <View style={styles.appToggles}>
          {/* Main block toggle */}
          <View style={styles.toggleGroup}>
            <Text style={styles.toggleLabel}>Block</Text>
            <Switch
              value={app.isFullyBlocked}
              onValueChange={() => toggleAppBlocked(app.packageName)}
              trackColor={{ false: Colors.surfaceElevated, true: Colors.dangerBg }}
              thumbColor={app.isFullyBlocked ? Colors.danger : Colors.textMuted}
            />
          </View>

          {/* Instagram Reels */}
          {isInstagram && (
            <View style={styles.toggleGroup}>
              <Text style={[styles.toggleLabel, { color: Colors.warning }]}>Reels</Text>
              <Switch
                value={app.isReelsBlocked}
                onValueChange={() => toggleReelsBlocked(app.packageName)}
                trackColor={{ false: Colors.surfaceElevated, true: Colors.warningBg }}
                thumbColor={app.isReelsBlocked ? Colors.warning : Colors.textMuted}
              />
            </View>
          )}

          {/* YouTube Shorts */}
          {isYouTube && (
            <View style={styles.toggleGroup}>
              <Text style={[styles.toggleLabel, { color: Colors.info }]}>Shorts</Text>
              <Switch
                value={app.isShortsBlocked}
                onValueChange={() => toggleShortsBlocked(app.packageName)}
                trackColor={{ false: Colors.surfaceElevated, true: Colors.infoBg }}
                thumbColor={app.isShortsBlocked ? Colors.info : Colors.textMuted}
              />
            </View>
          )}
        </View>
      </View>
    </GlassCard>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: Colors.background },
  content: { paddingBottom: 100 },

  masterToggle: { flexDirection: 'row', alignItems: 'center', gap: 8 },
  masterLabel: { fontSize: 13, fontWeight: '600' },

  tabRow: {
    flexDirection: 'row', marginHorizontal: 16, backgroundColor: Colors.surface,
    borderRadius: 12, padding: 4, marginBottom: 12,
  },
  tab: { flex: 1, paddingVertical: 10, alignItems: 'center', borderRadius: 10 },
  tabActive: { backgroundColor: Colors.card },
  tabText: { fontSize: 14, color: Colors.textSecondary, fontWeight: '500' },
  tabTextActive: { color: Colors.gold },

  categoryScroll: { paddingHorizontal: 16, marginBottom: 12 },
  categoryChip: {
    paddingHorizontal: 16, paddingVertical: 8, borderRadius: 20,
    backgroundColor: Colors.surfaceElevated, marginRight: 8,
  },
  categoryChipActive: { backgroundColor: Colors.goldSubtle, borderWidth: 1, borderColor: Colors.borderGold },
  categoryText: { fontSize: 13, color: Colors.textSecondary },
  categoryTextActive: { color: Colors.gold },

  globalBlockCard: { marginHorizontal: 16, marginBottom: 12 },
  globalBlockRow: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' },
  globalBlockTitle: { fontSize: 15, fontWeight: '600', color: Colors.textPrimary },
  globalBlockSub: { fontSize: 12, color: Colors.textSecondary, marginTop: 2 },

  appRow: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' },
  appInfo: { flex: 1 },
  appName: { fontSize: 15, fontWeight: '600', color: Colors.textPrimary },
  appCategory: { fontSize: 12, color: Colors.textSecondary, marginTop: 2 },
  appToggles: { flexDirection: 'row', gap: 12 },
  toggleGroup: { alignItems: 'center' },
  toggleLabel: { fontSize: 10, color: Colors.textMuted, marginBottom: 4, textTransform: 'uppercase', letterSpacing: 0.5 },

  addDomainCard: { marginHorizontal: 16, marginBottom: 12 },
  addDomainTitle: { fontSize: 15, fontWeight: '600', color: Colors.textPrimary, marginBottom: 12 },
  domainInputRow: { flexDirection: 'row', gap: 8 },
  domainInput: {
    flex: 1, backgroundColor: Colors.surfaceElevated, borderRadius: 10,
    paddingHorizontal: 14, paddingVertical: 10, color: Colors.textPrimary, fontSize: 14,
    borderWidth: 1, borderColor: Colors.border,
  },

  websiteItem: { marginHorizontal: 16 },
  websiteRow: { flexDirection: 'row', alignItems: 'center' },
  websiteDomain: { fontSize: 15, fontWeight: '500', color: Colors.textPrimary },
  websiteCategory: { fontSize: 12, color: Colors.textSecondary, marginTop: 2 },
  removeBtn: { fontSize: 13, color: Colors.danger, fontWeight: '600' },
});
