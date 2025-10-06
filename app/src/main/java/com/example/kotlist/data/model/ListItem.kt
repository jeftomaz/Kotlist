package com.example.kotlist.data.model

import java.util.UUID

data class ListItem (
    val id: String = UUID.randomUUID().toString(),
    val listId: String,
    var name: String,
    var quantity: Int,
    var unit: ItemUnit,
    var category: ItemCategory,
    var isChecked: Boolean = false
)