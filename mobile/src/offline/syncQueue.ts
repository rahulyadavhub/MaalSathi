// ---------------------------------------------------------------
// REQUIRED BACKEND REST ENDPOINTS (none exist yet):
//
// User:
//   POST   /api/users/register              → userApi.register
//
// Trip operations:
//   POST   /api/users/:userId/trips          → tripApi.create
//   GET    /api/users/:userId/trips          → tripApi.list
//   GET    /api/users/:userId/trips/active   → tripApi.getActive
//   GET    /api/trips/:tripId                → tripApi.getById
//   PUT    /api/trips/:tripId/confirm        → tripApi.confirm
//   PUT    /api/trips/:tripId/complete       → tripApi.complete
//   PUT    /api/trips/:tripId/cancel         → tripApi.cancel
//
// Expense operations:
//   POST   /api/users/:userId/expenses       → expenseApi.add
//   POST   /api/users/:userId/expenses/batch → expenseApi.addMulti
//
// All will 404 until these are added to the backend. The app
// works fully offline until then — data is local-first.
// ---------------------------------------------------------------

import AsyncStorage from "@react-native-async-storage/async-storage";
import { AppState, type AppStateStatus } from "react-native";
import NetInfo, { type NetInfoState } from "@react-native-community/netinfo";
import { userApi, tripApi, expenseApi } from "../api/endpoints";
import { useAppStore } from "../store/useAppStore";

const QUEUE_KEY = "maalsaathi_sync_queue";
const MAX_RETRIES = 5;

export type OperationType =
  | "register"
  | "create_trip"
  | "add_expense"
  | "complete_trip";

export interface SyncOperation {
  id: string;
  type: OperationType;
  localTripId: string;
  userId: string;
  payload: Record<string, unknown>;
  createdAt: string;
  retryCount: number;
  failed: boolean;
  lastError: string | null;
}

export type SyncSummary = {
  pending: number;
  failed: number;
  items: { id: string; label: string; failed: boolean }[];
};

function labelFor(op: SyncOperation): string {
  switch (op.type) {
    case "register":
      return `Registration: ${(op.payload.name as string) || "User"}`;
    case "create_trip":
      return `Trip: ${op.payload.origin as string} → ${op.payload.destination as string}`;
    case "add_expense":
      return `Kharcha: ₹${op.payload.amount as number} ${op.payload.category as string}`;
    case "complete_trip":
      return "Trip complete";
  }
}

async function readQueue(): Promise<SyncOperation[]> {
  const raw = await AsyncStorage.getItem(QUEUE_KEY);
  if (!raw) return [];
  const parsed: SyncOperation[] = JSON.parse(raw);
  return parsed.map((op) => ({
    ...op,
    retryCount: op.retryCount ?? 0,
    failed: op.failed ?? false,
    lastError: op.lastError ?? null,
  }));
}

async function writeQueue(ops: SyncOperation[]): Promise<void> {
  await AsyncStorage.setItem(QUEUE_KEY, JSON.stringify(ops));
  updateStoreCounts(ops);
}

function updateStoreCounts(ops: SyncOperation[]): void {
  const pending = ops.filter((o) => !o.failed).length;
  const failed = ops.filter((o) => o.failed).length;
  useAppStore.getState().setSyncPending(pending);
  useAppStore.getState().setSyncFailed(failed);
}

export async function enqueue(
  op: Omit<SyncOperation, "id" | "createdAt" | "retryCount" | "failed" | "lastError">,
): Promise<void> {
  const queue = await readQueue();
  queue.push({
    ...op,
    id: `sync_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`,
    createdAt: new Date().toISOString(),
    retryCount: 0,
    failed: false,
    lastError: null,
  });
  await writeQueue(queue);
}

export async function getSyncSummary(): Promise<SyncSummary> {
  const queue = await readQueue();
  return {
    pending: queue.filter((o) => !o.failed).length,
    failed: queue.filter((o) => o.failed).length,
    items: queue.map((o) => ({ id: o.id, label: labelFor(o), failed: o.failed })),
  };
}

export async function getSyncPendingCount(): Promise<number> {
  const queue = await readQueue();
  return queue.length;
}

async function processOne(op: SyncOperation): Promise<boolean> {
  try {
    switch (op.type) {
      case "register": {
        const { phone, name, language, role, truckRegistration } = op.payload;
        const { data } = await userApi.register({
          phone: phone as string,
          name: name as string,
          language: language as "hindi" | "hinglish" | "english",
          role: ((role as string) || "maalik") as "maalik" | "driver",
          truckRegistration: truckRegistration as string,
        });
        await useAppStore.getState().setUser(data.user);
        return true;
      }
      case "create_trip": {
        const { origin, destination, cargoType, cargoWeightTons, freightAmount, advancePaid } =
          op.payload;
        await tripApi.create(op.userId, {
          origin: origin as string,
          destination: destination as string,
          cargoType: cargoType as string | undefined,
          cargoWeightTons: cargoWeightTons as number | undefined,
          freightAmount: freightAmount as number | undefined,
          advancePaid: advancePaid as number | undefined,
        });
        return true;
      }
      case "add_expense": {
        const { category, amount, note, source, tripId } = op.payload;
        await expenseApi.add(op.userId, {
          tripId: tripId as string | undefined,
          category: category as string,
          amount: amount as number,
          note: note as string | undefined,
          source: source as string | undefined,
        });
        return true;
      }
      case "complete_trip": {
        await tripApi.complete(op.localTripId);
        return true;
      }
      default:
        return false;
    }
  } catch {
    return false;
  }
}

export async function processQueue(): Promise<void> {
  const queue = await readQueue();
  if (queue.length === 0) return;

  const failedTripIds = new Set<string>();
  const result: SyncOperation[] = [];

  for (const op of queue) {
    if (op.failed) {
      result.push(op);
      continue;
    }

    const dependsOnFailedTrip =
      (op.type === "add_expense" || op.type === "complete_trip") &&
      failedTripIds.has(op.localTripId);

    if (dependsOnFailedTrip) {
      result.push(op);
      continue;
    }

    const ok = await processOne(op);
    if (ok) continue;

    op.retryCount++;
    if (op.retryCount >= MAX_RETRIES) {
      op.failed = true;
      op.lastError = "Max retries — kuch gadbad hai, dobara try karo";
    }
    result.push(op);

    if (op.type === "create_trip") {
      failedTripIds.add(op.localTripId);
    }
  }

  await writeQueue(result);
}

export async function retryFailed(): Promise<void> {
  const queue = await readQueue();
  let changed = false;
  for (const op of queue) {
    if (op.failed) {
      op.failed = false;
      op.retryCount = 0;
      op.lastError = null;
      changed = true;
    }
  }
  if (changed) {
    await writeQueue(queue);
    await processQueue();
  }
}

export async function clearQueue(): Promise<void> {
  await writeQueue([]);
}

let netInfoUnsub: (() => void) | null = null;
let appStateSub: ReturnType<typeof AppState.addEventListener> | null = null;

export function startSyncListeners(): void {
  if (netInfoUnsub) return;

  netInfoUnsub = NetInfo.addEventListener((state: NetInfoState) => {
    if (state.isConnected && state.isInternetReachable !== false) {
      processQueue();
    }
  });

  appStateSub = AppState.addEventListener(
    "change",
    (next: AppStateStatus) => {
      if (next === "active") processQueue();
    },
  );
}

export function stopSyncListeners(): void {
  netInfoUnsub?.();
  netInfoUnsub = null;
  appStateSub?.remove();
  appStateSub = null;
}
