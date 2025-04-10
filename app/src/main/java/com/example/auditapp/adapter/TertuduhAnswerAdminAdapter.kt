package com.example.auditapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.auditapp.databinding.ListItemTertuduhAnswerAdapterBinding
import com.example.auditapp.model.AuditeeData

class TertuduhAnswerAdminAdapter(
    private val listTertuduh: List<AuditeeData>
) : RecyclerView.Adapter<TertuduhAnswerAdminAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: ListItemTertuduhAnswerAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBindItem(dataTertuduh: AuditeeData) {
            binding.tvTertuduh.text = dataTertuduh.name ?: ""
            binding.tvTemuan.text = dataTertuduh.temuan.toString() // ?: "0"
        }
    }

    override fun getItemCount(): Int {
        return listTertuduh.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBindItem(listTertuduh[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ListItemTertuduhAnswerAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
}