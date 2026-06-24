export const colors = {
  primary: "#25D366",
  warnMuted: "#8B8F9A",
  loss: "#FF4444",
  dark: "#0B0D12",
  surface: "#16181F",
  surfaceElevated: "#1E212B",
  textPrimary: "#F2F2F0",
  textSecondary: "#8B8F9A",
  border: "#262A35",
} as const;

export const fontSizes = {
  xs: 12,
  sm: 14,
  body: 16,
  lg: 18,
  xl: 20,
  "2xl": 24,
  "3xl": 30,
  money: 28,
  "4xl": 36,
} as const;

export const spacing = {
  xs: 4,
  sm: 8,
  md: 12,
  lg: 16,
  xl: 20,
  "2xl": 24,
  "3xl": 32,
  "4xl": 40,
} as const;

export const radii = {
  sm: 8,
  card: 16,
  button: 12,
  full: 9999,
} as const;

export const hitSlop = { top: 8, bottom: 8, left: 8, right: 8 } as const;

export const MIN_TOUCH_TARGET = 48;
export const CTA_HEIGHT = 56;
export const VOICE_BUTTON_SIZE = 80;
