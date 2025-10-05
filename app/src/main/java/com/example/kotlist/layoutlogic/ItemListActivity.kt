package com.example.kotlist.layoutlogic

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kotlist.data.repository.ListItemRepository
import com.example.kotlist.data.repository.ShoppingListRepository
import com.example.kotlist.databinding.ActivityAddItemBinding
import com.example.kotlist.databinding.ActivityItemListBinding
import com.example.kotlist.layoutlogic.ItemListAdapter

class ItemListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityItemListBinding
    private lateinit var itemListAdapter: ItemListAdapter
    private lateinit var sourceListId: String

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
    }

    fun recyclerViewConfiguration() {
        val listItems = ListItemRepository.getItemsFromList(sourceListId)

        itemListAdapter = ItemListAdapter(listItems) {
                item, isChecked ->
            item.isChecked = isChecked
        }

        val recyclerView = binding.itemListRecyclerItemsView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = itemListAdapter
    }
}