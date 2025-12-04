package com.kotlist.app.ui.items

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlist.app.data.model.ItemCategory
import com.kotlist.app.data.model.ItemUnit
import com.kotlist.app.data.model.ListItem
import com.kotlist.app.data.repository.ListItemRepository
import com.kotlist.app.ui.auth.SignUpState
import com.kotlist.app.ui.auth.SignUpViewModel.ValidationResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.compareTo

sealed class AddItemUiState {
    data object Idle : AddItemUiState()
    data object Loading : AddItemUiState()
    data object Success : AddItemUiState()
    data class Error(val message: String) : AddItemUiState()
    data class ValidationFailure(
        val nameError: String? = null,
        val quantityError: String? = null,
        val unitError: String? = null,
        val categoryError: String? = null
    ) : AddItemUiState()
}

class AddItemViewModel(
    private val listItemRepository: ListItemRepository
) : ViewModel() {

    private data class ValidationResult(
        val isFailure: Boolean,
        val errorState: AddItemUiState.ValidationFailure? = null
    )

    private val _uiState = MutableStateFlow<AddItemUiState>(AddItemUiState.Idle)
    val uiState: StateFlow<AddItemUiState> = _uiState

    fun addItem(listId: String?, name: String, quantityText: String, unit: ItemUnit?, category: ItemCategory?) {
        val trimmedName = name.trim()
        val quantity = quantityText.toIntOrNull()

        val validation = validateInputs(
            name = trimmedName,
            quantity = quantity,
            unit = unit,
            category = category)

        if(validation.isFailure) {
            validation.errorState?.let { _uiState.value = it }
            return
        }

        _uiState.value = AddItemUiState.Loading

        viewModelScope.launch {
            try {
                if(listId.isNullOrEmpty()) {
                    _uiState.value = AddItemUiState.Error("Erro: lista não encontrada para adicionar o item")
                    return@launch
                }

                val newListItem = ListItem(
                    name = trimmedName,
                    quantity = quantity!!,
                    unitEnum = unit!!,
                    categoryEnum = category!!
                )

                listItemRepository.addItem(listId, newListItem)
                _uiState.value = AddItemUiState.Success

            } catch (e: Exception) {
                _uiState.value = AddItemUiState.Error("Erro ao adicionar item. Tente novamente")
            }
        }
    }

    private fun validateInputs(name: String, quantity: Int?, unit: ItemUnit?, category: ItemCategory?): ValidationResult {
        var nameError: String? = null
        var quantityError: String? = null
        var unitError: String? = null
        var categoryError: String? = null
        var hasError = false

        if(name.isEmpty()) {
            nameError = "O nome do item não pode ser vazio."
            hasError = true
        }

        if(quantity == null || quantity <= 0) {
            quantityError = "Informe uma quantidade válida."
            hasError = true
        }

        if(unit == null) {
            unitError = "Selecione uma unidade."
            hasError = true
        }

        if(category == null) {
            categoryError = "Selecione uma categoria."
            hasError = true
        }

        return if(hasError) {
            ValidationResult(
                isFailure = true,
                errorState = AddItemUiState.ValidationFailure(
                    nameError = nameError,
                    quantityError = quantityError,
                    unitError = unitError,
                    categoryError = categoryError
                )
            )
        }
        else {
            ValidationResult(isFailure = false)
        }
    }
}
