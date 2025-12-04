package com.kotlist.app.ui.items

import android.graphics.Color
import android.os.Bundle
import android.view.View
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
import com.kotlist.app.data.repository.ServiceLocator
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

    private val listItemRepository by lazy {
        ServiceLocator.provideListItemRepository()
    }

    private val viewModel: EditItemViewModel by viewModels {
        EditItemViewModelFactory(listItemRepository)
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

        viewModel.loadItem(shoppingListId, itemId)

        setupDropdownAdapters()
        setupListeners()
        setupObservers()
    }

    private fun setupDropdownAdapters() {
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
            viewModel.deleteItem()
        }

        binding.editItemSaveItemButton.setOnClickListener {
            val itemName = binding.editItemNameInput.text.toString()
            val itemQuantity = binding.editItemQuantityInput.text.toString()

            viewModel.updateItem(
                name = itemName,
                quantityText = itemQuantity,
                unit = selectedUnit,
                category = selectedCategory
            )
        }

        binding.editItemCancelButton.setOnClickListener {
            finish()
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // observes UI states
                viewModel.uiState.collect { state ->
                    when (state) {
                        is EditItemUiState.Idle -> {
                            setLoading(false)
                            clearErrors()
                        }
                        is EditItemUiState.Loading -> {
                            setLoading(true)
                            clearErrors()
                        }
                        is EditItemUiState.ItemLoaded -> {
                            setLoading(false)
                            populateInputFields(state.item)
                        }
                        is EditItemUiState.Success -> {
                            setLoading(false)
                            Toast.makeText(
                                this@EditItemActivity,
                                state.message,
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                        is EditItemUiState.Error -> {
                            setLoading(false)
                            Toast.makeText(
                                this@EditItemActivity,
                                state.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        is EditItemUiState.ValidationFailure -> {
                            setLoading(false)
                            binding.editItemNameInputWrapper.error = state.nameError
                            binding.editItemQuantityInputWrapper.error = state.quantityError
                            binding.editItemUnitDropdownWrapper.error = state.unitError
                            binding.editItemCategoryDropdownWrapper.error = state.categoryError
                        }
                    }
                }
            }
        }
    }

    private fun populateInputFields(item: ListItem) {
        binding.editItemNameInput.setText(item.name)
        binding.editItemQuantityInput.setText(item.quantity.toString())

        val unit = item.getUnitEnum()
        val category = item.getCategoryEnum()
        binding.editItemUnitDropdown.setText(getString(unit.unitNameId), false)
        binding.editItemCategoryDropdown.setText(getString(category.categoryNameId), false)

        selectedUnit = unit
        selectedCategory = category
    }

    private fun clearErrors() {
        binding.editItemNameInputWrapper.error = null
        binding.editItemQuantityInputWrapper.error = null
        binding.editItemUnitDropdownWrapper.error = null
        binding.editItemCategoryDropdownWrapper.error = null
    }

    private fun setLoading(isLoading: Boolean) {
        if(isLoading) {
            binding.editItemLoadingIndicator.visibility = View.VISIBLE
            binding.editItemNameInputWrapper.visibility = View.GONE
            binding.editItemQuantityInputWrapper.visibility = View.GONE
            binding.editItemUnitDropdownWrapper.visibility = View.GONE
            binding.editItemCategoryDropdownWrapper.visibility = View.GONE
        }
        else {
            binding.editItemLoadingIndicator.visibility = View.GONE
            binding.editItemNameInputWrapper.visibility = View.VISIBLE
            binding.editItemQuantityInputWrapper.visibility = View.VISIBLE
            binding.editItemUnitDropdownWrapper.visibility = View.VISIBLE
            binding.editItemCategoryDropdownWrapper.visibility = View.VISIBLE
        }
    }
}