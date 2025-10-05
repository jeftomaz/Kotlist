package com.example.kotlist.layoutlogic

import com.example.kotlist.R
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.kotlist.data.model.ItemCategory
import com.example.kotlist.data.model.ItemUnit
import com.example.kotlist.databinding.ActivityAddItemBinding
import com.example.kotlist.databinding.ActivityAddListBinding

class AddItemActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddItemBinding
    private var shoppingListId: String? = null
    private var selectedCategory: ItemCategory? = null
    private var selectedUnit: ItemUnit? = null

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
            Log.d("AdicionarItemActivity", "Categoria selecionada: $selectedCategory")
        }

        binding.addItemUnitDropdown.setOnItemClickListener { parent, view, position, id ->
            selectedUnit = ItemUnit.entries[position]
            Log.d("AdicionarItemActivity", "Unidade selecionada: $selectedUnit")
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
        // validar list id

        // validar nome

        // validar quantidade

        // validar unidade

        // validar categoria
    }
}