import axios from "axios";
import type { AxiosError, InternalAxiosRequestConfig } from "axios";
import { useAppStore } from "../store/useAppStore";

const BASE_URL = __DEV__
  ? "http://10.0.2.2:3000"
  : "https://api.maalsaathi.com";

export const apiClient = axios.create({
  baseURL: BASE_URL,
  timeout: 15000,
  headers: { "Content-Type": "application/json" },
});

apiClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const user = useAppStore.getState().user;
  if (user?._id) {
    config.headers.set("x-user-id", user._id);
  }
  if (user?.phone) {
    config.headers.set("x-user-phone", user.phone);
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (!error.response) {
      useAppStore.getState().setOnline(false);
    }
    return Promise.reject(error);
  },
);
