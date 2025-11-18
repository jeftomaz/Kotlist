package com.example.kotlist.ui.items

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kotlist.data.repository.ListItemRepository
import com.example.kotlist.data.repository.ShoppingListRepository

class ItemListViewModelFactory(
    private val listItemRepository: ListItemRepository,
    private val shoppingListRepository: ShoppingListRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ItemListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ItemListViewModel(listItemRepository, shoppingListRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}