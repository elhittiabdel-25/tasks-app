package com.ael.todo.ui.screen.completed

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.ael.todo.R
import com.ael.todo.ui.components.SwipeableTaskRow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CompletedScreen(
    onEditTask: (Long) -> Unit,
    viewModel: CompletedViewModel = hiltViewModel()
) {
    val tasks by viewModel.tasks.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.tab_completed)) }) }
    ) { innerPadding ->
        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.no_completed),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            val now = Calendar.getInstance()
            fun dayLabel(epochMs: Long): String {
                val cal = Calendar.getInstance().apply { timeInMillis = epochMs }
                val dayDiff = (now.get(Calendar.DAY_OF_YEAR) - cal.get(Calendar.DAY_OF_YEAR))
                    .let { if (now.get(Calendar.YEAR) != cal.get(Calendar.YEAR)) 999 else it }
                return when {
                    dayDiff == 0 -> "Today"
                    dayDiff == 1 -> "Yesterday"
                    dayDiff <= 6 -> "This week"
                    else -> "Last week"
                }
            }
            val grouped = tasks.groupBy { dayLabel(it.task.completedAt ?: it.task.createdAt) }
            val order = listOf("Today", "Yesterday", "This week", "Last week")
            LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                order.forEach { label ->
                    val group = grouped[label] ?: return@forEach
                    stickyHeader(key = "header_$label") {
                        ListItem(
                            headlineContent = { Text(label, style = MaterialTheme.typography.titleMedium) },
                            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                    items(group, key = { it.task.id }) { item ->
                        SwipeableTaskRow(
                            taskWithDetails = item,
                            onComplete = { viewModel.uncomplete(item) },
                            onDelete = { viewModel.delete(item) },
                            onClick = { onEditTask(item.task.id) }
                        )
                    }
                }
            }
        }
    }
}
