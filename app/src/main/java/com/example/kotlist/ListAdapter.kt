package com.example.kotlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlist.databinding.ListItemGridBinding

class ListAdapter(
    private val lists: List<ListModel>,
    private val onItemClicked: (ListModel) -> Unit
) : RecyclerView.Adapter<ListAdapter.ListViewHolder>() {

    inner class ListViewHolder(private val binding: ListItemGridBinding) :
        RecyclerView.ViewHolder(binding.root) {
        // ^ O root é o MaterialCardView

        fun bind(list: ListModel) {
            binding.textViewListTitle.text = list.title

            // Lógica para carregar a imagem ou placeholder
            if (list.imageResId != null && list.imageResId != 0) {
                binding.imageViewListPhoto.setImageResource(list.imageResId)
            } else {
                binding.imageViewListPhoto.setImageResource(R.drawable.list_image_placeholder)
            }

            // Configura o clique no item usando a view raiz do binding
            binding.root.setOnClickListener {
                onItemClicked(list)
            }
        }
    }

    // Infla o layout usando o Binding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding = ListItemGridBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        // passa o objeto binding para o ViewHolder
        return ListViewHolder(binding)
    }

    // conecta os dados (bind) ao ViewHolder
    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.bind(lists[position])
    }

    // retorna o número total de itens
    override fun getItemCount(): Int = lists.size
}