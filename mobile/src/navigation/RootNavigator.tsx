import { createNativeStackNavigator } from "@react-navigation/native-stack";
import type { RootStackParamList } from "../types/navigation";
import { useAppStore } from "../store/useAppStore";
import { LanguageSelectScreen } from "../screens/LanguageSelectScreen";
import { RoleSelectScreen } from "../screens/RoleSelectScreen";
import { OnboardingScreen } from "../screens/OnboardingScreen";
import { ComponentPreviewScreen } from "../screens/ComponentPreviewScreen";
import { TabNavigator } from "./TabNavigator";

const Stack = createNativeStackNavigator<RootStackParamList>();

export function RootNavigator() {
  const { language, role, isOnboarded } = useAppStore();

  const getInitialRoute = (): keyof RootStackParamList => {
    if (!language) return "LanguageSelect";
    if (!role) return "RoleSelect";
    if (!isOnboarded) return "Onboarding";
    return "MainTabs";
  };

  return (
    <Stack.Navigator
      initialRouteName={getInitialRoute()}
      screenOptions={{
        headerShown: false,
        animation: "slide_from_right",
        contentStyle: { backgroundColor: "#0B0D12" },
      }}
    >
      <Stack.Screen name="LanguageSelect" component={LanguageSelectScreen} />
      <Stack.Screen name="RoleSelect" component={RoleSelectScreen} />
      <Stack.Screen name="Onboarding" component={OnboardingScreen} />
      <Stack.Screen name="MainTabs" component={TabNavigator} />
      <Stack.Group screenOptions={{ presentation: "modal", animation: "slide_from_bottom" }}>
        <Stack.Screen name="VoiceCapture" component={VoiceCaptureStub} />
      </Stack.Group>
      {__DEV__ && (
        <Stack.Screen name="ComponentPreview" component={ComponentPreviewScreen} />
      )}
    </Stack.Navigator>
  );
}

function VoiceCaptureStub() {
  const { View, Text } = require("react-native");
  return (
    <View style={{ flex: 1, backgroundColor: "#0B0D12", alignItems: "center", justifyContent: "center" }}>
      <Text style={{ color: "#F2F2F0", fontSize: 24, fontWeight: "bold" }}>Voice Capture</Text>
      <Text style={{ color: "#8B8F9A", fontSize: 16, marginTop: 8 }}>Bol ke batao</Text>
    </View>
  );
}
