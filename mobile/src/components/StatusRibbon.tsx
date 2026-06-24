import { Pressable, Text, View } from "react-native";
import { MaterialIcons } from "@expo/vector-icons";
import { colors } from "../theme/tokens";

type RibbonVariant = "trip-active" | "sync-pending" | "offline";

interface StatusRibbonProps {
  variant: RibbonVariant;
  message: string;
  onPress?: () => void;
}

const VARIANT_CONFIG: Record<
  RibbonVariant,
  { bg: string; icon: keyof typeof MaterialIcons.glyphMap; iconColor: string }
> = {
  "trip-active": {
    bg: "bg-primary/15",
    icon: "local-shipping",
    iconColor: colors.primary,
  },
  "sync-pending": {
    bg: "bg-text-secondary/10",
    icon: "sync",
    iconColor: colors.textSecondary,
  },
  offline: {
    bg: "bg-loss/15",
    icon: "cloud-off",
    iconColor: colors.loss,
  },
};

export function StatusRibbon({ variant, message, onPress }: StatusRibbonProps) {
  const config = VARIANT_CONFIG[variant];
  const Container = onPress ? Pressable : View;

  return (
    <Container
      className={`min-h-touch flex-row items-center gap-3 px-5 py-2 ${config.bg}`}
      {...(onPress ? { onPress, accessibilityRole: "button" as const } : {})}
    >
      <MaterialIcons name={config.icon} size={20} color={config.iconColor} />
      <Text className="flex-1 text-sm font-semibold text-text-primary">
        {message}
      </Text>
      {onPress && (
        <MaterialIcons name="chevron-right" size={20} color={colors.textSecondary} />
      )}
    </Container>
  );
}
