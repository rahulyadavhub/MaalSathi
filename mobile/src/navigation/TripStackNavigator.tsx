import { createNativeStackNavigator } from "@react-navigation/native-stack";
import type { TripStackParamList } from "../types/navigation";
import { HomeScreen } from "../screens/HomeScreen";
import { TripListScreen } from "../screens/TripListScreen";
import { TripDetailScreen } from "../screens/TripDetailScreen";
import { NewTripScreen } from "../screens/NewTripScreen";
import { TripCompleteScreen } from "../screens/TripCompleteScreen";
import { AddExpenseScreen } from "../screens/AddExpenseScreen";

const Stack = createNativeStackNavigator<TripStackParamList>();

export function TripStackNavigator() {
  return (
    <Stack.Navigator
      screenOptions={{
        headerShown: false,
        animation: "slide_from_right",
        contentStyle: { backgroundColor: "#0B0D12" },
      }}
    >
      <Stack.Screen name="Home" component={HomeScreen} />
      <Stack.Screen name="TripList" component={TripListScreen} />
      <Stack.Screen name="TripDetail" component={TripDetailScreen} />
      <Stack.Screen name="NewTrip" component={NewTripScreen} />
      <Stack.Screen name="TripComplete" component={TripCompleteScreen} />
      <Stack.Screen name="AddExpense" component={AddExpenseScreen} />
    </Stack.Navigator>
  );
}
