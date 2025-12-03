package com.kotlist.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.kotlist.app.data.datasource.CoverImageRemoteDataSource
import com.kotlist.app.data.datasource.ShoppingListRemoteDataSource
import com.kotlist.app.data.datasource.UserRemoteDataSource

object ServiceLocator {
    fun provideUserRepository(): UserRepository {
        val dataSource = UserRemoteDataSource(FirebaseAuth.getInstance())
        return UserRepository(dataSource)
    }

    fun provideShoppingListRepository(): ShoppingListRepository {
        val listsDataSource = ShoppingListRemoteDataSource(FirebaseFirestore.getInstance())
        val coversDataSource = CoverImageRemoteDataSource(FirebaseStorage.getInstance())
        return ShoppingListRepository(listsDataSource, coversDataSource)
    }
}