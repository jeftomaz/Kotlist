package com.example.kotlist

data class ListModel(
    // um Id único da lista (pronto para o banco de dados)
    val id: Long,

    // título para a lista
    val title: String,

    // referência para a imagem
    val imageResId: Int? = null
)