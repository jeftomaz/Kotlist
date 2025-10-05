package com.example.kotlist.layoutlogic

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlist.R
import com.example.kotlist.data.model.ShoppingList
import com.example.kotlist.databinding.ListItemGridBinding

class ListAdapter(
    private var lists: List<ShoppingList>,
    private val onItemClicked: (ShoppingList) -> Unit
) : RecyclerView.Adapter<ListAdapter.ListViewHolder>() {

    class ListViewHolder(private val binding: ListItemGridBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(list: ShoppingList, onItemClicked: (ShoppingList) -> Unit) {
            binding.textViewListTitle.text = list.title

            // --- Lógica de Imagem AJUSTADA  ---
            if (list.coverImageUri != null) {
                // Tenta carregar a imagem do usuário via URI
            } else if (list.placeholderImageId != 0) {
                // Carrega o placeholder gerado
                binding.imageViewListPhoto.setImageResource(list.placeholderImageId)
            } else {
                // Fallback
                binding.imageViewListPhoto.setImageResource(R.drawable.placeholder_img_list_0)
            }

            // Configura o clique no item
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
        val binding = ListItemGridBinding.inflate(
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