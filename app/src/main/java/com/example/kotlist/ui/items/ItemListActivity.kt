package com.example.kotlist.ui.items

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kotlist.data.model.ListItem
import com.example.kotlist.data.repository.ListItemRepository
import com.example.kotlist.data.repository.ShoppingListRepository
import com.example.kotlist.databinding.ActivityItemListBinding
import com.example.kotlist.ui.items.ItemListAdapter
import com.example.kotlist.ui.lists.EditListActivity

class ItemListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityItemListBinding
    private lateinit var itemListAdapter: ItemListAdapter
    private lateinit var sourceListId: String
    private var searchMasterItemList = mutableListOf<ListItem>()

    companion object {
        const val EXTRA_LIST_ID = "EXTRA_LIST_ID"
        const val EXTRA_LIST_ITEM_ID = "EXTRA_LIST_ITEM_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.Companion.light(
                Color.TRANSPARENT, Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.Companion.light(
                Color.TRANSPARENT, Color.TRANSPARENT
            )
        )

        // ViewBinding configuration
        binding = ActivityItemListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.itemListMain) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sourceListId = intent.getStringExtra(EXTRA_LIST_ID)!!
        binding.itemListListName.text = ShoppingListRepository.getListById(sourceListId)?.title
        recyclerViewConfiguration()
        loadAndDisplayItemList()
        setupSearchView()

        binding.itemListEditListButton.setOnClickListener {
            val intent = Intent(this, EditListActivity::class.java).apply {
                putExtra(EXTRA_LIST_ID, intent.getStringExtra(EXTRA_LIST_ID))
            }
            startActivity(intent)
        }

        binding.itemListAddItemButton.setOnClickListener {
            val intent = Intent(this, AddItemActivity::class.java).apply {
                putExtra(EXTRA_LIST_ID, intent.getStringExtra(EXTRA_LIST_ID))
            }
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()

        binding.itemListListName.text = ShoppingListRepository.getListById(sourceListId)?.title
        loadAndDisplayItemList()
    }

    fun recyclerViewConfiguration() {
        itemListAdapter = ItemListAdapter(
            onCheckboxClicked = { item, isChecked ->
                item.isChecked = isChecked
                ListItemRepository.updateItem(item)
                binding.itemListRecyclerItemsView.post {
                    loadAndDisplayItemList()
                }
            },

            onItemClick = { item ->
                val intent = Intent(this, EditItemActivity::class.java).apply {
                    putExtra(EXTRA_LIST_ID, sourceListId)
                    putExtra(EXTRA_LIST_ITEM_ID, item.id)
                }
                startActivity(intent)
            }
        )

        val recyclerView = binding.itemListRecyclerItemsView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = itemListAdapter
    }

    fun getSortedList(items: MutableList<ListItem>): MutableList<ListItem> {
        return items.sortedWith(
            compareBy<ListItem> { it.isChecked }
                .thenBy { it.category.name }
                .thenBy { it.name }
        ).toMutableList()
    }

    fun setupSearchView() {
        binding.itemListSearchInput.addTextChangedListener { editable ->
            val query = editable?.toString().orEmpty()
            filterItemList(query)
        }
    }

    fun loadAndDisplayItemList() {
        searchMasterItemList = ListItemRepository.getItemsFromList(sourceListId)
        val currentQuery = binding.itemListSearchInput.text.toString()
        filterItemList(currentQuery)
    }

    fun filterItemList(query: String) {
        val filteredList: List<ListItem> = if(query.isEmpty()) {
            searchMasterItemList
        } else {
            searchMasterItemList.filter { item ->
                item.name.contains(query, ignoreCase = true)
            }
        }

        val sortedList = getSortedList(filteredList.toMutableList())
        itemListAdapter.submitList(sortedList)

        if(sortedList.isEmpty()) {
            binding.itemListRecyclerItemsView.visibility = View.GONE
            binding.listsFeedbackMessage.visibility = View.VISIBLE

            if(query.isNotBlank()) {
                binding.listsFeedbackMessage.text = "Nenhum item encontrado para \"$query\"."
            } else {
                binding.listsFeedbackMessage.text = "Esta lista está vazia! Toque no '+' para adicionar itens."
            }
        } else {
            binding.itemListRecyclerItemsView.visibility = View.VISIBLE
            binding.listsFeedbackMessage.visibility = View.GONE
        }
    }
}