package com.kotlist.app.data.datasource

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

class UserRemoteDataSource(private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()) {
    suspend fun createUser(email: String, password: String, displayName: String): FirebaseUser {
        val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = authResult.user ?: throw Exception("Erro ao criar usuário no Firebase.")

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build()

        firebaseUser.updateProfile(profileUpdates).await()

        return firebaseUser
    }

    suspend fun signInWithEmailAndPassword(email: String, password: String): FirebaseUser? {
        val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        return authResult.user
    }

    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    fun isUserSignedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    fun signOut() {
        firebaseAuth.signOut()
    }

    suspend fun sendPasswordResetEmail(email: String) {
        firebaseAuth.sendPasswordResetEmail(email).await()
    }
}