package com.example.kotlist.data.repository

import android.content.Context

object ServiceLocator {
    @Volatile private var userRepository: UserRepository? = null

    fun provideUserRepository(context: Context): UserRepository {
        return userRepository ?: synchronized(this) {
            userRepository ?: UserRepository(
                context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            ).also { userRepository = it }
        }
    }
}