package com.example.auditapp.model

data class RegisterRequest(
    val nik: String,
    val name: String,
    val username: String,
    val email: String,
    val password: String,
)
