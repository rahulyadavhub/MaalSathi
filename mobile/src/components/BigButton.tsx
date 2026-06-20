import { Pressable, Text, ActivityIndicator, View } from "react-native";
import { MaterialIcons } from "@expo/vector-icons";
import * as Haptics from "expo-haptics";
import { colors } from "../theme/tokens";

type ButtonVariant = "primary" | "secondary" | "danger";

interface BigButtonProps {
  title: string;
  onPress: () => void;
  variant?: ButtonVariant;
  loading?: boolean;
  disabled?: boolean;
  icon?: keyof typeof MaterialIcons.glyphMap;
}

const VARIANT_STYLES: Record<
  ButtonVariant,
  { bg: string; text: string; loaderColor: string; activeBg: string }
> = {
  primary: {
    bg: "bg-primary",
    text: "text-dark",
    loaderColor: colors.dark,
    activeBg: "active:opacity-80",
  },
  secondary: {
    bg: "bg-surface-elevated border border-border",
    text: "text-text-primary",
    loaderColor: colors.textPrimary,
    activeBg: "active:bg-surface",
  },
  danger: {
    bg: "bg-loss",
    text: "text-text-primary",
    loaderColor: colors.textPrimary,
    activeBg: "active:opacity-80",
  },
};

export function BigButton({
  title,
  onPress,
  variant = "primary",
  loading = false,
  disabled = false,
  icon,
}: BigButtonProps) {
  const config = VARIANT_STYLES[variant];
  const isDisabled = disabled || loading;

  const handlePress = () => {
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Medium);
    onPress();
  };

  return (
    <Pressable
      className={`min-h-cta flex-row items-center justify-center gap-2 rounded-button ${config.bg} ${config.activeBg} ${isDisabled ? "opacity-50" : ""}`}
      onPress={handlePress}
      disabled={isDisabled}
      accessibilityRole="button"
      accessibilityLabel={title}
      accessibilityState={{ disabled: isDisabled }}
    >
      {loading ? (
        <ActivityIndicator color={config.loaderColor} />
      ) : (
        <View className="flex-row items-center gap-2">
          {icon && (
            <MaterialIcons
              name={icon}
              size={22}
              color={variant === "primary" ? colors.dark : colors.textPrimary}
            />
          )}
          <Text className={`text-lg font-bold ${config.text}`}>{title}</Text>
        </View>
      )}
    </Pressable>
  );
}
