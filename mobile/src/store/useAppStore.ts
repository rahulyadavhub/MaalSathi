import { create } from "zustand";
import AsyncStorage from "@react-native-async-storage/async-storage";
import type { Language, UserRole, User } from "../types/user";

const STORAGE_KEYS = {
  LANGUAGE: "maalsaathi_language",
  ROLE: "maalsaathi_role",
  USER: "maalsaathi_user",
  ONBOARDED: "maalsaathi_onboarded",
} as const;

interface AppState {
  language: Language | null;
  role: UserRole | null;
  user: User | null;
  isOnboarded: boolean;
  isHydrated: boolean;
  isOnline: boolean;
  syncPending: number;
  syncFailed: number;

  setLanguage: (lang: Language) => Promise<void>;
  setRole: (role: UserRole) => Promise<void>;
  setUser: (user: User) => Promise<void>;
  completeOnboarding: () => Promise<void>;
  setOnline: (online: boolean) => void;
  setSyncPending: (count: number) => void;
  setSyncFailed: (count: number) => void;
  hydrate: () => Promise<void>;
  reset: () => Promise<void>;
}

export const useAppStore = create<AppState>((set) => ({
  language: null,
  role: null,
  user: null,
  isOnboarded: false,
  isHydrated: false,
  isOnline: true,
  syncPending: 0,
  syncFailed: 0,

  setLanguage: async (language) => {
    await AsyncStorage.setItem(STORAGE_KEYS.LANGUAGE, language);
    set({ language });
  },

  setRole: async (role) => {
    await AsyncStorage.setItem(STORAGE_KEYS.ROLE, role);
    set({ role });
  },

  setUser: async (user) => {
    await AsyncStorage.setItem(STORAGE_KEYS.USER, JSON.stringify(user));
    set({ user });
  },

  completeOnboarding: async () => {
    await AsyncStorage.setItem(STORAGE_KEYS.ONBOARDED, "true");
    set({ isOnboarded: true });
  },

  setOnline: (isOnline) => set({ isOnline }),

  setSyncPending: (syncPending) => set({ syncPending }),

  setSyncFailed: (syncFailed) => set({ syncFailed }),

  hydrate: async () => {
    const [language, role, userJson, onboarded] = await Promise.all([
      AsyncStorage.getItem(STORAGE_KEYS.LANGUAGE),
      AsyncStorage.getItem(STORAGE_KEYS.ROLE),
      AsyncStorage.getItem(STORAGE_KEYS.USER),
      AsyncStorage.getItem(STORAGE_KEYS.ONBOARDED),
    ]);

    set({
      language: (language as Language) || null,
      role: (role as UserRole) || null,
      user: userJson ? JSON.parse(userJson) : null,
      isOnboarded: onboarded === "true",
      isHydrated: true,
    });
  },

  reset: async () => {
    await AsyncStorage.multiRemove(Object.values(STORAGE_KEYS));
    set({
      language: null,
      role: null,
      user: null,
      isOnboarded: false,
    });
  },
}));
