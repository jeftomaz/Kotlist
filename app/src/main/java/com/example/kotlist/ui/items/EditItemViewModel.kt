package com.example.kotlist.ui.items

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlist.data.model.ItemCategory
import com.example.kotlist.data.model.ItemUnit
import com.example.kotlist.data.model.ListItem
import com.example.kotlist.data.model.ShoppingList
import com.example.kotlist.data.repository.ListItemRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class EditItemUiState(
    val nameError: String? = null,
    val quantityError: String? = null,
    val unitError: String? = null,
    val categoryError: String? = null
)

sealed class EditItemEvent {
    data class ShowMessage(val message: String) : EditItemEvent()
    object Finish : EditItemEvent()
}

class EditItemViewModel(
    private val listItemRepository: ListItemRepository
) : ViewModel() {

    private var itemOnEditing: ListItem? = null
    private val _itemToEdit = MutableStateFlow<ListItem?>(null)
    val itemToEdit: StateFlow<ListItem?> = _itemToEdit

    private val _uiState = MutableStateFlow(EditItemUiState())
    val uiState: StateFlow<EditItemUiState> = _uiState

    private val _events = MutableSharedFlow<EditItemEvent>()
    val events: SharedFlow<EditItemEvent> = _events

    fun loadItem(itemId: String?) {
        if(itemId == null) {
            viewModelScope.launch {
                _events.emit(EditItemEvent.ShowMessage("Algo deu errado ao carregar o item."))
                _events.emit(EditItemEvent.Finish)
            }
            return
        }

        val item = listItemRepository.getItemById(itemId)
        if(item == null) {
            viewModelScope.launch {
                _events.emit(EditItemEvent.ShowMessage("Item não encontrado."))
                _events.emit(EditItemEvent.Finish)
            }
            return
        }

        itemOnEditing = item
        _itemToEdit.value = item
    }

    fun deleteItem(itemId: String?) {
        if(itemId == null) {
            viewModelScope.launch {
                _events.emit(EditItemEvent.ShowMessage("Algo deu errado ao excluir o item."))
            }
            return
        }

        listItemRepository.deleteItem(itemId)

        viewModelScope.launch {
            _events.emit(EditItemEvent.ShowMessage("Item excluído com sucesso!"))
            _events.emit(EditItemEvent.Finish)
        }
    }

    fun updateItem(
        listId: String?,
        name: String,
        quantityText: String,
        unit: ItemUnit?,
        category: ItemCategory?
    ) {
        _uiState.value = EditItemUiState()

        val currentItem = itemOnEditing
        if(listId == null || currentItem == null) {
            viewModelScope.launch {
                _events.emit(EditItemEvent.ShowMessage("Algo deu errado ao editar o item."))
                _events.emit(EditItemEvent.Finish)
            }
            return
        }

        val trimmedName = name.trim()
        val quantity = quantityText.toIntOrNull()

        var hasError = false
        var nameError: String? = null
        var quantityError: String? = null
        var unitError: String? = null
        var categoryError: String? = null

        if(trimmedName.isEmpty()) {
            nameError = "O nome do item não pode ser vazio."
            hasError = true
        }

        if(quantity == null) {
            quantityError = "A quantidade do item não pode ser vazia."
            hasError = true
        }

        if(unit == null) {
            unitError = "A unidade do item não pode ser vazia."
            hasError = true
        }

        if(category == null) {
            categoryError = "A categoria do item não pode ser vazia."
            hasError = true
        }

        if(hasError) {
            _uiState.value = EditItemUiState(
                nameError = nameError,
                quantityError = quantityError,
                unitError = unitError,
                categoryError = categoryError
            )

            viewModelScope.launch {
                _events.emit(
                    EditItemEvent.ShowMessage(
                        "Preencha todos os campos para adicionar um item."
                    )
                )
            }
            return
        }

        val updatedItem = currentItem.copy(
            name = trimmedName,
            quantity = quantity!!,
            unit = unit!!,
            category = category!!
        )

        listItemRepository.updateItem(updatedItem)
        itemOnEditing = updatedItem

        viewModelScope.launch {
            _events.emit(EditItemEvent.ShowMessage("Item editado com sucesso!"))
            _events.emit(EditItemEvent.Finish)
        }
    }
}