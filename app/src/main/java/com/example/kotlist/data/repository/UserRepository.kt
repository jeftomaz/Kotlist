package com.example.kotlist.data.repository

import com.example.kotlist.data.model.User

object UserRepository {
    private val users = mutableListOf<User>()

    fun signUpUser(newUser: User) {
        users.add(newUser)
    }

    fun getUserByEmail(email: String): User? {
        return users.find {
            it.email.equals(email, ignoreCase = true)
        }
    }
}