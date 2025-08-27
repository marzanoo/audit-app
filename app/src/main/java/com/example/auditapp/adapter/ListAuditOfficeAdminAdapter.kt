package com.example.auditapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.auditapp.databinding.ListListAuditOfficeAnswerAdminAdapterBinding
import com.example.auditapp.model.AuditAnswerItem

class ListAuditOfficeAdminAdapter(
    private val listAuditOfficeAdmin: MutableList<AuditAnswerItem>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<ListAuditOfficeAdminAdapter.ViewHolder>() {
    interface OnItemClickListener {
        fun onViewClick(dataAuditOfficeAdmin: AuditAnswerItem)
        fun onApproveClick(dataAuditOfficeAdmin: AuditAnswerItem)
    }

    inner class ViewHolder(val binding: ListListAuditOfficeAnswerAdminAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBindItem(dataAuditOfficeAdmin: AuditAnswerItem?) {
            binding.auditor.text = dataAuditOfficeAdmin?.auditor?.name
            binding.tanggal.text = dataAuditOfficeAdmin?.tanggal.toString()
            val status = dataAuditOfficeAdmin?.status.orEmpty().uppercase()
            binding.status.text = status

            // Atur warna berdasarkan status
            val context = binding.root.context
            val statusColor = when (dataAuditOfficeAdmin?.status?.lowercase()) {
                "pending" -> android.R.color.holo_red_dark
                "approved" -> android.R.color.holo_green_dark
                else -> android.R.color.darker_gray
            }
            binding.status.setTextColor(ContextCompat.getColor(context, statusColor))

            binding.tvLihat.setOnClickListener {
                dataAuditOfficeAdmin?.let { listener.onViewClick(it) }
            }

            binding.tvApprove.setOnClickListener {
                dataAuditOfficeAdmin?.let { listener.onApproveClick(it) }
            }
        }
    }

    override fun getItemCount(): Int {
        return listAuditOfficeAdmin.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListListAuditOfficeAnswerAdminAdapterBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBindItem(listAuditOfficeAdmin[position])
    }
}