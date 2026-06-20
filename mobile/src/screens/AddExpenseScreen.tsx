import { useState, useRef } from "react";
import {
  View,
  Text,
  ScrollView,
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
import {
  EXPENSE_CATEGORIES,
  CATEGORY_LABELS,
  CATEGORY_ICONS,
  type ExpenseCategory,
} from "../types/expense";
import { useTripStore } from "../store/useTripStore";
import { FormField } from "../components/FormField";
import { colors } from "../theme/tokens";

type Props = NativeStackScreenProps<TripStackParamList, "AddExpense">;

const QUICK_CATEGORIES: ExpenseCategory[] = [
  "diesel",
  "toll",
  "food",
  "repair",
  "parking",
  "loading",
];

export function AddExpenseScreen({ navigation, route }: Props) {
  const { tripId } = route.params;
  const addExpense = useTripStore((s) => s.addExpense);

  const [category, setCategory] = useState<ExpenseCategory | null>(null);
  const [amount, setAmount] = useState("");
  const [note, setNote] = useState("");
  const [loading, setLoading] = useState(false);
  const [showAllCategories, setShowAllCategories] = useState(false);

  const amountRef = useRef<TextInput>(null);
  const noteRef = useRef<TextInput>(null);

  const parsedAmount = parseFloat(amount) || 0;
  const canSubmit = category !== null && parsedAmount > 0 && !loading;

  const handleSubmit = async () => {
    if (!canSubmit || !category) return;
    setLoading(true);
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Medium);

    await addExpense(tripId, category, parsedAmount, note.trim() || undefined, "manual");
    Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success);
    navigation.goBack();
  };

  const visibleCategories = showAllCategories
    ? EXPENSE_CATEGORIES
    : QUICK_CATEGORIES;

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
        <Text className="text-2xl font-bold text-text-primary">Kharcha Daalo</Text>
      </View>

      <KeyboardAvoidingView
        className="flex-1"
        behavior={Platform.OS === "ios" ? "padding" : "height"}
      >
        <ScrollView
          className="flex-1"
          contentContainerClassName="px-5 pb-8 pt-4"
          keyboardShouldPersistTaps="handled"
          showsVerticalScrollIndicator={false}
        >
          <Text className="mb-3 text-body font-medium text-text-secondary">
            Kya kharcha hai?
          </Text>
          <View className="mb-2 flex-row flex-wrap gap-2">
            {visibleCategories.map((cat) => {
              const selected = category === cat;
              const iconName = CATEGORY_ICONS[cat];
              return (
                <Pressable
                  key={cat}
                  className={`min-h-touch flex-row items-center gap-2 rounded-button px-3.5 py-2 ${
                    selected
                      ? "bg-surface-elevated border border-text-primary"
                      : "bg-surface-elevated border border-border"
                  }`}
                  onPress={() => {
                    setCategory(cat);
                    amountRef.current?.focus();
                  }}
                >
                  <MaterialIcons
                    name={iconName as keyof typeof MaterialIcons.glyphMap}
                    size={16}
                    color={selected ? colors.textPrimary : colors.textSecondary}
                  />
                  <Text
                    className={`text-sm font-medium ${
                      selected ? "text-text-primary" : "text-text-secondary"
                    }`}
                  >
                    {CATEGORY_LABELS[cat]}
                  </Text>
                </Pressable>
              );
            })}
          </View>
          {!showAllCategories && (
            <Pressable
              className="mb-5 flex-row items-center gap-1"
              onPress={() => setShowAllCategories(true)}
            >
              <Text className="text-body font-medium text-text-secondary">
                Aur dikhao
              </Text>
              <MaterialIcons name="expand-more" size={16} color={colors.textSecondary} />
            </Pressable>
          )}

          <View className="mt-2 gap-5">
            <FormField
              label="Kitne rupaye (₹)"
              placeholder="4500"
              value={amount}
              onChangeText={setAmount}
              ref={amountRef}
              keyboardType="numeric"
              returnKeyType="next"
              onSubmitEditing={() => noteRef.current?.focus()}
            />
            <FormField
              label="Kuch aur likhna hai?"
              placeholder="HP pump Nashik"
              value={note}
              onChangeText={setNote}
              ref={noteRef}
              autoCapitalize="sentences"
              returnKeyType="done"
              onSubmitEditing={handleSubmit}
            />
          </View>
        </ScrollView>
      </KeyboardAvoidingView>

      <View className="border-t border-border bg-surface px-5 pb-6 pt-4">
        <Pressable
          className={`min-h-cta flex-row items-center justify-center gap-2 rounded-button ${
            canSubmit ? "bg-primary active:opacity-80" : "bg-surface-elevated opacity-50"
          }`}
          onPress={handleSubmit}
          disabled={!canSubmit}
        >
          <MaterialIcons
            name="check"
            size={22}
            color={canSubmit ? colors.dark : colors.textSecondary}
          />
          <Text
            className={`text-lg font-bold ${canSubmit ? "text-dark" : "text-text-secondary"}`}
          >
            {category && parsedAmount > 0
              ? `₹${parsedAmount.toLocaleString("en-IN")} ${CATEGORY_LABELS[category]} Save Karo`
              : "Kharcha Save Karo"}
          </Text>
        </Pressable>
      </View>
    </SafeAreaView>
  );
}
