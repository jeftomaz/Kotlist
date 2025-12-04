package com.kotlist.app.data.datasource

import com.google.firebase.firestore.FirebaseFirestore
import com.kotlist.app.data.model.ListItem
import kotlinx.coroutines.tasks.await

class ListItemRemoteDataSource(private val firestore: FirebaseFirestore) {
    // used to make collections name changing easier (lists and items firebase collections)
    companion object {
        private const val LISTS_COLLECTION = "lists"
        private const val ITEMS_COLLECTION = "items"
    }

    suspend fun getItemsFromList(listId: String): List<ListItem> {
        val snapshot = firestore
            .collection(LISTS_COLLECTION)
            .document(listId)
            .collection(ITEMS_COLLECTION)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(ListItem::class.java)?.copy(id = doc.id)
        }
    }

    suspend fun getItemById(listId: String, itemId: String): ListItem? {
        val snapshot = firestore
            .collection(LISTS_COLLECTION)
            .document(listId)
            .collection(ITEMS_COLLECTION)
            .document(itemId)
            .get()
            .await()

        return snapshot.toObject(ListItem::class.java)?.copy(id = snapshot.id)
    }

    suspend fun addItem(listId: String, item: ListItem): String {
        val itemData = hashMapOf(
            "name" to item.name,
            "quantity" to item.quantity,
            "unit" to item.unit,
            "category" to item.category,
            "checked" to item.checked
        )

        val docRef = firestore
            .collection(LISTS_COLLECTION)
            .document(listId)
            .collection(ITEMS_COLLECTION)
            .add(itemData)
            .await()

        return docRef.id
    }

    suspend fun updateItem(listId: String, item: ListItem) {
        val itemData: Map<String, Any> = hashMapOf(
            "name" to item.name,
            "quantity" to item.quantity,
            "unit" to item.unit,
            "category" to item.category,
            "checked" to item.checked
        )

        firestore
            .collection(LISTS_COLLECTION)
            .document(listId)
            .collection(ITEMS_COLLECTION)
            .document(item.id)
            .update(itemData)
            .await()
    }

    suspend fun deleteItem(listId: String, itemId: String) {
        firestore
            .collection(LISTS_COLLECTION)
            .document(listId)
            .collection(ITEMS_COLLECTION)
            .document(itemId)
            .delete()
            .await()
    }

    suspend fun updateItemCheckedStatus(listId: String, itemId: String, isChecked: Boolean) {
        firestore
            .collection(LISTS_COLLECTION)
            .document(listId)
            .collection(ITEMS_COLLECTION)
            .document(itemId)
            .update("checked", isChecked)
            .await()
    }

    suspend fun updateItemsCheckedStatusBatch(
        listId: String,
        changes: Map<String, Boolean>
    ) {
        if(changes.isEmpty()) return

        val itemsCollection = firestore
            .collection(LISTS_COLLECTION)
            .document(listId)
            .collection(ITEMS_COLLECTION)

        val batch = firestore.batch()

        for((itemId, isChecked) in changes) {
            val docRef = itemsCollection.document(itemId)
            batch.update(docRef, "checked", isChecked)
        }

        batch.commit().await()
    }
}