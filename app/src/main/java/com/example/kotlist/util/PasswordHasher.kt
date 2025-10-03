package com.example.kotlist.util

import org.mindrot.jbcrypt.BCrypt

object PasswordHasher {
    fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt(12))
    }

    fun checkPassword(textPassword: String, hash: String): Boolean {
        return try {
            BCrypt.checkpw(textPassword, hash)
        } catch (e: Exception) {
            false
        }
    }
}