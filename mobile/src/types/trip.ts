import type { TripExpense } from "./expense";

export const TRIP_STATUSES = [
  "draft",
  "pending_confirmation",
  "active",
  "completed",
  "cancelled",
] as const;

export type TripStatus = (typeof TRIP_STATUSES)[number];

export interface Trip {
  _id: string;
  userId: string;
  truckRegistration: string;
  origin: string;
  destination: string;
  cargoType?: string;
  cargoWeightTons?: number;
  freightAmount: number;
  advancePaid: number;
  balanceDue: number;
  driverName?: string;
  driverPhone?: string;
  partyName?: string;
  status: TripStatus;
  expenses: TripExpense[];
  startedAt: string;
  completedAt: string | null;
  cancelledAt: string | null;
  notes?: string;
  createdAt: string;
  updatedAt: string;
  totalExpenses: number;
  netProfit: number;
}

export interface CreateTripInput {
  origin: string;
  destination: string;
  cargoType?: string;
  cargoWeightTons?: number;
  freightAmount?: number;
  advancePaid?: number;
}
