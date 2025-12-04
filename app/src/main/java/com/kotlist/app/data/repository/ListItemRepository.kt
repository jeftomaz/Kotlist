package com.kotlist.app.data.repository

import com.kotlist.app.data.datasource.ListItemRemoteDataSource
import com.kotlist.app.data.model.ListItem

class ListItemRepository(private val dataSource: ListItemRemoteDataSource) {

    suspend fun getItemsFromList(listId: String): List<ListItem> {
        return dataSource.getItemsFromList(listId)
    }

    suspend fun getItemById(listId: String, itemId: String): ListItem? {
        return dataSource.getItemById(listId, itemId)
    }

    suspend fun addItem(listId: String, item: ListItem): String {
        return dataSource.addItem(listId, item)
    }

    suspend fun updateItem(listId: String, item: ListItem) {
        dataSource.updateItem(listId, item)
    }

    suspend fun deleteItem(listId: String, itemId: String) {
        dataSource.deleteItem(listId, itemId)
    }

    suspend fun updateItemCheckedStatus(listId: String, itemId: String, isChecked: Boolean) {
        dataSource.updateItemCheckedStatus(listId, itemId, isChecked)
    }

    suspend fun updateItemsCheckedStatusBatch(listId: String, changes: Map<String, Boolean>) {
        dataSource.updateItemsCheckedStatusBatch(listId, changes)
    }
}