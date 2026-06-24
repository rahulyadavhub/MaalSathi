import { useState } from "react";
import { ScrollView, View, Text } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { VoiceButton, type VoiceButtonState } from "../components/VoiceButton";
import { MoneyPill } from "../components/MoneyPill";
import { ConfirmationCard } from "../components/ConfirmationCard";
import { StatusRibbon } from "../components/StatusRibbon";
import { BigButton } from "../components/BigButton";
import { ShareCard } from "../components/ShareCard";
import type { Trip } from "../types/trip";

const MOCK_TRIP: Trip = {
  _id: "1",
  userId: "u1",
  truckRegistration: "MH12AB1234",
  origin: "Mumbai",
  destination: "Delhi",
  cargoType: "Steel",
  cargoWeightTons: 20,
  freightAmount: 85000,
  advancePaid: 20000,
  balanceDue: 65000,
  status: "completed",
  expenses: [
    { _id: "e1", category: "diesel", amount: 25000, source: "voice", recordedAt: new Date().toISOString() },
    { _id: "e2", category: "toll", amount: 4500, source: "text", recordedAt: new Date().toISOString() },
    { _id: "e3", category: "food", amount: 1200, source: "text", recordedAt: new Date().toISOString() },
  ],
  startedAt: new Date().toISOString(),
  completedAt: new Date().toISOString(),
  cancelledAt: null,
  totalExpenses: 30700,
  netProfit: 54300,
  createdAt: new Date().toISOString(),
  updatedAt: new Date().toISOString(),
};

function Section({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <View className="mb-8">
      <Text className="mb-3 px-5 text-sm font-bold uppercase tracking-wider text-text-secondary">
        {title}
      </Text>
      {children}
    </View>
  );
}

export function ComponentPreviewScreen() {
  const [voiceState, setVoiceState] = useState<VoiceButtonState>("idle");

  const cycleVoiceState = () => {
    setVoiceState((s) =>
      s === "idle" ? "listening" : s === "listening" ? "processing" : "idle",
    );
  };

  return (
    <SafeAreaView className="flex-1 bg-dark">
      <ScrollView className="flex-1" contentContainerClassName="pb-12">
        <Text className="px-5 py-6 text-2xl font-bold text-text-primary">
          Component Preview
        </Text>

        <Section title="Voice Button">
          <View className="items-center py-4">
            <VoiceButton state={voiceState} onPress={cycleVoiceState} size={80} />
            <Text className="mt-2 text-sm text-text-secondary">
              State: {voiceState} (tap to cycle)
            </Text>
          </View>
        </Section>

        <Section title="Money Pill">
          <View className="gap-4 px-5">
            <MoneyPill amount={54300} label="Aaj ka Profit" size="lg" />
            <MoneyPill amount={-12500} label="Loss" size="lg" />
            <MoneyPill amount={85000} label="Freight" size="md" neutral showSign={false} />
            <MoneyPill amount={1250} size="sm" />
          </View>
        </Section>

        <Section title="Status Ribbon">
          <View className="gap-2">
            <StatusRibbon
              variant="trip-active"
              message="Mumbai → Delhi chal rahi hai"
              onPress={() => {}}
            />
            <StatusRibbon variant="sync-pending" message="3 entries sync pending" />
            <StatusRibbon variant="offline" message="Offline mode — data locally saved" />
          </View>
        </Section>

        <Section title="Big Button">
          <View className="gap-3 px-5">
            <BigButton title="Trip Shuru Karo" onPress={() => {}} icon="play-arrow" />
            <BigButton title="Kharcha Daalo" onPress={() => {}} variant="secondary" icon="add" />
            <BigButton title="Trip Cancel" onPress={() => {}} variant="danger" icon="close" />
            <BigButton title="Loading..." onPress={() => {}} loading />
          </View>
        </Section>

        <Section title="Confirmation Card">
          <ConfirmationCard
            title="Trip Confirm Karo"
            data={[
              { label: "From", value: "Mumbai", icon: "my-location" },
              { label: "To", value: "Delhi", icon: "place" },
              { label: "Maal", value: "Steel — 20 ton", icon: "inventory" },
            ]}
            amount={{ value: 85000, label: "Freight" }}
            onConfirm={() => {}}
            onEdit={() => {}}
            onCancel={() => {}}
            confirmLabel="Haan, Sahi Hai"
            cancelLabel="Cancel"
          />
        </Section>

        <Section title="Share Card">
          <ShareCard trip={MOCK_TRIP} />
        </Section>
      </ScrollView>
    </SafeAreaView>
  );
}
