package com.example.auditapp.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.auditapp.R
import com.example.auditapp.activity.FullScreenImageActivity
import com.example.auditapp.databinding.ListItemFotoAuditOfficeAnswerAdminAdapterBinding
import com.example.auditapp.model.ImageData

class DetailFotoAnswerAdminAdapter(
    private val listDetailFoto: MutableList<ImageData>
) : RecyclerView.Adapter<DetailFotoAnswerAdminAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: ListItemFotoAuditOfficeAnswerAdminAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBindItem(dataDetailFoto: ImageData) {
            val BASE_URL = "http://124.243.134.244/storage/"
            val imageUrl = BASE_URL + (dataDetailFoto.image_path ?: "")
            Glide.with(binding.root.context)
                .load(imageUrl)
                .placeholder(R.drawable.logo_wag)
                .error(R.drawable.logo_wag)
                .into(binding.imgFoto)

            binding.imgFoto.setOnClickListener {
                val intent = Intent(binding.root.context, FullScreenImageActivity::class.java)
                intent.putExtra(FullScreenImageActivity.EXTRA_IMAGE_URI, dataDetailFoto.image_path)
                binding.root.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        return listDetailFoto.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBindItem(listDetailFoto[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemFotoAuditOfficeAnswerAdminAdapterBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }
}