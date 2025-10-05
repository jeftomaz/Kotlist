package com.example.kotlist.layoutlogic

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast // IMPORTANTE: Adicionar a importação de Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.kotlist.data.model.ShoppingList
import com.example.kotlist.data.repository.ShoppingListRepository
import com.example.kotlist.data.repository.UserRepository
import com.example.kotlist.databinding.ActivityListsScreenBinding

class ListsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListsScreenBinding
    private lateinit var listAdapter: ListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ... (onCreate permanece o mesmo) ...

        binding = ActivityListsScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuração inicial do Adapter
        listAdapter = ListAdapter(emptyList()) { clickedList ->
            navigateToItemDetails(clickedList) // Chamada de função mantida
        }

        // Configuração do LayoutManager - Grid com 2 colunas
        binding.recyclerViewLists.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerViewLists.adapter = listAdapter

        // Ação do FAB - adicionar lista
        binding.fabAddList.setOnClickListener {
            val intent = Intent(this, AddListActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadAndDisplayLists()
    }

    // Carrega as listas e gerencia a exibição do Empty State.
    private fun loadAndDisplayLists() {
        val lists = loadAllLists()

        if (lists.isEmpty()) {
            binding.recyclerViewLists.visibility = View.GONE
            binding.textViewEmptyState.visibility = View.VISIBLE
        } else {
            binding.recyclerViewLists.visibility = View.VISIBLE
            binding.textViewEmptyState.visibility = View.GONE
            listAdapter.updateData(lists)
        }
    }

    /**
     * Carrega todas as listas de compras usando o repositório.
     */
    private fun loadAllLists(): List<ShoppingList> {
        val currentUserId = UserRepository.getUserLoggedIn()?.id ?: return emptyList()
        return ShoppingListRepository.getUserLists(currentUserId)
    }

    /**
     * Navega para a tela de detalhes do item (ItemListActivity), passando o ID e título da lista.
     * Implementação TEMPORÁRIA (Toast).
     * @param list O objeto ShoppingList que foi clicado.
     */
    private fun navigateToItemDetails(list: ShoppingList) {
        // Implementação TEMPORÁRIA: Exibe uma mensagem de funcionalidade não pronta.
        Toast.makeText(
            this,
            "A tela de itens da lista '${list.title}' ainda está em construção.",
            Toast.LENGTH_SHORT
        ).show()

        // Para ativar a funcionalidade real no futuro, basta descomentar/re-adicionar o código:
        /*
        val intent = Intent(this, ItemListActivity::class.java).apply {
            putExtra("LIST_ID", list.id)
            putExtra("LIST_TITLE", list.title)
        }
        startActivity(intent)
        */
    }
}