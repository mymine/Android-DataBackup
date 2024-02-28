package com.xayah.feature.setup

sealed class SetupRoutes(val route: String) {
    data object One : SetupRoutes(route = "setup_one")
    data object Two : SetupRoutes(route = "setup_two")
}
