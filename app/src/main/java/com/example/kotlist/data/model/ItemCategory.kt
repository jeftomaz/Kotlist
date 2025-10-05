package com.example.kotlist.data.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.kotlist.R

enum class ItemCategory (
    @StringRes val categoryNameId: Int,
    @DrawableRes val categoryIconId: Int
) {
    FRUTAS(R.string.food_category, R.drawable.meat_icon),
    BEBIDAS(R.string.drink_category, R.drawable.meat_icon),
    ELETRONICOS(R.string.electronics_category, R.drawable.meat_icon),
    HIGIENE(R.string.hygiene_category, R.drawable.meat_icon),
    UTENSILIOS(R.string.utensils_category, R.drawable.meat_icon),
    OUTROS(R.string.others_category, R.drawable.meat_icon);
}