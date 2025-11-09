package com.example.kotlist.ui

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ListAdapter
import com.example.kotlist.R
import com.example.kotlist.data.model.ListItem
import com.example.kotlist.databinding.ListItemBinding
import com.google.android.material.checkbox.MaterialCheckBox

class ItemListAdapter (
    private val onCheckboxClicked: (ListItem, Boolean) -> Unit,
    private val onItemClick: (ListItem) -> Unit
) : ListAdapter<ListItem, ItemListAdapter.ItemViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, onCheckboxClicked, onItemClick)
    }

    class ItemViewHolder(private val binding: ListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        private val context: Context = binding.root.context

        private val categoryIcon: ImageView = binding.listItemCategoryIcon
        private val itemName: TextView = binding.listItemItemName
        private val itemQuantityUnit: TextView = binding.listItemItemQuantityUnit
        private val itemCheckbox: MaterialCheckBox = binding.listItemCheckbox

        fun bind(item: ListItem, onCheckboxClicked: (ListItem, Boolean) -> Unit, onItemClick: (ListItem) -> Unit) {
            categoryIcon.setImageResource(item.category.categoryIconId)
            itemName.text = item.name
            itemQuantityUnit.text = "${item.quantity} ${context.getString(item.unit.unitNameId)}"
            itemCheckbox.isChecked = item.isChecked

            updateUI(itemCheckbox.isChecked)

            itemCheckbox.setOnCheckedChangeListener { _, isChecked ->
                onCheckboxClicked(item, isChecked)
                updateUI(isChecked)
            }

            itemView.setOnClickListener {
                onItemClick(item)
            }
        }

        private fun updateUI(isChecked: Boolean) {
            if(isChecked) {
                itemName.paintFlags = itemName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

                binding.listItemCard.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.light_grey)
                )

                binding.root.alpha = 0.7f
            }
            else {
                itemName.paintFlags = itemName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()

                binding.listItemCard.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.white)
                )

                binding.root.alpha = 1.0f
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<ListItem>() {
        override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
            return oldItem == newItem
        }
    }
}