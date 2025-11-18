package com.example.kotlist.ui.items

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kotlist.data.model.ItemCategory
import com.example.kotlist.data.model.ItemUnit
import com.example.kotlist.data.model.ListItem
import com.example.kotlist.data.repository.ListItemRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AddItemUiState(
    val nameError: String? = null,
    val quantityError: String? = null,
    val unitError: String? = null,
    val categoryError: String? = null
)

sealed class AddItemEvent {
    data class ShowMessage(val message: String) : AddItemEvent()
    object Success : AddItemEvent()
}

class AddItemViewModel(
    private val listItemRepository: ListItemRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddItemUiState())
    val uiState: StateFlow<AddItemUiState> = _uiState

    private val _events = MutableSharedFlow<AddItemEvent>()
    val events: SharedFlow<AddItemEvent> = _events

    fun addItem(listId: String?, name: String, quantityText: String, unit: ItemUnit?, category: ItemCategory?) {
        _uiState.value = AddItemUiState()

        if(listId == null) {
            viewModelScope.launch {
                _events.emit(AddItemEvent.ShowMessage("Algo deu errado ao adicionar o item."))
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
            _uiState.value = AddItemUiState(
                nameError = nameError,
                quantityError = quantityError,
                unitError = unitError,
                categoryError = categoryError
            )

            viewModelScope.launch {
                _events.emit(AddItemEvent.ShowMessage("Preencha todos os campos para adicionar um item."))
            }
            return
        }

        val newListItem = ListItem(
            listId = listId,
            name = trimmedName,
            quantity = quantity!!,
            unit = unit!!,
            category = category!!
        )

        listItemRepository.addItem(newListItem)

        viewModelScope.launch {
            _events.emit(AddItemEvent.ShowMessage("Novo item da lista criado com sucesso."))
            _events.emit(AddItemEvent.Success)
        }
    }
}
