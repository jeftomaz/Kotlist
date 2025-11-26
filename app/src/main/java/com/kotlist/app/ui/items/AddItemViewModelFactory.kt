package com.kotlist.app.ui.items

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kotlist.app.data.repository.ListItemRepository

class AddItemViewModelFactory(
    private val listItemRepository: ListItemRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(AddItemViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddItemViewModel(listItemRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}