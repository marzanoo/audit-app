package com.example.auditapp.model

import com.google.gson.annotations.SerializedName

class UserResponse {
    @field:SerializedName("message")
    @JvmField
    val message: String? = null

    @field:SerializedName("data")
    @JvmField
    val data: List<UserData>? = null
}

data class UserResponseGetById (
    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("data")
    val data: UserData? = null
)

data class UserData (
    val id: Int,
    val name: String,
    val email: String
)