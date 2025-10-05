package com.example.kotlist.layoutlogic

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.appcompat.widget.SearchView

import com.example.kotlist.data.model.ShoppingList
import com.example.kotlist.data.repository.ShoppingListRepository
import com.example.kotlist.data.repository.UserRepository
import com.example.kotlist.databinding.ActivityListsScreenBinding
import com.example.kotlist.R


class ListsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListsScreenBinding
    private lateinit var listAdapter: ListAdapter
    private var allLists: List<ShoppingList> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityListsScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuração inicial do Adapter
        listAdapter = ListAdapter(emptyList()) { clickedList ->
            navigateToItemDetails(clickedList)
        }

        // Configuração do LayoutManager - Grid com 2 colunas
        binding.recyclerViewLists.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerViewLists.adapter = listAdapter

        // Ação do FAB - adicionar lista
        binding.fabAddList.setOnClickListener {
            val intent = Intent(this, AddListActivity::class.java)
            startActivity(intent)
        }

        binding.logoutButton.setOnClickListener {
            Toast.makeText(this, "Viu, Bruno? Funcionou", Toast.LENGTH_SHORT).show()
            handleLogout()
        }

        // Configura o listener de busca (TextWatcher)
        setupSearchListener()

    }

    override fun onResume() {
        super.onResume()
        loadAndDisplayLists()
    }

    // Carrega as listas e gerencia a exibição do Empty State.
    private fun loadAndDisplayLists() {
        // 1. Carrega todas as listas e armazena
        allLists = loadAllLists()

        filterLists("")
    }

    // Carrega todas as listas de compras usando o repositório.
    private fun loadAllLists(): List<ShoppingList> {
        val currentUserId = UserRepository.getUserLoggedIn()?.id ?: return emptyList()

        // Tenta buscar as listas existentes para o usuário
        var lists = ShoppingListRepository.getUserLists(currentUserId)

        if (lists.isEmpty()) {
            val mockList = ShoppingList(
                title = "Lista de Exemplo",
                coverImageUri = null,
                // Garante que o placeholder seja aleatório
                placeholderImageId = ShoppingListRepository.getRandomPlaceholderId(),
                userId = currentUserId
            )
            ShoppingListRepository.addList(mockList)

            // Busca a lista novamente (agora com a lista de exemplo)
            lists = ShoppingListRepository.getUserLists(currentUserId)
        }

        return lists
    }

    // Implementação TEMPORÁRIA para navegação em detalhes do item.
    private fun navigateToItemDetails(list: ShoppingList) {
        Toast.makeText(
            this,
            "A tela de itens da lista '${list.title}' ainda está em construção.",
            Toast.LENGTH_SHORT
        ).show()
    }

     // Reseta o usuário logado e navega para a tela de Login.
    private fun handleLogout() {
        // Resetar o usuário no repositório (limpar o estado de sessão)
        UserRepository.logoutUser()

        // Criar o Intent para a tela de Login
        val intent = Intent(this, LoginActivity::class.java)

        // Flags para limpar a pilha de atividades:
        // Isso impede que o usuário volte para a ListsActivity usando o botão "Voltar".
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        // Iniciar a nova Activity
        startActivity(intent)

        // Opcional: Adicionar animação para a transição (assumindo a existência das animações)
        overridePendingTransition(R.anim.zoom_in, R.anim.fade_out)
    }

    private fun setupSearchListener() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

            // Chamado quando o usuário aperta Enter
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterLists(query.orEmpty())
                return true
            }

            // Chamado a cada mudança de texto (Busca em Tempo Real)
            override fun onQueryTextChange(newText: String?): Boolean {
                filterLists(newText.orEmpty())
                return true
            }
        })

        // binding.searchView.isIconified = false
    }

    // Filtra as listas com base na query e atualiza o Adapter e o Empty State.
    private fun filterLists(query: String) {
        val filteredLists = if (query.isBlank()) {
            // Se a busca estiver vazia, exibe todas as listas
            allLists
        } else {
            // Filtra as listas cujo título contenha a query (ignorando maiúsculas/minúsculas)
            allLists.filter {
                it.title.contains(query, ignoreCase = true)
            }
        }

        if (filteredLists.isEmpty()) {
            binding.recyclerViewLists.visibility = View.GONE
            binding.textViewEmptyState.visibility = View.VISIBLE

            // Ajusta a mensagem de Empty State dependendo se é um erro de busca ou lista vazia
            if (query.isNotBlank()) {
                binding.textViewEmptyState.text = "Nenhuma lista encontrada para \"$query\"."
            } else {
                // Mensagem original (sem listas cadastradas no total)
                binding.textViewEmptyState.text =
                    "Você ainda não tem listas! Toque no '+' para começar a adicionar."
            }

        } else {
            binding.recyclerViewLists.visibility = View.VISIBLE
            binding.textViewEmptyState.visibility = View.GONE
            listAdapter.updateData(filteredLists)
        }
    }
}
