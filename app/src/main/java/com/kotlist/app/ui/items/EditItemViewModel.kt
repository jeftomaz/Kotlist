package com.kotlist.app.ui.items

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlist.app.data.model.ItemCategory
import com.kotlist.app.data.model.ItemUnit
import com.kotlist.app.data.model.ListItem
import com.kotlist.app.data.repository.ListItemRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class EditItemUiState {
    data object Idle : EditItemUiState()
    data object Loading : EditItemUiState()
    data class ItemLoaded(val item: ListItem) : EditItemUiState()
    data class Success(val message: String) : EditItemUiState()
    data class Error(val message: String) : EditItemUiState()
    data class ValidationFailure(
        val nameError: String? = null,
        val quantityError: String? = null,
        val unitError: String? = null,
        val categoryError: String? = null
    ) : EditItemUiState()
}

class EditItemViewModel(
    private val listItemRepository: ListItemRepository
) : ViewModel() {

    private data class ValidationResult(
        val isFailure: Boolean,
        val errorState: EditItemUiState.ValidationFailure? = null
    )

    private val _uiState = MutableStateFlow<EditItemUiState>(EditItemUiState.Idle)
    val uiState: StateFlow<EditItemUiState> = _uiState

    private var currentItem: ListItem? = null
    private var currentListId: String? = null

    fun loadItem(listId: String?, itemId: String?) {
        if(listId.isNullOrEmpty()) {
            _uiState.value = EditItemUiState.Error("Erro ao encontrar a lista relacionada")
            return
        }

        if(itemId.isNullOrEmpty()) {
            _uiState.value = EditItemUiState.Error("Erro ao encontrar o item para editar")
            return
        }

        currentListId = listId
        _uiState.value = EditItemUiState.Loading

        viewModelScope.launch {
            try {
                val item = listItemRepository.getItemById(listId, itemId)

                if(item == null) {
                    _uiState.value = EditItemUiState.Error("Erro ao encontrar o item para editar")
                    return@launch
                }

                currentItem = item
                _uiState.value = EditItemUiState.ItemLoaded(item)

            } catch (e: Exception) {
                _uiState.value = EditItemUiState.Error("Erro ao carregar item. Tente novamente.")
            }
        }
    }

    fun updateItem(
        name: String,
        quantityText: String,
        unit: ItemUnit?,
        category: ItemCategory?
    ) {
        val trimmedName = name.trim()
        val quantity = quantityText.toIntOrNull()

        val validation = validateInputs(
            name = trimmedName,
            quantity = quantity,
            unit = unit,
            category = category
        )

        if(validation.isFailure) {
            validation.errorState?.let { _uiState.value = it }
            return
        }

        val item = currentItem
        val listId = currentListId

        if(item == null || listId.isNullOrEmpty()) {
            _uiState.value = EditItemUiState.Error("Erro: não possível atualizar o item")
            return
        }

        _uiState.value = EditItemUiState.Loading

        viewModelScope.launch {
            try {
                val updatedItem = item.copy(
                    name = trimmedName,
                    quantity = quantity!!,
                    unit = unit!!.name,
                    category = category!!.name
                )

                listItemRepository.updateItem(listId, updatedItem)
                currentItem = updatedItem
                _uiState.value = EditItemUiState.Success("Item atualizado com sucesso")

            } catch (e: Exception) {
                _uiState.value = EditItemUiState.Error("Erro ao atualizar item. Tente novamente.")
            }
        }
    }

    fun deleteItem() {
        val item = currentItem
        val listId = currentListId

        if(item == null || listId.isNullOrEmpty()) {
            _uiState.value = EditItemUiState.Error("Erro: não foi possível excluir o item")
            return
        }

        _uiState.value = EditItemUiState.Loading

        viewModelScope.launch {
            try {
                listItemRepository.deleteItem(listId, item.id)
                _uiState.value = EditItemUiState.Success("Item excluído com sucesso")

            } catch (e: Exception) {
                _uiState.value = EditItemUiState.Error("Erro ao deletar item. Tente novamente.")
            }
        }
    }

    private fun validateInputs(
        name: String,
        quantity: Int?,
        unit: ItemUnit?,
        category: ItemCategory?
    ): ValidationResult {
        var nameError: String? = null
        var quantityError: String? = null
        var unitError: String? = null
        var categoryError: String? = null
        var hasError = false

        if (name.isEmpty()) {
            nameError = "O nome do item não pode ser vazio."
            hasError = true
        }

        if (quantity == null || quantity <= 0) {
            quantityError = "Informe uma quantidade válida."
            hasError = true
        }

        if (unit == null) {
            unitError = "Selecione uma unidade."
            hasError = true
        }

        if (category == null) {
            categoryError = "Selecione uma categoria."
            hasError = true
        }

        return if (hasError) {
            ValidationResult(
                isFailure = true,
                errorState = EditItemUiState.ValidationFailure(
                    nameError = nameError,
                    quantityError = quantityError,
                    unitError = unitError,
                    categoryError = categoryError
                )
            )
        } else {
            ValidationResult(isFailure = false)
        }
    }
}