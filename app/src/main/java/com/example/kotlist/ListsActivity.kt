package com.example.kotlist

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.kotlist.databinding.ActivityListsScreenBinding

class ListsActivity : AppCompatActivity() {

    // Variável para armazenar a instância do binding
    private lateinit var binding: ActivityListsScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Infla o layout e configura o binding
        binding = ActivityListsScreenBinding.inflate(layoutInflater)
        setContentView(binding.root) // Define a view raiz do binding como o conteúdo

        // Os dados de exemplo
        val listData = loadAllLists()

        // Configuração do RecyclerView usando a variável binding

        // Configuração do LayoutManager - Grid com 2 colunas
        binding.recyclerViewLists.layoutManager = GridLayoutManager(this, 2)

        // Inicializar o Adapter
        val listAdapter = ListAdapter(listData) { clickedList ->
            navigateToItemDetails(clickedList)
        }
        binding.recyclerViewLists.adapter = listAdapter

        // Ação do FAB - adicionar lista
        binding.fabAddList.setOnClickListener {
            // Ação: Iniciar a tela de cadastro de nova lista
            val intent = Intent(this, AddNewListActivity::class.java)
            startActivity(intent)
        }

        // Exemplo de uso para o campo de busca -> Implementação futura
        // binding.editTextSearch.text.clear()
    }

    // ... (loadAllLists e navigateToItemDetails continuam as mesmas)
    private fun loadAllLists(): List<ListModel> { /* ... */ }
    private fun navigateToItemDetails(list: ListModel) { /* ... */ }
}