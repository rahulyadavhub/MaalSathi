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
import type { Language } from "../types/user";
import { useAppStore } from "../store/useAppStore";
import { colors } from "../theme/tokens";

type Props = NativeStackScreenProps<RootStackParamList, "LanguageSelect">;

const LANGUAGES: { value: Language; label: string; sublabel: string; script: string }[] = [
  { value: "hindi", label: "हिंदी", sublabel: "Sab kuch Hindi mein", script: "अ" },
  { value: "hinglish", label: "Hinglish", sublabel: "Hindi + English mix", script: "Hi" },
  { value: "english", label: "English", sublabel: "Everything in English", script: "En" },
];

function AnimatedOption({
  lang,
  index,
  onSelect,
}: {
  lang: (typeof LANGUAGES)[number];
  index: number;
  onSelect: (v: Language) => void;
}) {
  const opacity = useSharedValue(0);
  const translateY = useSharedValue(30);

  useEffect(() => {
    opacity.value = withDelay(
      200 + index * 120,
      withTiming(1, { duration: 400, easing: Easing.out(Easing.ease) }),
    );
    translateY.value = withDelay(
      200 + index * 120,
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
        className="min-h-cta flex-row items-center gap-4 rounded-card border border-border bg-surface-elevated px-5 py-4 active:border-text-secondary active:bg-surface"
        onPress={() => {
          Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light);
          onSelect(lang.value);
        }}
        accessibilityRole="button"
        accessibilityLabel={`${lang.label} language select karo`}
      >
        <View className="h-12 w-12 items-center justify-center rounded-full bg-surface">
          <Text className="text-lg font-bold text-text-primary">{lang.script}</Text>
        </View>
        <View className="flex-1">
          <Text className="text-xl font-semibold text-text-primary">
            {lang.label}
          </Text>
          <Text className="text-sm text-text-secondary">{lang.sublabel}</Text>
        </View>
        <MaterialIcons name="chevron-right" size={24} color={colors.textSecondary} />
      </Pressable>
    </Animated.View>
  );
}

export function LanguageSelectScreen({ navigation }: Props) {
  const setLanguage = useAppStore((s) => s.setLanguage);

  const brandOpacity = useSharedValue(0);
  const brandTranslateY = useSharedValue(20);

  useEffect(() => {
    brandOpacity.value = withTiming(1, { duration: 600 });
    brandTranslateY.value = withTiming(0, { duration: 600, easing: Easing.out(Easing.ease) });
  }, [brandOpacity, brandTranslateY]);

  const brandStyle = useAnimatedStyle(() => ({
    opacity: brandOpacity.value,
    transform: [{ translateY: brandTranslateY.value }],
  }));

  const handleSelect = async (lang: Language) => {
    await setLanguage(lang);
    navigation.replace("RoleSelect");
  };

  return (
    <SafeAreaView className="flex-1 bg-dark">
      <View className="flex-1 justify-between px-6 pb-10 pt-16">
        <Animated.View style={brandStyle}>
          <View className="mb-4 h-16 w-16 items-center justify-center rounded-2xl bg-primary">
            <MaterialIcons name="local-shipping" size={36} color={colors.dark} />
          </View>
          <Text className="text-4xl font-bold text-text-primary">
            MaalSaathi
          </Text>
          <Text className="mt-2 text-lg text-text-secondary">
            Apna transport business{"\n"}smart banao
          </Text>
        </Animated.View>

        <View>
          <Text className="mb-5 text-body font-semibold text-text-secondary">
            Apni bhasha chuniye
          </Text>
          <View className="gap-3">
            {LANGUAGES.map((lang, i) => (
              <AnimatedOption
                key={lang.value}
                lang={lang}
                index={i}
                onSelect={handleSelect}
              />
            ))}
          </View>
        </View>
      </View>
    </SafeAreaView>
  );
}
