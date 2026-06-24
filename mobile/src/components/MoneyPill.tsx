import { View, Text } from "react-native";
import { MaterialIcons } from "@expo/vector-icons";
import { colors } from "../theme/tokens";

type MoneyPillSize = "sm" | "md" | "lg";

interface MoneyPillProps {
  amount: number;
  label?: string;
  size?: MoneyPillSize;
  showSign?: boolean;
  neutral?: boolean;
}

const SIZE_CONFIG: Record<MoneyPillSize, { text: string; icon: number; label: string }> = {
  sm: { text: "text-lg font-bold", icon: 16, label: "text-sm" },
  md: { text: "text-2xl font-bold", icon: 20, label: "text-sm" },
  lg: { text: "text-money font-bold", icon: 24, label: "text-body" },
};

function formatAmount(amount: number): string {
  const abs = Math.abs(amount);
  if (abs >= 10_000_000) return `${(abs / 10_000_000).toFixed(1)} Cr`;
  if (abs >= 100_000) return `${(abs / 100_000).toFixed(1)} L`;
  return abs.toLocaleString("en-IN");
}

export function MoneyPill({
  amount,
  label,
  size = "md",
  showSign = true,
  neutral = false,
}: MoneyPillProps) {
  const isProfit = amount >= 0;
  const config = SIZE_CONFIG[size];
  const color = neutral ? colors.textPrimary : isProfit ? colors.primary : colors.loss;
  const iconName = isProfit ? "arrow-upward" : "arrow-downward";
  const sign = showSign ? (isProfit ? "+" : "-") : "";

  return (
    <View className="flex-row items-center gap-1">
      <View className="items-start">
        {label && (
          <Text className={`${config.label} text-text-secondary mb-0.5`}>
            {label}
          </Text>
        )}
        <View className="flex-row items-center gap-1">
          {!neutral && (
            <MaterialIcons name={iconName} size={config.icon} color={color} />
          )}
          <Text className={config.text} style={{ color }}>
            {sign}₹{formatAmount(amount)}
          </Text>
        </View>
      </View>
    </View>
  );
}
