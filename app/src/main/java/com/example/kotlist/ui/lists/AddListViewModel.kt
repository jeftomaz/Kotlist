package com.example.kotlist.ui.lists

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kotlist.data.model.ShoppingList
import com.example.kotlist.data.repository.ShoppingListRepository
import com.example.kotlist.data.repository.UserRepository

class AddListViewModel(
    private val userRepository: UserRepository,
    private val shoppingListRepository: ShoppingListRepository
) : ViewModel() {

    // LiveData para o ID da imagem placeholder
    private val _placeholderImageId = MutableLiveData<Int>()
    val placeholderImageId: LiveData<Int> = _placeholderImageId

    // LiveData para erro de validação do nome
    private val _listNameError = MutableLiveData<String?>()
    val listNameError: LiveData<String?> = _listNameError

    // LiveData para erros gerais (Toast)
    private val _toastError = MutableLiveData<String>()
    val toastError: LiveData<String> = _toastError

    // LiveData para sinalizar que a Activity deve fechar (com sucesso)
    private val _finishEvent = MutableLiveData<String>()
    val finishEvent: LiveData<String> = _finishEvent

    // Chamado pela Activity quando ela é criada
    fun loadInitialPlaceholder() {
        _placeholderImageId.value = shoppingListRepository.getRandomPlaceholderId()
    }

    // Chamado quando o botão "Criar Lista" é clicado
    fun createNewList(listTitle: String, coverImageUri: String?, placeholderImageId: Int) {
        // 1. Validar o nome
        if (listTitle.isEmpty()) {
            _listNameError.value = "A lista deve ter um nome."
            return
        }
        _listNameError.value = null // Limpa o erro se for válido

        // 2. Validar o usuário
        val userId = userRepository.getUserLoggedIn()?.id
        if (userId == null) {
            _toastError.value = "Houve um erro ao tentar criar a lista. (Usuário não encontrado)"
            return
        }

        // 3. Criar o modelo
        val newList = ShoppingList(
            title = listTitle,
            coverImageUri = coverImageUri,
            placeholderImageId = placeholderImageId,
            userId = userId
        )

        // 4. Salvar no repositório
        shoppingListRepository.addList(newList)

        // 5. Sinalizar sucesso e fechar
        _finishEvent.value = "Nova lista criada com sucesso!"
    }
}