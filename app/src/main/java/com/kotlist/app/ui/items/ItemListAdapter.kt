package com.kotlist.app.ui.items

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kotlist.app.R
import com.kotlist.app.data.model.ListItem
import com.kotlist.app.databinding.ComponentListItemBinding
import com.google.android.material.checkbox.MaterialCheckBox

class ItemListAdapter (
    private val onCheckboxClicked: (ListItem, Boolean) -> Unit,
    private val onItemClick: (ListItem) -> Unit
) : ListAdapter<ListItem, ItemListAdapter.ItemViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ComponentListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, onCheckboxClicked, onItemClick)
    }

    class ItemViewHolder(
        private val binding: ComponentListItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private val context: Context = binding.root.context

        private val categoryIcon: ImageView = binding.listItemCategoryIcon
        private val itemName: TextView = binding.listItemItemName
        private val itemQuantityUnit: TextView = binding.listItemItemQuantityUnit
        private val itemCheckbox: MaterialCheckBox = binding.listItemCheckbox

        fun bind(item: ListItem, onCheckboxClicked: (ListItem, Boolean) -> Unit, onItemClick: (ListItem) -> Unit) {
            val category = item.getCategoryEnum()
            val unit = item.getUnitEnum()

            categoryIcon.setImageResource(category.categoryIconId)
            itemName.text = item.name
            itemQuantityUnit.text = "${item.quantity} ${context.getString(unit.unitAbbreviationId)}"

            itemCheckbox.setOnCheckedChangeListener(null)
            itemCheckbox.isChecked = item.checked

            applyCheckedStyle(itemCheckbox.isChecked)

            itemCheckbox.setOnCheckedChangeListener { _, isChecked ->
                applyCheckedStyle(isChecked)
                onCheckboxClicked(item, isChecked)
            }

            itemView.setOnClickListener {
                onItemClick(item)
            }
        }

        private fun applyCheckedStyle(isChecked: Boolean) = with(binding) {
            if(isChecked) {
                itemName.paintFlags = itemName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

                binding.listItemCard.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.checkedItemBackground)
                )

                binding.listItemCard.alpha = 0.6f
            }
            else {
                itemName.paintFlags = itemName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()

                binding.listItemCard.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.white)
                )

                binding.listItemCard.alpha = 1.0f
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