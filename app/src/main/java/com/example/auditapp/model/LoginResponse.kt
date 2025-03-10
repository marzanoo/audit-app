package com.example.auditapp.model

import com.google.gson.annotations.SerializedName

class LoginResponse {

    @field:SerializedName("access_token")
    val access_token: String? = null

    @field:SerializedName("token_type")
    val token_type: String? = null

    @field:SerializedName("expires_at")
    val expires_at: String? = null

    @field:SerializedName("message")
    val message: Any? = null

    @field:SerializedName("success")
    val success: Boolean? = null

    @field:SerializedName("name")
    val name: String? = null

    @field:SerializedName("email")
    val email: String? = null

    @field:SerializedName("role")
    val role: Int? = null

    @field:SerializedName("id")
    val id: Int? = null
}