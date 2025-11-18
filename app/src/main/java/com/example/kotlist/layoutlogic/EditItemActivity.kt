package com.example.kotlist.layoutlogic

import android.graphics.Color
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.kotlist.R
import com.example.kotlist.data.model.ItemCategory
import com.example.kotlist.data.model.ItemUnit
import com.example.kotlist.data.model.ListItem
import com.example.kotlist.data.repository.ListItemRepository
import com.example.kotlist.databinding.ActivityEditItemBinding

class EditItemActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditItemBinding
    private lateinit var itemOnEditing: ListItem
    private var selectedCategory: ItemCategory? = null
    private var isCategroySelected: Boolean = false
    private var selectedUnit: ItemUnit? = null
    private var isUnitSelected: Boolean = false

    companion object {
        const val EXTRA_LIST_ID = "EXTRA_LIST_ID"
        const val EXTRA_LIST_ITEM_ID = "EXTRA_LIST_ITEM_ID"
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

        // ViewBinding configuration
        binding = ActivityEditItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.editItemMain) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        itemOnEditing = ListItemRepository.getItemById(intent.getStringExtra(EXTRA_LIST_ITEM_ID)!!)!!
        setDropdownAdapters()
        populateInputFields(itemOnEditing)

        binding.editItemCategoryDropdown.setOnItemClickListener { parent, view, position, id ->
            selectedCategory = ItemCategory.entries[position]
        }

        binding.editItemUnitDropdown.setOnItemClickListener { parent, view, position, id ->
            selectedUnit = ItemUnit.entries[position]
        }

        binding.editItemDeleteItemButton.setOnClickListener {
            ListItemRepository.deleteItem(intent.getStringExtra(EXTRA_LIST_ITEM_ID)!!)
            finish()
        }

        binding.editItemSaveItemButton.setOnClickListener {
            val itemListId = intent.getStringExtra(EXTRA_LIST_ID)
            val itemName = binding.editItemNameInput.text.toString().trim()
            val itemQuantity = binding.editItemQuantityInput.text.toString()
            val itemCategory = selectedCategory
            val itemUnit = selectedUnit

            validateAndUpdateItem(itemListId, itemName, itemQuantity.toIntOrNull(), itemUnit, itemCategory)
        }

        binding.editItemCancelButton.setOnClickListener {
            finish()
        }
    }

    fun setDropdownAdapters() {
        val itemUnitsStringArray: List<String> = ItemUnit.entries.map { getString(it.unitNameId) }
        val itemCategoriesStringArray: List<String> = ItemCategory.entries.map { getString(it.categoryNameId) }

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
    }

    fun populateInputFields(item: ListItem) {
        binding.editItemNameInput.setText(item.name)
        binding.editItemQuantityInput.setText(item.quantity.toString())
        binding.editItemUnitDropdown.setText(getString(item.unit.unitNameId), false)
        binding.editItemCategoryDropdown.setText(getString(item.category.categoryNameId), false)

        selectedUnit = item.unit
        selectedCategory = item.category
    }

    fun validateAndUpdateItem(listId: String?, name: String, quantity: Int?, unit: ItemUnit?, category: ItemCategory?) {
        binding.editItemNameInputWrapper.error = ""
        binding.editItemQuantityInputWrapper.error = ""
        binding.editItemUnitDropdownWrapper.error = ""
        binding.editItemCategoryDropdownWrapper.error = ""

        if(listId == null) {
            Toast.makeText(this, "Algo deu errado ao adicionar o item.", Toast.LENGTH_SHORT).show()
            return
        }

        if(name.isEmpty() || quantity == null || unit == null || category == null) {
            if(name.isEmpty())
                binding.editItemNameInputWrapper.error = "O nome do item não pode ser vazio."

            if(quantity == null)
                binding.editItemQuantityInputWrapper.error = "A quantidade do item não pode ser vazia."

            if(unit == null)
                binding.editItemUnitDropdownWrapper.error = "A unidade do item não pode ser vazia."

            if(category == null)
                binding.editItemCategoryDropdownWrapper.error = "A categoria do item não pode ser vazia."

            Toast.makeText(this, "Preencha todos os campos para adicionar um item.", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedItem = itemOnEditing.copy(
            name = name,
            quantity = quantity,
            unit = unit,
            category = category
        )

        ListItemRepository.updateItem(updatedItem)

        Toast.makeText(this, "Item editado com sucesso!", Toast.LENGTH_SHORT).show()
        finish()
    }
}