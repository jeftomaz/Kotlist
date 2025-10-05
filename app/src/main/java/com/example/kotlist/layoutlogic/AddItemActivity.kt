package com.example.kotlist.layoutlogic

import com.example.kotlist.R
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.kotlist.data.model.ItemCategory
import com.example.kotlist.data.model.ItemUnit
import com.example.kotlist.data.model.ListItem
import com.example.kotlist.data.repository.ListItemRepository
import com.example.kotlist.databinding.ActivityAddItemBinding
import com.example.kotlist.databinding.ActivityAddListBinding

class AddItemActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddItemBinding
    private var shoppingListId: String? = null
    private var selectedCategory: ItemCategory? = null
    private var isCategroySelected: Boolean = false
    private var selectedUnit: ItemUnit? = null
    private var isUnitSelected: Boolean = false

    companion object {
        const val EXTRA_LIST_ID = "EXTRA_LIST_ID"
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
        binding = ActivityAddItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.addItemMain) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        shoppingListId = intent.getStringExtra(EXTRA_LIST_ID)
        setDropdownAdapters()

        binding.addItemCategoryDropdown.setOnItemClickListener { parent, view, position, id ->
            selectedCategory = ItemCategory.entries[position]
        }

        binding.addItemUnitDropdown.setOnItemClickListener { parent, view, position, id ->
            selectedUnit = ItemUnit.entries[position]
        }

        binding.addItemAddItemButton.setOnClickListener {
            val itemListId = shoppingListId
            val itemName = binding.addItemNameInput.text.toString().trim()
            val itemQuantity = binding.addItemQuantityInput.text.toString()
            val itemCategory = selectedCategory
            val itemUnit = selectedUnit

            validateAndAddItem(itemListId, itemName, itemQuantity.toIntOrNull(), itemUnit, itemCategory)
        }

        // adicionar listener do botao de voltar para a tela da lista
    }

    fun setDropdownAdapters() {
        val itemUnitsStringArray: List<String> = ItemUnit.entries.map { getString(it.unitNameId) }
        val itemCategoriesStringArray: List<String> = ItemCategory.entries.map { getString(it.categoryNameId) }

        val itemUnitsAdapter = ArrayAdapter(
            this,
            R.layout.dropdown_input_item,
            itemUnitsStringArray
        )

        val itemCategoriesAdapter = ArrayAdapter(
            this,
            R.layout.dropdown_input_item,
            itemCategoriesStringArray
        )

        binding.addItemUnitDropdown.setAdapter(itemUnitsAdapter)
        binding.addItemCategoryDropdown.setAdapter(itemCategoriesAdapter)
    }

    fun validateAndAddItem(listId: String?, name: String, quantity: Int?, unit: ItemUnit?, category: ItemCategory?) {
        binding.addItemNameInputWrapper.error = ""
        binding.addItemQuantityInputWrapper.error = ""
        binding.addItemUnitDropdownWrapper.error = ""
        binding.addItemCategoryDropdownWrapper.error = ""

        if(listId == null) {
            Toast.makeText(this, "Algo deu errado ao adicionar o item.", Toast.LENGTH_SHORT).show()
            return
        }

        if(name.isEmpty() || quantity == null || unit == null || category == null) {
            if(name.isEmpty())
                binding.addItemNameInputWrapper.error = "O nome do item não pode ser vazio."

            if(quantity == null)
                binding.addItemQuantityInputWrapper.error = "A quantidade do item não pode ser vazia."

            if(unit == null)
                binding.addItemUnitDropdownWrapper.error = "A unidade do item não pode ser vazia."

            if(category == null)
                binding.addItemCategoryDropdownWrapper.error = "A categoria do item não pode ser vazia."

            Toast.makeText(this, "Preencha todos os campos para adicionar um item.", Toast.LENGTH_SHORT).show()
            return
        }

        val newListItem = ListItem(listId = listId, name = name, quantity = quantity, unit = unit, category = category)
        ListItemRepository.addItem(newListItem)

        Toast.makeText(this, "Novo item da lista criado com sucesso.", Toast.LENGTH_SHORT).show()
        finish()
    }
}