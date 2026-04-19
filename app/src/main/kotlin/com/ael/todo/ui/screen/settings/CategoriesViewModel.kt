package com.ael.todo.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ael.todo.data.db.entity.Category
import com.ael.todo.data.db.relation.CategoryWithKeywords
import com.ael.todo.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {

    val categoriesWithKeywords = repository.getAllCategoriesWithKeywords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addCategory(name: String, color: Int) = viewModelScope.launch {
        repository.insertCategory(Category(name = name, color = color, isPredefined = false))
    }

    fun renameCategory(category: Category, newName: String) = viewModelScope.launch {
        repository.updateCategory(category.copy(name = newName))
    }

    fun deleteCategory(category: Category) = viewModelScope.launch {
        if (!category.isPredefined) repository.deleteCategory(category)
    }

    fun updateKeywords(categoryId: Long, keywords: List<String>) = viewModelScope.launch {
        repository.replaceKeywords(categoryId, keywords)
    }
}
