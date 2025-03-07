package com.example.auditapp.model

import com.google.gson.annotations.SerializedName

class AreaResponse {
    @field:SerializedName("message")
    @JvmField
    val message: String? = null

    @field:SerializedName("data")
    @JvmField
    val data: List<Area>? = null

    @field:SerializedName("total")
    @JvmField
    val total: Int? = null
}

data class SingleAreaResponse (
    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("data")
    val data: Area? = null
)

data class UpdateAreaResponse (
    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("data")
    val data: Area? = null
)

data class Area(
    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("area")
    val area: String? = null,

    @field:SerializedName("lantai_id")
    val lantai_id: Int? = null,

    @field:SerializedName("lantai")
    val lantai: Lantai? = null,

    @field:SerializedName("pic_area")
    val pic_area: String? = null,

    @field:SerializedName("karyawans")
    val karyawans: Karyawan? = null,

    @field:SerializedName("created_at")
    val created_at: String? = null,

    @field:SerializedName("updated_at")
    val updated_at: String? = null
)
