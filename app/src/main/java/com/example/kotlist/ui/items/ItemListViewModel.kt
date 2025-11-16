package com.example.kotlist.ui.items

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kotlist.data.model.ListItem
import com.example.kotlist.data.repository.ListItemRepository
import com.example.kotlist.data.repository.ShoppingListRepository

class ItemListViewModel(
    private val listItemRepository: ListItemRepository,
    private val shoppingListRepository: ShoppingListRepository
) : ViewModel() {

    // LiveData observado pela Activity
    private val _items = MutableLiveData<List<ListItem>>()
    val items: LiveData<List<ListItem>> = _items

    private val _listTitle = MutableLiveData<String>()
    val listTitle: LiveData<String> = _listTitle

    private val _feedbackMessage = MutableLiveData<String?>()
    val feedbackMessage: LiveData<String?> = _feedbackMessage

    // Estado interno (não exposto)
    private var allItems: List<ListItem> = emptyList()
    private var currentListId: String? = null

    // Chamado pela Activity no onCreate/onStart
    fun loadData(listId: String) {
        currentListId = listId

        // Carrega o título da lista
        val list = shoppingListRepository.getListById(listId)
        _listTitle.value = list?.title

        // Carrega os itens
        allItems = listItemRepository.getItemsFromList(listId)

        // Aplica filtro inicial (vazio)
        filterItems("")
    }

    // Chamado quando o texto de busca muda
    fun onSearchQueryChanged(query: String) {
        filterItems(query)
    }

    // Chamado quando checkbox é clicado
    fun onItemCheckedChanged(item: ListItem, isChecked: Boolean) {
        item.isChecked = isChecked
        listItemRepository.updateItem(item)

        // Recarrega dados para refletir a mudança
        currentListId?.let { loadData(it) }
    }

    // Lógica de filtro e ordenação (movida da Activity)
    private fun filterItems(query: String) {
        val filteredList = if (query.isEmpty()) {
            allItems
        } else {
            allItems.filter { item ->
                item.name.contains(query, ignoreCase = true)
            }
        }

        // Ordenação: itens não marcados primeiro, depois por categoria e nome
        val sortedList = filteredList.sortedWith(
            compareBy<ListItem> { it.isChecked }
                .thenBy { it.category.name }
                .thenBy { it.name }
        )

        _items.value = sortedList

        // Define mensagem de feedback
        if (sortedList.isEmpty()) {
            _feedbackMessage.value = if (query.isNotBlank()) {
                "Nenhum item encontrado para \"$query\"."
            } else {
                "Esta lista está vazia! Toque no '+' para adicionar itens."
            }
        } else {
            _feedbackMessage.value = null
        }
    }
}