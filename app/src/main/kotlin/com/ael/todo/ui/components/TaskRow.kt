package com.ael.todo.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ael.todo.data.db.entity.Priority
import com.ael.todo.data.db.entity.TaskStatus
import com.ael.todo.data.db.relation.TaskWithDetails
import com.ael.todo.ui.theme.PriorityHigh
import com.ael.todo.ui.theme.PriorityLow
import com.ael.todo.ui.theme.PriorityMedium
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableTaskRow(
    taskWithDetails: TaskWithDetails,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val task = taskWithDetails.task
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> { onComplete(); false }
                SwipeToDismissBoxValue.EndToStart -> { onDelete(); true }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                when (dismissState.dismissDirection) {
                    SwipeToDismissBoxValue.StartToEnd -> Color(0xFF4CAF50)
                    SwipeToDismissBoxValue.EndToStart -> Color(0xFFEF5350)
                    else -> Color.Transparent
                }, label = "swipe_bg"
            )
            Box(
                modifier = Modifier.fillMaxSize().background(color).padding(horizontal = 16.dp),
                contentAlignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart)
                    Alignment.CenterEnd else Alignment.CenterStart
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
            }
        }
    ) {
        TaskRowContent(taskWithDetails = taskWithDetails, onComplete = onComplete, onClick = onClick)
    }
}

@Composable
fun TaskRowContent(
    taskWithDetails: TaskWithDetails,
    onComplete: () -> Unit,
    onClick: () -> Unit
) {
    val task = taskWithDetails.task
    val isCompleted = task.status == TaskStatus.COMPLETED
    val priorityColor = when (task.priority) {
        Priority.HIGH -> PriorityHigh
        Priority.MEDIUM -> PriorityMedium
        Priority.LOW -> PriorityLow
        Priority.NONE -> Color.Transparent
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (task.priority != Priority.NONE) {
            Box(modifier = Modifier.width(4.dp).height(56.dp).background(priorityColor))
        }
        Checkbox(
            checked = isCompleted,
            onCheckedChange = { onComplete() },
            modifier = Modifier.padding(start = 4.dp)
        )
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp, top = 8.dp, bottom = 8.dp)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (isCompleted) TextDecoration.LineThrough else null,
                color = if (isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            val fmt = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
            task.dueAt?.let {
                Text(
                    text = fmt.format(it),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            taskWithDetails.category?.let {
                Text(
                    text = it.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(it.color).copy(alpha = 0.85f)
                )
            }
        }
    }
}
