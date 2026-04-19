package com.ael.todo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.ael.todo.ui.nav.NavGraph
import com.ael.todo.ui.screen.settings.SettingsViewModel
import com.ael.todo.ui.theme.TasksTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val openTaskId = intent?.getLongExtra("task_id", -1L).takeIf { it != -1L }
        setContent {
            val settingsVm: SettingsViewModel = hiltViewModel()
            val themeMode by settingsVm.themeMode.collectAsState()
            TasksTheme(themeMode = themeMode) {
                NavGraph(initialTaskId = openTaskId)
            }
        }
    }
}
