package com.example.auditapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.auditapp.R
import com.example.auditapp.databinding.ListItemFotoAuditOfficeAnswerAdminAdapterBinding
import com.example.auditapp.model.ImageData

class DetailFotoAnswerAdminAdapter(
    private val listDetailFoto: MutableList<ImageData>
) : RecyclerView.Adapter<DetailFotoAnswerAdminAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: ListItemFotoAuditOfficeAnswerAdminAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {
            fun onBindItem(dataDetailFoto: ImageData) {
                val BASE_URL = "http://192.168.19.90:8000/storage/"
                val imageUrl = BASE_URL + (dataDetailFoto.image_path?: "")
                Glide.with(binding.root.context)
                    .load(imageUrl)
                    .placeholder(R.drawable.logo_wag)
                    .error(R.drawable.logo_wag)
                    .into(binding.imgFoto)
            }
        }

    override fun getItemCount(): Int {
        return listDetailFoto.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBindItem(listDetailFoto[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemFotoAuditOfficeAnswerAdminAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }
}