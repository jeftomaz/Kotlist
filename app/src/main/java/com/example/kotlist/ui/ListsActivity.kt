package com.example.kotlist.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import com.example.kotlist.data.model.ShoppingList
import com.example.kotlist.data.repository.ShoppingListRepository
import com.example.kotlist.data.repository.UserRepository
import com.example.kotlist.databinding.ActivityListsScreenBinding
import com.example.kotlist.ui.MainTempActivity.Companion.EXTRA_LIST_ID


class ListsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListsScreenBinding
    private lateinit var listAdapter: ListAdapter
    private var allLists: List<ShoppingList> = emptyList()

    companion object {
        const val CREATE_EXAMPLE_LIST = "CREATE_EXAMPLE_LIST"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT, Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT, Color.TRANSPARENT
            )
        )

        // ViewBinding configuration
        binding = ActivityListsScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.listsMain) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        listAdapter = ListAdapter(emptyList()) { clickedList ->
            navigateToItemDetails(clickedList)
        }

        binding.recyclerViewLists.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerViewLists.adapter = listAdapter

        binding.fabAddList.setOnClickListener {
            val intent = Intent(this, AddListActivity::class.java)
            startActivity(intent)
        }

        binding.logoutButton.setOnClickListener {
            Toast.makeText(this, "Sessão encerrada", Toast.LENGTH_SHORT).show()
            handleLogout()
        }

        setupSearchListener()
    }

    override fun onResume() {
        super.onResume()
        loadAndDisplayLists()
    }

    private fun loadAndDisplayLists() {
        allLists = loadAllLists()
        filterLists("")
    }

    private fun loadAllLists(): List<ShoppingList> {
        val currentUserId = UserRepository.getUserLoggedIn()!!.id

        var lists = ShoppingListRepository.getUserLists(currentUserId)
        val shouldCreateExample = intent.getBooleanExtra(CREATE_EXAMPLE_LIST, false)

        if (lists.isEmpty() && shouldCreateExample) {
            val mockList = ShoppingList(
                title = "Lista de Exemplo",
                coverImageUri = null,
                placeholderImageId = ShoppingListRepository.getRandomPlaceholderId(),
                userId = currentUserId
            )
            ShoppingListRepository.addList(mockList)

            lists = ShoppingListRepository.getUserLists(currentUserId)
        }

        return lists
    }

    private fun navigateToItemDetails(list: ShoppingList) {
        val intent = Intent(this, ItemListActivity::class.java).apply {
            putExtra(EXTRA_LIST_ID, list.id)
        }
        startActivity(intent)
    }

    private fun handleLogout() {
        UserRepository.logoutUser()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
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
