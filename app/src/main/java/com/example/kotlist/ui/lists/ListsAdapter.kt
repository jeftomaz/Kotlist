package com.example.kotlist.ui.lists

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
                }
            } else if(list.placeholderImageId != -1) {
                binding.imageViewListPhoto.setImageResource(list.placeholderImageId)
            } else {
                binding.imageViewListPhoto.setImageResource(R.drawable.placeholder_img_list_0)
            }

            binding.root.setOnClickListener {
                onItemClicked(list)
            }
        }
    }

    fun updateData(newLists: List<ShoppingList>) {
        this.lists = newLists
        notifyDataSetChanged()
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