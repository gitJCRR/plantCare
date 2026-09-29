package com.tareaandroid.plantcare.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector
import com.tareaandroid.plantcare.R
import kotlin.reflect.KClass

/** Destinos principales que aparecen en la barra / rail de navegación. */
enum class TopLevelDestination(
    val route: Any,
    val routeClass: KClass<*>,
    val icon: ImageVector,
    @StringRes val label: Int,
) {
    HOME(HomeRoute, HomeRoute::class, Icons.Filled.Home, R.string.nav_home),
    LIGHT(LightMeterRoute, LightMeterRoute::class, Icons.Filled.WbSunny, R.string.nav_light),
    SETTINGS(SettingsRoute, SettingsRoute::class, Icons.Filled.Person, R.string.nav_settings),
}
