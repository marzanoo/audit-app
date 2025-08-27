package com.example.auditapp.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal
import java.util.Date

data class FinesResponse(
    val karyawan: Karyawan,
    val fines: List<Fine>,
    val totalFines: Double,
    val totalPayments: Double,
    val totalDue: Double
) // Sesuaikan dengan struktur modelmu (buat class Karyawan, Fine, dll.)

data class PaymentResponse(
    val message: String,
    val status: String? = null
)

data class KaryawanFinesResponse(
    val emp_id: String,
    val emp_name: String,
    val total_due: Int
)

data class PaymentRequest(
    val name: String,
    val amount: Int
)


data class Fine(
    @SerializedName("id") val id: Int, // Asumsi Laravel return ID dari model
    @SerializedName("emp_id") val empId: String,
    @SerializedName("audit_answer_id") val auditAnswerId: Int? = null,
    @SerializedName("detail_audit_answer_id") val detailAuditAnswerId: Int? = null,
    @SerializedName("type") val type: String,
    @SerializedName("amount") val amount: BigDecimal, // Menggunakan BigDecimal untuk presisi desimal
    @SerializedName("description") val description: String,
    @SerializedName("evidence_path") val evidencePath: String? = null,
    @SerializedName("payment_method") val paymentMethod: String? = null,
    @SerializedName("paid_at") val paidAt: Date? = null,
    @SerializedName("status") val status: String,
    @SerializedName("karyawan") val karyawan: Karyawan? = null, // Relasi ke karyawan
    @SerializedName("auditAnswer") val auditAnswer: AuditAnswer? = null, // Relasi ke auditAnswer
    @SerializedName("detailAuditAnswer") val detailAuditAnswer: DetailAuditAnswer? = null // Relasi ke detailAuditAnswer
)