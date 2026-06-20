import { useRef, useEffect } from "react";
import {
  View,
  Text,
  Pressable,
  KeyboardAvoidingView,
  Platform,
  ScrollView,
  TextInput,
  ActivityIndicator,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import Animated, {
  useSharedValue,
  useAnimatedStyle,
  withTiming,
  Easing,
} from "react-native-reanimated";
import { MaterialIcons } from "@expo/vector-icons";
import * as Haptics from "expo-haptics";
import type { NativeStackScreenProps } from "@react-navigation/native-stack";
import type { RootStackParamList } from "../types/navigation";
import { useOnboarding } from "../hooks/useOnboarding";
import { FormField } from "../components/FormField";
import { colors } from "../theme/tokens";

type Props = NativeStackScreenProps<RootStackParamList, "Onboarding">;

export function OnboardingScreen({ navigation }: Props) {
  const {
    phone,
    name,
    truckNumber,
    loading,
    error,
    setPhone,
    setName,
    setTruckNumber,
    submit,
    phoneValidation,
    nameValid,
    truckValidation,
    canSubmit,
  } = useOnboarding();

  const nameRef = useRef<TextInput>(null);
  const truckRef = useRef<TextInput>(null);

  const formOpacity = useSharedValue(0);

  useEffect(() => {
    formOpacity.value = withTiming(1, { duration: 500, easing: Easing.out(Easing.ease) });
  }, [formOpacity]);

  const formStyle = useAnimatedStyle(() => ({
    opacity: formOpacity.value,
  }));

  const handleSubmit = async () => {
    if (!canSubmit) return;
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Medium);
    const success = await submit();
    if (success) {
      Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success);
      navigation.replace("MainTabs", { screen: "TripTab", params: { screen: "Home" } });
    }
  };

  const phoneTouched = phone.length > 0;
  const nameTouched = name.length > 0;
  const truckTouched = truckNumber.length > 0;

  return (
    <SafeAreaView className="flex-1 bg-dark">
      <KeyboardAvoidingView
        className="flex-1"
        behavior={Platform.OS === "ios" ? "padding" : "height"}
        keyboardVerticalOffset={Platform.OS === "ios" ? 0 : 20}
      >
        <ScrollView
          className="flex-1"
          contentContainerClassName="flex-grow justify-end"
          keyboardShouldPersistTaps="handled"
          showsVerticalScrollIndicator={false}
        >
          <Animated.View className="px-6 pb-10" style={formStyle}>
            <View className="mb-8">
              <Text className="mb-1 text-3xl font-bold text-text-primary">
                Apni jaankari dijiye
              </Text>
              <Text className="text-lg text-text-secondary">
                Bas 3 cheezein — aur shuru!
              </Text>
            </View>

            <View className="gap-5">
              <FormField
                label="Mobile Number"
                placeholder="9876543210"
                value={phone}
                onChangeText={setPhone}
                keyboardType="phone-pad"
                maxLength={10}
                returnKeyType="next"
                onSubmitEditing={() => nameRef.current?.focus()}
                error={
                  phoneTouched && !phoneValidation.valid
                    ? "10 digit ka mobile number daalo"
                    : null
                }
                hint="Jaise: 9876543210"
              />

              <FormField
                label="Naam"
                placeholder="Apna naam likho"
                value={name}
                onChangeText={setName}
                ref={nameRef}
                autoCapitalize="words"
                maxLength={50}
                returnKeyType="next"
                onSubmitEditing={() => truckRef.current?.focus()}
                error={
                  nameTouched && !nameValid
                    ? "Kam se kam 2 akshar ka naam daalo"
                    : null
                }
              />

              <FormField
                label="Truck Number"
                placeholder="MH12AB1234"
                value={truckNumber}
                onChangeText={setTruckNumber}
                ref={truckRef}
                autoCapitalize="characters"
                maxLength={13}
                returnKeyType="done"
                onSubmitEditing={handleSubmit}
                error={
                  truckTouched && !truckValidation.valid
                    ? "Jaise: MH12AB1234 ya DL1CAB5678"
                    : null
                }
              />

              {error && (
                <View className="flex-row items-center gap-2 rounded-button bg-loss/10 px-4 py-3">
                  <MaterialIcons name="error-outline" size={20} color={colors.loss} />
                  <Text className="flex-1 text-body text-loss">{error}</Text>
                </View>
              )}

              <Pressable
                className={`mt-2 min-h-cta flex-row items-center justify-center gap-2 rounded-button ${
                  canSubmit
                    ? "bg-primary active:opacity-80"
                    : "bg-surface-elevated opacity-50"
                }`}
                onPress={handleSubmit}
                disabled={!canSubmit}
                accessibilityRole="button"
                accessibilityLabel="Shuru Karo"
                accessibilityState={{ disabled: !canSubmit }}
              >
                {loading ? (
                  <ActivityIndicator color={colors.dark} />
                ) : (
                  <>
                    <Text
                      className={`text-xl font-bold ${
                        canSubmit ? "text-dark" : "text-text-secondary"
                      }`}
                    >
                      Shuru Karo
                    </Text>
                    <MaterialIcons
                      name="arrow-forward"
                      size={22}
                      color={canSubmit ? colors.dark : colors.textSecondary}
                    />
                  </>
                )}
              </Pressable>
            </View>
          </Animated.View>
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}
