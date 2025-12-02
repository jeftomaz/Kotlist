package com.kotlist.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue

data class ShoppingList (
    val id: String = "",
    val name: String = "",
    val ownerId: String = "",
    val customCoverImageUrl: String? = null,
    val placeholderImageId: Int = -1,
    val createdAt: Timestamp? = null,
    val lastModifiedAt: Timestamp? = null
) {
    // empty constructor required by firebase
    constructor() : this(
        id = "",
        name = "",
        ownerId = "",
        customCoverImageUrl = null,
        placeholderImageId = -1,
        createdAt = null,
        lastModifiedAt = null
    )

    fun toMapOnCreate(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "ownerId" to ownerId,
            "customCoverImageUrl" to customCoverImageUrl,
            "placeholderImageId" to placeholderImageId,
            "createdAt" to FieldValue.serverTimestamp(),
            "lastModifiedAt" to FieldValue.serverTimestamp()
        )
    }

    fun toMapOnUpdate(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "customCoverImageUrl" to customCoverImageUrl,
            "lastModifiedAt" to FieldValue.serverTimestamp()
        )
    }
}