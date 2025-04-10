package com.example.auditapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.auditapp.R
import com.example.auditapp.databinding.ListAuditOfficeAnswerAdminAdapterBinding
import com.example.auditapp.model.DetailAuditAnswerUpdate

class AuditOfficeAdminAdapter(
    private val listAuditOfficeAdmin: MutableList<DetailAuditAnswerUpdate>
) : RecyclerView.Adapter<AuditOfficeAdminAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: ListAuditOfficeAnswerAdminAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBindItem(dataAuditOfficeAdmin: DetailAuditAnswerUpdate) {
            binding.tvKategoriForm.text = dataAuditOfficeAdmin.kategori
            binding.tvTemaForm.text = dataAuditOfficeAdmin.tema
            binding.tvStandarVariabel.text = dataAuditOfficeAdmin.standarVariabel
            val BASE_URL = "http://192.168.19.90:8000/storage/"
            val imageStandarUrl = BASE_URL + dataAuditOfficeAdmin.standarFoto
            Glide.with(binding.root.context)
                .load(imageStandarUrl)
                .placeholder(R.drawable.logo_wag)
                .error(R.drawable.logo_wag)
                .into(binding.ivStandarFoto)
            binding.tvVariabel.text = dataAuditOfficeAdmin.variabel
            dataAuditOfficeAdmin.images.let { images ->
                val detailFotoAdapter = DetailFotoAnswerAdminAdapter(images?.toMutableList() ?: mutableListOf())
                binding.rvFoto.apply {
                    layoutManager = LinearLayoutManager(binding.root.context)
                    adapter = detailFotoAdapter
                }
            }

            dataAuditOfficeAdmin.auditees.let { auditees ->
                val tertuduhAdapter = TertuduhAnswerAdminAdapter(auditees?.toMutableList() ?: mutableListOf())
                binding.rvTertuduh.apply {
                    layoutManager = LinearLayoutManager(binding.root.context)
                    adapter = tertuduhAdapter
                }
            }

            binding.tvScore.text = dataAuditOfficeAdmin.score.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListAuditOfficeAnswerAdminAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listAuditOfficeAdmin.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBindItem(listAuditOfficeAdmin[position])
    }
}