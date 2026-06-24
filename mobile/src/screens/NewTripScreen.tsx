import { useState, useRef, useEffect } from "react";
import {
  View,
  Text,
  Pressable,
  KeyboardAvoidingView,
  Platform,
  TextInput,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { MaterialIcons } from "@expo/vector-icons";
import * as Haptics from "expo-haptics";
import type { NativeStackScreenProps } from "@react-navigation/native-stack";
import type { TripStackParamList } from "../types/navigation";
import { useTripStore } from "../store/useTripStore";
import { FormField } from "../components/FormField";
import { ConfirmationCard } from "../components/ConfirmationCard";
import { colors } from "../theme/tokens";

type Props = NativeStackScreenProps<TripStackParamList, "NewTrip">;

type Step = 1 | 2 | 3 | "preview";

export function NewTripScreen({ navigation }: Props) {
  const createTrip = useTripStore((s) => s.createTrip);

  const [step, setStep] = useState<Step>(1);
  const [loading, setLoading] = useState(false);

  const [origin, setOrigin] = useState("");
  const [destination, setDestination] = useState("");
  const [cargoType, setCargoType] = useState("");
  const [cargoWeight, setCargoWeight] = useState("");
  const [freight, setFreight] = useState("");
  const [advance, setAdvance] = useState("");

  const destRef = useRef<TextInput>(null);
  const field2Ref = useRef<TextInput>(null);

  useEffect(() => {
    if (step === 2 || step === 3) {
      setTimeout(() => field2Ref.current?.focus(), 100);
    }
  }, [step]);

  const parsedFreight = parseFloat(freight) || 0;
  const parsedAdvance = parseFloat(advance) || 0;
  const parsedWeight = parseFloat(cargoWeight) || undefined;

  const canGoStep2 = origin.trim().length >= 2 && destination.trim().length >= 2;

  const handleNext = () => {
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light);
    if (step === 1 && canGoStep2) setStep(2);
    else if (step === 2) setStep(3);
    else if (step === 3) setStep("preview");
  };

  const handleSkip = () => {
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light);
    if (step === 2) setStep(3);
    else if (step === 3) setStep("preview");
  };

  const handleConfirm = async () => {
    setLoading(true);
    try {
      const trip = await createTrip({
        origin: origin.trim(),
        destination: destination.trim(),
        cargoType: cargoType.trim() || undefined,
        cargoWeightTons: parsedWeight,
        freightAmount: parsedFreight,
        advancePaid: parsedAdvance,
      });
      Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success);
      navigation.replace("TripDetail", { tripId: trip._id });
    } finally {
      setLoading(false);
    }
  };

  if (step === "preview") {
    const confirmData = [
      { label: "Kahan se", value: origin.trim(), icon: "my-location" as const },
      { label: "Kahan tak", value: destination.trim(), icon: "place" as const },
      ...(cargoType.trim()
        ? [{ label: "Maal", value: `${cargoType.trim()}${parsedWeight ? ` — ${parsedWeight} ton` : ""}`, icon: "inventory" as const }]
        : []),
      ...(parsedAdvance > 0
        ? [{ label: "Advance", value: `₹${parsedAdvance.toLocaleString("en-IN")}`, icon: "payments" as const }]
        : []),
    ];

    return (
      <SafeAreaView className="flex-1 bg-dark">
        <View className="flex-1 justify-end pb-8">
          <ConfirmationCard
            title="Trip Confirm Karo"
            data={confirmData}
            amount={parsedFreight > 0 ? { value: parsedFreight, label: "Bhada" } : undefined}
            onConfirm={handleConfirm}
            onEdit={() => setStep(1)}
            onCancel={() => navigation.goBack()}
            loading={loading}
            confirmLabel="Haan, Sahi Hai"
            cancelLabel="Cancel"
          />
        </View>
      </SafeAreaView>
    );
  }

  const stepTitle = step === 1
    ? "Kahan jaana hai?"
    : step === 2
      ? "Kya maal hai?"
      : "Kitna bhada?";

  const stepHint = step === 1
    ? "Bas do jagah batao — trip shuru"
    : step === 2
      ? "Nahi pata toh chhod do"
      : "Nahi pata toh chhod do";

  const isOptionalStep = step === 2 || step === 3;
  const canProceed = step === 1 ? canGoStep2 : true;

  return (
    <SafeAreaView className="flex-1 bg-dark" edges={["top"]}>
      <View className="flex-row items-center gap-3 px-5 pb-2 pt-4">
        <Pressable
          className="min-h-touch min-w-touch items-center justify-center"
          onPress={() => {
            if (step === 1) navigation.goBack();
            else setStep((step - 1) as Step);
          }}
          accessibilityLabel="Wapas"
        >
          <MaterialIcons name="arrow-back" size={24} color={colors.textPrimary} />
        </Pressable>
        <Text className="flex-1 text-2xl font-bold text-text-primary">Nayi Trip</Text>
        <Text className="text-body text-text-secondary">{step}/3</Text>
      </View>

      <KeyboardAvoidingView
        className="flex-1"
        behavior={Platform.OS === "ios" ? "padding" : "height"}
      >
        <View className="flex-1 justify-end px-5 pb-8">
          <View className="mb-8">
            <Text className="text-xl font-bold text-text-primary">{stepTitle}</Text>
            <Text className="mt-1 text-body text-text-secondary">{stepHint}</Text>
          </View>

          {step === 1 && (
            <View className="gap-5">
              <FormField
                label="Kahan se"
                placeholder="Mumbai"
                value={origin}
                onChangeText={setOrigin}
                autoCapitalize="words"
                returnKeyType="next"
                onSubmitEditing={() => destRef.current?.focus()}
              />
              <FormField
                label="Kahan tak"
                placeholder="Delhi"
                value={destination}
                onChangeText={setDestination}
                ref={destRef}
                autoCapitalize="words"
                returnKeyType="done"
                onSubmitEditing={handleNext}
              />
            </View>
          )}

          {step === 2 && (
            <View className="gap-5">
              <FormField
                label="Maal ka type"
                placeholder="Steel, Cement, etc."
                value={cargoType}
                onChangeText={setCargoType}
                ref={field2Ref}
                autoCapitalize="words"
                returnKeyType="done"
                onSubmitEditing={handleNext}
              />
              <FormField
                label="Kitna ton"
                placeholder="20"
                value={cargoWeight}
                onChangeText={setCargoWeight}
                keyboardType="numeric"
                returnKeyType="done"
                onSubmitEditing={handleNext}
              />
            </View>
          )}

          {step === 3 && (
            <View className="gap-5">
              <FormField
                label="Bhada (₹)"
                placeholder="85000"
                value={freight}
                onChangeText={setFreight}
                ref={field2Ref}
                keyboardType="numeric"
                returnKeyType="done"
                onSubmitEditing={handleNext}
              />
              <FormField
                label="Advance (₹)"
                placeholder="20000"
                value={advance}
                onChangeText={setAdvance}
                keyboardType="numeric"
                returnKeyType="done"
                onSubmitEditing={handleNext}
              />
            </View>
          )}
        </View>
      </KeyboardAvoidingView>

      <View className="border-t border-border bg-surface px-5 pb-6 pt-4">
        <Pressable
          className={`min-h-cta flex-row items-center justify-center gap-2 rounded-button ${
            canProceed ? "bg-primary active:opacity-80" : "bg-surface-elevated opacity-50"
          }`}
          onPress={handleNext}
          disabled={!canProceed}
        >
          <Text className={`text-lg font-bold ${canProceed ? "text-dark" : "text-text-secondary"}`}>
            {step === 3 ? "Dekho aur Confirm Karo" : "Aage"}
          </Text>
          <MaterialIcons name="arrow-forward" size={20} color={canProceed ? colors.dark : colors.textSecondary} />
        </Pressable>

        {isOptionalStep && (
          <Pressable className="mt-3 min-h-touch items-center justify-center" onPress={handleSkip}>
            <Text className="text-body text-text-secondary">Chhod do, aage jao</Text>
          </Pressable>
        )}
      </View>
    </SafeAreaView>
  );
}
