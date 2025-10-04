package com.example.kotlist.data.repository

import com.example.kotlist.R
import com.example.kotlist.data.model.ShoppingList

object ShoppingListRepository {
    private val shoppingLists = mutableListOf<ShoppingList>()
    private val placeholderImages = listOf(
        R.drawable.placeholder_img_list_0,
        R.drawable.placeholder_img_list_1,
        R.drawable.placeholder_img_list_2
    )

    fun addList(newList: ShoppingList) {
        shoppingLists.add(newList)
    }

    fun getUserLists(userId: String): List<ShoppingList> {
        return shoppingLists.filter { it.userId == userId }
    }

    fun deleteList(listId: String) {
        shoppingLists.removeAll { it.id == listId }
    }

    fun getRandomPlaceholderId(): Int {
        return placeholderImages.random()
    }
}