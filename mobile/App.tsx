import "./global.css";
import { useEffect, useState } from "react";
import { View, ActivityIndicator } from "react-native";
import { StatusBar } from "expo-status-bar";
import { NavigationContainer } from "@react-navigation/native";
import { SafeAreaProvider } from "react-native-safe-area-context";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { RootNavigator } from "./src/navigation/RootNavigator";
import { useAppStore } from "./src/store/useAppStore";
import { useTripStore } from "./src/store/useTripStore";
import { useNetworkStatus } from "./src/hooks/useNetworkStatus";
import {
  getSyncPendingCount,
  startSyncListeners,
} from "./src/offline/syncQueue";
import { colors } from "./src/theme/tokens";

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 2,
      staleTime: 30_000,
      gcTime: 5 * 60_000,
    },
  },
});

function AppContent() {
  const { isHydrated, hydrate } = useAppStore();
  const [ready, setReady] = useState(false);

  useNetworkStatus();

  useEffect(() => {
    hydrate().finally(() => setReady(true));
  }, [hydrate]);

  useEffect(() => {
    if (!ready) return;

    useTripStore.getState().loadTrips();

    getSyncPendingCount().then((count) => {
      if (count > 0) {
        startSyncListeners();
      }
    });
  }, [ready]);

  if (!ready || !isHydrated) {
    return (
      <View
        style={{
          flex: 1,
          backgroundColor: colors.dark,
          alignItems: "center",
          justifyContent: "center",
        }}
      >
        <ActivityIndicator size="large" color={colors.primary} />
      </View>
    );
  }

  return (
    <NavigationContainer
      theme={{
        dark: true,
        colors: {
          primary: colors.primary,
          background: colors.dark,
          card: colors.surface,
          text: colors.textPrimary,
          border: colors.border,
          notification: colors.primary,
        },
        fonts: {
          regular: { fontFamily: "System", fontWeight: "400" },
          medium: { fontFamily: "System", fontWeight: "500" },
          bold: { fontFamily: "System", fontWeight: "700" },
          heavy: { fontFamily: "System", fontWeight: "900" },
        },
      }}
    >
      <RootNavigator />
    </NavigationContainer>
  );
}

export default function App() {
  return (
    <SafeAreaProvider>
      <QueryClientProvider client={queryClient}>
        <StatusBar style="light" />
        <AppContent />
      </QueryClientProvider>
    </SafeAreaProvider>
  );
}
