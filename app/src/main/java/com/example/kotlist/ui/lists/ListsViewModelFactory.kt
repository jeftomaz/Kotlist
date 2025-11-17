package com.example.kotlist.ui.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kotlist.data.repository.ShoppingListRepository
import com.example.kotlist.data.repository.UserRepository

class ListsViewModelFactory(
    private val userRepository: UserRepository,
    private val shoppingListRepository: ShoppingListRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ListsViewModel(userRepository, shoppingListRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}