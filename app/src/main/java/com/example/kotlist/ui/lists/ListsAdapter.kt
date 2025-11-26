package com.example.kotlist.ui.lists

import androidx.recyclerview.widget.DiffUtil
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.kotlist.R
import com.example.kotlist.data.model.ShoppingList
import com.example.kotlist.databinding.ComponentListButtonBinding

class ListsAdapter(
    private var lists: List<ShoppingList>,
    private val onItemClicked: (ShoppingList) -> Unit
) : RecyclerView.Adapter<ListsAdapter.ListViewHolder>() {

    class ListViewHolder(private val binding: ComponentListButtonBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(list: ShoppingList, onItemClicked: (ShoppingList) -> Unit) {
            binding.textViewListTitle.text = list.title

            if(list.coverImageUri != null) {
                val imageUri = list.coverImageUri.toUri()

                binding.imageViewListPhoto.load(imageUri) {
                    error(list.placeholderImageId)
                    placeholder(R.drawable.placeholder_img_list_0)
                }
            }
            else if(list.placeholderImageId != -1)
                binding.imageViewListPhoto.load(list.placeholderImageId) { }
            else
                binding.imageViewListPhoto.load(R.drawable.placeholder_img_list_0) { }

            binding.root.setOnClickListener {
                onItemClicked(list)
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