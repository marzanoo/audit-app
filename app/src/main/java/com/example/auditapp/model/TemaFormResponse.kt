package com.example.auditapp.model

import com.google.gson.annotations.SerializedName

class TemaFormResponse {
    @field:SerializedName("message")
    @JvmField
    val message: String? = null

    @field:SerializedName("data")
    @JvmField
    val data: List<TemaForm>? = null
}

data class UpdateTemaFormResponse (
    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("data")
    val data: TemaForm? = null,
)

data class TemaForm(
    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("form_id")
    val form_id: Int? = null,

    @field:SerializedName("tema")
    val tema: String? = null,

    val created_at: String? = null,
    val updated_at: String? = null
)