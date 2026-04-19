package com.ael.todo.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ael.todo.ui.screen.all.AllScreen
import com.ael.todo.ui.screen.category.CategoryDetailScreen
import com.ael.todo.ui.screen.completed.CompletedScreen
import com.ael.todo.ui.screen.editor.TaskEditorSheet
import com.ael.todo.ui.screen.settings.CategoriesScreen
import com.ael.todo.ui.screen.settings.SettingsScreen
import com.ael.todo.ui.screen.today.TodayScreen

sealed class Screen(val route: String, val label: String) {
    object Today : Screen("today", "Today")
    object All : Screen("all", "All")
    object Completed : Screen("completed", "Completed")
    object Settings : Screen("settings", "Settings")
    object Categories : Screen("categories", "Categories")
    object CategoryDetail : Screen("category/{categoryId}", "Category")
    object TaskEditor : Screen("editor?taskId={taskId}", "Task")
}

@Composable
fun NavGraph(initialTaskId: Long? = null) {
    val navController = rememberNavController()
    var editorTaskId by remember { mutableStateOf<Long?>(null) }
    var showEditor by remember { mutableStateOf(false) }

    LaunchedEffect(initialTaskId) {
        if (initialTaskId != null) {
            editorTaskId = initialTaskId
            showEditor = true
        }
    }

    val tabs = listOf(Screen.Today, Screen.All, Screen.Completed)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in tabs.map { it.route }) {
                NavigationBar {
                    tabs.forEach { screen ->
                        NavigationBarItem(
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = when (screen) {
                                        Screen.Today -> Icons.Default.Today
                                        Screen.All -> Icons.Default.List
                                        else -> Icons.Default.CheckCircle
                                    },
                                    contentDescription = screen.label
                                )
                            },
                            label = { Text(screen.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Today.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Today.route) {
                TodayScreen(
                    onAddTask = { editorTaskId = null; showEditor = true },
                    onEditTask = { id -> editorTaskId = id; showEditor = true }
                )
            }
            composable(Screen.All.route) {
                AllScreen(
                    onAddTask = { editorTaskId = null; showEditor = true },
                    onEditTask = { id -> editorTaskId = id; showEditor = true },
                    onCategoryClick = { id -> navController.navigate("category/$id") }
                )
            }
            composable(Screen.Completed.route) {
                CompletedScreen(
                    onEditTask = { id -> editorTaskId = id; showEditor = true }
                )
            }
            composable("category/{categoryId}") { back ->
                val catId = back.arguments?.getString("categoryId")?.toLongOrNull() ?: return@composable
                CategoryDetailScreen(
                    categoryId = catId,
                    onAddTask = { editorTaskId = null; showEditor = true },
                    onEditTask = { id -> editorTaskId = id; showEditor = true },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onCategoriesClick = { navController.navigate(Screen.Categories.route) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Categories.route) {
                CategoriesScreen(onBack = { navController.popBackStack() })
            }
        }
    }

    if (showEditor) {
        TaskEditorSheet(
            taskId = editorTaskId,
            onDismiss = { showEditor = false }
        )
    }
}
