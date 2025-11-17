package com.example.kotlist.ui.lists

import androidx.lifecycle.ViewModel
import com.example.kotlist.data.model.ShoppingList
import com.example.kotlist.data.repository.ShoppingListRepository
import com.example.kotlist.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ListsViewModel(
    private val userRepository: UserRepository,
    private val shoppingListRepository: ShoppingListRepository
) : ViewModel() {
    private val _lists = MutableStateFlow<List<ShoppingList>>(emptyList())
    val lists: StateFlow<List<ShoppingList>> = _lists

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage

    private var allLists: List<ShoppingList> = emptyList()
    private var currentUserId: String? = null

    fun loadData(shouldCreateExample: Boolean) {
        if (currentUserId == null) {
            currentUserId = userRepository.getUserLoggedIn()?.id
        }

        if (currentUserId == null) {
            _feedbackMessage.value = "Erro: Usuário não encontrado."
            return
        }

        var lists = shoppingListRepository.getUserLists(currentUserId!!)

        if (lists.isEmpty() && shouldCreateExample) {
            val mockList = ShoppingList(
                title = "Lista Exemplo",
                coverImageUri = null,
                placeholderImageId = shoppingListRepository.getRandomPlaceholderId(),
                userId = currentUserId!!
            )
            shoppingListRepository.addList(mockList)
            lists = shoppingListRepository.getUserLists(currentUserId!!)
        }

        allLists = lists
        filterLists("")
    }

    fun onSearchQueryChanged(query: String) {
        filterLists(query)
    }

    fun onLogoutClicked() {
        userRepository.logoutUser()
    }

    private fun filterLists(query: String) {
        val filteredLists = if(query.isBlank()) {
            allLists
        } else {
            allLists.filter {
                it.title.contains(query, ignoreCase = true)
            }
        }

        _lists.value = filteredLists

        _feedbackMessage.value = when {
            filteredLists.isEmpty() && query.isNotBlank() ->
                "Nenhuma lista encontrada para \"$query\"."
            filteredLists.isEmpty() && query.isBlank() ->
                "Você ainda não tem listas! Toque no '+' para começar a adicionar."
            else -> null
        }
    }
}