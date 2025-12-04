package com.kotlist.app.ui.lists

import android.app.Activity
import androidx.recyclerview.widget.DiffUtil
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.kotlist.app.R
import com.kotlist.app.data.model.ShoppingList
import com.kotlist.app.databinding.ComponentListButtonBinding
import com.kotlist.app.extensions.placeholderIdToDrawable

class ListsAdapter(
    private var lists: List<ShoppingList>,
    private val onItemClicked: (ShoppingList) -> Unit
) : RecyclerView.Adapter<ListsAdapter.ListViewHolder>() {

    class ListViewHolder(private val binding: ComponentListButtonBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(list: ShoppingList, onItemClicked: (ShoppingList) -> Unit) {
            binding.textViewListTitle.text = list.name

            if(list.customCoverImageUrl != null && !list.customCoverImageUrl.isBlank())
                loadImageWithProgress(list.customCoverImageUrl.toUri())
            else if(list.placeholderImageId != -1)
                loadImageWithProgress(placeholderIdToDrawable(list.placeholderImageId))
            else
                loadImageWithProgress(placeholderIdToDrawable(0))

            binding.root.setOnClickListener {
                onItemClicked(list)
            }
        }

        private fun loadImageWithProgress(imageUrl: Any) {
            binding.componentListButtonLoadingIndicator.visibility = View.VISIBLE

            binding.componentListButtonCoverImage.load(imageUrl) {
                crossfade(true)
                crossfade(300)
                listener(
                    onSuccess = { _, _ ->
                        binding.componentListButtonLoadingIndicator.visibility = View.GONE
                    },
                    onError = { _, _ ->
                        binding.componentListButtonLoadingIndicator.visibility = View.GONE
                    }
                )
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