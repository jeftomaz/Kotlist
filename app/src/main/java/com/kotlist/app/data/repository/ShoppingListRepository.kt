package com.kotlist.app.data.repository

import com.kotlist.app.R
import com.kotlist.app.data.datasource.ShoppingListRemoteDataSource
import com.kotlist.app.data.model.ShoppingList

class ShoppingListRepository(
    private val remoteDataSource: ShoppingListRemoteDataSource
) {
    private val placeholderImages = listOf(
        R.drawable.placeholder_img_list_0,
        R.drawable.placeholder_img_list_1,
        R.drawable.placeholder_img_list_2
    )

    suspend fun createList(list: ShoppingList): String {
        return remoteDataSource.createList(list)
    }

    suspend fun getUserLists(userId: String): List<ShoppingList> {
        return remoteDataSource.getUserLists(userId)
    }

    suspend fun getListById(listId: String): ShoppingList? {
        return remoteDataSource.getListById(listId)
    }

    suspend fun updateList(list: ShoppingList) {
        remoteDataSource.updateList(list)
    }

    suspend fun deleteList(listId: String) {
        remoteDataSource.deleteList(listId)
    }

    suspend fun hasAnyList(userId: String): Boolean {
        return remoteDataSource.hasAnyList(userId)
    }

    fun getRandomPlaceholderId(): Int {
        return placeholderImages.random()
    }
}