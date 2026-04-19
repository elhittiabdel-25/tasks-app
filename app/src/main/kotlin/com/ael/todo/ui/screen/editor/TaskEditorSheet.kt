package com.ael.todo.ui.screen.editor

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ael.todo.data.db.entity.Priority
import com.ael.todo.data.db.entity.Recurrence
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditorSheet(
    taskId: Long?,
    onDismiss: () -> Unit,
    viewModel: TaskEditorViewModel = hiltViewModel()
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val categories by viewModel.categories.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(taskId) {
        if (taskId != null && taskId > 0) viewModel.load(taskId)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(if (taskId != null && taskId > 0) "Edit Task" else "New Task",
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge)

            // Title
            OutlinedTextField(
                value = viewModel.title,
                onValueChange = viewModel::onTitleChange,
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Notes
            OutlinedTextField(
                value = viewModel.notes,
                onValueChange = { viewModel.notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            // Due date button
            val fmt = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault())
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = {
                    val cal = Calendar.getInstance().also {
                        viewModel.dueAt?.let { ms -> it.timeInMillis = ms }
                    }
                    DatePickerDialog(context, { _, y, m, d ->
                        TimePickerDialog(context, { _, h, min ->
                            cal.set(y, m, d, h, min, 0)
                            viewModel.dueAt = cal.timeInMillis
                        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
                    }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                }) {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                    Text(viewModel.dueAt?.let { fmt.format(it) } ?: "Set due date")
                }
                if (viewModel.dueAt != null) {
                    IconButton(onClick = { viewModel.dueAt = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear date")
                    }
                }
            }

            // Priority dropdown
            EnumDropdown(
                label = "Priority",
                selected = viewModel.priority,
                options = Priority.entries,
                onSelect = { viewModel.priority = it }
            )

            // Category dropdown
            if (categories.isNotEmpty()) {
                var expanded by remember { mutableStateOf(false) }
                val selectedCat = categories.firstOrNull { it.id == viewModel.categoryId }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = selectedCat?.name ?: "Personal",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = { viewModel.categoryId = cat.id; expanded = false }
                            )
                        }
                    }
                }
            }

            // Recurrence dropdown
            EnumDropdown(
                label = "Recurrence",
                selected = viewModel.recurrence,
                options = Recurrence.entries,
                onSelect = { viewModel.recurrence = it }
            )

            // Subtasks
            Text("Subtasks", style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
            viewModel.subtasks.forEachIndexed { i, sub ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = sub.title,
                        onValueChange = { viewModel.setSubtaskTitle(i, it) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text("Subtask ${i + 1}") }
                    )
                    IconButton(onClick = { viewModel.removeSubtask(i) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Remove subtask")
                    }
                }
            }
            TextButton(onClick = { viewModel.addSubtask() }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("Add subtask")
            }

            Spacer(Modifier.height(8.dp))

            // Save button
            Button(
                onClick = { viewModel.save(onDismiss) },
                enabled = viewModel.title.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save") }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T : Enum<T>> EnumDropdown(
    label: String,
    selected: T,
    options: List<T>,
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected.name.lowercase().replaceFirstChar { it.uppercase() },
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    onClick = { onSelect(option); expanded = false }
                )
            }
        }
    }
}
