package com.kotlist.app.data.repository

import android.content.SharedPreferences
import com.kotlist.app.data.model.User
import com.kotlist.app.util.PasswordHasher
import androidx.core.content.edit

class UserRepository(private val prefs: SharedPreferences) {
    private val users = mutableListOf<User>()
    private var userLoggedIn: User? = null

    fun loginUser(email: String, password: String): User? {
        val user = getUserByEmail(email)

        if(user != null && PasswordHasher.checkPassword(password, user.password)) {
            setUserLoggedIn(user)
            return user
        }

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

    fun hasCreatedExampleList(userId: String): Boolean {
        return prefs.getBoolean("example_list_created_$userId", false)
    }

    fun setCreatedExampleList(userId: String) {
        prefs.edit { putBoolean("example_list_created_$userId", true) }
    }
}