import { useState, useEffect, useCallback } from "react";
import { View, Text, Pressable } from "react-native";
import { MaterialIcons } from "@expo/vector-icons";
import * as Haptics from "expo-haptics";
import { useAppStore } from "../store/useAppStore";
import { getSyncSummary, retryFailed, type SyncSummary } from "../offline/syncQueue";
import { colors } from "../theme/tokens";

export function SyncStatusBar() {
  const { isOnline, syncPending, syncFailed } = useAppStore();
  const [expanded, setExpanded] = useState(false);
  const [summary, setSummary] = useState<SyncSummary | null>(null);
  const [retrying, setRetrying] = useState(false);

  const total = syncPending + syncFailed;

  useEffect(() => {
    if (expanded && total > 0) {
      getSyncSummary().then(setSummary);
    }
  }, [expanded, total]);

  const handleRetry = useCallback(async () => {
    setRetrying(true);
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Medium);
    await retryFailed();
    const updated = await getSyncSummary();
    setSummary(updated);
    setRetrying(false);
  }, []);

  if (isOnline && total === 0) return null;

  return (
    <View>
      {!isOnline && (
        <Pressable
          className="min-h-touch flex-row items-center gap-3 bg-loss/15 px-5 py-2"
          onPress={() => setExpanded((e) => !e)}
        >
          <MaterialIcons name="cloud-off" size={20} color={colors.loss} />
          <Text className="flex-1 text-sm font-semibold text-text-primary">
            Offline — data locally saved hai
          </Text>
        </Pressable>
      )}

      {isOnline && total > 0 && (
        <Pressable
          className="min-h-touch flex-row items-center gap-3 bg-text-secondary/10 px-5 py-2"
          onPress={() => setExpanded((e) => !e)}
          accessibilityRole="button"
          accessibilityLabel={`${total} items sync pending`}
        >
          <MaterialIcons name="sync" size={20} color={colors.textSecondary} />
          <Text className="flex-1 text-body font-semibold text-text-primary">
            {syncFailed > 0
              ? `${syncPending} baaki, ${syncFailed} mein gadbad`
              : `${syncPending} cheez save hona baaki hai`}
          </Text>
          <MaterialIcons
            name={expanded ? "expand-less" : "expand-more"}
            size={20}
            color={colors.textSecondary}
          />
        </Pressable>
      )}

      {expanded && summary && summary.items.length > 0 && (
        <View className="border-t border-border bg-surface px-5 py-3">
          {summary.items.map((item) => (
            <View key={item.id} className="flex-row items-center gap-2 py-1.5">
              <MaterialIcons
                name={item.failed ? "error-outline" : "schedule"}
                size={14}
                color={item.failed ? colors.loss : colors.textSecondary}
              />
              <Text
                className={`flex-1 text-sm ${
                  item.failed ? "text-loss" : "text-text-secondary"
                }`}
                numberOfLines={1}
              >
                {item.label}
              </Text>
            </View>
          ))}

          {syncFailed > 0 && (
            <Pressable
              className="mt-2 min-h-touch flex-row items-center justify-center gap-2 rounded-button bg-surface-elevated active:bg-border"
              onPress={handleRetry}
              disabled={retrying}
            >
              <MaterialIcons name="refresh" size={16} color={colors.textPrimary} />
              <Text className="text-body font-semibold text-text-primary">
                {retrying ? "Ho raha hai..." : "Dobara try karo"}
              </Text>
            </Pressable>
          )}
        </View>
      )}
    </View>
  );
}
