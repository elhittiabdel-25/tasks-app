package com.ael.todo.ui.screen.all

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.ael.todo.R
import com.ael.todo.ui.components.SwipeableTaskRow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AllScreen(
    onAddTask: () -> Unit,
    onEditTask: (Long) -> Unit,
    onCategoryClick: (Long) -> Unit,
    viewModel: AllViewModel = hiltViewModel()
) {
    val tasks by viewModel.tasks.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var collapsedCategories by remember { mutableStateOf(setOf<String>()) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.tab_all)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTask) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_task))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.no_tasks),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            val grouped = tasks.groupBy { it.category }
            LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                grouped.forEach { (category, taskList) ->
                    val catName = category?.name ?: "Personal"
                    val collapsed = catName in collapsedCategories
                    stickyHeader(key = "header_$catName") {
                        ListItem(
                            headlineContent = {
                                Text(catName, style = MaterialTheme.typography.titleMedium)
                            },
                            trailingContent = {
                                Text(if (collapsed) "▸" else "▾")
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.clickable {
                                collapsedCategories = if (collapsed)
                                    collapsedCategories - catName
                                else
                                    collapsedCategories + catName
                            }
                        )
                    }
                    if (!collapsed) {
                        items(taskList, key = { it.task.id }) { item ->
                            SwipeableTaskRow(
                                taskWithDetails = item,
                                onComplete = { viewModel.complete(item) },
                                onDelete = {
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar("Task deleted", "Undo")
                                        if (result == SnackbarResult.ActionPerformed) viewModel.undoDelete(item)
                                    }
                                    viewModel.delete(item)
                                },
                                onClick = { onEditTask(item.task.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
