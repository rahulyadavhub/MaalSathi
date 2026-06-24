import { createBottomTabNavigator } from "@react-navigation/bottom-tabs";
import { MaterialIcons } from "@expo/vector-icons";
import type { TabParamList } from "../types/navigation";
import { TripStackNavigator } from "./TripStackNavigator";
import { HisaabScreen } from "../screens/HisaabScreen";
import { MadadScreen } from "../screens/MadadScreen";
import { colors } from "../theme/tokens";

const Tab = createBottomTabNavigator<TabParamList>();

export function TabNavigator() {
  return (
    <Tab.Navigator
      screenOptions={{
        headerShown: false,
        tabBarStyle: {
          backgroundColor: colors.surface,
          borderTopColor: colors.border,
          borderTopWidth: 1,
          height: 64,
          paddingBottom: 8,
          paddingTop: 8,
        },
        tabBarActiveTintColor: colors.primary,
        tabBarInactiveTintColor: colors.textSecondary,
        tabBarLabelStyle: {
          fontSize: 13,
          fontWeight: "600",
        },
      }}
    >
      <Tab.Screen
        name="TripTab"
        component={TripStackNavigator}
        options={{
          tabBarLabel: "Trip",
          tabBarIcon: ({ color, size }) => (
            <MaterialIcons name="local-shipping" size={size} color={color} />
          ),
        }}
      />
      <Tab.Screen
        name="HisaabTab"
        component={HisaabScreen}
        options={{
          tabBarLabel: "Hisaab",
          tabBarIcon: ({ color, size }) => (
            <MaterialIcons name="account-balance-wallet" size={size} color={color} />
          ),
        }}
      />
      <Tab.Screen
        name="MadadTab"
        component={MadadScreen}
        options={{
          tabBarLabel: "Madad",
          tabBarIcon: ({ color, size }) => (
            <MaterialIcons name="support-agent" size={size} color={color} />
          ),
        }}
      />
    </Tab.Navigator>
  );
}
