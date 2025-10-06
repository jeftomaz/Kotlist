package com.example.kotlist.layoutlogic

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kotlist.data.model.ListItem
import com.example.kotlist.data.repository.ListItemRepository
import com.example.kotlist.data.repository.ShoppingListRepository
import com.example.kotlist.data.repository.UserRepository
import com.example.kotlist.databinding.ActivityAddItemBinding
import com.example.kotlist.databinding.ActivityItemListBinding
import com.example.kotlist.layoutlogic.ItemListAdapter
import com.example.kotlist.layoutlogic.MainTempActivity.Companion.EXTRA_LIST_ID

class ItemListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityItemListBinding
    private lateinit var itemListAdapter: ItemListAdapter
    private lateinit var sourceListId: String
    private var searchMasterItemList = mutableListOf<ListItem>()

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
        binding = ActivityItemListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.itemListMain) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sourceListId = intent.getStringExtra(MainTempActivity.EXTRA_LIST_ID)!!
        binding.itemListListName.text = ShoppingListRepository.getListById(sourceListId)?.title
        recyclerViewConfiguration()
        loadAndDisplayItemList()
        setupSearchView()

        binding.itemListAddItemButton.setOnClickListener {
            val intent = Intent(this, AddItemActivity::class.java).apply {
                putExtra(EXTRA_LIST_ID, intent.getStringExtra(EXTRA_LIST_ID))
            }
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadAndDisplayItemList()
    }

    fun recyclerViewConfiguration() {
        itemListAdapter = ItemListAdapter {
                item, isChecked ->
            item.isChecked = isChecked

            ListItemRepository.updateItem(item)

            binding.itemListRecyclerItemsView.post {
                loadAndDisplayItemList()
            }
        }

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
        binding.itemListSearchBar.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    filterAndSortItemList(newText)
                }
                return true
            }
        })
    }

    fun loadAndDisplayItemList() {
        searchMasterItemList = ListItemRepository.getItemsFromList(sourceListId)
        val currentQuery = binding.itemListSearchBar.query.toString()
        filterAndSortItemList(currentQuery)
    }

    fun filterAndSortItemList(query: String) {
        var filteredList: List<ListItem>

        if (query.isEmpty()) {
            filteredList = searchMasterItemList
        } else {
            filteredList = searchMasterItemList.filter { item ->
                item.name.contains(query, ignoreCase = true)
            }
        }

        val sortedList = getSortedList(filteredList.toMutableList())
        itemListAdapter.submitList(sortedList)
    }
}