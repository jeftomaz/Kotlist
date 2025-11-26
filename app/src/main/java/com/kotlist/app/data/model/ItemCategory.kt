package com.kotlist.app.data.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.kotlist.app.R

enum class ItemCategory (
    @StringRes val categoryNameId: Int,
    @DrawableRes val categoryIconId: Int
) {
    COMIDA(R.string.food_category, R.drawable.icon_meat),
    BEBIDAS(R.string.drink_category, R.drawable.icon_drinks),
    ELETRONICOS(R.string.electronics_category, R.drawable.icon_electronics),
    HIGIENE(R.string.hygiene_category, R.drawable.icon_hygiene),
    UTENSILIOS(R.string.utensils_category, R.drawable.icon_utensils),
    OUTROS(R.string.others_category, R.drawable.icon_others);
}