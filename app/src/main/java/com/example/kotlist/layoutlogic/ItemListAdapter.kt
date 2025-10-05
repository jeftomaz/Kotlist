package com.example.kotlist.layoutlogic

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlist.data.model.ListItem
import com.example.kotlist.databinding.ListItemBinding
import com.google.android.material.checkbox.MaterialCheckBox

class ItemListAdapter (
    private val itemsList: MutableList<ListItem>,
    private val onCheckboxClicked: (ListItem, Boolean) -> Unit
) : RecyclerView.Adapter<ItemListAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemsList[position]
        holder.bind(item, onCheckboxClicked)
    }

    override fun getItemCount(): Int {
        return itemsList.size
    }

    class ItemViewHolder(binding: ListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        private val context: Context = binding.root.context

        private val categoryIcon: ImageView = binding.listItemCategoryIcon
        private val itemName: TextView = binding.listItemItemName
        private val itemQuantityUnit: TextView = binding.listItemItemQuantityUnit
        private val itemCheckbox: MaterialCheckBox = binding.listItemCheckbox

        fun bind(item: ListItem, onCheckboxClicked: (ListItem, Boolean) -> Unit) {
            categoryIcon.setImageResource(item.category.categoryIconId)
            itemName.text = item.name
            itemQuantityUnit.text = "${item.quantity} ${context.getString(item.unit.unitNameId)}"
            itemCheckbox.isChecked = item.isChecked

            itemCheckbox.setOnCheckedChangeListener { _, isChecked ->
                onCheckboxClicked(item, isChecked)
            }

            // Efeito de riscado para itens marcados (ver isso depois)

//            val paintFlags = if (item.isComprado) {
//                textNome.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
//            } else {
//                textNome.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
//            }
//            textNome.paintFlags = paintFlags
        }
    }
}