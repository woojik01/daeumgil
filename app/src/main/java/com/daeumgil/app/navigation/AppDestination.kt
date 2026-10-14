package com.daeumgil.app.navigation

import androidx.annotation.StringRes
import com.daeumgil.app.R

enum class AppDestination(val route: String, @param:StringRes val titleRes: Int) {
    HOME("home", R.string.app_name),
    CURRENT_SITUATION("current-situation", R.string.current_situation),
    DESTINATION_SEARCH("destination-search", R.string.destination),
    ROUTE_RESULTS("route-results", R.string.routes_title),
    NAVIGATION("navigation", R.string.navigation_title),
    ROUTE_RECOVERY("route-recovery", R.string.recovery_title),
    FAVORITES("favorites", R.string.favorites),
    SETTINGS("settings", R.string.settings),
}

