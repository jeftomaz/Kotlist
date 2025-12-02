package com.kotlist.app.ui.lists

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kotlist.app.data.model.ShoppingList
import com.kotlist.app.data.repository.ShoppingListRepository
import com.kotlist.app.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ListsUiState {
    data object Idle : ListsUiState()
    data object Loading : ListsUiState()
    data class Success(val lists: List<ShoppingList>) : ListsUiState()
    data class Error(val message: String) : ListsUiState()
    data object Empty : ListsUiState()
}

class ListsViewModel(
    private val userRepository: UserRepository,
    private val shoppingListRepository: ShoppingListRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<ListsUiState>(ListsUiState.Idle)
    val uiState: StateFlow<ListsUiState> = _uiState

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage

    private var userLists: List<ShoppingList> = emptyList()
    private var currentUserId: String = ""

    fun loadData() {
        viewModelScope.launch {
            try {
                _uiState.value = ListsUiState.Loading

                if(currentUserId.isEmpty())
                    currentUserId = userRepository.getUserSignedIn()?.id ?: ""

                if(currentUserId.isEmpty()) {
                    _uiState.value = ListsUiState.Error("Erro: usuário não encontrado")
                    _feedbackMessage.value = "Ocorreu um erro ao carregar as listas. Tente novamente."
                    return@launch
                }

                val lists = shoppingListRepository.getUserLists(currentUserId)
                userLists = lists
                filterLists("")
            }
            catch(e: Exception) {
                _uiState.value = ListsUiState.Error("Erro ao carregar listas: ${e.message}")
                _feedbackMessage.value = "Ocorreu um erro ao carregar as listas. Tente novamente."

                Log.e("Erro ao carregar listas", e.message.toString())
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        filterLists(query)
    }

    fun onLogoutClicked() {
        userRepository.signOut()
    }

    private fun filterLists(query: String) {
        val filteredLists = if(query.isBlank()) {
            userLists
        } else {
            userLists.filter {
                it.name.contains(query, ignoreCase = true)
            }
        }

        _uiState.value = if(filteredLists.isNotEmpty())
            ListsUiState.Success(filteredLists)
        else
            ListsUiState.Empty

        _feedbackMessage.value = when {
            filteredLists.isEmpty() && query.isNotBlank() ->
                "Nenhuma lista encontrada para \"$query\"."
            filteredLists.isEmpty() && query.isBlank() ->
                "Você ainda não tem listas! Toque no '+' para começar a adicionar."
            else -> null
        }
    }
}