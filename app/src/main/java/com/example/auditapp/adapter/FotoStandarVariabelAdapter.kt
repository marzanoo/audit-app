package com.example.auditapp.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.auditapp.R
import com.example.auditapp.activity.FullScreenImageActivity
import com.example.auditapp.databinding.ListFotoStandarVariabelBinding
import com.example.auditapp.model.StandarFoto

class FotoStandarVariabelAdapter(
    private val standarFotoList: MutableList<StandarFoto>
) : RecyclerView.Adapter<FotoStandarVariabelAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: ListFotoStandarVariabelBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBindItem(dataStandarFoto: StandarFoto) {
            val BASE_URL = "http://124.243.134.244/storage/"
            Glide.with(binding.root.context)
                .load(BASE_URL + dataStandarFoto.image_path)
                .placeholder(R.drawable.logo_wag)
                .error(R.drawable.logo_wag)
                .into(binding.ivStandarFoto)

            binding.ivStandarFoto.setOnClickListener {
                val intent = Intent(binding.root.context, FullScreenImageActivity::class.java)
                intent.putExtra(FullScreenImageActivity.EXTRA_IMAGE_URI, dataStandarFoto.image_path)
                binding.root.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListFotoStandarVariabelBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBindItem(standarFotoList[position])
    }

    override fun getItemCount(): Int = standarFotoList.size
}