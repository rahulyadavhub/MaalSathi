import { useEffect } from "react";
import NetInfo from "@react-native-community/netinfo";
import { useAppStore } from "../store/useAppStore";

export function useNetworkStatus() {
  const setOnline = useAppStore((s) => s.setOnline);

  useEffect(() => {
    const unsubscribe = NetInfo.addEventListener((state) => {
      setOnline(!!state.isConnected && state.isInternetReachable !== false);
    });
    return unsubscribe;
  }, [setOnline]);

  return useAppStore((s) => s.isOnline);
}
