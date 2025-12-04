package com.kotlist.app.data.datasource

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class CoverImageRemoteDataSource(private val storage: FirebaseStorage) {
    suspend fun uploadListCoverImage(userId: String, imageUri: Uri, listId: String): String {
        val fileName = "$listId.jpg"
        val storageRef = storage.reference.child("users/$userId/list_covers/$fileName")

        storageRef.putFile(imageUri).await()
        return storageRef.downloadUrl.await().toString()
    }

    suspend fun deleteListCoverImageByUrl(imageUrl: String) {
        val ref = storage.getReferenceFromUrl(imageUrl)
        ref.delete().await()
    }
}