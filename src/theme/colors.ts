// FocusForge Premium Color System
// Deep obsidian + warm gold — luxury focus aesthetic

export const Colors = {
  // Core Backgrounds
  background: '#07070C',
  surface: '#0E0E16',
  surfaceElevated: '#14141E',
  card: '#181824',
  cardHover: '#1E1E2C',

  // Premium Gold System
  gold: '#C9A84C',
  goldLight: '#D4BC6E',
  goldMuted: '#8B7A50',
  goldSubtle: 'rgba(201, 168, 76, 0.12)',
  goldGlow: 'rgba(201, 168, 76, 0.25)',

  // Text
  textPrimary: '#F0EDE6',
  textSecondary: '#8A8A9A',
  textMuted: '#5A5A6A',
  textGold: '#C9A84C',

  // Borders
  border: 'rgba(255, 255, 255, 0.06)',
  borderGold: 'rgba(201, 168, 76, 0.3)',

  // Status
  success: '#5CB85C',
  successBg: 'rgba(92, 184, 92, 0.12)',
  warning: '#D4A03C',
  warningBg: 'rgba(212, 160, 60, 0.12)',
  danger: '#C75050',
  dangerBg: 'rgba(199, 80, 80, 0.12)',
  info: '#5B8FB9',
  infoBg: 'rgba(91, 143, 185, 0.12)',

  // Accent Variants
  bronze: '#8B6914',
  copper: '#B87333',
  champagne: '#F7E7CE',
  ivory: '#FFFFF0',

  // Glass
  glassBg: 'rgba(14, 14, 22, 0.75)',
  glassBorder: 'rgba(201, 168, 76, 0.08)',
} as const;

export type ColorKey = keyof typeof Colors;
