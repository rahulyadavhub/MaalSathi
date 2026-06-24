import { apiClient } from "./client";
import type { User, Language, UserRole } from "../types/user";
import type { Trip } from "../types/trip";
import type { Expense } from "../types/expense";

interface RegisterPayload {
  phone: string;
  name: string;
  language: Language;
  role: UserRole;
  truckRegistration: string;
}

interface RegisterResponse {
  user: User;
  isNew: boolean;
}

interface ProfitResponse {
  period: string;
  revenue: number;
  expenses: number;
  profit: number;
  tripCount: number;
}

export const userApi = {
  register: (data: RegisterPayload) =>
    apiClient.post<RegisterResponse>("/api/users/register", data),

  getProfile: (userId: string) =>
    apiClient.get<User>(`/api/users/${userId}`),

  updateProfile: (userId: string, data: Partial<Pick<User, "name" | "language">>) =>
    apiClient.put<User>(`/api/users/${userId}/profile`, data),
};

export const tripApi = {
  getActive: (userId: string) =>
    apiClient.get<Trip | null>(`/api/users/${userId}/trips/active`),

  list: (userId: string, status?: string) =>
    apiClient.get<Trip[]>(`/api/users/${userId}/trips`, { params: { status } }),

  create: (userId: string, data: {
    origin: string;
    destination: string;
    cargoType?: string;
    cargoWeightTons?: number;
    freightAmount?: number;
    advancePaid?: number;
  }) =>
    apiClient.post<Trip>(`/api/users/${userId}/trips`, data),

  confirm: (tripId: string) =>
    apiClient.put<Trip>(`/api/trips/${tripId}/confirm`),

  complete: (tripId: string) =>
    apiClient.put<Trip>(`/api/trips/${tripId}/complete`),

  cancel: (tripId: string) =>
    apiClient.put<Trip>(`/api/trips/${tripId}/cancel`),

  getById: (tripId: string) =>
    apiClient.get<Trip>(`/api/trips/${tripId}`),
};

export const expenseApi = {
  add: (userId: string, data: {
    tripId?: string;
    category: string;
    amount: number;
    note?: string;
    source?: string;
  }) =>
    apiClient.post<Expense>(`/api/users/${userId}/expenses`, data),

  addMulti: (userId: string, expenses: {
    tripId?: string;
    category: string;
    amount: number;
    note?: string;
  }[]) =>
    apiClient.post<Expense[]>(`/api/users/${userId}/expenses/batch`, { expenses }),
};

export const profitApi = {
  get: (userId: string, period: "today" | "week" | "month") =>
    apiClient.get<ProfitResponse>(`/api/users/${userId}/profit`, { params: { period } }),
};
