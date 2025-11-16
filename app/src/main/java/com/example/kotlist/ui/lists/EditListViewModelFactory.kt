package com.example.kotlist.ui.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kotlist.data.repository.ShoppingListRepository

class EditListViewModelFactory(
    private val shoppingListRepository: ShoppingListRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditListViewModel(shoppingListRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}