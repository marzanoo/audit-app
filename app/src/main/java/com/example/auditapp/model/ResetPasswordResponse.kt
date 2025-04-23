package com.example.auditapp.model

import com.google.gson.annotations.SerializedName

class ResetPasswordResponse {
    @field:SerializedName("message")
    @JvmField val message: String? = null
}

data class ResetPasswordRequest (
    val email: String?,
    val password: String,
    val password_confirmation: String
)

