import { useState, useEffect } from "react";
import { View, Text, FlatList, Pressable } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { MaterialIcons } from "@expo/vector-icons";
import type { NativeStackScreenProps } from "@react-navigation/native-stack";
import type { TripStackParamList } from "../types/navigation";
import type { Trip } from "../types/trip";
import { useTripStore } from "../store/useTripStore";
import { TripCard } from "../components/TripCard";
import { colors } from "../theme/tokens";

type Props = NativeStackScreenProps<TripStackParamList, "TripList">;

type Filter = "all" | "active" | "completed";

const FILTERS: { value: Filter; label: string }[] = [
  { value: "all", label: "Sab" },
  { value: "active", label: "Chal Rahi" },
  { value: "completed", label: "Complete" },
];

export function TripListScreen({ navigation }: Props) {
  const { trips, isLoaded, loadTrips } = useTripStore();
  const [filter, setFilter] = useState<Filter>("all");

  useEffect(() => {
    if (!isLoaded) loadTrips();
  }, [isLoaded, loadTrips]);

  const filtered = trips.filter((t: Trip) => {
    if (filter === "active") return t.status === "active";
    if (filter === "completed") return t.status === "completed";
    return true;
  });

  return (
    <SafeAreaView className="flex-1 bg-dark" edges={["top"]}>
      <View className="flex-row items-center justify-between px-5 pb-3 pt-4">
        <View className="flex-row items-center gap-3">
          <Pressable
            className="min-h-touch min-w-touch items-center justify-center"
            onPress={() => navigation.goBack()}
            accessibilityLabel="Back"
          >
            <MaterialIcons name="arrow-back" size={24} color={colors.textPrimary} />
          </Pressable>
          <Text className="text-2xl font-bold text-text-primary">Trips</Text>
        </View>
        <Pressable
          className="min-h-touch flex-row items-center gap-1.5 rounded-button bg-primary px-4 active:opacity-80"
          onPress={() => navigation.navigate("NewTrip")}
          accessibilityLabel="Nayi Trip"
        >
          <MaterialIcons name="add" size={20} color={colors.dark} />
          <Text className="text-sm font-bold text-dark">Nayi Trip</Text>
        </Pressable>
      </View>

      <View className="mb-3 flex-row gap-2 px-5">
        {FILTERS.map((f) => (
          <Pressable
            key={f.value}
            className={`rounded-full px-4 py-2 ${
              filter === f.value
                ? "bg-surface-elevated"
                : "bg-surface"
            }`}
            onPress={() => setFilter(f.value)}
          >
            <Text
              className={`text-sm font-semibold ${
                filter === f.value ? "text-text-primary" : "text-text-secondary"
              }`}
            >
              {f.label}
            </Text>
          </Pressable>
        ))}
      </View>

      <FlatList
        data={filtered}
        keyExtractor={(item) => item._id}
        renderItem={({ item }) => (
          <TripCard
            trip={item}
            onPress={() => navigation.navigate("TripDetail", { tripId: item._id })}
          />
        )}
        contentContainerStyle={filtered.length === 0 ? { flex: 1 } : undefined}
        contentContainerClassName="gap-3 pb-6"
        ListEmptyComponent={
          <View className="flex-1 items-center justify-center px-6">
            <MaterialIcons name="local-shipping" size={56} color={colors.border} />
            <Text className="mt-4 text-lg font-semibold text-text-secondary">
              Koi trip nahi hai
            </Text>
            <Text className="mt-1 text-sm text-text-secondary">
              "Nayi Trip" pe tap karo shuru karne ke liye
            </Text>
          </View>
        }
      />
    </SafeAreaView>
  );
}
