package com.kotlist.app.ui.items

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
import com.kotlist.app.R
import com.kotlist.app.data.model.ItemCategory
import com.kotlist.app.data.model.ItemUnit
import com.kotlist.app.data.model.ListItem
import com.kotlist.app.data.repository.ListItemRepository
import com.kotlist.app.databinding.ActivityEditItemBinding
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class EditItemActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditItemBinding

    private var selectedCategory: ItemCategory? = null
    private var selectedUnit: ItemUnit? = null

    private var shoppingListId: String? = null
    private var itemId: String? = null

    companion object {
        const val EXTRA_LIST_ID = "EXTRA_LIST_ID"
        const val EXTRA_LIST_ITEM_ID = "EXTRA_LIST_ITEM_ID"
    }

    private val viewModel: EditItemViewModel by viewModels {
        EditItemViewModelFactory(ListItemRepository)
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

        binding = ActivityEditItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.editItemMain) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        shoppingListId = intent.getStringExtra(EXTRA_LIST_ID)
        itemId = intent.getStringExtra(EXTRA_LIST_ITEM_ID)

        viewModel.loadItem(itemId)

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

        binding.editItemUnitDropdown.setAdapter(itemUnitsAdapter)
        binding.editItemCategoryDropdown.setAdapter(itemCategoriesAdapter)

        binding.editItemCategoryDropdown.setOnItemClickListener { _, _, position, _ ->
            selectedCategory = ItemCategory.entries[position]
        }

        binding.editItemUnitDropdown.setOnItemClickListener { _, _, position, _ ->
            selectedUnit = ItemUnit.entries[position]
        }
    }

    private fun setupListeners() {
        binding.editItemDeleteItemButton.setOnClickListener {
            viewModel.deleteItem(itemId)
        }

        binding.editItemSaveItemButton.setOnClickListener {
            val itemListId = shoppingListId
            val itemName = binding.editItemNameInput.text.toString()
            val itemQuantity = binding.editItemQuantityInput.text.toString()
            val itemCategory = selectedCategory
            val itemUnit = selectedUnit

            viewModel.updateItem(
                listId = itemListId,
                name = itemName,
                quantityText = itemQuantity,
                unit = itemUnit,
                category = itemCategory
            )
        }

        binding.editItemCancelButton.setOnClickListener {
            finish()
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // observes input errors
                launch {
                    viewModel.uiState.collect { state ->
                        binding.editItemNameInputWrapper.error = state.nameError
                        binding.editItemQuantityInputWrapper.error = state.quantityError
                        binding.editItemUnitDropdownWrapper.error = state.unitError
                        binding.editItemCategoryDropdownWrapper.error = state.categoryError
                    }
                }

                // observes item data to populate inputs
                launch {
                    viewModel.itemToEdit.filterNotNull().collect { item ->
                        populateInputFields(item)
                    }
                }

                // observes events
                launch {
                    viewModel.events.collect { event ->
                        when(event) {
                            is EditItemEvent.ShowMessage -> {
                                Toast.makeText(
                                    this@EditItemActivity,
                                    event.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            is EditItemEvent.Finish -> {
                                finish()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun populateInputFields(item: ListItem) {
        binding.editItemNameInput.setText(item.name)
        binding.editItemQuantityInput.setText(item.quantity.toString())
        binding.editItemUnitDropdown.setText(getString(item.unit.unitNameId), false)
        binding.editItemCategoryDropdown.setText(getString(item.category.categoryNameId), false)

        selectedUnit = item.unit
        selectedCategory = item.category
    }
}