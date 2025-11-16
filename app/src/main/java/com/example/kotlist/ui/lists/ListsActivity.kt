package com.example.kotlist.ui.lists

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels // Import necessário
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.kotlist.data.model.ShoppingList
// Repositórios importados APENAS para a Factory
import com.example.kotlist.data.repository.ShoppingListRepository
import com.example.kotlist.data.repository.UserRepository
import com.example.kotlist.databinding.ActivityListsBinding
import com.example.kotlist.ui.auth.LoginActivity
import com.example.kotlist.ui.items.ItemListActivity

class ListsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListsBinding
    private lateinit var listsAdapter: ListsAdapter

    // Inicializa o ViewModel usando a Factory
    private val viewModel: ListsViewModel by viewModels {
        ListsViewModelFactory(UserRepository, ShoppingListRepository)
    }

    companion object {
        const val CREATE_EXAMPLE_LIST = "CREATE_EXAMPLE_LIST"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.Companion.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.Companion.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )

        binding = ActivityListsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.listsMain) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        setupListeners()
        setupObservers() // Nova função para observar o ViewModel
    }

    override fun onStart() {
        super.onStart()
        // A Activity apenas "avisa" o ViewModel para carregar os dados
        val shouldCreateExample = intent.getBooleanExtra(CREATE_EXAMPLE_LIST, false)
        viewModel.loadData(shouldCreateExample)
    }

    private fun setupRecyclerView() {
        listsAdapter = ListsAdapter(emptyList()) { clickedList ->
            navigateToItemDetails(clickedList)
        }
        binding.recyclerViewLists.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerViewLists.adapter = listsAdapter
    }

    private fun setupListeners() {
        // A Activity "avisa" o ViewModel sobre o clique
        binding.fabAddList.setOnClickListener {
            val intent = Intent(this, AddListActivity::class.java)
            startActivity(intent)
        }

        // A Activity "avisa" o ViewModel e cuida da navegação
        binding.listsLogoutButton.setOnClickListener {
            Toast.makeText(this, "Sessão encerrada", Toast.LENGTH_SHORT).show()
            viewModel.onLogoutClicked() // 1. Avisa o VM
            navigateToLoginScreen() // 2. Cuida da navegação
        }

        // A Activity "avisa" o ViewModel sobre a mudança no texto
        binding.listsSearchInput.addTextChangedListener { editable ->
            val query = editable?.toString().orEmpty()
            viewModel.onSearchQueryChanged(query)
        }
    }

    // A Activity "observa" as mudanças do ViewModel e reage
    private fun setupObservers() {
        // Observa a lista de compras
        viewModel.lists.observe(this) { lists ->
            // Apenas atualiza o adapter com a lista recebida
            listsAdapter.updateData(lists)
        }

        // Observa a mensagem de feedback
        viewModel.feedbackMessage.observe(this) { message ->
            if (message == null) {
                // Sem mensagem, mostra a lista
                binding.recyclerViewLists.visibility = View.VISIBLE
                binding.listsFeedbackMessage.visibility = View.GONE
            } else {
                // Com mensagem, esconde a lista e mostra a mensagem
                binding.recyclerViewLists.visibility = View.GONE
                binding.listsFeedbackMessage.visibility = View.VISIBLE
                binding.listsFeedbackMessage.text = message
            }
        }
    }

    // Funções de navegação permanecem na Activity
    private fun navigateToItemDetails(list: ShoppingList) {
        val intent = Intent(this, ItemListActivity::class.java).apply {
            putExtra(ItemListActivity.EXTRA_LIST_ID, list.id)
        }
        startActivity(intent)
    }

    private fun navigateToLoginScreen() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    // As funções loadAndDisplayLists, filterLists e handleLogout foram REMOVIDAS
    // A variável allLists foi REMOVIDA
}