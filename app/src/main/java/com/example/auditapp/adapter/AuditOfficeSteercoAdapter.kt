package com.example.auditapp.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.auditapp.R
import com.example.auditapp.databinding.ListAuditOfficeAnswerSteercoAdapterBinding
import com.example.auditapp.model.DetailAuditAnswerUpdate

class AuditOfficeSteercoAdapter(
    private val listAuditOfficeSteerco: MutableList<DetailAuditAnswerUpdate>,
) : RecyclerView.Adapter<AuditOfficeSteercoAdapter.ViewHolder>(){
    inner class ViewHolder(val binding: ListAuditOfficeAnswerSteercoAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBindItem(dataAuditOfficeSteerco: DetailAuditAnswerUpdate) {
            binding.tvKategoriForm.text = dataAuditOfficeSteerco.kategori
            binding.tvTemaForm.text = dataAuditOfficeSteerco.tema
            binding.tvStandarVariabel.text = dataAuditOfficeSteerco.standarVariabel
            val BASE_URL = "http://192.168.19.204:8000/storage/"
            val imageStandarUrl = BASE_URL + dataAuditOfficeSteerco.standarFoto
            Glide.with(binding.root.context)
                .load(imageStandarUrl)
                .placeholder(R.drawable.logo_wag)
                .error(R.drawable.logo_wag)
                .into(binding.ivStandarFoto)
            binding.tvVariabel.text = dataAuditOfficeSteerco.variabel
            dataAuditOfficeSteerco.images.let { images ->
                val detailFotoAdapter = DetailFotoAnswerSteercoAdapter(images?.toMutableList() ?: mutableListOf())
                binding.rvFoto.apply {
                    layoutManager = LinearLayoutManager(binding.root.context)
                    adapter = detailFotoAdapter
                }
            }

            dataAuditOfficeSteerco.auditees.let { auditees ->
                val tertuduhAdapter = TertuduhAnswerSteercoAdapter(auditees?.toMutableList() ?: mutableListOf())
                binding.rvTertuduh.apply {
                    layoutManager = LinearLayoutManager(binding.root.context)
                    adapter = tertuduhAdapter
                }
            }

            binding.tvScore.text = dataAuditOfficeSteerco.score.toString()

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListAuditOfficeAnswerSteercoAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBindItem(listAuditOfficeSteerco[position])
    }

    override fun getItemCount(): Int {
        return listAuditOfficeSteerco.size
    }
}