package com.example.auditapp.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class KaryawanResponse {
    @field:SerializedName("message")
    @JvmField
    val message: String? = null

    @field:SerializedName("data")
    @JvmField
    val data: List<Karyawan>? = null
}

data class SingleKaryawanResponse(
    val emp_id: String?,
    val emp_name: String?,
    val position_title: String?,
    val dept: String?,
    val remarks: String?,
    val created_at: String?,
    val updated_at: String?,
    val email: String?
)

data class Karyawan(
    @field:SerializedName("emp_id")
    val emp_id: String? = null,

    @field:SerializedName("emp_name")
    val emp_name: String? = null,

    @field:SerializedName("position_title")
    val position_title: String? = null,

    @field:SerializedName("dept")
    val dept: String? = null,

    @field:SerializedName("remarks")
    val remarks: String? = null
) : Serializable {
    override fun toString(): String {
        return emp_name ?: ""
    }
}