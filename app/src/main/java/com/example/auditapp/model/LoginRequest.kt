package com.example.auditapp.model

data class LoginRequest(
    val username: String,
    val password: String,
    val device_id: String
)
