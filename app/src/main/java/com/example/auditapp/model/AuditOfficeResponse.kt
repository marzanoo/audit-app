package com.example.auditapp.model

data class AuditOfficeResponse (
    val message: String,
    val data: AuditData
)

data class AuditData (
    val audit_details: List<AuditDetail>,
    val audit_answer: AuditAnswer,
    val grade: String,
    val signatures: Signature?
)

data class AuditExcelResponse(
    val success: Boolean,
    val message: String,
    val fileName: String,
    val fileUrl: String
)

data class AuditDetail(
    val id: Int,
    val audit_answer_id: Int,
    val variabel_form_id: Int,
    val variabel: String,
    val standar_variabel: String,
    val standar_foto: String?,
    val standar_foto_url: String?,
    val tema: String,
    val kategori: String,
    val score: Int,
    val auditees: List<Auditee>,
    val images: List<AuditImage>
)

data class Auditee(
    val id: Int,
    val auditee: String,
    val temuan: String
)

data class AuditImage(
    val id: Int,
    val image_path: String,
    val image_url: String
)

data class AuditAnswer(
    val id: Int,
    val area_id: Int,
    val auditor_id: Int,
    val form_id: Int,
    val total_score: Int,
    val created_at: String,
    val updated_at: String
)

data class Signature(
    val id: Int,
    val audit_answer_id: Int,
    val signature_auditor: String?,
    val signature_auditee: String?,
    val created_at: String,
    val updated_at: String
)

// ExcelDownloadResponse.kt
data class ExcelDownloadResponse(
    val success: Boolean,
    val message: String,
    val file_name: String,
    val download_url: String
)

