package com.example.kotlist.ui.items

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kotlist.data.model.ListItem
import com.example.kotlist.data.repository.ListItemRepository
import com.example.kotlist.data.repository.ShoppingListRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ItemListViewModel(
    private val listItemRepository: ListItemRepository,
    private val shoppingListRepository: ShoppingListRepository
) : ViewModel() {

    private val _items = MutableStateFlow<List<ListItem>>(emptyList())
    val items: StateFlow<List<ListItem>> = _items

    private val _listTitle = MutableStateFlow<String?>(null)
    val listTitle: StateFlow<String?> = _listTitle

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage

    private var allItems: List<ListItem> = emptyList()
    private var currentListId: String? = null

    fun loadData(listId: String) {
        currentListId = listId

        val list = shoppingListRepository.getListById(listId)
        _listTitle.value = list?.title

        allItems = listItemRepository.getItemsFromList(listId)
        filterItems("")
    }

    fun onSearchQueryChanged(query: String) {
        filterItems(query)
    }

    fun onItemCheckedChanged(item: ListItem, isChecked: Boolean) {
        item.isChecked = isChecked
        listItemRepository.updateItem(item)
        currentListId?.let { loadData(it) }
    }

    private fun filterItems(query: String) {
        val filteredList = if(query.isEmpty()) {
            allItems
        } else {
            allItems.filter { item ->
                item.name.contains(query, ignoreCase = true)
            }
        }

        val sortedList = filteredList.sortedWith(
            compareBy<ListItem> { it.isChecked }
                .thenBy { it.category.name }
                .thenBy { it.name }
        )

        _items.value = sortedList

        if(sortedList.isEmpty()) {
            _feedbackMessage.value = if(query.isNotBlank()) {
                "Nenhum item encontrado para \"$query\"."
            } else {
                "Esta lista está vazia! Toque no '+' para adicionar itens."
            }
        } else
            _feedbackMessage.value = null
    }
}