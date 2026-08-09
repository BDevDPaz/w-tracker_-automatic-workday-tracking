package com.example.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.ToastNotificationBanner
import com.example.ui.screens.AiAssistantScreen
import com.example.ui.screens.AjustesScreen
import com.example.ui.screens.DestinationsScreen
import com.example.ui.screens.HistorialScreen
import com.example.ui.screens.HoyScreen
import com.example.ui.screens.ReportesScreen
import com.example.ui.theme.CarbonMistElevated
import com.example.ui.theme.GraphiteSurface
import com.example.ui.theme.MutedGold
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.SteelLineBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.viewmodel.MainViewModel

import com.example.ui.screens.OnboardingScreen

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Hoy : Screen("hoy", "Hoy", Icons.Default.Home)
    object Zonas : Screen("zonas", "Zonas", Icons.Default.Place)
    object Historial : Screen("historial", "Historial", Icons.Default.History)
    object Reportes : Screen("reportes", "Reportes", Icons.Default.Assessment)
    object Asistente : Screen("asistente", "IA", Icons.Default.AutoAwesome)
    object Ajustes : Screen("ajustes", "Ajustes", Icons.Default.Settings)
}

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val prefs by viewModel.userPreferences.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    if (!prefs.isOnboardingCompleted) {
        Box(modifier = Modifier.fillMaxSize()) {
            OnboardingScreen(viewModel = viewModel)
            ToastNotificationBanner(
                message = toastMessage,
                onDismiss = { viewModel.clearToast() }
            )
        }
        return
    }

    val screens = listOf(
        Screen.Hoy,
        Screen.Zonas,
        Screen.Historial,
        Screen.Reportes,
        Screen.Asistente,
        Screen.Ajustes
    )

    Scaffold(
        containerColor = ObsidianBackground,
        bottomBar = {
            NavigationBar(
                containerColor = GraphiteSurface,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .background(GraphiteSurface)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                screens.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                tint = if (isSelected) MutedGold else TextMuted
                            )
                        },
                        label = {
                            Text(
                                text = screen.title.uppercase(),
                                color = if (isSelected) MutedGold else TextMuted,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = CarbonMistElevated
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Hoy.route
            ) {
                composable(Screen.Hoy.route) {
                    HoyScreen(viewModel = viewModel)
                }
                composable(Screen.Zonas.route) {
                    DestinationsScreen(viewModel = viewModel)
                }
                composable(Screen.Historial.route) {
                    HistorialScreen(viewModel = viewModel)
                }
                composable(Screen.Reportes.route) {
                    ReportesScreen(viewModel = viewModel)
                }
                composable(Screen.Asistente.route) {
                    AiAssistantScreen(viewModel = viewModel)
                }
                composable(Screen.Ajustes.route) {
                    AjustesScreen(viewModel = viewModel)
                }
            }

            // Global Toast Overlay
            ToastNotificationBanner(
                message = toastMessage,
                onDismiss = { viewModel.clearToast() }
            )
        }
    }
}
