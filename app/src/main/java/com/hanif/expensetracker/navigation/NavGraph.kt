package com.hanif.expensetracker.navigation

import ExpenseEntryScreen
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.hanif.expensetracker.view.ExpenseListScreen
import com.hanif.expensetracker.view.ExpenseReportScreen

@Composable
fun NavGraph(navController: NavHostController, modifier: Modifier) {
    NavHost(
        navController = navController,
        startDestination = BottomBarScreen.Home.route
    ) {
        composable(route = BottomBarScreen.Home.route) {
            ExpenseListScreen(
                onNavigateToAddExpense = { navController.navigate(Screens.AddRecordScreen) }
            )
        }
        composable(route = BottomBarScreen.Analytics.route) {
            ExpenseReportScreen()
        }
        composable(route = Screens.AddRecordScreen) {
            ExpenseEntryScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}