package com.example.auditapp.model

import android.net.Uri
import android.os.Parcelable
import com.google.gson.annotations.SerializedName


class AuditAnswerResponse {
    @field:SerializedName("message")
    @JvmField
    val message: String? = null

    @field:SerializedName("total")
    @JvmField
    val total: Int? = null

    @field:SerializedName("audit_answer")
    @JvmField
    val auditAnswer: List<AuditAnswerItem>? = null

    @field:SerializedName("detail_audit_answer")
    @JvmField
    val detailAuditAnswer: List<DetailAuditAnswerItem?>? = null
}

class AuditAnswerResponseUpdate {
    @field:SerializedName("message")
    @JvmField
    val message: String? = null

    @field:SerializedName("audit_answer")
    @JvmField
    val auditAnswer: AuditAnswerItem? = null

    @field:SerializedName("detail_audit_answer")
    @JvmField
    val detailAuditAnswer: List<DetailAuditAnswerItem?>? = null
}

class DetailAuditAnswerResponse {
    @field:SerializedName("data")
    @JvmField
    val data: List<DetailAuditAnswer?>? = null
}

class DetailAuditAnswerResponseUpdate {
    @field:SerializedName("data")
    @JvmField
    val data: List<DetailAuditAnswerUpdate?>? = null

    @SerializedName("auditor_signature")
    val auditor_signature: String? = null

    @SerializedName("auditee_signature")
    val auditee_signature: String? = null

    @SerializedName("facilitator_signature")
    val facilitator_signature: String? = null
}

data class DetailAuditAnswer(
    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("audit_answer_id")
    val auditAnswerId: Int? = null,

    @field:SerializedName("variabel_form_id")
    val variabelFormId: Int? = null,

    @field:SerializedName("score")
    var score: Int?,

    @field:SerializedName("standar_variabel")
    val standarVariabel: String? = null,

    @field:SerializedName("variabel")
    val variabel: String? = null,

    @field:SerializedName("list_standar_foto")
    var listStandarFoto: MutableList<StandarFoto>? = null,

    var listDetailFoto: MutableList<DetailFoto>? = null,

    var listTertuduh: MutableList<TertuduhData>? = null,

    @field:SerializedName("tema")
    val tema: String? = null,

    var tertuduh: String? = null,

    @field:SerializedName("kategori")
    val kategori: String? = null,

    var imageUri: Uri? = null,
)

data class TertuduhData(
    val name: String,
    val temuan: Int
)

data class DetailAuditAnswerUpdate(
    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("audit_answer_id")
    val auditAnswerId: Int? = null,

    @SerializedName("variabel_form_id")
    val variabelFormId: Int? = null,

    @SerializedName("score")
    var score: Int? = null,

    @SerializedName("standar_variabel")
    val standarVariabel: String? = null,

    @SerializedName("variabel")
    val variabel: String? = null,

    @SerializedName("standar_foto")
    val standarFoto: String? = null,

    @field:SerializedName("list_standar_foto")
    var listStandarFoto: MutableList<StandarFoto>? = null,

    @SerializedName("tema")
    val tema: String? = null,

    @SerializedName("kategori")
    val kategori: String? = null,

    @SerializedName("auditees")
    val auditees: List<AuditeeData>? = null,

    @SerializedName("images")
    val images: List<ImageData>? = null,

    // Local properties
    var imageUri: Uri? = null
)

data class AuditeeData(
    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("auditee")
    val name: String? = null,

    @SerializedName("temuan")
    val temuan: Int? = null
)

data class ImageData(
    @SerializedName("id")
    val id: Int? = null,

    @SerializedName("image_path")
    val image_path: String? = null
)


data class AuditAnswerItem(
    @field:SerializedName("id")
    val id: Int? = null,

    val tanggal: String? = null,

    @field:SerializedName("auditor_id")
    val auditorId: Int? = null,

    @field:SerializedName("area_id")
    val areaId: Int? = null,

    val area: Area? = null,

    val pic_area: Int? = null,

    val pic_name: String? = null,

    val grade: String? = null,

    @field:SerializedName("total_score")
    val totalScore: Int? = null,

    val status: String? = null,

    val created_at: String? = null,
    val updated_at: String? = null,
    val auditor: UserData? = null
)

data class DetailAuditAnswerItem(
    @field:SerializedName("id")
    val id: Int? = null,

    @field:SerializedName("audit_answer_id")
    val auditAnswerId: Int? = null,

    @field:SerializedName("variabel_form_id")
    val variabelFormId: Int? = null,

    @field:SerializedName("score")
    val score: Int? = null,

    val created_at: String? = null,
    val updated_at: String? = null
)
