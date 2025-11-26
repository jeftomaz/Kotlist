package com.kotlist.app.data.model

import androidx.annotation.StringRes
import com.kotlist.app.R

enum class ItemUnit(
    @StringRes val unitNameId: Int,
    @StringRes val unitAbbreviationId: Int
) {
    UNIT(R.string.unit_name, R.string.unit_abbreviation),
    KILOGRAM(R.string.kilogram_name, R.string.kilogram_abbreviation),
    GRAM(R.string.gram_name, R.string.gram_abbreviation),
    LITER(R.string.liter_name, R.string.liter_abbreviation),
    MILLILITER(R.string.milliliter_name, R.string.milliliter_abbreviation);
}