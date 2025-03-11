package com.example.auditapp.model

import com.google.gson.annotations.SerializedName
import java.util.Date

class AuditAnswerResponse {
    @field:SerializedName("message")
    @JvmField
    val message: String? = null

    @field:SerializedName("audit_answer")
    @JvmField
    val auditAnswer: List<AuditAnswerItem?>? = null

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

data class AuditAnswerItem(
    @field:SerializedName("id")
    val id: Int? = null,

    val tanggal: String? = null,

    @field:SerializedName("auditor_id")
    val auditorId: Int? = null,

    @field:SerializedName("area_id")
    val areaId: Int? = null,

    @field:SerializedName("total_score")
    val totalScore: Int? = null,

    val created_at: String? = null,
    val updated_at: String? = null
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
