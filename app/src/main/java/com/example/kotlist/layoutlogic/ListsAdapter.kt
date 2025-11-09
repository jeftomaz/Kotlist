package com.example.kotlist.layoutlogic

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.kotlist.R
import com.example.kotlist.data.model.ShoppingList
import com.example.kotlist.databinding.ListButtonBinding

class ListsAdapter(
    private var lists: List<ShoppingList>,
    private val onItemClicked: (ShoppingList) -> Unit
) : RecyclerView.Adapter<ListsAdapter.ListViewHolder>() {

    class ListViewHolder(private val binding: ListButtonBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(list: ShoppingList, onItemClicked: (ShoppingList) -> Unit) {
            binding.textViewListTitle.text = list.title

            if (list.coverImageUri != null) {
                // Converte a String URI salva em um objeto Uri
                val imageUri = Uri.parse(list.coverImageUri)

                // Usa o Coil para carregar a imagem da URI na ImageView
                binding.imageViewListPhoto.load(imageUri) {
                    // Garante que, se o carregamento falhar, o placeholder seja usado como fallback
                    error(list.placeholderImageId)
                }

            } else if (list.placeholderImageId != 0) {
                // Carrega o placeholder gerado (se não houver URI)
                binding.imageViewListPhoto.setImageResource(list.placeholderImageId)
            } else {
                // Fallback final
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
        val binding = ListButtonBinding.inflate(
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