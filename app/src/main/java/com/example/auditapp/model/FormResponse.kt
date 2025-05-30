package com.example.auditapp.model

import com.google.gson.annotations.SerializedName

class FormResponse {
    @field:SerializedName("message")
    @JvmField
    val message: String? = null

    @field:SerializedName("data")
    @JvmField
    val data: List<Form>? = null
}

data class UpdateFormResponse (
    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("data")
    val data: Form? = null
)

data class Form(
    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("kategori")
    val kategori: String? = null,

    val created_at: String? = null,
    val updated_at: String? = null,

)

data class FormAnswer(
    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("kategori")
    val kategori: String? = null,

    val created_at: String? = null,
    val updated_at: String? = null,

    val temaList: List<TemaFormAnswer>
)