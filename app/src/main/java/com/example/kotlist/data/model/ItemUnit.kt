package com.example.kotlist.data.model

import androidx.annotation.StringRes
import com.example.kotlist.R

enum class ItemUnit(
    @StringRes val unitNameId: Int
) {
    UNIT(R.string.unit),
    KILOGRAM(R.string.kilogram),
    GRAM(R.string.gram),
    LITER(R.string.liter),
    MILLILITER(R.string.milliliter);
}