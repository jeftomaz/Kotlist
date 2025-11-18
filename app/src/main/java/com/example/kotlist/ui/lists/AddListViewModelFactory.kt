package com.example.kotlist.ui.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kotlist.data.repository.ShoppingListRepository
import com.example.kotlist.data.repository.UserRepository

class AddListViewModelFactory(
    private val userRepository: UserRepository,
    private val shoppingListRepository: ShoppingListRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(AddListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddListViewModel(userRepository, shoppingListRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}