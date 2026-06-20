import { useState, useMemo, useEffect } from "react";
import { View, Text, Pressable, ScrollView, Share, Platform } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { MaterialIcons } from "@expo/vector-icons";
import * as Haptics from "expo-haptics";
import { useTripStore } from "../store/useTripStore";
import { useAppStore } from "../store/useAppStore";
import { MoneyPill } from "../components/MoneyPill";
import { BigButton } from "../components/BigButton";
import { CATEGORY_LABELS, type ExpenseCategory } from "../types/expense";
import { colors } from "../theme/tokens";

type Period = "week" | "month";

function periodRange(period: Period): { start: Date; end: Date; label: string } {
  const now = new Date();
  if (period === "week") {
    const day = now.getDay();
    const diff = day === 0 ? 6 : day - 1;
    const start = new Date(now.getFullYear(), now.getMonth(), now.getDate() - diff);
    return { start, end: now, label: "Is Hafte" };
  }
  const start = new Date(now.getFullYear(), now.getMonth(), 1);
  return { start, end: now, label: "Is Mahine" };
}

interface CategoryBreakdown {
  category: string;
  label: string;
  total: number;
}

function buildShareText(
  period: string,
  revenue: number,
  expenses: number,
  profit: number,
  tripCount: number,
  breakdown: CategoryBreakdown[],
  userName: string,
): string {
  const lines = [
    `📊 *MaalSaathi — ${period} ka Hisaab*`,
    ``,
    `🚛 Trips: ${tripCount}`,
    `💰 Kamaai: ₹${revenue.toLocaleString("en-IN")}`,
    `💸 Kharcha: ₹${expenses.toLocaleString("en-IN")}`,
    ``,
    profit >= 0
      ? `✅ *Fayda: ₹${profit.toLocaleString("en-IN")}*`
      : `❌ *Nuksan: ₹${Math.abs(profit).toLocaleString("en-IN")}*`,
  ];

  if (breakdown.length > 0) {
    lines.push(``, `📋 *Kharche ka breakdown:*`);
    for (const b of breakdown.slice(0, 8)) {
      lines.push(`  • ${b.label}: ₹${b.total.toLocaleString("en-IN")}`);
    }
  }

  lines.push(``, `_${userName} — via MaalSaathi_`);
  return lines.join("\n");
}

export function HisaabScreen() {
  const { trips, isLoaded, loadTrips } = useTripStore();
  const user = useAppStore((s) => s.user);
  const [period, setPeriod] = useState<Period>("week");

  useEffect(() => {
    if (!isLoaded) loadTrips();
  }, [isLoaded, loadTrips]);

  const stats = useMemo(() => {
    const { start, end } = periodRange(period);
    let revenue = 0;
    let expenses = 0;
    let tripCount = 0;
    const catMap = new Map<string, number>();

    for (const t of trips) {
      const completed = t.completedAt ? new Date(t.completedAt) : null;
      const started = new Date(t.startedAt);
      const inRange =
        (completed && completed >= start && completed <= end) ||
        (t.status === "active" && started >= start && started <= end);

      if (!inRange) continue;
      tripCount++;
      revenue += t.freightAmount;

      for (const e of t.expenses) {
        const rec = new Date(e.recordedAt);
        if (rec >= start && rec <= end) {
          expenses += e.amount;
          catMap.set(e.category, (catMap.get(e.category) || 0) + e.amount);
        }
      }
    }

    const breakdown: CategoryBreakdown[] = [...catMap.entries()]
      .map(([category, total]) => ({
        category,
        label: CATEGORY_LABELS[category as ExpenseCategory] || category,
        total,
      }))
      .sort((a, b) => b.total - a.total);

    return { revenue, expenses, profit: revenue - expenses, tripCount, breakdown };
  }, [trips, period]);

  const { label } = periodRange(period);

  const handleShare = async () => {
    Haptics.impactAsync(Haptics.ImpactFeedbackStyle.Light);
    const text = buildShareText(
      label,
      stats.revenue,
      stats.expenses,
      stats.profit,
      stats.tripCount,
      stats.breakdown,
      user?.name || "User",
    );
    await Share.share(
      { message: text },
      Platform.OS === "android" ? { dialogTitle: "Hisaab Share Karo" } : {},
    );
  };

  return (
    <SafeAreaView className="flex-1 bg-dark" edges={["top"]}>
      <View className="px-5 pb-3 pt-4">
        <Text className="text-2xl font-bold text-text-primary">Hisaab</Text>
      </View>

      <View className="mx-5 mb-4 flex-row rounded-button bg-surface p-1">
        {(["week", "month"] as const).map((p) => (
          <Pressable
            key={p}
            className={`min-h-touch flex-1 items-center justify-center rounded-button ${
              period === p ? "bg-surface-elevated" : ""
            }`}
            onPress={() => setPeriod(p)}
          >
            <Text
              className={`text-sm font-semibold ${
                period === p ? "text-text-primary" : "text-text-secondary"
              }`}
            >
              {p === "week" ? "Hafta" : "Mahina"}
            </Text>
          </Pressable>
        ))}
      </View>

      <ScrollView className="flex-1" showsVerticalScrollIndicator={false}>
        <View className="mx-5 flex-row gap-3">
          <View className="flex-1 rounded-card bg-surface p-4">
            <MoneyPill
              amount={stats.revenue}
              label="Kamaai"
              size="md"
              neutral
              showSign={false}
            />
          </View>
          <View className="flex-1 rounded-card bg-surface p-4">
            <MoneyPill
              amount={stats.expenses}
              label="Kharcha"
              size="md"
              neutral
              showSign={false}
            />
          </View>
        </View>

        <View className="mx-5 mt-3 rounded-card bg-surface-elevated p-5">
          <MoneyPill amount={stats.profit} label={`${label} ka Fayda / Nuksan`} size="lg" />
        </View>

        <View className="mx-5 mt-3 rounded-card bg-surface p-4">
          <View className="flex-row items-center gap-2">
            <MaterialIcons name="local-shipping" size={18} color={colors.textSecondary} />
            <Text className="text-body text-text-secondary">
              {stats.tripCount} {stats.tripCount === 1 ? "trip" : "trips"}
            </Text>
          </View>
        </View>

        {stats.breakdown.length > 0 && (
          <View className="mx-5 mt-4">
            <Text className="mb-3 text-lg font-bold text-text-primary">
              Kharche ka breakdown
            </Text>
            <View className="gap-2">
              {stats.breakdown.map((b) => (
                <View
                  key={b.category}
                  className="flex-row items-center justify-between rounded-button bg-surface px-4 py-3"
                >
                  <Text className="text-body text-text-secondary">{b.label}</Text>
                  <Text className="text-body font-bold text-text-primary">
                    ₹{b.total.toLocaleString("en-IN")}
                  </Text>
                </View>
              ))}
            </View>
          </View>
        )}

        {stats.tripCount === 0 && (
          <View className="items-center px-6 py-12">
            <MaterialIcons name="account-balance-wallet" size={48} color={colors.border} />
            <Text className="mt-4 text-lg font-semibold text-text-secondary">
              {label} mein koi trip nahi
            </Text>
            <Text className="mt-1 text-sm text-text-secondary">
              Trip complete hone pe yahan dikhega
            </Text>
          </View>
        )}

        <View className="px-5 pb-8 pt-6">
          <BigButton
            title="Hisaab Share Karo"
            onPress={handleShare}
            variant="secondary"
            icon="share"
          />
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}
