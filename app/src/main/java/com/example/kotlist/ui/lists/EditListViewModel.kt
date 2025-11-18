package com.example.kotlist.ui.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlist.data.model.ShoppingList
import com.example.kotlist.data.repository.ShoppingListRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EditListViewModel(
    private val shoppingListRepository: ShoppingListRepository
) : ViewModel() {

    private var listOnEditing: ShoppingList? = null
    private val _listToEdit = MutableStateFlow<ShoppingList?>(null)
    val listToEdit: StateFlow<ShoppingList?> = _listToEdit

    private val _listNameError = MutableStateFlow<String?>(null)
    val listNameError: StateFlow<String?> = _listNameError

    private val _updateEventMessage = MutableSharedFlow<String>()
    val updateEventMessage: SharedFlow<String> = _updateEventMessage

    private val _deleteEventMessage = MutableSharedFlow<String>()
    val deleteEventMessage: SharedFlow<String> = _deleteEventMessage

    fun loadList(listId: String) {
        listOnEditing = shoppingListRepository.getListById(listId)
        _listToEdit.value = listOnEditing
    }

    fun saveList(newTitle: String, newImageUri: String?) {
        if(newTitle.isEmpty()) {
            _listNameError.value = "A lista deve ter um nome."
            return
        }

        _listNameError.value = null

        val currentList = listOnEditing ?: return

        val updatedList = currentList.copy(
            title = newTitle,
            coverImageUri = newImageUri ?: currentList.coverImageUri
        )

        shoppingListRepository.updateList(updatedList)

        viewModelScope.launch {
            _updateEventMessage.emit("Lista atualizada com sucesso!")
        }
    }

    fun deleteList() {
        val listId = listOnEditing?.id ?: return
        shoppingListRepository.deleteList(listId)

        viewModelScope.launch {
            _deleteEventMessage.emit("Lista excluída com sucesso!")
        }
    }
}