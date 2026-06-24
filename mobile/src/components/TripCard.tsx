import { Pressable, View, Text } from "react-native";
import { MaterialIcons } from "@expo/vector-icons";
import type { Trip, TripStatus } from "../types/trip";
import { colors } from "../theme/tokens";

interface TripCardProps {
  trip: Trip;
  onPress: () => void;
}

const STATUS_CONFIG: Record<TripStatus, { label: string; bg: string; text: string }> = {
  draft: { label: "Draft", bg: "bg-text-secondary/20", text: "text-text-secondary" },
  pending_confirmation: { label: "Pending", bg: "bg-text-secondary/20", text: "text-text-secondary" },
  active: { label: "Chal Rahi", bg: "bg-primary/20", text: "text-primary" },
  completed: { label: "Complete", bg: "bg-text-secondary/15", text: "text-text-secondary" },
  cancelled: { label: "Cancel", bg: "bg-loss/20", text: "text-loss" },
};

function formatDate(iso: string): string {
  return new Date(iso).toLocaleDateString("hi-IN", {
    day: "numeric",
    month: "short",
  });
}

export function TripCard({ trip, onPress }: TripCardProps) {
  const status = STATUS_CONFIG[trip.status];
  const profit = trip.netProfit;

  return (
    <Pressable
      className="mx-5 rounded-card border border-border bg-surface-elevated p-4 active:bg-surface"
      onPress={onPress}
      accessibilityRole="button"
      accessibilityLabel={`Trip ${trip.origin} se ${trip.destination}`}
    >
      <View className="mb-2 flex-row items-center justify-between">
        <View className="flex-1 flex-row items-center gap-2">
          <Text className="text-lg font-semibold text-text-primary">
            {trip.origin}
          </Text>
          <MaterialIcons name="arrow-forward" size={14} color={colors.textSecondary} />
          <Text className="text-lg font-semibold text-text-primary">
            {trip.destination}
          </Text>
        </View>
        <View className={`rounded-full px-2.5 py-0.5 ${status.bg}`}>
          <Text className={`text-sm font-bold ${status.text}`}>
            {status.label}
          </Text>
        </View>
      </View>

      <View className="flex-row items-center justify-between">
        <View className="flex-row items-center gap-3">
          <Text className="text-sm text-text-secondary">
            {trip.truckRegistration}
          </Text>
          <Text className="text-sm text-text-secondary">
            {formatDate(trip.startedAt)}
          </Text>
        </View>
        <View className="flex-row items-center gap-1">
          {trip.status === "completed" && (
            <MaterialIcons
              name={profit >= 0 ? "arrow-upward" : "arrow-downward"}
              size={14}
              color={profit >= 0 ? colors.primary : colors.loss}
            />
          )}
          <Text
            className="text-body font-bold"
            style={{
              color:
                trip.status === "completed"
                  ? profit >= 0
                    ? colors.primary
                    : colors.loss
                  : colors.textPrimary,
            }}
          >
            ₹{trip.freightAmount.toLocaleString("en-IN")}
          </Text>
        </View>
      </View>
    </Pressable>
  );
}
