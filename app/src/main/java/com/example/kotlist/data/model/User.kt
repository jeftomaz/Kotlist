package com.example.kotlist.data.model

import java.util.UUID

data class User (
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    val password: String
)