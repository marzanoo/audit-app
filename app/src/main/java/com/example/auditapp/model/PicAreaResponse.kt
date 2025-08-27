package com.example.auditapp.model

class PicAreaResponse {
    val message: String? = null
    val data: List<PicArea>? = null
}

class UpdatePicAreaResponse {
    val message: String? = null
    val data: PicArea? = null
}

data class PicArea(
    val id: Int? = null,
    val pic_id: String? = null,
    val karyawan: Karyawan? = null,
    val created_at: String? = null,
    val updated_at: String? = null
) {
    override fun toString(): String {
        return if (id == null && karyawan == null) {
            "Pilih PIC Area" // Placeholder text for dummy item
        } else {
            "${karyawan?.emp_name ?: "Unknown"} (${karyawan?.dept ?: "No Dept"})"
        }
    }
}