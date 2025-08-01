package com.example.auditapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.auditapp.R
import com.example.auditapp.databinding.ListExportPreviewItemBinding
import com.example.auditapp.model.DetailAuditAnswerUpdate

class AuditExportPreviewAdapter(
    private val listAuditData: MutableList<DetailAuditAnswerUpdate>
) : RecyclerView.Adapter<AuditExportPreviewAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: ListExportPreviewItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBindItem(data: DetailAuditAnswerUpdate) {
            binding.tvKategori.text = data.kategori
            binding.tvTema.text = data.tema
            binding.tvStandar.text = data.standarVariabel
            binding.tvVariabel.text = data.variabel
            binding.tvScore.text = data.score.toString()

            val BASE_URL = "http://124.243.134.244/storage/"
            if (!data.standarFoto.isNullOrEmpty()) {
                val imageUrl = BASE_URL + data.standarFoto
                Glide.with(binding.root.context)
                    .load(imageUrl)
                    .placeholder(R.drawable.logo_wag)
                    .error(R.drawable.logo_wag)
                    .into(binding.ivStandarFoto)
                binding.tvFotoStandar.text = "Foto Standar"
            } else {
                binding.tvFotoStandar.text = "Tidak ada foto standar"
                binding.ivStandarFoto.setImageResource(R.drawable.logo_wag)
            }

            data.images?.let { images ->
                if (images.isNotEmpty()) {
                    val detailFotoAdapter = DetailFotoAnswerAdminAdapter(images.toMutableList())
                    binding.rvFotoTemuan.apply {
                        layoutManager = LinearLayoutManager(
                            binding.root.context,
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                        adapter = detailFotoAdapter
                    }
                    binding.tvFotoTemuan.text = "Foto Temuan"
                } else {
                    binding.tvFotoTemuan.text = "Tidak ada foto temuan"
                }
            }
            data.auditees?.let { auditees ->
                if (auditees.isNotEmpty()) {
                    val auditeeAdapter = TertuduhAnswerAdminAdapter(auditees.toMutableList())
                    binding.rvAuditees.apply {
                        layoutManager = LinearLayoutManager(binding.root.context)
                        adapter = auditeeAdapter
                    }

                    // Prepare temuan text for display
                    val temuanBuilder = StringBuilder()
                    auditees.forEach { auditee ->
                        temuanBuilder.append("${auditee.name}: ${auditee.temuan}\n")
                    }
                    binding.tvTemuan.text = temuanBuilder.toString()
                } else {
                    binding.tvTemuan.text = "Tidak ada temuan"
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListExportPreviewItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listAuditData.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBindItem(listAuditData[position])
    }
}