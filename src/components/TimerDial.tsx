import React from 'react';
import { View, Text, StyleSheet } from 'react-native';
import Svg, { Circle } from 'react-native-svg';
import { Colors } from '../theme/colors';

interface Props {
  seconds: number;
  totalSeconds: number;
  mode: string;
  size?: number;
}

export function TimerDial({ seconds, totalSeconds, mode, size = 220 }: Props) {
  const progress = totalSeconds > 0 ? (totalSeconds - seconds) / totalSeconds : 0;
  const radius = (size - 12) / 2;
  const circumference = 2 * Math.PI * radius;
  const strokeDashoffset = circumference * (1 - progress);

  const minutes = Math.floor(seconds / 60);
  const secs = seconds % 60;

  const modeLabel = mode === 'POMODORO' ? 'Focus' : mode === 'SHORT_BREAK' ? 'Short Break' : 'Long Break';
  const modeColor = mode === 'POMODORO' ? Colors.gold : mode === 'SHORT_BREAK' ? Colors.success : Colors.info;

  return (
    <View style={[styles.container, { width: size, height: size }]}>
      <Svg width={size} height={size}>
        <Circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          stroke={Colors.surfaceElevated}
          strokeWidth={6}
          fill="none"
        />
        <Circle
          cx={size / 2}
          cy={size / 2}
          r={radius}
          stroke={modeColor}
          strokeWidth={6}
          fill="none"
          strokeDasharray={circumference}
          strokeDashoffset={strokeDashoffset}
          strokeLinecap="round"
          transform={`rotate(-90 ${size / 2} ${size / 2})`}
          style={{ shadowColor: modeColor, shadowOpacity: 0.5, shadowRadius: 8 }}
        />
      </Svg>
      <View style={styles.inner}>
        <Text style={styles.time}>
          {String(minutes).padStart(2, '0')}:{String(secs).padStart(2, '0')}
        </Text>
        <Text style={[styles.modeLabel, { color: modeColor }]}>{modeLabel}</Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { alignItems: 'center', justifyContent: 'center' },
  inner: {
    position: 'absolute',
    alignItems: 'center',
    justifyContent: 'center',
  },
  time: {
    fontSize: 48,
    fontWeight: '700',
    color: Colors.textPrimary,
    fontVariant: ['tabular-nums'],
    letterSpacing: -2,
  },
  modeLabel: {
    fontSize: 13,
    fontWeight: '600',
    marginTop: 4,
    textTransform: 'uppercase',
    letterSpacing: 1.5,
  },
});
