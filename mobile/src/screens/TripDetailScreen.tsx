import { useEffect } from "react";
import { View, Text, ScrollView, Pressable } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { MaterialIcons } from "@expo/vector-icons";
import type { NativeStackScreenProps } from "@react-navigation/native-stack";
import type { TripStackParamList } from "../types/navigation";
import type { TripExpense, ExpenseCategory } from "../types/expense";
import { CATEGORY_LABELS, CATEGORY_ICONS } from "../types/expense";
import { useTripStore } from "../store/useTripStore";
import { MoneyPill } from "../components/MoneyPill";
import { BigButton } from "../components/BigButton";
import { colors } from "../theme/tokens";

type Props = NativeStackScreenProps<TripStackParamList, "TripDetail">;

function formatDateTime(iso: string): string {
  const d = new Date(iso);
  return d.toLocaleDateString("hi-IN", {
    day: "numeric",
    month: "short",
    hour: "2-digit",
    minute: "2-digit",
  });
}

function formatDate(iso: string): string {
  return new Date(iso).toLocaleDateString("hi-IN", {
    day: "numeric",
    month: "short",
    year: "numeric",
  });
}

function ExpenseTimelineItem({ expense, isLast }: { expense: TripExpense; isLast: boolean }) {
  const iconName = CATEGORY_ICONS[expense.category as ExpenseCategory] || "payments";
  const label = CATEGORY_LABELS[expense.category as ExpenseCategory] || expense.category;

  return (
    <View className="flex-row gap-3">
      <View className="items-center">
        <View className="h-9 w-9 items-center justify-center rounded-full bg-surface">
          <MaterialIcons
            name={iconName as keyof typeof MaterialIcons.glyphMap}
            size={18}
            color={colors.textSecondary}
          />
        </View>
        {!isLast && <View className="mt-1 flex-1 w-0.5 bg-border" />}
      </View>

      <View className={`flex-1 pb-5 ${isLast ? "" : ""}`}>
        <View className="flex-row items-center justify-between">
          <Text className="text-body font-medium text-text-primary">{label}</Text>
          <Text className="text-body font-bold text-text-primary">
            ₹{expense.amount.toLocaleString("en-IN")}
          </Text>
        </View>
        <View className="mt-0.5 flex-row items-center gap-2">
          <Text className="text-sm text-text-secondary">
            {formatDateTime(expense.recordedAt)}
          </Text>
          {expense.note && (
            <Text className="text-sm text-text-secondary">• {expense.note}</Text>
          )}
        </View>
      </View>
    </View>
  );
}

export function TripDetailScreen({ navigation, route }: Props) {
  const { tripId } = route.params;
  const { trips, isLoaded, loadTrips } = useTripStore();
  const trip = trips.find((t) => t._id === tripId);

  useEffect(() => {
    if (!isLoaded) loadTrips();
  }, [isLoaded, loadTrips]);

  if (!trip) {
    return (
      <SafeAreaView className="flex-1 items-center justify-center bg-dark">
        <Text className="text-lg text-text-secondary">Trip nahi mili</Text>
      </SafeAreaView>
    );
  }

  const isActive = trip.status === "active";
  const sortedExpenses = [...trip.expenses].sort(
    (a, b) => new Date(a.recordedAt).getTime() - new Date(b.recordedAt).getTime(),
  );

  return (
    <SafeAreaView className="flex-1 bg-dark" edges={["top"]}>
      <View className="flex-row items-center gap-3 px-5 pb-2 pt-4">
        <Pressable
          className="min-h-touch min-w-touch items-center justify-center"
          onPress={() => navigation.goBack()}
          accessibilityLabel="Back"
        >
          <MaterialIcons name="arrow-back" size={24} color={colors.textPrimary} />
        </Pressable>
        <View className="flex-1">
          <Text className="text-xl font-bold text-text-primary">
            {trip.origin} → {trip.destination}
          </Text>
          <Text className="text-sm text-text-secondary">
            {trip.truckRegistration} • {formatDate(trip.startedAt)}
          </Text>
        </View>
        <View
          className={`rounded-full px-2.5 py-0.5 ${
            isActive ? "bg-primary/20" : "bg-text-secondary/15"
          }`}
        >
          <Text
            className={`text-sm font-bold ${
              isActive ? "text-primary" : "text-text-secondary"
            }`}
          >
            {isActive ? "Chal Rahi" : trip.status === "completed" ? "Complete" : trip.status}
          </Text>
        </View>
      </View>

      <ScrollView className="flex-1" showsVerticalScrollIndicator={false}>
        <View className="mx-5 mt-3 flex-row gap-3">
          <View className="flex-1 rounded-card bg-surface p-4">
            <MoneyPill
              amount={trip.freightAmount}
              label="Bhada"
              size="md"
              neutral
              showSign={false}
            />
          </View>
          <View className="flex-1 rounded-card bg-surface p-4">
            <MoneyPill
              amount={trip.totalExpenses}
              label="Kharcha"
              size="md"
              neutral
              showSign={false}
            />
          </View>
        </View>

        <View className="mx-5 mt-3 rounded-card bg-surface-elevated p-4">
          <MoneyPill amount={trip.netProfit} label="Fayda / Nuksan" size="lg" />
        </View>

        {trip.cargoType && (
          <View className="mx-5 mt-3 flex-row gap-4 rounded-card bg-surface p-4">
            <View className="flex-row items-center gap-2">
              <MaterialIcons name="inventory" size={16} color={colors.textSecondary} />
              <Text className="text-sm text-text-secondary">{trip.cargoType}</Text>
            </View>
            {trip.cargoWeightTons && (
              <View className="flex-row items-center gap-2">
                <MaterialIcons name="scale" size={16} color={colors.textSecondary} />
                <Text className="text-sm text-text-secondary">{trip.cargoWeightTons} ton</Text>
              </View>
            )}
          </View>
        )}

        <View className="mx-5 mt-6 mb-2 flex-row items-center justify-between">
          <Text className="text-lg font-bold text-text-primary">Kharche</Text>
          {isActive && (
            <Pressable
              className="min-h-touch flex-row items-center gap-1.5 rounded-button bg-surface-elevated px-3 active:bg-surface"
              onPress={() => navigation.navigate("AddExpense", { tripId })}
            >
              <MaterialIcons name="add" size={18} color={colors.textSecondary} />
              <Text className="text-sm font-semibold text-text-secondary">Kharcha Daalo</Text>
            </Pressable>
          )}
        </View>

        {sortedExpenses.length > 0 ? (
          <View className="mx-5 mb-6">
            {sortedExpenses.map((exp, i) => (
              <ExpenseTimelineItem
                key={exp._id}
                expense={exp}
                isLast={i === sortedExpenses.length - 1}
              />
            ))}
          </View>
        ) : (
          <View className="mx-5 mb-6 items-center py-8">
            <MaterialIcons name="receipt-long" size={40} color={colors.border} />
            <Text className="mt-3 text-sm text-text-secondary">
              Abhi tak koi kharcha nahi
            </Text>
          </View>
        )}
      </ScrollView>

      {isActive && (
        <View className="border-t border-border bg-surface px-5 pb-6 pt-4">
          <BigButton
            title="Trip Khatam Karo"
            onPress={() => navigation.navigate("TripComplete", { tripId })}
            variant="primary"
            icon="check-circle"
          />
          <Pressable
            className="mt-3 min-h-touch flex-row items-center justify-center gap-2"
            onPress={() => navigation.navigate("AddExpense", { tripId })}
          >
            <MaterialIcons name="add" size={18} color={colors.textSecondary} />
            <Text className="text-body font-semibold text-text-secondary">Kharcha Daalo</Text>
          </Pressable>
        </View>
      )}
    </SafeAreaView>
  );
}
