package com.example.kotlist.data.repository

import com.example.kotlist.data.model.User
import com.example.kotlist.util.PasswordHasher

class UserRepository {
    private val users = mutableListOf<User>()
    private var userLoggedIn: User? = null

    // Função para verificação de login - ajuste de arquitetura

    fun loginUser(email: String, password: String): User? {
        val user = getUserByEmail(email)

        if (user != null && PasswordHasher.checkPassword(password, user.password)) {
            // Sucesso no login
            setUserLoggedIn(user)
            return user
        }

        // Falha no login
        return null
    }

    fun signUpUser(newUser: User) {
        users.add(newUser)
    }

    fun getUserByEmail(email: String): User? {
        return users.find {
            it.email.equals(email, ignoreCase = true)
        }
    }

    fun getUserLoggedIn(): User? {
        return userLoggedIn
    }

    fun setUserLoggedIn(user: User) {
        userLoggedIn = user
    }

    fun logoutUser() {
        userLoggedIn = null
    }
}