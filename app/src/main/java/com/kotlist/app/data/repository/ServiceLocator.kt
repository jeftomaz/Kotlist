package com.kotlist.app.data.repository

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.kotlist.app.data.datasource.UserRemoteDataSource

object ServiceLocator {
    fun provideUserRepository(context: Context): UserRepository {
        val dataSource = UserRemoteDataSource(FirebaseAuth.getInstance())
        return UserRepository(dataSource)
    }
}