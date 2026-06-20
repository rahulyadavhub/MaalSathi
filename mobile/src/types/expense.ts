export const EXPENSE_CATEGORIES = [
  "diesel",
  "toll",
  "tyre",
  "repair",
  "food",
  "driver_salary",
  "fine",
  "parking",
  "loading",
  "unloading",
  "greasing",
  "weighbridge",
  "insurance",
  "emi",
  "commission",
  "washing",
  "permit",
  "battery",
  "maintenance",
  "other",
] as const;

export type ExpenseCategory = (typeof EXPENSE_CATEGORIES)[number];

export type ExpenseSource = "text" | "voice" | "manual" | "api";

export interface TripExpense {
  _id: string;
  category: ExpenseCategory;
  amount: number;
  note?: string;
  source: ExpenseSource;
  recordedAt: string;
}

export interface Expense {
  _id: string;
  userId: string;
  tripId: string | null;
  category: ExpenseCategory;
  amount: number;
  note?: string;
  source: ExpenseSource;
  rawMessage?: string;
  recordedAt: string;
  createdAt: string;
  updatedAt: string;
}

export const CATEGORY_LABELS: Record<ExpenseCategory, string> = {
  diesel: "Diesel",
  toll: "Toll",
  tyre: "Tyre",
  repair: "Repair",
  food: "Khana",
  driver_salary: "Driver Salary",
  fine: "Fine",
  parking: "Parking",
  loading: "Loading",
  unloading: "Unloading",
  greasing: "Greasing / Oil",
  weighbridge: "Weighbridge",
  insurance: "Insurance",
  emi: "EMI",
  commission: "Commission",
  washing: "Washing",
  permit: "Permit / RC",
  battery: "Battery",
  maintenance: "Maintenance",
  other: "Other",
};

export const CATEGORY_ICONS: Record<ExpenseCategory, string> = {
  diesel: "local-gas-station",
  toll: "toll",
  tyre: "tire-repair",
  repair: "build",
  food: "restaurant",
  driver_salary: "person",
  fine: "gavel",
  parking: "local-parking",
  loading: "inventory",
  unloading: "inventory",
  greasing: "opacity",
  weighbridge: "scale",
  insurance: "description",
  emi: "account-balance",
  commission: "work",
  washing: "local-car-wash",
  permit: "article",
  battery: "battery-full",
  maintenance: "handyman",
  other: "payments",
};
