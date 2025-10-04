package com.example.kotlist
import com.example.kotlist.R

data class ListModel(
    // um Id único da lista (pronto para o banco de dados)
    val id: Long,

    // título para a lista
    val title: String,

    // referência para a imagem (será null se for uma URI de imagem do usuário)
    val imageResId: Int? = null,

    // Novo campo para armazenar a URI da imagem do usuário, caso ele selecione uma
    val imageUrl: String? = null
) {
    // Companion Object para lidar com a lógica do placeholder
    companion object {
        // IDs dos Drawables que você enviou (placeholders)
        private val PLACEHOLDER_DRAWABLES = listOf(
            R.drawable.placeholder_img_list_0,
            R.drawable.placeholder_img_list_1,
            R.drawable.placeholder_img_list_2
        )

         // Seleciona aleatoriamente um ID de recurso Drawable.
        fun getRandomPlaceholderId(): Int {
            return PLACEHOLDER_DRAWABLES.random()
        }
    }
}