import { useEffect } from "react";
import { View, Text, Pressable } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import Animated, {
  useSharedValue,
  useAnimatedStyle,
  withTiming,
  withDelay,
  Easing,
} from "react-native-reanimated";
import * as Haptics from "expo-haptics";
import { MaterialIcons } from "@expo/vector-icons";
import type { NativeStackScreenProps } from "@react-navigation/native-stack";
import type { RootStackParamList } from "../types/navigation";
import type { UserRole } from "../types/user";
import { useAppStore } from "../store/useAppStore";
import { colors } from "../theme/tokens";

type Props = NativeStackScreenProps<RootStackParamList, "RoleSelect">;

interface RoleConfig {
  value: UserRole;
  label: string;
  icon: keyof typeof MaterialIcons.glyphMap;
  desc: string;
  features: string[];
}

const ROLES: RoleConfig[] = [
  {
    value: "maalik",
    label: "Maalik",
    icon: "local-shipping",
    desc: "Truck owner / Fleet operator",
    features: ["Trip manage karo", "Kharcha track karo", "P&L dekho"],
  },
  {
    value: "driver",
    label: "Driver",
    icon: "person",
    desc: "Truck driver",
    features: ["Apna kharcha daalo", "Trip status dekho", "Hisaab check karo"],
  },
];

function AnimatedRoleCard({
  role,
  index,
  onSelect,
}: {
  role: RoleConfig;
  index: number;
  onSelect: (v: UserRole) => void;
}) {
  const opacity = useSharedValue(0);
  const translateY = useSharedValue(30);

  useEffect(() => {
    opacity.value = withDelay(
      150 + index * 150,
      withTiming(1, { duration: 400 }),
    );
    translateY.value = withDelay(
      150 + index * 150,
      withTiming(0, { duration: 400, easing: Easing.out(Easing.ease) }),
    );
  }, [index, opacity, translateY]);

  const style = useAnimatedStyle(() => ({
    opacity: opacity.value,
    transform: [{ translateY: translateY.value }],
  }));

  return (
    <Animated.View style={style}>
      <Pressable
        className="rounded-card border border-border bg-surface-elevated p-5 active:border-text-secondary active:bg-surface"
        onPress={() => {
          Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Medium);
          onSelect(role.value);
        }}
        accessibilityRole="button"
        accessibilityLabel={`${role.label} - ${role.desc}`}
      >
        <View className="mb-3 flex-row items-center gap-3">
          <View className="h-12 w-12 items-center justify-center rounded-full bg-surface">
            <MaterialIcons name={role.icon} size={26} color={colors.textSecondary} />
          </View>
          <View className="flex-1">
            <Text className="text-xl font-bold text-text-primary">
              {role.label}
            </Text>
            <Text className="text-sm text-text-secondary">{role.desc}</Text>
          </View>
          <MaterialIcons name="chevron-right" size={24} color={colors.textSecondary} />
        </View>

        <View className="gap-1.5 pl-[60px]">
          {role.features.map((f) => (
            <View key={f} className="flex-row items-center gap-2">
              <MaterialIcons name="check" size={14} color={colors.textSecondary} />
              <Text className="text-sm text-text-secondary">{f}</Text>
            </View>
          ))}
        </View>
      </Pressable>
    </Animated.View>
  );
}

export function RoleSelectScreen({ navigation }: Props) {
  const setRole = useAppStore((s) => s.setRole);

  const titleOpacity = useSharedValue(0);

  useEffect(() => {
    titleOpacity.value = withTiming(1, { duration: 500 });
  }, [titleOpacity]);

  const titleStyle = useAnimatedStyle(() => ({
    opacity: titleOpacity.value,
  }));

  const handleSelect = async (role: UserRole) => {
    await setRole(role);
    navigation.replace("Onboarding");
  };

  return (
    <SafeAreaView className="flex-1 bg-dark">
      <View className="flex-1 justify-end px-6 pb-10">
        <Animated.View style={titleStyle}>
          <Text className="mb-2 text-3xl font-bold text-text-primary">
            Aap kaun hain?
          </Text>
          <Text className="mb-8 text-lg text-text-secondary">
            Apna role chuniye — baad mein badal sakte ho
          </Text>
        </Animated.View>

        <View className="gap-4">
          {ROLES.map((role, index) => (
            <AnimatedRoleCard
              key={role.value}
              role={role}
              index={index}
              onSelect={handleSelect}
            />
          ))}
        </View>
      </View>
    </SafeAreaView>
  );
}
