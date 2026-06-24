import { useState, useCallback, useRef, useEffect, useMemo } from "react";
import { View, Text, ScrollView, RefreshControl } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { useNavigation } from "@react-navigation/native";
import type { NativeStackNavigationProp } from "@react-navigation/native-stack";
import { useAppStore } from "../store/useAppStore";
import { useTripStore } from "../store/useTripStore";
import { VoiceButton, type VoiceButtonState } from "../components/VoiceButton";
import { MoneyPill } from "../components/MoneyPill";
import { StatusRibbon } from "../components/StatusRibbon";
import { SyncStatusBar } from "../components/SyncStatusBar";
import { BigButton } from "../components/BigButton";
import type { TripStackParamList } from "../types/navigation";
import { colors } from "../theme/tokens";

type Nav = NativeStackNavigationProp<TripStackParamList, "Home">;

function todayRange(): { start: Date; end: Date } {
  const now = new Date();
  const start = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  const end = new Date(start.getTime() + 86_400_000);
  return { start, end };
}

export function HomeScreen() {
  const nav = useNavigation<Nav>();
  const user = useAppStore((s) => s.user);
  const { trips, isLoaded, loadTrips } = useTripStore();

  const [refreshing, setRefreshing] = useState(false);
  const [voiceState, setVoiceState] = useState<VoiceButtonState>("idle");
  const voiceTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    if (!isLoaded) loadTrips();
  }, [isLoaded, loadTrips]);

  useEffect(() => {
    return () => {
      if (voiceTimerRef.current) clearTimeout(voiceTimerRef.current);
    };
  }, []);

  const activeTrip = useMemo(
    () => trips.find((t) => t.status === "active"),
    [trips],
  );

  const todayStats = useMemo(() => {
    const { start, end } = todayRange();
    let revenue = 0;
    let expenses = 0;
    for (const t of trips) {
      const completed = t.completedAt ? new Date(t.completedAt) : null;
      if (completed && completed >= start && completed < end) {
        revenue += t.freightAmount;
        expenses += t.totalExpenses;
      }
      if (t.status === "active") {
        for (const e of t.expenses) {
          const rec = new Date(e.recordedAt);
          if (rec >= start && rec < end) {
            expenses += e.amount;
          }
        }
      }
    }
    return { revenue, expenses, profit: revenue - expenses };
  }, [trips]);

  // TODO [STT]: Replace this cycle with real speech-to-text integration.
  // Wire expo-speech or react-native-voice here. onPress should call
  // Voice.start('hi-IN'), onSpeechResults should parse the transcript
  // and route to trip/expense/profit flows via intent classification.
  const handleVoicePress = useCallback(() => {
    if (voiceState === "idle") {
      setVoiceState("listening");
      voiceTimerRef.current = setTimeout(() => {
        setVoiceState("processing");
        voiceTimerRef.current = setTimeout(() => {
          setVoiceState("idle");
        }, 1500);
      }, 3000);
    } else if (voiceState === "listening") {
      if (voiceTimerRef.current) clearTimeout(voiceTimerRef.current);
      setVoiceState("processing");
      voiceTimerRef.current = setTimeout(() => {
        setVoiceState("idle");
      }, 1500);
    }
  }, [voiceState]);

  const onRefresh = useCallback(() => {
    setRefreshing(true);
    loadTrips().finally(() => setRefreshing(false));
  }, [loadTrips]);

  return (
    <SafeAreaView className="flex-1 bg-dark" edges={["top"]}>
      <SyncStatusBar />
      {activeTrip && (
        <StatusRibbon
          variant="trip-active"
          message={`${activeTrip.origin} → ${activeTrip.destination} chal rahi hai`}
          onPress={() => nav.navigate("TripDetail", { tripId: activeTrip._id })}
        />
      )}

      <ScrollView
        className="flex-1"
        contentContainerClassName="flex-grow"
        refreshControl={
          <RefreshControl
            refreshing={refreshing}
            onRefresh={onRefresh}
            tintColor={colors.primary}
            colors={[colors.primary]}
            progressBackgroundColor={colors.surface}
          />
        }
        showsVerticalScrollIndicator={false}
      >
        <View className="px-5 pb-4 pt-6">
          <Text className="text-2xl font-bold text-text-primary">
            Namaste{user?.name && user.name !== "Friend" ? `, ${user.name}` : ""} ji
          </Text>
          <Text className="mt-1 text-body text-text-secondary">Aaj ka hisaab</Text>
        </View>

        <View className="mx-5 flex-row gap-3">
          <View className="flex-1 rounded-card bg-surface p-4">
            <MoneyPill
              amount={todayStats.revenue}
              label="Kamaai"
              size="md"
              neutral
              showSign={false}
            />
          </View>
          <View className="flex-1 rounded-card bg-surface p-4">
            <MoneyPill
              amount={todayStats.expenses}
              label="Kharcha"
              size="md"
              neutral
              showSign={false}
            />
          </View>
        </View>

        <View className="mx-5 mt-3 rounded-card bg-surface-elevated p-5">
          <MoneyPill amount={todayStats.profit} label="Aaj ka Fayda / Nuksan" size="lg" />
        </View>

        <View className="flex-1 items-center justify-center py-10">
          <VoiceButton state={voiceState} onPress={handleVoicePress} size={88} />
          <Text className="mt-4 text-lg font-semibold text-text-primary">
            {voiceState === "idle" && "Bol ke batao"}
            {voiceState === "listening" && "Sun raha hun..."}
            {voiceState === "processing" && "Samajh raha hun..."}
          </Text>
          <Text className="mt-1 text-sm text-text-secondary">
            {voiceState === "idle" && '"diesel 4500" ya "Mumbai se Pune trip"'}
          </Text>
        </View>

        <View className="gap-3 px-5 pb-8">
          {activeTrip && (
            <BigButton
              title="Trip Khatam Karo"
              onPress={() => nav.navigate("TripComplete", { tripId: activeTrip._id })}
              variant="primary"
              icon="check-circle"
            />
          )}
          {!activeTrip && (
            <BigButton
              title="Nayi Trip Shuru Karo"
              onPress={() => nav.navigate("NewTrip")}
              variant="primary"
              icon="add"
            />
          )}
          <BigButton
            title="Saari Trips Dekho"
            onPress={() => nav.navigate("TripList")}
            variant="secondary"
            icon="list"
          />
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}
