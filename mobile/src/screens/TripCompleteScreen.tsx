import { useState, useEffect } from "react";
import { View, Text, ActivityIndicator } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import * as Haptics from "expo-haptics";
import type { NativeStackScreenProps } from "@react-navigation/native-stack";
import type { TripStackParamList } from "../types/navigation";
import { useTripStore } from "../store/useTripStore";
import { ShareCard } from "../components/ShareCard";
import { BigButton } from "../components/BigButton";
import { colors } from "../theme/tokens";

type Props = NativeStackScreenProps<TripStackParamList, "TripComplete">;

export function TripCompleteScreen({ navigation, route }: Props) {
  const { tripId } = route.params;
  const { trips, completeTrip } = useTripStore();
  const [completing, setCompleting] = useState(false);
  const [done, setDone] = useState(false);

  const trip = trips.find((t) => t._id === tripId);

  useEffect(() => {
    if (!trip || trip.status === "completed") {
      setDone(true);
      return;
    }

    setCompleting(true);
    completeTrip(tripId).then(() => {
      Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success);
      setCompleting(false);
      setDone(true);
    });
  }, [tripId, trip?.status, completeTrip]);

  if (completing || !done || !trip) {
    return (
      <SafeAreaView className="flex-1 items-center justify-center bg-dark">
        <ActivityIndicator size="large" color={colors.primary} />
        <Text className="mt-4 text-lg text-text-secondary">
          Trip complete ho rahi hai...
        </Text>
      </SafeAreaView>
    );
  }

  const completedTrip = trips.find((t) => t._id === tripId) || trip;

  return (
    <SafeAreaView className="flex-1 bg-dark">
      <View className="flex-1 justify-center">
        <View className="mb-6 items-center">
          <Text className="text-2xl font-bold text-primary">
            Trip Complete!
          </Text>
          <Text className="mt-1 text-body text-text-secondary">
            Final hisaab neeche hai
          </Text>
        </View>

        <ShareCard trip={completedTrip} />
      </View>

      <View className="px-5 pb-6">
        <BigButton
          title="Wapas Jao"
          onPress={() => navigation.popToTop()}
          variant="secondary"
          icon="home"
        />
      </View>
    </SafeAreaView>
  );
}
