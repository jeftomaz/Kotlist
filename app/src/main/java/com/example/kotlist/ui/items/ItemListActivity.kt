package com.example.kotlist.ui.items

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kotlist.data.repository.ListItemRepository
import com.example.kotlist.data.repository.ShoppingListRepository
import com.example.kotlist.databinding.ActivityItemListBinding
import com.example.kotlist.ui.lists.EditListActivity

class ItemListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityItemListBinding
    private lateinit var itemListAdapter: ItemListAdapter
    private lateinit var sourceListId: String

    // Inicializa o ViewModel usando a Factory
    private val viewModel: ItemListViewModel by viewModels {
        ItemListViewModelFactory(ListItemRepository, ShoppingListRepository)
    }

    companion object {
        const val EXTRA_LIST_ID = "EXTRA_LIST_ID"
        const val EXTRA_LIST_ITEM_ID = "EXTRA_LIST_ITEM_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.Companion.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.Companion.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )

        binding = ActivityItemListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.itemListMain) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sourceListId = intent.getStringExtra(EXTRA_LIST_ID)!!

        setupRecyclerView()
        setupListeners()
        setupObservers()
    }

    override fun onStart() {
        super.onStart()
        // A Activity apenas avisa o ViewModel para carregar os dados
        viewModel.loadData(sourceListId)
    }

    private fun setupRecyclerView() {
        itemListAdapter = ItemListAdapter(
            onCheckboxClicked = { item, isChecked ->
                // A Activity avisa o ViewModel sobre a mudança
                viewModel.onItemCheckedChanged(item, isChecked)
            },
            onItemClick = { item ->
                navigateToEditItem(item.id)
            }
        )

        binding.itemListRecyclerItemsView.layoutManager = LinearLayoutManager(this)
        binding.itemListRecyclerItemsView.adapter = itemListAdapter
    }

    private fun setupListeners() {
        // A Activity avisa o ViewModel sobre mudanças no texto de busca
        binding.itemListSearchInput.addTextChangedListener { editable ->
            val query = editable?.toString().orEmpty()
            viewModel.onSearchQueryChanged(query)
        }

        binding.itemListEditListButton.setOnClickListener {
            navigateToEditList()
        }

        binding.itemListAddItemButton.setOnClickListener {
            navigateToAddItem()
        }
    }

    // A Activity observa as mudanças do ViewModel e reage
    private fun setupObservers() {
        // Observa a lista de itens
        viewModel.items.observe(this) { items ->
            itemListAdapter.submitList(items)
        }

        // Observa o título da lista
        viewModel.listTitle.observe(this) { title ->
            binding.itemListListName.text = title
        }

        // Observa a mensagem de feedback
        viewModel.feedbackMessage.observe(this) { message ->
            if (message == null) {
                binding.itemListRecyclerItemsView.visibility = View.VISIBLE
                binding.listsFeedbackMessage.visibility = View.GONE
            } else {
                binding.itemListRecyclerItemsView.visibility = View.GONE
                binding.listsFeedbackMessage.visibility = View.VISIBLE
                binding.listsFeedbackMessage.text = message
            }
        }
    }

    // Funções de navegação permanecem na Activity
    private fun navigateToEditList() {
        val intent = Intent(this, EditListActivity::class.java).apply {
            putExtra(EXTRA_LIST_ID, sourceListId)
        }
        startActivity(intent)
    }

    private fun navigateToAddItem() {
        val intent = Intent(this, AddItemActivity::class.java).apply {
            putExtra(EXTRA_LIST_ID, sourceListId)
        }
        startActivity(intent)
    }

    private fun navigateToEditItem(itemId: String) {
        val intent = Intent(this, EditItemActivity::class.java).apply {
            putExtra(EXTRA_LIST_ID, sourceListId)
            putExtra(EXTRA_LIST_ITEM_ID, itemId)
        }
        startActivity(intent)
    }

    // Funções REMOVIDAS:
    // - loadAndDisplayItemList() → movida para ViewModel.loadData()
    // - filterItemList() → movida para ViewModel.filterItems()
    // - getSortedList() → integrada no ViewModel.filterItems()
    // - setupSearchView() → substituída por setupListeners()
    // - recyclerViewConfiguration() → renomeada para setupRecyclerView()
    // Variável REMOVIDA:
    // - searchMasterItemList → movida para ViewModel.allItems
}