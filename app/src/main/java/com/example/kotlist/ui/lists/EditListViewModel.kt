package com.example.kotlist.ui.lists

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kotlist.data.model.ShoppingList
import com.example.kotlist.data.repository.ShoppingListRepository

class EditListViewModel(
    private val shoppingListRepository: ShoppingListRepository
) : ViewModel() {

    // A lista original que está sendo editada (estado interno)
    private var listOnEditing: ShoppingList? = null

    // LiveData para a Activity observar e preencher os campos
    private val _listToEdit = MutableLiveData<ShoppingList>()
    val listToEdit: LiveData<ShoppingList> = _listToEdit

    // LiveData para erro de validação do nome
    private val _listNameError = MutableLiveData<String?>()
    val listNameError: LiveData<String?> = _listNameError

    // LiveData para sinalizar que a Activity deve fechar (com sucesso no update)
    private val _updateSuccessEvent = MutableLiveData<String>()
    val updateSuccessEvent: LiveData<String> = _updateSuccessEvent

    // LiveData para sinalizar que a Activity deve fechar (com sucesso no delete)
    private val _deleteSuccessEvent = MutableLiveData<String>()
    val deleteSuccessEvent: LiveData<String> = _deleteSuccessEvent

    // Chamado pela Activity no onCreate
    fun loadList(listId: String) {
        // Busca a lista e a armazena localmente E no LiveData
        listOnEditing = shoppingListRepository.getListById(listId)
        _listToEdit.value = listOnEditing
    }

    // Chamado quando o botão "Salvar" é clicado
    fun saveList(newTitle: String, newImageUri: String?) {
        // 1. Validar o nome
        if (newTitle.isEmpty()) {
            _listNameError.value = "A lista deve ter um nome."
            return
        }
        _listNameError.value = null // Limpa o erro

        // 2. Garantir que a lista foi carregada
        val currentList = listOnEditing ?: return // Não deve acontecer

        // 3. Criar o objeto atualizado
        val updatedList = currentList.copy(
            title = newTitle,
            // Se a nova imagem for nula, mantém a antiga
            coverImageUri = newImageUri ?: currentList.coverImageUri
            // placeholder e userId não mudam
        )

        // 4. Salvar no repositório
        shoppingListRepository.updateList(updatedList)

        // 5. Sinalizar sucesso e fechar
        _updateSuccessEvent.value = "Lista atualizada com sucesso!"
    }

    // Chamado quando o botão "Excluir" é clicado
    fun deleteList() {
        val listId = listOnEditing?.id ?: return
        shoppingListRepository.deleteList(listId)
        _deleteSuccessEvent.value = "Lista excluída."
    }
}