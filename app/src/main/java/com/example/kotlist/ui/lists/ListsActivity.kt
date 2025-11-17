package com.example.kotlist.ui.lists

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.example.kotlist.data.model.ShoppingList
import com.example.kotlist.data.repository.ShoppingListRepository
import com.example.kotlist.data.repository.UserRepository
import com.example.kotlist.databinding.ActivityListsBinding
import com.example.kotlist.ui.auth.LoginActivity
import com.example.kotlist.ui.items.ItemListActivity
import kotlinx.coroutines.launch

class ListsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListsBinding
    private lateinit var listsAdapter: ListsAdapter

    private val viewModel: ListsViewModel by viewModels {
        ListsViewModelFactory(UserRepository, ShoppingListRepository)
    }

    companion object {
        const val CREATE_EXAMPLE_LIST = "CREATE_EXAMPLE_LIST"
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

        binding = ActivityListsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.listsMain) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        setupListeners()
        setupObservers()
    }

    override fun onStart() {
        super.onStart()
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
        binding.fabAddList.setOnClickListener {
            val intent = Intent(this, AddListActivity::class.java)
            startActivity(intent)
        }

        binding.listsLogoutButton.setOnClickListener {
            Toast.makeText(this, "Sessão encerrada", Toast.LENGTH_SHORT).show()
            viewModel.onLogoutClicked()
            navigateToLoginScreen()
        }

        binding.listsSearchInput.addTextChangedListener { editable ->
            val query = editable?.toString().orEmpty()
            viewModel.onSearchQueryChanged(query)
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // lists
                launch {
                    viewModel.lists.collect { lists ->
                        listsAdapter.updateData(lists)
                    }
                }

                // search/empty feedback message
                launch {
                    viewModel.feedbackMessage.collect { message ->
                        if(message == null) {
                            binding.recyclerViewLists.visibility = View.VISIBLE
                            binding.listsFeedbackMessage.visibility = View.GONE
                        }
                        else {
                            binding.recyclerViewLists.visibility = View.GONE
                            binding.listsFeedbackMessage.visibility = View.VISIBLE
                            binding.listsFeedbackMessage.text = message
                        }
                    }
                }
            }
        }
    }

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
}