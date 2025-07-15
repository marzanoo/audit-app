package com.example.auditapp.model

import com.google.gson.annotations.SerializedName

class VariabelFormResponse {
    @field:SerializedName("message")
    @JvmField
    val message: String? = null

    @field:SerializedName("total")
    @JvmField
    val total: Int? = null

    @field:SerializedName("data")
    @JvmField
    val data: List<VariabelForm>? = null

    @field:SerializedName("photo_url")
    @JvmField
    val photo_url: String? = null
}

data class UpdateVariabelFormResponse(
    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("data")
    val data: VariabelForm? = null,

    @field:SerializedName("photo_url")
    val photo_url: String? = null
)

data class VariabelForm(
    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("tema_form_id")
    val tema_form_id: Int? = null,

    @field:SerializedName("variabel")
    val variabel: String? = null,

    @field:SerializedName("standar_variabel")
    val standar_variabel: String? = null,

    @field:SerializedName("standar_foto")
    val standar_foto: String? = null,

    @field:SerializedName("listStandarFoto")
    val listStandarFoto: List<StandarFoto>? = null,

    val created_at: String? = null,
    val updated_at: String? = null
)

data class StandarFoto(
    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("variabel_form_id")
    val variabel_form_id: Int? = null,

    @field:SerializedName("image_path")
    val image_path: String? = null,

    @field:SerializedName("photo_url")
    val photo_url: String? = null // URL lengkap untuk foto
)

data class StandarFotoResponse(
    @field:SerializedName("success")
    val success: Boolean? = null,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("data")
    val data: List<StandarFoto>? = null,
)

data class VariabelFormAnswer(
    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("tema_form_id")
    val tema_form_id: Int? = null,

    @field:SerializedName("variabel")
    val variabel: String? = null,

    @field:SerializedName("standar_variabel")
    val standar_variabel: String? = null,

    @field:SerializedName("standar_foto")
    val standar_foto: String? = null,

    val created_at: String? = null,
    val updated_at: String? = null,
    val listDetailFoto: List<DetailFoto>
)