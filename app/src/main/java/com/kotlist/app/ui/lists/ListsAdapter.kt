package com.kotlist.app.ui.lists

import androidx.recyclerview.widget.DiffUtil
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.kotlist.app.R
import com.kotlist.app.data.model.ShoppingList
import com.kotlist.app.databinding.ComponentListButtonBinding

class ListsAdapter(
    private var lists: List<ShoppingList>,
    private val onItemClicked: (ShoppingList) -> Unit
) : RecyclerView.Adapter<ListsAdapter.ListViewHolder>() {

    class ListViewHolder(private val binding: ComponentListButtonBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(list: ShoppingList, onItemClicked: (ShoppingList) -> Unit) {
            binding.textViewListTitle.text = list.name

            if(list.customCoverImageUrl != null && !list.customCoverImageUrl.isEmpty()) {
                val imageUri = list.customCoverImageUrl.toUri()

                binding.imageViewListPhoto.load(imageUri) {
                    error(placeholderIdToDrawable(list.placeholderImageId))
                    placeholder(R.drawable.placeholder_img_list_0)
                }
            }
            else if(list.placeholderImageId != -1)
                binding.imageViewListPhoto.load(placeholderIdToDrawable(list.placeholderImageId)) { }
            else
                binding.imageViewListPhoto.load(R.drawable.placeholder_img_list_0) { }

            binding.root.setOnClickListener {
                onItemClicked(list)
            }
        }

        private fun placeholderIdToDrawable(placeholderId: Int): Int {
            return when (placeholderId) {
                0 -> R.drawable.placeholder_img_list_0
                1 -> R.drawable.placeholder_img_list_1
                2 -> R.drawable.placeholder_img_list_2
                else -> R.drawable.placeholder_img_list_0
            }
        }
    }

    fun updateData(newLists: List<ShoppingList>) {
        val diffCallback = ListsDiffCallback(lists, newLists)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        lists = newLists
        diffResult.dispatchUpdatesTo(this)
    }

    private class ListsDiffCallback(
        private val oldList: List<ShoppingList>,
        private val newList: List<ShoppingList>
    ) : DiffUtil.Callback() {

        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldPos: Int, newPos: Int) =
            oldList[oldPos].id == newList[newPos].id

        override fun areContentsTheSame(oldPos: Int, newPos: Int) =
            oldList[oldPos] == newList[newPos]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding = ComponentListButtonBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.bind(lists[position], onItemClicked)
    }

    override fun getItemCount(): Int = lists.size
}