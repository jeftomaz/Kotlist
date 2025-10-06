package com.example.kotlist.data.repository

import com.example.kotlist.data.model.ListItem

object ListItemRepository {
    private val items = mutableListOf<ListItem>()

    fun addItem(newItem: ListItem) {
        items.add(newItem)
    }

    fun getItemsFromList(listId: String): MutableList<ListItem> {
        return items.filter { it.listId == listId } as MutableList<ListItem>
    }

    fun getItemById(itemId: String): ListItem? {
        return items.find { it.id == itemId }
    }

    fun updateItem(itemUpdated: ListItem) {
        val index = items.indexOfFirst { it.id == itemUpdated.id }
        if(index != -1)
            items[index] = itemUpdated
    }

    fun deleteItem(itemId: String) {
        items.removeAll { it.id == itemId }
    }

    fun deleteItemsFromList(listId: String) {
        items.removeAll { it.listId == listId }
    }
}