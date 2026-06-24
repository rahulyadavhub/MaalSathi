export type Language = "hindi" | "hinglish" | "english";
export type UserRole = "maalik" | "driver";

export interface Truck {
  registration: string;
  vehicleType: "truck" | "mini-truck" | "trailer" | "tanker" | "tipper" | "container" | "unknown";
  make?: string;
  model?: string;
  capacityTons?: number;
  year?: number;
  isActive: boolean;
  addedAt: string;
}

export interface UserStats {
  totalTrips: number;
  completedTrips: number;
  cancelledTrips: number;
  totalRevenue: number;
  totalExpenses: number;
  totalMessages: number;
}

export interface User {
  _id: string;
  phone: string;
  name: string;
  language: Language;
  trucks: Truck[];
  onboardingComplete: boolean;
  onboardingStep: null | "name" | "truck";
  isActive: boolean;
  lastActiveAt: string;
  stats: UserStats;
  createdAt: string;
  updatedAt: string;
}
