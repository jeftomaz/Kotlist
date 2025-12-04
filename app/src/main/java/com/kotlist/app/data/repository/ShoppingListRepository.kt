package com.kotlist.app.data.repository

import android.net.Uri
import com.kotlist.app.data.datasource.CoverImageRemoteDataSource
import com.kotlist.app.data.datasource.ShoppingListRemoteDataSource
import com.kotlist.app.data.model.ShoppingList

class ShoppingListRepository(
    private val listsRemoteDataSource: ShoppingListRemoteDataSource,
    private val coversRemoteDataSource: CoverImageRemoteDataSource
) {

    suspend fun createList(list: ShoppingList): String {
        return listsRemoteDataSource.createList(list)
    }

    suspend fun uploadCoverImage(userId: String, imageUri: Uri, listId: String): String {
        return coversRemoteDataSource.uploadListCoverImage(userId, imageUri, listId)
    }

    suspend fun getUserLists(userId: String): List<ShoppingList> {
        return listsRemoteDataSource.getUserLists(userId)
    }

    suspend fun getListById(listId: String): ShoppingList? {
        return listsRemoteDataSource.getListById(listId)
    }

    suspend fun updateList(list: ShoppingList) {
        listsRemoteDataSource.updateList(list)
    }

    suspend fun deleteList(list: ShoppingList) {
        listsRemoteDataSource.deleteList(list.id)
        if(!list.customCoverImageUrl.isNullOrBlank())
            coversRemoteDataSource.deleteListCoverImageByUrl(list.customCoverImageUrl)
    }

    suspend fun hasAnyList(userId: String): Boolean {
        return listsRemoteDataSource.hasAnyList(userId)
    }

    fun getRandomPlaceholderId(): Int {
        return (0..2).random()
    }
}