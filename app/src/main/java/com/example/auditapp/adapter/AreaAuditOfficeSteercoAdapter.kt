package com.example.auditapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import com.example.auditapp.config.NetworkConfig
import com.example.auditapp.databinding.ListAreaAuditOfficeSteercoAdapterBinding
import com.example.auditapp.helper.SessionManager
import com.example.auditapp.model.Area

class AreaAuditOfficeSteercoAdapter(
    private val listArea: MutableList<Area>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<AreaAuditOfficeSteercoAdapter.ViewHolder>() {
    interface OnItemClickListener {
        fun onViewClick(dataArea: Area)
    }

    inner class ViewHolder(val binding: ListAreaAuditOfficeSteercoAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBindItem(dataArea: Area?) {
            binding.area.text = dataArea?.area
//            binding.PicArea.text = dataArea?.karyawans?.emp_name ?: "Tidak ada"

            binding.tvLihat.setOnClickListener {
                dataArea?.let { listener.onViewClick(it) }
            }
        }
    }

    override fun getItemCount(): Int {
        return listArea.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListAreaAuditOfficeSteercoAdapterBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBindItem(listArea[position])
    }
}