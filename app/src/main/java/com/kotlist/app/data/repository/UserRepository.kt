package com.kotlist.app.data.repository

import com.kotlist.app.data.model.User
import com.google.firebase.auth.FirebaseUser
import com.kotlist.app.data.datasource.UserRemoteDataSource

class UserRepository(
    private val remoteDataSource: UserRemoteDataSource
) {
    suspend fun signUpUser(newUser: User) {
        remoteDataSource.createUser(
            newUser.email,
            newUser.password,
            newUser.name
        )
    }

    suspend fun signInUser(user: User): User? {
        val firebaseUser = remoteDataSource.signInWithEmailAndPassword(
            user.email,
            user.password
        )

        return firebaseUser?.toUser()
    }

    fun getUserSignedIn(): User? {
        val firebaseUser = remoteDataSource.getCurrentUser()
        return firebaseUser?.toUser()
    }

    fun isUserSignedIn(): Boolean {
        return remoteDataSource.isUserSignedIn()
    }

    fun signOut() {
        remoteDataSource.signOut()
    }

    suspend fun sendPasswordResetEmail(userEmail: String) {
        remoteDataSource.sendPasswordResetEmail(userEmail)
    }

    private fun FirebaseUser.toUser(): User {
        return User(
            id = this.uid,
            name = this.displayName ?: "",
            email = this.email ?: "",
            password = ""
        )
    }
}