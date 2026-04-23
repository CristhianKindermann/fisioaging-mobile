package com.example.fisioaging.model

data class LoginResponse(
    val token: String,
    val expiresIn: Long,
    val userId: Long
)
