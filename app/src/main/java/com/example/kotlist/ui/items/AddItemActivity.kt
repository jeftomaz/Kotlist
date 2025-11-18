package com.example.kotlist.ui.items

import android.graphics.Color
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.kotlist.R
import com.example.kotlist.data.model.ItemCategory
import com.example.kotlist.data.model.ItemUnit
import com.example.kotlist.data.repository.ListItemRepository
import com.example.kotlist.databinding.ActivityAddItemBinding
import kotlinx.coroutines.launch

class AddItemActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddItemBinding
    private var shoppingListId: String? = null
    private var selectedCategory: ItemCategory? = null
    private var selectedUnit: ItemUnit? = null

    companion object {
        const val EXTRA_LIST_ID = "EXTRA_LIST_ID"
    }

    private val viewModel: AddItemViewModel by viewModels {
        AddItemViewModelFactory(ListItemRepository)
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

        binding = ActivityAddItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.addItemMain) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        shoppingListId = intent.getStringExtra(EXTRA_LIST_ID)

        setDropdownAdapters()
        setupListeners()
        setupObservers()
    }

    private fun setDropdownAdapters() {
        val itemUnitsStringArray: List<String> =
            ItemUnit.entries.map { getString(it.unitNameId) }
        val itemCategoriesStringArray: List<String> =
            ItemCategory.entries.map { getString(it.categoryNameId) }

        val itemUnitsAdapter = ArrayAdapter(
            this,
            R.layout.component_dropdown_input_item,
            itemUnitsStringArray
        )

        val itemCategoriesAdapter = ArrayAdapter(
            this,
            R.layout.component_dropdown_input_item,
            itemCategoriesStringArray
        )

        binding.addItemUnitDropdown.setAdapter(itemUnitsAdapter)
        binding.addItemCategoryDropdown.setAdapter(itemCategoriesAdapter)

        binding.addItemCategoryDropdown.setOnItemClickListener { _, _, position, _ ->
            selectedCategory = ItemCategory.entries[position]
        }

        binding.addItemUnitDropdown.setOnItemClickListener { _, _, position, _ ->
            selectedUnit = ItemUnit.entries[position]
        }
    }

    private fun setupListeners() {
        binding.addItemAddItemButton.setOnClickListener {
            val itemListId = shoppingListId
            val itemName = binding.addItemNameInput.text.toString()
            val itemQuantityText = binding.addItemQuantityInput.text.toString()
            val itemCategory = selectedCategory
            val itemUnit = selectedUnit

            viewModel.addItem(
                listId = itemListId,
                name = itemName,
                quantityText = itemQuantityText,
                unit = itemUnit,
                category = itemCategory
            )
        }

        binding.addItemCancelButton.setOnClickListener {
            finish()
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // observes input errors
                launch {
                    viewModel.uiState.collect { state ->
                        binding.addItemNameInputWrapper.error = state.nameError
                        binding.addItemQuantityInputWrapper.error = state.quantityError
                        binding.addItemUnitDropdownWrapper.error = state.unitError
                        binding.addItemCategoryDropdownWrapper.error = state.categoryError
                    }
                }

                // observes events (toasts and finishes)
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is AddItemEvent.ShowMessage -> {
                                Toast.makeText(
                                    this@AddItemActivity,
                                    event.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            is AddItemEvent.Success -> {
                                finish()
                            }
                        }
                    }
                }
            }
        }
    }
}