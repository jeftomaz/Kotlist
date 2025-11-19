package com.example.kotlist.ui.items

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kotlist.data.repository.ListItemRepository

class EditItemViewModelFactory(
    private val listItemRepository: ListItemRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditItemViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditItemViewModel(listItemRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}