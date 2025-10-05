package com.example.kotlist.data.model

import java.util.UUID

data class ListItem (
    val id: String = UUID.randomUUID().toString(),
    val listId: String,
    val name: String,
    val quantity: Int,
    val unit: ItemUnit,
    val category: ItemCategory,
    val isChecked: Boolean = false
)