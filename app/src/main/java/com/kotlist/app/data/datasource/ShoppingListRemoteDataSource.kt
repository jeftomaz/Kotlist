package com.kotlist.app.data.datasource

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kotlist.app.data.model.ShoppingList
import kotlinx.coroutines.tasks.await

class ShoppingListRemoteDataSource(private val firestore: FirebaseFirestore) {
    // used to make collection name changing easier (lists firebase collection)
    companion object {
        private const val LISTS_COLLECTION = "lists"
    }

    suspend fun createList(list: ShoppingList): String {
        val docRef = firestore.collection(LISTS_COLLECTION).document()
        val listWithId = list.copy(id = docRef.id)

        docRef.set(listWithId.toMapOnCreate()).await()
        return docRef.id
    }

    suspend fun getUserLists(userId: String): List<ShoppingList> {
        val snapshot = firestore.collection(LISTS_COLLECTION)
            .orderBy("lastModifiedAt", Query.Direction.DESCENDING)
            .whereEqualTo("ownerId", userId)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(ShoppingList::class.java)?.copy(id = doc.id)
        }
    }

    suspend fun getListById(listId: String): ShoppingList? {
        val snapshot = firestore.collection(LISTS_COLLECTION)
            .document(listId)
            .get()
            .await()

        return snapshot.toObject(ShoppingList::class.java)?.copy(id = snapshot.id)
    }

    suspend fun updateList(list: ShoppingList) {
        firestore.collection(LISTS_COLLECTION)
            .document(list.id)
            .update(list.toMapOnUpdate())
            .await()
    }

    suspend fun deleteList(listId: String) {
        firestore.collection(LISTS_COLLECTION)
            .document(listId)
            .delete()
            .await()
    }

    suspend fun hasAnyList(userId: String): Boolean {
        val snapshot = firestore.collection(LISTS_COLLECTION)
            .whereEqualTo("ownerId", userId)
            .limit(1)
            .get()
            .await()

        return !snapshot.isEmpty
    }
}