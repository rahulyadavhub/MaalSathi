import { useState, useCallback } from "react";
import { useAppStore } from "../store/useAppStore";
import { userApi } from "../api/endpoints";
import { enqueue, startSyncListeners } from "../offline/syncQueue";
import { validateName, validatePhone, validateTruckNumber } from "../utils/validators";
import type { Language } from "../types/user";

// ---------------------------------------------------------------
// Backend contract (handleOnboarding.js):
//
// The WhatsApp backend creates users via User.findOrCreate(phone)
// in webhookController.js — phone comes from the WhatsApp message
// payload, NOT from user input during onboarding.
// Onboarding only collects: name (free text) and truckRegistration
// (regex: /^[A-Z]{2}\d{1,2}[A-Z]{1,3}\d{1,4}$/).
//
// The backend has NO REST endpoint for mobile registration yet.
// userApi.register() will 404 until the backend adds one.
//
// Phone number in the mobile app is collected as:
//   1. The user's unique local identifier (matches User.phone)
//   2. The auth key for future OTP login
//   3. Part of the registration payload for when REST API is added
//
// On API failure, the user is created locally with a "local_" ID
// prefix. A sync queue (syncQueue.ts) retries via NetInfo
// and AppState listeners. On successful sync, the local_ ID is
// replaced with the real backend _id everywhere.
// ---------------------------------------------------------------

interface OnboardingState {
  phone: string;
  name: string;
  truckNumber: string;
  loading: boolean;
  error: string | null;
}

interface OnboardingActions {
  setPhone: (v: string) => void;
  setName: (v: string) => void;
  setTruckNumber: (v: string) => void;
  submit: () => Promise<boolean>;
  phoneValidation: { valid: boolean; normalized: string };
  nameValid: boolean;
  truckValidation: { valid: boolean; normalized: string };
  canSubmit: boolean;
}

export function useOnboarding(): OnboardingState & OnboardingActions {
  const { language, role, setUser, completeOnboarding } = useAppStore();
  const [phone, setPhone] = useState("");
  const [name, setName] = useState("");
  const [truckNumber, setTruckNumber] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const phoneValidation = validatePhone(phone);
  const nameValid = validateName(name);
  const truckValidation = validateTruckNumber(truckNumber);

  const canSubmit =
    phoneValidation.valid && nameValid && truckValidation.valid && !loading;

  const submit = useCallback(async (): Promise<boolean> => {
    if (!canSubmit) return false;
    setLoading(true);
    setError(null);

    const regPayload = {
      phone: phoneValidation.normalized,
      name: name.trim(),
      language: (language || "hinglish") as Language,
      role: role || "maalik",
      truckRegistration: truckValidation.normalized,
    };

    try {
      const { data } = await userApi.register(regPayload);
      await setUser(data.user);
      await completeOnboarding();
      return true;
    } catch {
      const localUser = {
        _id: `local_${Date.now()}`,
        phone: regPayload.phone,
        name: regPayload.name,
        language: regPayload.language,
        trucks: [{
          registration: regPayload.truckRegistration,
          vehicleType: "truck" as const,
          isActive: true,
          addedAt: new Date().toISOString(),
        }],
        onboardingComplete: true,
        onboardingStep: null,
        isActive: true,
        lastActiveAt: new Date().toISOString(),
        stats: {
          totalTrips: 0,
          completedTrips: 0,
          cancelledTrips: 0,
          totalRevenue: 0,
          totalExpenses: 0,
          totalMessages: 0,
        },
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      await setUser(localUser);
      await completeOnboarding();
      await enqueue({
        type: "register",
        localTripId: "",
        userId: localUser._id,
        payload: regPayload as unknown as Record<string, unknown>,
      });
      startSyncListeners();
      return true;
    } finally {
      setLoading(false);
    }
  }, [
    canSubmit,
    phoneValidation.normalized,
    name,
    language,
    role,
    truckValidation.normalized,
    setUser,
    completeOnboarding,
  ]);

  return {
    phone,
    name,
    truckNumber,
    loading,
    error,
    setPhone,
    setName,
    setTruckNumber,
    submit,
    phoneValidation,
    nameValid,
    truckValidation,
    canSubmit,
  };
}
