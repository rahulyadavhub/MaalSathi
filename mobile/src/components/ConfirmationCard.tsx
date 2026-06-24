import { View, Text, Pressable, ActivityIndicator } from "react-native";
import { MaterialIcons } from "@expo/vector-icons";
import * as Haptics from "expo-haptics";
import { MoneyPill } from "./MoneyPill";
import { colors } from "../theme/tokens";

interface DataRow {
  label: string;
  value: string;
  icon?: keyof typeof MaterialIcons.glyphMap;
}

interface ConfirmationCardProps {
  title: string;
  data: DataRow[];
  amount?: { value: number; label?: string };
  onConfirm: () => void;
  onEdit?: () => void;
  onCancel: () => void;
  loading?: boolean;
  confirmLabel?: string;
  cancelLabel?: string;
}

export function ConfirmationCard({
  title,
  data,
  amount,
  onConfirm,
  onEdit,
  onCancel,
  loading = false,
  confirmLabel = "Confirm",
  cancelLabel = "Cancel",
}: ConfirmationCardProps) {
  const handleConfirm = () => {
    Haptics.notificationAsync(Haptics.NotificationFeedbackType.Success);
    onConfirm();
  };

  const handleCancel = () => {
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light);
    onCancel();
  };

  return (
    <View className="mx-4 rounded-card border border-border bg-surface-elevated p-5">
      <Text className="mb-4 text-xl font-bold text-text-primary">{title}</Text>

      <View className="mb-4 gap-3">
        {data.map((row) => (
          <View key={row.label} className="flex-row items-center gap-3">
            {row.icon && (
              <MaterialIcons name={row.icon} size={20} color={colors.textSecondary} />
            )}
            <View className="flex-1">
              <Text className="text-sm text-text-secondary">{row.label}</Text>
              <Text className="text-lg font-medium text-text-primary">{row.value}</Text>
            </View>
          </View>
        ))}
      </View>

      {amount && (
        <View className="mb-5 rounded-button bg-dark px-4 py-3">
          <MoneyPill
            amount={amount.value}
            label={amount.label}
            size="lg"
            neutral
            showSign={false}
          />
        </View>
      )}

      <View className="gap-3">
        <Pressable
          className="min-h-cta items-center justify-center rounded-button bg-primary active:opacity-80"
          onPress={handleConfirm}
          disabled={loading}
          accessibilityRole="button"
          accessibilityLabel={confirmLabel}
        >
          {loading ? (
            <ActivityIndicator color={colors.dark} />
          ) : (
            <Text className="text-lg font-bold text-dark">{confirmLabel}</Text>
          )}
        </Pressable>

        <View className="flex-row gap-3">
          {onEdit && (
            <Pressable
              className="min-h-touch flex-1 items-center justify-center rounded-button border border-border bg-surface active:bg-surface-elevated"
              onPress={onEdit}
              accessibilityRole="button"
              accessibilityLabel="Edit"
            >
              <Text className="text-body font-semibold text-text-primary">Edit</Text>
            </Pressable>
          )}

          <Pressable
            className="min-h-touch flex-1 items-center justify-center rounded-button active:bg-surface"
            onPress={handleCancel}
            accessibilityRole="button"
            accessibilityLabel={cancelLabel}
          >
            <Text className="text-body font-semibold text-loss">{cancelLabel}</Text>
          </Pressable>
        </View>
      </View>
    </View>
  );
}
