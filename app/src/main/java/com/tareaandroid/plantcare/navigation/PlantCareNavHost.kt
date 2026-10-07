package com.tareaandroid.plantcare.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.tareaandroid.plantcare.ui.auth.LoginScreen
import com.tareaandroid.plantcare.ui.auth.RegisterScreen
import com.tareaandroid.plantcare.ui.detail.PlantDetailScreen
import com.tareaandroid.plantcare.ui.edit.PlantEditScreen
import com.tareaandroid.plantcare.ui.home.HomeScreen
import com.tareaandroid.plantcare.ui.light.LightMeterScreen
import com.tareaandroid.plantcare.ui.settings.SettingsScreen

/**
 * Raíz de la navegación. Los destinos principales se muestran dentro de un
 * [NavigationSuiteScaffold], que usa barra inferior en móvil y rail lateral
 * en pantallas anchas (diseño adaptativo).
 */
@Composable
fun PlantCareApp(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val showNavigation = TopLevelDestination.entries.any {
        currentDestination?.hasRoute(it.routeClass) == true
    }

    if (showNavigation) {
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                TopLevelDestination.entries.forEach { destination ->
                    item(
                        selected = currentDestination?.hierarchy?.any { it.hasRoute(destination.routeClass) } == true,
                        onClick = { navController.navigateToTopLevel(destination) },
                        icon = { Icon(destination.icon, contentDescription = null) },
                        label = { Text(stringResource(destination.label)) },
                    )
                }
            },
        ) {
            PlantCareNavHost(navController)
        }
    } else {
        PlantCareNavHost(navController)
    }
}

@Composable
private fun PlantCareNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = LoginRoute, modifier = modifier) {
        composable<LoginRoute> {
            LoginScreen(
                onLoginSuccess = { navController.navigateClearingBackStack(HomeRoute) },
                onGoToRegister = { navController.navigate(RegisterRoute) },
            )
        }
        composable<RegisterRoute> {
            RegisterScreen(
                onRegisterSuccess = { navController.navigateClearingBackStack(HomeRoute) },
                onBack = { navController.popBackStack() },
            )
        }
        composable<HomeRoute> {
            HomeScreen(
                onPlantClick = { plantId -> navController.navigate(PlantDetailRoute(plantId)) },
                onAddPlant = { navController.navigate(PlantEditRoute()) },
            )
        }
        composable<PlantDetailRoute> { entry ->
            val route = entry.toRoute<PlantDetailRoute>()
            PlantDetailScreen(
                plantId = route.plantId,
                onEdit = { navController.navigate(PlantEditRoute(route.plantId)) },
                onBack = { navController.popBackStack() },
            )
        }
        composable<PlantEditRoute> {
            // El ViewModel lee el plantId de la ruta a través de su SavedStateHandle
            PlantEditScreen(
                onDone = { navController.popBackStack() },
                onBack = { navController.popBackStack() },
            )
        }
        composable<LightMeterRoute> {
            LightMeterScreen()
        }
        composable<SettingsRoute> {
            SettingsScreen(
                onLogout = { navController.navigateClearingBackStack(LoginRoute) },
            )
        }
    }
}

/** Navega a un destino principal sin apilar copias y conservando su estado. */
private fun NavHostController.navigateToTopLevel(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** Navega a [route] borrando toda la pila (tras login / logout). */
private fun NavHostController.navigateClearingBackStack(route: Any) {
    navigate(route) {
        popUpTo(0) { inclusive = true }
        launchSingleTop = true
    }
}
