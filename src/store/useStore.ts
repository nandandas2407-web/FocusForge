import { create } from 'zustand';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { useEffect } from 'react';

// ── Types ──

export interface BlockedApp {
  packageName: string;
  appName: string;
  category: string;
  isFullyBlocked: boolean;
  isReelsBlocked: boolean;
  isShortsBlocked: boolean;
  icon?: string;
}

export interface YoutubeChannel {
  channelId: string;
  channelTitle: string;
  category: string;
}

export interface WebsiteBlock {
  domain: string;
  category: string;
}

export interface Task {
  id: string;
  title: string;
  notes: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH';
  category: string;
  isCompleted: boolean;
  estimatedMinutes: number;
  dueDate: string | null;
  createdAt: number;
}

export interface CalendarEvent {
  id: string;
  title: string;
  eventType: 'STUDY_BLOCK' | 'EXAM' | 'CLASS' | 'DEADLINE';
  startTime: number;
  endTime: number;
  dateString: string;
  notes: string;
  isExamCountdown: boolean;
}

export interface FocusSession {
  id: string;
  title: string;
  durationMinutes: number;
  completedAt: number;
  isCompleted: boolean;
}

export interface StreakGoal {
  dailyScreenTimeGoalMinutes: number;
  currentStreakDays: number;
  bestStreakDays: number;
  lastActiveDate: string;
  totalFocusMinutesAllTime: number;
}

interface AppState {
  // Blocking
  blockedApps: BlockedApp[];
  youtubeStudyMode: boolean;
  globalBlockerEnabled: boolean;
  youtubeWhitelist: YoutubeChannel[];
  websiteBlocks: WebsiteBlock[];

  // Timer
  timerSeconds: number;
  isTimerRunning: boolean;
  timerMode: 'POMODORO' | 'SHORT_BREAK' | 'LONG_BREAK';
  pomodoroCount: number;

  // Tasks
  tasks: Task[];

  // Calendar
  calendarEvents: CalendarEvent[];

  // Sessions
  focusSessions: FocusSession[];

  // Streaks
  streakGoal: StreakGoal;

  // Settings
  accentColor: string;

  // Actions
  toggleAppBlocked: (pkg: string) => void;
  toggleReelsBlocked: (pkg: string) => void;
  toggleShortsBlocked: (pkg: string) => void;
  setYoutubeStudyMode: (enabled: boolean) => void;
  setGlobalBlocker: (enabled: boolean) => void;
  addYoutubeChannel: (ch: YoutubeChannel) => void;
  removeYoutubeChannel: (channelId: string) => void;
  addWebsiteBlock: (wb: WebsiteBlock) => void;
  removeWebsiteBlock: (domain: string) => void;

  setTimerMode: (mode: 'POMODORO' | 'SHORT_BREAK' | 'LONG_BREAK') => void;
  startTimer: () => void;
  pauseTimer: () => void;
  resetTimer: () => void;
  tickTimer: () => void;

  addTask: (task: Omit<Task, 'id' | 'createdAt' | 'isCompleted'>) => void;
  toggleTask: (id: string) => void;
  deleteTask: (id: string) => void;

  addEvent: (event: Omit<CalendarEvent, 'id'>) => void;
  deleteEvent: (id: string) => void;

  updateStreak: (minutes: number) => void;
  setAccentColor: (color: string) => void;
}

const DEFAULT_YOUTUBE_CHANNELS: YoutubeChannel[] = [
  { channelId: 'UC_x5XG1OV2P6uZZ5FSM9Ttw', channelTitle: 'MIT OpenCourseWare', category: 'Education' },
  { channelId: 'UCsXVk37bltHxD1rDPwtNM8Q', channelTitle: 'Kurzgesagt', category: 'Education' },
  { channelId: 'UC8butISFwT-Wl7EV0hUK0BQ', channelTitle: 'freeCodeCamp.org', category: 'Education' },
];

const DEFAULT_WEBSITES: WebsiteBlock[] = [
  { domain: 'reddit.com', category: 'Social' },
  { domain: 'twitter.com', category: 'Social' },
  { domain: 'x.com', category: 'Social' },
  { domain: 'tiktok.com', category: 'Social' },
];

const DEFAULT_APPS: BlockedApp[] = [
  { packageName: 'com.instagram.android', appName: 'Instagram', category: 'Social', isFullyBlocked: false, isReelsBlocked: true, isShortsBlocked: false },
  { packageName: 'com.google.android.youtube', appName: 'YouTube', category: 'Video', isFullyBlocked: false, isReelsBlocked: false, isShortsBlocked: true },
  { packageName: 'com.zhiliaoapp.musically', appName: 'TikTok', category: 'Social', isFullyBlocked: true, isReelsBlocked: false, isShortsBlocked: false },
  { packageName: 'com.twitter.android', appName: 'Twitter/X', category: 'Social', isFullyBlocked: true, isReelsBlocked: false, isShortsBlocked: false },
  { packageName: 'com.reddit.frontpage', appName: 'Reddit', category: 'Social', isFullyBlocked: true, isReelsBlocked: false, isShortsBlocked: false },
  { packageName: 'com.facebook.katana', appName: 'Facebook', category: 'Social', isFullyBlocked: false, isReelsBlocked: false, isShortsBlocked: false },
  { packageName: 'com.snapchat.android', appName: 'Snapchat', category: 'Social', isFullyBlocked: false, isReelsBlocked: false, isShortsBlocked: false },
  { packageName: 'com.discord', appName: 'Discord', category: 'Social', isFullyBlocked: false, isReelsBlocked: false, isShortsBlocked: false },
  { packageName: 'com.netflix.mediaclient', appName: 'Netflix', category: 'Video', isFullyBlocked: true, isReelsBlocked: false, isShortsBlocked: false },
  { packageName: 'com.twitch.android.app', appName: 'Twitch', category: 'Video', isFullyBlocked: true, isReelsBlocked: false, isShortsBlocked: false },
];

const DEFAULT_TASKS: Task[] = [
  { id: '1', title: 'Complete Calculus Chapter 4', notes: 'Integration techniques, problems 1-15', priority: 'HIGH', category: 'Math', isCompleted: false, estimatedMinutes: 45, dueDate: new Date(Date.now() + 86400000 * 2).toISOString(), createdAt: Date.now() },
  { id: '2', title: 'Review Physics Thermodynamics', notes: 'Prep for Friday quiz', priority: 'MEDIUM', category: 'Physics', isCompleted: false, estimatedMinutes: 30, dueDate: new Date(Date.now() + 86400000 * 3).toISOString(), createdAt: Date.now() },
  { id: '3', title: 'Read Chapter 7 - Organic Chemistry', notes: 'Reaction mechanisms', priority: 'LOW', category: 'Chemistry', isCompleted: false, estimatedMinutes: 25, dueDate: new Date(Date.now() + 86400000 * 5).toISOString(), createdAt: Date.now() },
];

const DEFAULT_EVENTS: CalendarEvent[] = [
  { id: '1', title: 'Final Physics Exam', eventType: 'EXAM', startTime: Date.now() + 86400000 * 5, endTime: Date.now() + 86400000 * 5 + 7200000, dateString: new Date(Date.now() + 86400000 * 5).toISOString().split('T')[0], notes: 'Exam Hall 3B', isExamCountdown: true },
  { id: '2', title: 'Organic Chemistry Study Block', eventType: 'STUDY_BLOCK', startTime: Date.now() + 86400000, endTime: Date.now() + 86400000 + 5400000, dateString: new Date(Date.now() + 86400000).toISOString().split('T')[0], notes: 'Focus on reaction mechanisms', isExamCountdown: false },
];

const DEFAULT_STREAK: StreakGoal = {
  dailyScreenTimeGoalMinutes: 120,
  currentStreakDays: 3,
  bestStreakDays: 12,
  lastActiveDate: new Date().toISOString().split('T')[0],
  totalFocusMinutesAllTime: 450,
};

const STORAGE_KEY = 'focusforge_state';

async function loadState(): Promise<Partial<AppState> | null> {
  try {
    const raw = await AsyncStorage.getItem(STORAGE_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch { return null; }
}

async function saveState(state: Partial<AppState>) {
  try {
    const toSave = {
      blockedApps: state.blockedApps,
      youtubeStudyMode: state.youtubeStudyMode,
      globalBlockerEnabled: state.globalBlockerEnabled,
      youtubeWhitelist: state.youtubeWhitelist,
      websiteBlocks: state.websiteBlocks,
      tasks: state.tasks,
      calendarEvents: state.calendarEvents,
      focusSessions: state.focusSessions,
      streakGoal: state.streakGoal,
      accentColor: state.accentColor,
      pomodoroCount: state.pomodoroCount,
    };
    await AsyncStorage.setItem(STORAGE_KEY, JSON.stringify(toSave));
  } catch {}
}

let timerInterval: ReturnType<typeof setInterval> | null = null;

export const useStore = create<AppState>((set, get) => ({
  blockedApps: DEFAULT_APPS,
  youtubeStudyMode: false,
  globalBlockerEnabled: true,
  youtubeWhitelist: DEFAULT_YOUTUBE_CHANNELS,
  websiteBlocks: DEFAULT_WEBSITES,

  timerSeconds: 25 * 60,
  isTimerRunning: false,
  timerMode: 'POMODORO',
  pomodoroCount: 0,

  tasks: DEFAULT_TASKS,
  calendarEvents: DEFAULT_EVENTS,
  focusSessions: [],
  streakGoal: DEFAULT_STREAK,
  accentColor: '#C9A84C',

  toggleAppBlocked: (pkg) => set((s) => {
    const apps = s.blockedApps.map((a) =>
      a.packageName === pkg ? { ...a, isFullyBlocked: !a.isFullyBlocked } : a
    );
    saveState({ ...s, blockedApps: apps });
    return { blockedApps: apps };
  }),

  toggleReelsBlocked: (pkg) => set((s) => {
    const apps = s.blockedApps.map((a) =>
      a.packageName === pkg ? { ...a, isReelsBlocked: !a.isReelsBlocked } : a
    );
    saveState({ ...s, blockedApps: apps });
    return { blockedApps: apps };
  }),

  toggleShortsBlocked: (pkg) => set((s) => {
    const apps = s.blockedApps.map((a) =>
      a.packageName === pkg ? { ...a, isShortsBlocked: !a.isShortsBlocked } : a
    );
    saveState({ ...s, blockedApps: apps });
    return { blockedApps: apps };
  }),

  setYoutubeStudyMode: (enabled) => set((s) => {
    saveState({ ...s, youtubeStudyMode: enabled });
    return { youtubeStudyMode: enabled };
  }),

  setGlobalBlocker: (enabled) => set((s) => {
    saveState({ ...s, globalBlockerEnabled: enabled });
    return { globalBlockerEnabled: enabled };
  }),

  addYoutubeChannel: (ch) => set((s) => {
    const channels = [...s.youtubeWhitelist, ch];
    saveState({ ...s, youtubeWhitelist: channels });
    return { youtubeWhitelist: channels };
  }),

  removeYoutubeChannel: (channelId) => set((s) => {
    const channels = s.youtubeWhitelist.filter((c) => c.channelId !== channelId);
    saveState({ ...s, youtubeWhitelist: channels });
    return { youtubeWhitelist: channels };
  }),

  addWebsiteBlock: (wb) => set((s) => {
    const blocks = [...s.websiteBlocks, wb];
    saveState({ ...s, websiteBlocks: blocks });
    return { websiteBlocks: blocks };
  }),

  removeWebsiteBlock: (domain) => set((s) => {
    const blocks = s.websiteBlocks.filter((b) => b.domain !== domain);
    saveState({ ...s, websiteBlocks: blocks });
    return { websiteBlocks: blocks };
  }),

  setTimerMode: (mode) => {
    if (timerInterval) { clearInterval(timerInterval); timerInterval = null; }
    const mins = mode === 'POMODORO' ? 25 : mode === 'SHORT_BREAK' ? 5 : 15;
    set({ timerMode: mode, timerSeconds: mins * 60, isTimerRunning: false });
  },

  startTimer: () => {
    if (timerInterval) return;
    set({ isTimerRunning: true });
    timerInterval = setInterval(() => {
      const s = get();
      if (s.timerSeconds <= 0) {
        clearInterval(timerInterval!);
        timerInterval = null;
        const newCount = s.timerMode === 'POMODORO' ? s.pomodoroCount + 1 : s.pomodoroCount;
        const session: FocusSession = {
          id: Date.now().toString(),
          title: `${s.timerMode} Session`,
          durationMinutes: s.timerMode === 'POMODORO' ? 25 : s.timerMode === 'SHORT_BREAK' ? 5 : 15,
          completedAt: Date.now(),
          isCompleted: true,
        };
        set({
          isTimerRunning: false,
          pomodoroCount: newCount,
          focusSessions: [...s.focusSessions, session],
        });
        return;
      }
      set({ timerSeconds: s.timerSeconds - 1 });
    }, 1000);
  },

  pauseTimer: () => {
    if (timerInterval) { clearInterval(timerInterval); timerInterval = null; }
    set({ isTimerRunning: false });
  },

  resetTimer: () => {
    if (timerInterval) { clearInterval(timerInterval); timerInterval = null; }
    const s = get();
    const mins = s.timerMode === 'POMODORO' ? 25 : s.timerMode === 'SHORT_BREAK' ? 5 : 15;
    set({ timerSeconds: mins * 60, isTimerRunning: false });
  },

  tickTimer: () => {
    const s = get();
    if (s.timerSeconds > 0) set({ timerSeconds: s.timerSeconds - 1 });
  },

  addTask: (task) => set((s) => {
    const newTask: Task = {
      ...task,
      id: Date.now().toString(),
      createdAt: Date.now(),
      isCompleted: false,
    };
    const tasks = [...s.tasks, newTask];
    saveState({ ...s, tasks });
    return { tasks };
  }),

  toggleTask: (id) => set((s) => {
    const tasks = s.tasks.map((t) => t.id === id ? { ...t, isCompleted: !t.isCompleted } : t);
    saveState({ ...s, tasks });
    return { tasks };
  }),

  deleteTask: (id) => set((s) => {
    const tasks = s.tasks.filter((t) => t.id !== id);
    saveState({ ...s, tasks });
    return { tasks };
  }),

  addEvent: (event) => set((s) => {
    const newEvent: CalendarEvent = { ...event, id: Date.now().toString() };
    const calendarEvents = [...s.calendarEvents, newEvent];
    saveState({ ...s, calendarEvents });
    return { calendarEvents };
  }),

  deleteEvent: (id) => set((s) => {
    const calendarEvents = s.calendarEvents.filter((e) => e.id !== id);
    saveState({ ...s, calendarEvents });
    return { calendarEvents };
  }),

  updateStreak: (minutes) => set((s) => {
    const today = new Date().toISOString().split('T')[0];
    const streak = { ...s.streakGoal };
    streak.totalFocusMinutesAllTime += minutes;
    if (streak.lastActiveDate !== today) {
      const yesterday = new Date(Date.now() - 86400000).toISOString().split('T')[0];
      if (streak.lastActiveDate === yesterday) {
        streak.currentStreakDays += 1;
      } else {
        streak.currentStreakDays = 1;
      }
      streak.lastActiveDate = today;
      if (streak.currentStreakDays > streak.bestStreakDays) {
        streak.bestStreakDays = streak.currentStreakDays;
      }
    }
    saveState({ ...s, streakGoal: streak });
    return { streakGoal: streak };
  }),

  setAccentColor: (color) => set((s) => {
    saveState({ ...s, accentColor: color });
    return { accentColor: color };
  }),
}));

// Load persisted state on startup
loadState().then((saved) => {
  if (saved) {
    useStore.setState((s) => ({
      ...saved,
      timerSeconds: s.timerSeconds,
      isTimerRunning: false,
    }));
  }
});
