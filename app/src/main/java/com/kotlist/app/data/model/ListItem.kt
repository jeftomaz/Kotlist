package com.kotlist.app.data.model

import java.util.UUID

data class ListItem (
    val id: String = "",
    var name: String = "",
    var quantity: Int = 0,
    var unit: String = "",
    var category: String = "",
    var checked: Boolean = false
) {
    fun getUnitEnum(): ItemUnit {
        return try {
            ItemUnit.valueOf(unit)
        } catch (e: IllegalArgumentException) {
            ItemUnit.UNIT
        }
    }

    fun getCategoryEnum(): ItemCategory {
        return try {
            ItemCategory.valueOf(category)
        } catch (e: IllegalArgumentException) {
            ItemCategory.OTHER
        }
    }

    constructor(
        id: String = "",
        name: String,
        quantity: Int,
        unitEnum: ItemUnit,
        categoryEnum: ItemCategory,
        checked: Boolean = false
    ) : this(
        id = id,
        name = name,
        quantity = quantity,
        unit = unitEnum.name,
        category = categoryEnum.name,
        checked = checked
    )
}