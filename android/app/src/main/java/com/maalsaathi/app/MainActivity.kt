package com.maalsaathi.app

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import com.maalsaathi.app.data.MockData
import com.maalsaathi.app.data.local.AppDatabase
import com.maalsaathi.app.data.repository.CalendarRepository
import com.maalsaathi.app.data.repository.TripRepository
import com.maalsaathi.app.ui.auth.*
import com.maalsaathi.app.ui.calendar.CalendarScreen
import com.maalsaathi.app.ui.calendar.CalendarViewModel
import com.maalsaathi.app.ui.common.BottomNavBar
import com.maalsaathi.app.ui.common.MaalSaathiTheme
import com.maalsaathi.app.ui.common.Ms
import com.maalsaathi.app.ui.common.Tab
import com.maalsaathi.app.ui.hisaab.HisaabScreen
import com.maalsaathi.app.ui.hisaab.HisaabViewModel
import com.maalsaathi.app.ui.madad.MadadScreen
import com.maalsaathi.app.ui.trip.NayiTripScreen
import com.maalsaathi.app.ui.trip.OngoingTripScreen
import com.maalsaathi.app.ui.trip.TripConfirmScreen
import com.maalsaathi.app.ui.trip.TripDetailScreen
import com.maalsaathi.app.ui.trip.TripScreen
import com.maalsaathi.app.ui.trip.TripSummaryScreen
import com.maalsaathi.app.ui.trip.TripViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val config = Configuration(newBase.resources.configuration)
        config.fontScale = 1.0f
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = AppDatabase.get(this)
        CoroutineScope(Dispatchers.IO).launch { MockData.seedIfEmpty(db) }
        val tripRepo = TripRepository(db.tripDao(), db.tripEntryDao())
        val calRepo = CalendarRepository(db.calendarReminderDao())
        val fleetRepo = com.maalsaathi.app.data.repository.FleetRepository(db.organizationDao(), db.truckDao(), db.driverInviteDao(), db.tripEntryDao(), db.tripDao())
        val authPrefs = AuthPreferences(this)
        setContent {
            MaalSaathiTheme {
                MainApp(tripRepo, calRepo, cacheDir, authPrefs, fleetRepo)
            }
        }
    }
}

private val TAB_ROUTES = setOf("trip_home", "fleet_dashboard", "driver_home", "hisaab", "calendar", "madad", "profile")
private val AUTH_ROUTES = setOf("splash", "phone", "welcome_back", "otp", "name_input", "truck_input", "success", "organization_setup", "add_first_truck", "no_invite")

@Composable
private fun MainApp(tripRepo: TripRepository, calRepo: CalendarRepository, cacheDir: java.io.File, authPrefs: AuthPreferences, fleetRepo: com.maalsaathi.app.data.repository.FleetRepository) {
    val navController = rememberNavController()
    var currentTab by rememberSaveable { mutableStateOf(Tab.Trip) }
    val tripVm: TripViewModel = viewModel(factory = TripViewModel.Factory(tripRepo, cacheDir))
    val hisaabVm: HisaabViewModel = viewModel(factory = HisaabViewModel.Factory(tripRepo))
    val calVm: CalendarViewModel = viewModel(factory = CalendarViewModel.Factory(calRepo, tripRepo))
    val authVm: AuthViewModel = viewModel(factory = AuthViewModel.Factory(authPrefs, fleetRepo))
    val orgId = authPrefs.organizationId.ifBlank { "org1" }
    val fleetVm: com.maalsaathi.app.ui.fleet.FleetViewModel = viewModel(factory = com.maalsaathi.app.ui.fleet.FleetViewModel.Factory(fleetRepo, orgId))
    val navEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navEntry?.destination?.route
    val showNav = currentRoute != null && currentRoute in TAB_ROUTES

    fun goToMain() {
        val home = when (authPrefs.userRoleEnum) {
            com.maalsaathi.app.data.models.UserRole.MAALIK -> "fleet_dashboard"
            com.maalsaathi.app.data.models.UserRole.DRIVER -> "driver_home"
            com.maalsaathi.app.data.models.UserRole.MAALIK_DRIVER -> "trip_home"
        }
        navController.navigate(home) { popUpTo(0) { inclusive = true } }
    }

    Column(Modifier.fillMaxSize().background(Ms.colors.background)) {
        Box(Modifier.weight(1f).fillMaxWidth()) {
            NavHost(
                navController = navController, startDestination = "splash",
                enterTransition = { slideInHorizontally(initialOffsetX = { it / 3 }) + fadeIn(tween(250)) },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it / 3 }) + fadeOut(tween(200)) },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it / 3 }) + fadeIn(tween(250)) },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it / 3 }) + fadeOut(tween(200)) },
            ) {

                // ─── Auth flow ───
                composable("splash") {
                    SplashScreen(
                        isLoggedIn = authVm.isLoggedIn(),
                        onNavigate = { loggedIn ->
                            if (loggedIn) goToMain()
                            else navController.navigate("phone") { popUpTo("splash") { inclusive = true } }
                        },
                    )
                }

                composable("phone") {
                    PhoneScreen(authVm) { result ->
                        when (result) {
                            PhoneResult.WHATSAPP_USER -> navController.navigate("welcome_back") { popUpTo("phone") { inclusive = true } }
                            PhoneResult.EXISTING_USER -> navController.navigate("otp")
                            PhoneResult.NEW_USER -> navController.navigate("otp")
                        }
                    }
                }

                composable("welcome_back") {
                    WelcomeBackScreen(authVm) { goToMain() }
                }

                composable("otp") {
                    OtpScreen(authVm,
                        onBack = { navController.popBackStack() },
                        onVerified = { isNew ->
                            if (isNew) {
                                val role = authVm.state.value.role
                                when (role) {
                                    com.maalsaathi.app.data.models.UserRole.MAALIK ->
                                        navController.navigate("name_input") { popUpTo("otp") { inclusive = true } }
                                    com.maalsaathi.app.data.models.UserRole.DRIVER ->
                                        navController.navigate("no_invite") { popUpTo("otp") { inclusive = true } }
                                    else ->
                                        navController.navigate("name_input") { popUpTo("otp") { inclusive = true } }
                                }
                            } else goToMain()
                        },
                    )
                }

                composable("name_input") {
                    NameScreen(authVm) {
                        val role = authVm.state.value.role
                        when (role) {
                            com.maalsaathi.app.data.models.UserRole.MAALIK ->
                                navController.navigate("organization_setup")
                            else ->
                                navController.navigate("truck_input")
                        }
                    }
                }

                composable("organization_setup") {
                    OrganizationSetupScreen(authVm) {
                        navController.navigate("add_first_truck")
                    }
                }

                composable("add_first_truck") {
                    AddFirstTruckScreen(authVm,
                        onDone = { navController.navigate("success") { popUpTo("organization_setup") { inclusive = true } } },
                        onSkip = { authVm.registerUser { navController.navigate("success") { popUpTo("organization_setup") { inclusive = true } } } },
                    )
                }

                composable("no_invite") {
                    NoInviteScreen(authVm,
                        onCodeAccepted = { navController.navigate("success") { popUpTo("no_invite") { inclusive = true } } },
                        onSwitchRole = {
                            authVm.updateRole(com.maalsaathi.app.data.models.UserRole.MAALIK_DRIVER)
                            navController.navigate("phone") { popUpTo(0) { inclusive = true } }
                        },
                    )
                }

                composable("truck_input") {
                    TruckScreen(authVm,
                        onDone = { navController.navigate("success") { popUpTo("name_input") { inclusive = true } } },
                        onSkip = { authVm.registerUser { navController.navigate("success") { popUpTo("name_input") { inclusive = true } } } },
                    )
                }

                composable("success") {
                    SuccessScreen(authVm) { goToMain() }
                }

                // ─── Fleet (MAALIK) ───
                composable("fleet_dashboard") {
                    currentTab = Tab.Trip
                    val ongoing by tripVm.ongoingTrip.collectAsState()
                    val allTrips by tripVm.pastTrips.collectAsState()
                    val ongoingList = listOfNotNull(ongoing)
                    com.maalsaathi.app.ui.fleet.FleetDashboardScreen(
                        viewModel = fleetVm,
                        ongoingTrips = ongoingList,
                        onTruckDetail = { navController.navigate("truck_detail/$it") },
                        onAddTruck = { navController.navigate("add_first_truck") },
                        onInviteDriver = { navController.navigate("invite_driver/$it") },
                        onStartTrip = { navController.navigate("nayi_trip") },
                        onProfileClick = { navController.navigate("profile") },
                    )
                }

                composable("truck_detail/{truckId}", arguments = listOf(navArgument("truckId") { type = NavType.StringType })) { backEntry ->
                    val tid = backEntry.arguments?.getString("truckId") ?: return@composable
                    val ongoing by tripVm.ongoingTrip.collectAsState()
                    val past by tripVm.pastTrips.collectAsState()
                    val all = listOfNotNull(ongoing) + past
                    com.maalsaathi.app.ui.fleet.TruckDetailScreen(
                        viewModel = fleetVm, truckId = tid, ongoingTrips = listOfNotNull(ongoing), allTrips = all,
                        onBack = { navController.popBackStack() },
                        onInviteDriver = { navController.navigate("invite_driver/$tid") },
                        onTripDetail = { id -> navController.navigate("trip_detail/$id") },
                    )
                }

                composable("invite_driver/{truckId}", arguments = listOf(navArgument("truckId") { type = NavType.StringType })) {
                    val tid = it.arguments?.getString("truckId") ?: return@composable
                    com.maalsaathi.app.ui.fleet.InviteDriverScreen(fleetVm, tid, onBack = { navController.popBackStack() })
                }

                // ─── Driver home ───
                composable("driver_home") {
                    currentTab = Tab.Trip
                    com.maalsaathi.app.ui.driver.DriverHomeScreen(
                        tripVm = tripVm,
                        driverName = authPrefs.userName,
                        truckNumber = authPrefs.truckNumber,
                        orgName = "Sharma Transport", // TODO: from prefs
                        ownerPhone = authPrefs.ownerPhone.ifBlank { "9912345678" },
                        onOngoingTrip = { navController.navigate("driver_ongoing_trip") },
                        onEndTrip = { id -> tripVm.endTrip(id); navController.navigate("trip_summary/$id") },
                        onNewTrip = { navController.navigate("nayi_trip") },
                        onTripDetail = { id -> navController.navigate("trip_detail/$id") },
                    )
                }

                composable("driver_ongoing_trip") {
                    com.maalsaathi.app.ui.driver.DriverOngoingTripScreen(
                        viewModel = tripVm,
                        ownerName = "Rahul Yadav",
                        ownerPhone = authPrefs.ownerPhone.ifBlank { "9912345678" },
                        imageRequired = false, // TODO: read from truck settings
                        onBack = { navController.popBackStack() },
                        onTripEnded = { id -> navController.navigate("trip_summary/$id") { popUpTo("driver_home") } },
                    )
                }

                // ─── Main app (MAALIK_DRIVER) ───
                composable("trip_home") {
                    currentTab = Tab.Trip
                    TripScreen(tripVm,
                        onNewTrip = { navController.navigate("nayi_trip") },
                        onOngoingTrip = { navController.navigate("ongoing_trip") },
                        onEndTrip = { id -> tripVm.endTrip(id); navController.navigate("trip_summary/$id") },
                        onTripDetail = { id -> navController.navigate("trip_detail/$id") },
                        onStartScheduled = { id -> tripVm.startScheduledTrip(id); navController.navigate("ongoing_trip") })
                }
                composable("nayi_trip") {
                    NayiTripScreen(tripVm, onBack = { navController.popBackStack() }, onExtracted = { navController.navigate("trip_confirm") })
                }
                composable("trip_confirm") {
                    TripConfirmScreen(tripVm, onBack = { navController.popBackStack() },
                        onConfirmed = { navController.navigate("ongoing_trip") { popUpTo("trip_home") } },
                        onEdit = { navController.popBackStack() })
                }
                composable("ongoing_trip") {
                    OngoingTripScreen(tripVm, onBack = { navController.popBackStack() },
                        onTripEnded = { id -> navController.navigate("trip_summary/$id") { popUpTo("trip_home") } })
                }
                composable("trip_summary/{tripId}", arguments = listOf(navArgument("tripId") { type = NavType.StringType })) {
                    val tripId = it.arguments?.getString("tripId") ?: return@composable
                    TripSummaryScreen(tripVm, tripId, onGoHome = { navController.navigate("trip_home") { popUpTo("trip_home") { inclusive = true } } })
                }
                composable("trip_detail/{tripId}", arguments = listOf(navArgument("tripId") { type = NavType.StringType })) {
                    val tripId = it.arguments?.getString("tripId") ?: return@composable
                    TripDetailScreen(tripVm, tripId, onBack = { navController.popBackStack() },
                        onRepeat = { trip -> tripVm.extractTripFromText("${trip.origin} se ${trip.destination}, ${trip.cargoType} ${trip.cargoWeightTons} ton, ${trip.freightAmount} bhada"); navController.navigate("nayi_trip") })
                }
                composable("hisaab") { currentTab = Tab.Hisaab; HisaabScreen(hisaabVm, onTripDetail = { id -> navController.navigate("trip_detail/$id") }) }
                composable("calendar") { currentTab = Tab.Calendar; CalendarScreen(calVm) }
                composable("madad") { currentTab = Tab.Madad; MadadScreen() }
                composable("profile") {
                    currentTab = Tab.Profile
                    val profileVm: com.maalsaathi.app.ui.profile.ProfileViewModel = viewModel(factory = com.maalsaathi.app.ui.profile.ProfileViewModel.Factory(authPrefs))
                    com.maalsaathi.app.ui.profile.ProfileScreen(
                        viewModel = profileVm,
                        onLogout = { navController.navigate("splash") { popUpTo(0) { inclusive = true } } },
                        onManageFleet = { navController.navigate("fleet_dashboard") },
                    )
                }
            }
        }
        if (showNav) {
            BottomNavBar(currentTab) { tab ->
                currentTab = tab
                val route = when (tab) { Tab.Trip -> "trip_home"; Tab.Hisaab -> "hisaab"; Tab.Calendar -> "calendar"; Tab.Madad -> "madad"; Tab.Profile -> "profile" }
                navController.navigate(route) { popUpTo("trip_home") { saveState = true }; launchSingleTop = true; restoreState = true }
            }
        }
    }
}
