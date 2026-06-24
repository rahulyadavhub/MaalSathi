import type { NavigatorScreenParams } from "@react-navigation/native";

export type TabParamList = {
  TripTab: NavigatorScreenParams<TripStackParamList>;
  HisaabTab: undefined;
  MadadTab: undefined;
};

export type TripStackParamList = {
  Home: undefined;
  TripList: undefined;
  TripDetail: { tripId: string };
  NewTrip: undefined;
  TripComplete: { tripId: string };
  AddExpense: { tripId: string };
};

export type RootStackParamList = {
  LanguageSelect: undefined;
  RoleSelect: undefined;
  Onboarding: undefined;
  MainTabs: NavigatorScreenParams<TabParamList>;
  VoiceCapture: undefined;
  ComponentPreview: undefined;
};
