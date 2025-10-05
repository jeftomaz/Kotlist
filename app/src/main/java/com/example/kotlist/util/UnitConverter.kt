package com.example.kotlist.util


fun String.getDisplayUnitName(): String {
    return when (this.uppercase()) {
        "UNIT" -> "un"
        "KILOGRAM" -> "kg"
        "GRAM" -> "g"
        "LITER" -> "L"
        "MILLILITER" -> "ml"
        else -> this
    }
}
