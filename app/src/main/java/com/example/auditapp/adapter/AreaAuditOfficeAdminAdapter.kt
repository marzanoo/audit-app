package com.example.auditapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.auditapp.databinding.ListAreaAuditOfficeAdminAdapterBinding
import com.example.auditapp.model.Area

class AreaAuditOfficeAdminAdapter(
    private val listArea: MutableList<Area>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<AreaAuditOfficeAdminAdapter.ViewHolder>(){
    interface OnItemClickListener {
        fun onViewClick(dataArea: Area)
    }
    inner class ViewHolder(val binding: ListAreaAuditOfficeAdminAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBindItem(dataArea: Area?) {
            binding.area.text = dataArea?.area
            binding.tvLihat.setOnClickListener {
                dataArea?.let { listener.onViewClick(it) }
            }
        }
    }

    override fun getItemCount(): Int {
        return listArea.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBindItem(listArea[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListAreaAuditOfficeAdminAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
}