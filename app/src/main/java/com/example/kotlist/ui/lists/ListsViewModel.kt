package com.example.kotlist.ui.lists

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kotlist.data.model.ShoppingList
import com.example.kotlist.data.repository.ShoppingListRepository
import com.example.kotlist.data.repository.UserRepository
import com.example.kotlist.databinding.ActivityListsBinding

class ListsViewModel(
    private val userRepository: UserRepository,
    private val shoppingListRepository: ShoppingListRepository
) : ViewModel() {

    // A activity observa o LiveData
    private val _lists = MutableLiveData<List<ShoppingList>>()
    val lists: LiveData<List<ShoppingList>> = _lists

    private val _feedbackMessage = MutableLiveData<String>()
    val feedbackMessage: LiveData<String> = _feedbackMessage

    // A activity mantem a lista completa original
    private var allLists: List<ShoppingList> = emptyList()
    private var currentUserId: String? = null

    // A activity chama essa função quando é iniciada
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
                title = "Lista de Exemplo",
                coverImageUri = null,
                placeholderImageId = shoppingListRepository.getRandomPlaceholderId(),
                userId = currentUserId!!
            )
            shoppingListRepository.addList(mockList)
            lists = shoppingListRepository.getUserLists(currentUserId!!)
        }

        // Armazena a lista completa
        allLists = lists

        // Aplica o filtro inicial (vazio)
        filterLists("")
    }

    // A activity chama essa função quando o texto de busca muda
    fun onSearchQueryChanged(query: String) {
        filterLists(query)
    }

    // A activity chama essa função no clique do botão logout
    fun onLogoutClicked() {
        userRepository.logoutUser()
    }

    // Lógico do filtro vinda da Activity
    private fun filterLists(query: String) {
        val filteredLists = if (query.isBlank()) {
            allLists
        } else {
            allLists.filter {
                it.title.contains(query, ignoreCase = true)
            }
        }

        // Atualiza o LiveData que a acitivty está observando
        _lists.value = filteredLists

        // Atualiza a mensagem de feedback

        if (filteredLists.isEmpty()) {
            if (query.isNotBlank())
                _feedbackMessage.value = "Nenhuma lista encontrada para \"$query\"."
            else
                _feedbackMessage.value = "Você ainda não tem listas! Toque no '+' para começar a adicionar."
        } else {
            _feedbackMessage.value = null // Limpa a mensagem
        }
    }
}