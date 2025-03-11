package com.example.auditapp.model

import com.google.gson.annotations.SerializedName

class DetailFotoResponse {
    @field:SerializedName("message")
    val message: String? = null

    @field:SerializedName("data")
    val data: List<DetailFoto>? = null
}

class DetailFotoResponseUpdate {
    @field:SerializedName("message")
    val message: String? = null

    @field:SerializedName("data")
    val data: DetailFoto? = null
}

data class DetailFoto (
    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("detail_audit_answer_id")
    val detail_audit_answer_id: Int? = null,

    @field:SerializedName("image_path")
    val image_path: String? = null,

    val created_at: String? = null,
    val updated_at: String? = null
)