package com.kotlist.app.data.model

import java.util.UUID

data class User (
    val id: String = "",
    val name: String,
    val email: String,
    val password: String = ""
)