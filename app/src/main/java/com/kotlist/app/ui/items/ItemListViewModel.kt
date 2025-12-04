package com.kotlist.app.ui.items

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlist.app.data.model.ListItem
import com.kotlist.app.data.repository.ListItemRepository
import com.kotlist.app.data.repository.ShoppingListRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ItemListUiState {
    data object Idle : ItemListUiState()
    data object Loading : ItemListUiState()
    data class Success(val items: List<ListItem>) : ItemListUiState()
    data class Error(val message: String) : ItemListUiState()
    data object Empty : ItemListUiState()
}

class ItemListViewModel(
    private val listItemRepository: ListItemRepository,
    private val shoppingListRepository: ShoppingListRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ItemListUiState>(ItemListUiState.Idle)
    val uiState: StateFlow<ItemListUiState> = _uiState

    private val _listTitle = MutableStateFlow<String?>(null)
    val listTitle: StateFlow<String?> = _listTitle

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage

    private var allItems: List<ListItem> = emptyList()
    private var currentListId: String? = null
    private val pendingCheckedChanges = mutableMapOf<String, Boolean>()
    private var currentSearchQuery: String = ""

    fun loadData(listId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = ItemListUiState.Loading
                currentListId = listId

                val list = shoppingListRepository.getListById(listId)
                _listTitle.value = list?.name ?: "Lista"

                allItems = listItemRepository.getItemsFromList(listId)
                filterItems("")
            } catch (e: Exception) {
                _uiState.value = ItemListUiState.Error("Erro ao carregar itens: ${e.message}")
                _feedbackMessage.value = "Ocorreu um erro ao carregar os itens. Tente novamente."
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        currentSearchQuery = query
        filterItems(query)
    }

    fun onItemCheckedChanged(item: ListItem, checked: Boolean) {
        val updatedItems = allItems.map { current ->
            if (current.id == item.id) {
                current.copy(checked = checked)
            } else {
                current
            }
        }
        allItems = updatedItems
        pendingCheckedChanges[item.id] = checked
        filterItems(currentSearchQuery)
    }

    private fun filterItems(query: String) {
        val filteredList = if (query.isEmpty()) {
            allItems
        } else {
            allItems.filter { item ->
                item.name.contains(query, ignoreCase = true)
            }
        }

        val sortedList = filteredList.sortedWith(
            compareBy<ListItem> { it.checked }
                .thenBy { it.category }
                .thenBy { it.name }
        )

        _uiState.value = if (sortedList.isNotEmpty()) {
            ItemListUiState.Success(sortedList)
        } else {
            ItemListUiState.Empty
        }

        _feedbackMessage.value = when {
            sortedList.isEmpty() && query.isNotBlank() ->
                "Nenhum item encontrado para \"$query\"."
            sortedList.isEmpty() && query.isBlank() ->
                "Esta lista está vazia! Toque no '+' para adicionar itens."
            else -> null
        }
    }

    fun syncPendingCheckedChanges() {
        val listId = currentListId ?: return
        if(pendingCheckedChanges.isEmpty()) return

        val changesToSync = pendingCheckedChanges.toMap()
        pendingCheckedChanges.clear()

        viewModelScope.launch {
            try {
                listItemRepository.updateItemsCheckedStatusBatch(listId, changesToSync)
            } catch (e: Exception) {
                // ?
            }
        }
    }
}