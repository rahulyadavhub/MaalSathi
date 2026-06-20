import { create } from "zustand";
import AsyncStorage from "@react-native-async-storage/async-storage";
import type { Trip, CreateTripInput, TripStatus } from "../types/trip";
import type { TripExpense, ExpenseCategory, ExpenseSource } from "../types/expense";
import { enqueue, startSyncListeners } from "../offline/syncQueue";
import { useAppStore } from "./useAppStore";

const TRIPS_KEY = "maalsaathi_trips";

function computeDerived(trip: Trip): Trip {
  const totalExpenses = trip.expenses.reduce((s, e) => s + e.amount, 0);
  return { ...trip, totalExpenses, netProfit: trip.freightAmount - totalExpenses };
}

function localId(): string {
  return `local_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`;
}

interface TripStore {
  trips: Trip[];
  isLoaded: boolean;

  loadTrips: () => Promise<void>;
  getTrip: (id: string) => Trip | undefined;
  getActiveTrip: () => Trip | undefined;
  createTrip: (input: CreateTripInput) => Promise<Trip>;
  addExpense: (tripId: string, category: ExpenseCategory, amount: number, note?: string, source?: ExpenseSource) => Promise<Trip | undefined>;
  completeTrip: (tripId: string) => Promise<Trip | undefined>;
  persistTrips: (trips: Trip[]) => Promise<void>;
}

export const useTripStore = create<TripStore>((set, get) => ({
  trips: [],
  isLoaded: false,

  loadTrips: async () => {
    const raw = await AsyncStorage.getItem(TRIPS_KEY);
    const trips: Trip[] = raw ? JSON.parse(raw) : [];
    set({ trips: trips.map(computeDerived), isLoaded: true });
  },

  persistTrips: async (trips: Trip[]) => {
    await AsyncStorage.setItem(TRIPS_KEY, JSON.stringify(trips));
    set({ trips });
  },

  getTrip: (id) => get().trips.find((t) => t._id === id),

  getActiveTrip: () => get().trips.find((t) => t.status === "active"),

  createTrip: async (input) => {
    const user = useAppStore.getState().user;
    const userId = user?._id || "unknown";
    const truckReg = user?.trucks[0]?.registration || "";

    const trip = computeDerived({
      _id: localId(),
      userId,
      truckRegistration: truckReg,
      origin: input.origin,
      destination: input.destination,
      cargoType: input.cargoType,
      cargoWeightTons: input.cargoWeightTons,
      freightAmount: input.freightAmount || 0,
      advancePaid: input.advancePaid || 0,
      balanceDue: Math.max(0, (input.freightAmount || 0) - (input.advancePaid || 0)),
      status: "active" as TripStatus,
      expenses: [],
      startedAt: new Date().toISOString(),
      completedAt: null,
      cancelledAt: null,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      totalExpenses: 0,
      netProfit: input.freightAmount || 0,
    });

    const updated = [trip, ...get().trips];
    await get().persistTrips(updated);

    await enqueue({
      type: "create_trip",
      localTripId: trip._id,
      userId,
      payload: input as unknown as Record<string, unknown>,
    });
    startSyncListeners();

    return trip;
  },

  addExpense: async (tripId, category, amount, note, source = "manual") => {
    const trips = get().trips;
    const idx = trips.findIndex((t) => t._id === tripId);
    if (idx === -1) return undefined;

    const expense: TripExpense = {
      _id: localId(),
      category,
      amount,
      note,
      source,
      recordedAt: new Date().toISOString(),
    };

    const trip = { ...trips[idx] };
    trip.expenses = [...trip.expenses, expense];
    trip.updatedAt = new Date().toISOString();
    const derived = computeDerived(trip);

    const updated = [...trips];
    updated[idx] = derived;
    await get().persistTrips(updated);

    const user = useAppStore.getState().user;
    await enqueue({
      type: "add_expense",
      localTripId: tripId,
      userId: user?._id || "unknown",
      payload: { tripId, category, amount, note, source },
    });
    startSyncListeners();

    return derived;
  },

  completeTrip: async (tripId) => {
    const trips = get().trips;
    const idx = trips.findIndex((t) => t._id === tripId);
    if (idx === -1) return undefined;

    const trip = { ...trips[idx] };
    trip.status = "completed";
    trip.completedAt = new Date().toISOString();
    trip.updatedAt = new Date().toISOString();
    const derived = computeDerived(trip);

    const updated = [...trips];
    updated[idx] = derived;
    await get().persistTrips(updated);

    const user = useAppStore.getState().user;
    await enqueue({
      type: "complete_trip",
      localTripId: tripId,
      userId: user?._id || "unknown",
      payload: { tripId },
    });
    startSyncListeners();

    return derived;
  },
}));
