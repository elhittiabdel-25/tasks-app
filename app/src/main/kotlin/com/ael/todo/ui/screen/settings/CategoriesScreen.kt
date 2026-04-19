package com.ael.todo.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ael.todo.data.db.entity.Category
import com.ael.todo.data.db.relation.CategoryWithKeywords

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onBack: () -> Unit,
    viewModel: CategoriesViewModel = hiltViewModel()
) {
    val categoriesWithKeywords by viewModel.categoriesWithKeywords.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CategoryWithKeywords?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add category")
            }
        }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            items(categoriesWithKeywords, key = { it.category.id }) { item ->
                ListItem(
                    headlineContent = { Text(item.category.name) },
                    supportingContent = {
                        Text(
                            item.keywords.joinToString(", ") { it.keyword }.ifEmpty { "No keywords" },
                            maxLines = 2
                        )
                    },
                    leadingContent = {
                        androidx.compose.foundation.Canvas(
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .also { }
                                .let {
                                    Modifier
                                        .padding(4.dp)
                                        .then(
                                            Modifier.padding(0.dp)
                                        )
                                }
                        ) {
                            drawCircle(color = Color(item.category.color), radius = 16f)
                        }
                    },
                    trailingContent = {
                        Row {
                            TextButton(onClick = { editingCategory = item }) { Text("Edit") }
                            if (!item.category.isPredefined) {
                                IconButton(onClick = { viewModel.deleteCategory(item.category) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
                            }
                        }
                    }
                )
            }
        }
    }

    if (showAddDialog) {
        AddCategoryDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, color -> viewModel.addCategory(name, color); showAddDialog = false }
        )
    }

    editingCategory?.let { catWithKw ->
        EditKeywordsDialog(
            categoryWithKeywords = catWithKw,
            onDismiss = { editingCategory = null },
            onConfirm = { keywords ->
                viewModel.updateKeywords(catWithKw.category.id, keywords)
                editingCategory = null
            }
        )
    }
}

@Composable
private fun AddCategoryDialog(onDismiss: () -> Unit, onConfirm: (String, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Category") },
        text = {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onConfirm(name, 0xFF607D8B.toInt()) },
                enabled = name.isNotBlank()) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun EditKeywordsDialog(
    categoryWithKeywords: CategoryWithKeywords,
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    var text by remember {
        mutableStateOf(categoryWithKeywords.keywords.joinToString(", ") { it.keyword })
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Keywords — ${categoryWithKeywords.category.name}") },
        text = {
            Column {
                Text("Comma-separated keywords:", style = MaterialTheme.typography.labelMedium)
                OutlinedTextField(value = text, onValueChange = { text = it }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val keywords = text.split(",").map { it.trim().lowercase() }.filter { it.isNotBlank() }
                onConfirm(keywords)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
