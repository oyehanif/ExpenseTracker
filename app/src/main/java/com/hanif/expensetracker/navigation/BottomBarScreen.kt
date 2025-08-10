package com.hanif.expensetracker.navigation

import com.hanif.expensetracker.R

sealed class BottomBarScreen(
    val route: String,
    val title: String,
    val icon: Int
) {
    data object Home : BottomBarScreen(
        route = "home",
        title = "Home",
        icon = R.drawable.ic_dashbaord
    )

    data object Analytics : BottomBarScreen(
        route = "Analytics",
        title = "Analytics",
        icon = R.drawable.ic_graph
    )
}

val screens = listOf(
    BottomBarScreen.Home,
    BottomBarScreen.Analytics,
)