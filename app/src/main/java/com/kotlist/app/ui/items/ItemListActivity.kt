package com.kotlist.app.ui.items

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.kotlist.app.data.repository.ListItemRepository
import com.kotlist.app.data.repository.ServiceLocator
import com.kotlist.app.data.repository.ShoppingListRepository
import com.kotlist.app.databinding.ActivityItemListBinding
import com.kotlist.app.ui.lists.EditListActivity
import kotlinx.coroutines.launch

class ItemListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityItemListBinding
    private lateinit var itemListAdapter: ItemListAdapter
    private lateinit var sourceListId: String

    private val listItemRepository by lazy {
        ServiceLocator.provideListItemRepository()
    }

    private val shoppingListRepository by lazy {
        ServiceLocator.provideShoppingListRepository()
    }

    private val viewModel: ItemListViewModel by viewModels {
        ItemListViewModelFactory(listItemRepository, shoppingListRepository)
    }

    companion object {
        const val EXTRA_LIST_ID = "EXTRA_LIST_ID"
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
        viewModel.loadData(sourceListId)
    }

    override fun onPause() {
        super.onPause()
        viewModel.syncPendingCheckedChanges()
    }

    override fun onStop() {
        super.onStop()
        viewModel.syncPendingCheckedChanges()
    }

    private fun setupRecyclerView() {
        itemListAdapter = ItemListAdapter(
            onCheckboxClicked = { item, isChecked ->
                viewModel.onItemCheckedChanged(item, isChecked)
            },
            onItemClick = { item ->
                navigateToEditItem(item.id)
            }
        )

        binding.itemListRecyclerItemsView.apply {
            layoutManager = LinearLayoutManager(this@ItemListActivity)
            adapter = itemListAdapter
            (itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
        }
    }

    private fun setupListeners() {
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

        binding.itemListBackButton.setOnClickListener {
            finish()
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // observes the UI state
                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            is ItemListUiState.Idle -> {
                                setLoading(false)
                            }
                            is ItemListUiState.Loading -> {
                                setLoading(true)
                            }
                            is ItemListUiState.Success -> {
                                setLoading(false)
                                itemListAdapter.submitList(state.items)
                                binding.itemListRecyclerItemsView.visibility = View.VISIBLE
                                binding.listsFeedbackMessage.visibility = View.GONE
                            }
                            is ItemListUiState.Error -> {
                                setLoading(false)
                                Toast.makeText(this@ItemListActivity, state.message, Toast.LENGTH_SHORT).show()
                                binding.itemListRecyclerItemsView.visibility = View.GONE
                                binding.listsFeedbackMessage.visibility = View.VISIBLE
                            }
                            is ItemListUiState.Empty -> {
                                setLoading(false)
                                itemListAdapter.submitList(emptyList())
                                binding.itemListRecyclerItemsView.visibility = View.GONE
                                binding.listsFeedbackMessage.visibility = View.VISIBLE
                            }
                        }
                    }
                }

                // observes the list title data
                launch {
                    viewModel.listTitle.collect { title ->
                        binding.itemListListName.text = title
                    }
                }

                // observes the item list feedback message
                launch {
                    viewModel.feedbackMessage.collect { message ->
                        binding.listsFeedbackMessage.text = message
                    }
                }
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        if(isLoading) {
            binding.itemListLoadingIndicator.visibility = View.VISIBLE
            binding.itemListRecyclerItemsView.visibility = View.GONE
            binding.listsFeedbackMessage.visibility = View.GONE
        } else {
            binding.itemListLoadingIndicator.visibility = View.GONE
            binding.itemListRecyclerItemsView.visibility = View.VISIBLE
            binding.listsFeedbackMessage.visibility = View.VISIBLE
        }
    }

    private fun navigateToEditList() {
        val intent = Intent(this, EditListActivity::class.java).apply {
            putExtra(EditListActivity.EXTRA_LIST_ID, sourceListId)
        }
        startActivity(intent)
    }

    private fun navigateToAddItem() {
        val intent = Intent(this, AddItemActivity::class.java).apply {
            putExtra(AddItemActivity.EXTRA_LIST_ID, sourceListId)
        }
        startActivity(intent)
    }

    private fun navigateToEditItem(itemId: String) {
        val intent = Intent(this, EditItemActivity::class.java).apply {
            putExtra(EditItemActivity.EXTRA_LIST_ID, sourceListId)
            putExtra(EditItemActivity.EXTRA_LIST_ITEM_ID, itemId)
        }
        startActivity(intent)
    }
}