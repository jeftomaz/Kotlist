package com.kotlist.app.data.model

import java.util.UUID

data class ShoppingList (
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val coverImageUri: String?,
    val placeholderImageId: Int,
    val userId: String
)