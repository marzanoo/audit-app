package com.example.auditapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.auditapp.databinding.ListListAuditOfficeAnswerSteercoAdapterBinding
import com.example.auditapp.model.AuditAnswerItem


class ListAuditOfficeSteercoAdapter(
    private val listAuditOfficeSteerco: MutableList<AuditAnswerItem>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<ListAuditOfficeSteercoAdapter.ViewHolder>(){
    interface OnItemClickListener {
        fun onViewClick(dataAuditOfficeSteerco: AuditAnswerItem)
    }
    inner class ViewHolder(val binding: ListListAuditOfficeAnswerSteercoAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBindItem(dataAuditOfficeSteerco: AuditAnswerItem?) {
            binding.auditor.text = dataAuditOfficeSteerco?.auditor?.name
            binding.tanggal.text = dataAuditOfficeSteerco?.tanggal.toString()

            binding.tvLihat.setOnClickListener {
                dataAuditOfficeSteerco?.let { listener.onViewClick(it) }
            }
        }
    }

    override fun getItemCount(): Int {
        return listAuditOfficeSteerco.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBindItem(listAuditOfficeSteerco[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListListAuditOfficeAnswerSteercoAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
}