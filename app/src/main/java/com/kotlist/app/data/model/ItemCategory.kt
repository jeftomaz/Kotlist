package com.kotlist.app.data.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kotlist.app.R

enum class ItemCategory (
    @StringRes val categoryNameId: Int,
    @DrawableRes val categoryIconId: Int
) {
    FOOD(R.string.food_category, R.drawable.icon_meat),
    DRINK(R.string.drink_category, R.drawable.icon_drinks),
    ELECTRONICS(R.string.electronics_category, R.drawable.icon_electronics),
    HYGIENE(R.string.hygiene_category, R.drawable.icon_hygiene),
    UTENSILS(R.string.utensils_category, R.drawable.icon_utensils),
    OTHER(R.string.others_category, R.drawable.icon_others);
}