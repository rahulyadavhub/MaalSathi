import { useEffect } from "react";
import { Pressable, View } from "react-native";
import { MaterialIcons } from "@expo/vector-icons";
import Animated, {
  useSharedValue,
  useAnimatedStyle,
  withRepeat,
  withTiming,
  withDelay,
  withSequence,
  cancelAnimation,
  Easing,
  interpolate,
} from "react-native-reanimated";
import * as Haptics from "expo-haptics";
import { colors } from "../theme/tokens";

export type VoiceButtonState = "idle" | "listening" | "processing";

interface VoiceButtonProps {
  state: VoiceButtonState;
  onPress: () => void;
  size?: number;
}

const AnimatedPressable = Animated.createAnimatedComponent(Pressable);

function PulseRing({ size, delay, active }: { size: number; delay: number; active: boolean }) {
  const progress = useSharedValue(0);

  useEffect(() => {
    if (active) {
      progress.value = 0;
      progress.value = withDelay(
        delay,
        withRepeat(
          withTiming(1, { duration: 1600, easing: Easing.out(Easing.ease) }),
          -1,
          false,
        ),
      );
    } else {
      cancelAnimation(progress);
      progress.value = withTiming(0, { duration: 300 });
    }
  }, [active, delay, progress]);

  const animatedStyle = useAnimatedStyle(() => ({
    position: "absolute" as const,
    width: size,
    height: size,
    borderRadius: size / 2,
    borderWidth: 2,
    borderColor: colors.primary,
    opacity: interpolate(progress.value, [0, 1], [0.6, 0]),
    transform: [{ scale: interpolate(progress.value, [0, 1], [1, 1.8]) }],
  }));

  return <Animated.View style={animatedStyle} />;
}

export function VoiceButton({ state, onPress, size = 80 }: VoiceButtonProps) {
  const scale = useSharedValue(1);
  const spinRotation = useSharedValue(0);

  useEffect(() => {
    if (state === "listening") {
      scale.value = withRepeat(
        withSequence(
          withTiming(1.08, { duration: 800, easing: Easing.inOut(Easing.ease) }),
          withTiming(1.0, { duration: 800, easing: Easing.inOut(Easing.ease) }),
        ),
        -1,
        true,
      );
    } else {
      cancelAnimation(scale);
      scale.value = withTiming(1, { duration: 200 });
    }

    if (state === "processing") {
      spinRotation.value = withRepeat(
        withTiming(360, { duration: 1000, easing: Easing.linear }),
        -1,
        false,
      );
    } else {
      cancelAnimation(spinRotation);
      spinRotation.value = withTiming(0, { duration: 200 });
    }
  }, [state, scale, spinRotation]);

  const buttonStyle = useAnimatedStyle(() => ({
    width: size,
    height: size,
    borderRadius: size / 2,
    transform: [{ scale: scale.value }],
  }));

  const spinStyle = useAnimatedStyle(() => ({
    transform: [{ rotate: `${spinRotation.value}deg` }],
  }));

  const handlePress = () => {
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Medium);
    onPress();
  };

  const isListening = state === "listening";
  const isProcessing = state === "processing";

  return (
    <View
      style={{ width: size * 2, height: size * 2, alignItems: "center", justifyContent: "center" }}
      accessibilityRole="button"
      accessibilityLabel={
        isListening ? "Sun raha hai, band karne ke liye dabao" :
        isProcessing ? "Process ho raha hai" :
        "Bol ke batao, mic dabao"
      }
    >
      {isListening && (
        <>
          <PulseRing size={size} delay={0} active />
          <PulseRing size={size} delay={500} active />
          <PulseRing size={size} delay={1000} active />
        </>
      )}

      <AnimatedPressable
        onPress={handlePress}
        disabled={isProcessing}
        style={[
          buttonStyle,
          {
            alignItems: "center",
            justifyContent: "center",
            backgroundColor: isListening ? colors.primary : colors.surfaceElevated,
            borderWidth: isListening ? 0 : 2,
            borderColor: isListening ? colors.primary : colors.border,
          },
        ]}
      >
        {isProcessing ? (
          <Animated.View style={spinStyle}>
            <MaterialIcons name="sync" size={size * 0.4} color={colors.textSecondary} />
          </Animated.View>
        ) : (
          <MaterialIcons
            name={isListening ? "mic" : "mic-none"}
            size={size * 0.45}
            color={isListening ? colors.dark : colors.textSecondary}
          />
        )}
      </AnimatedPressable>
    </View>
  );
}
