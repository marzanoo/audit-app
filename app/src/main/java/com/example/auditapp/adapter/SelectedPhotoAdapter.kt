package com.example.auditapp.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.auditapp.R
import com.example.auditapp.databinding.ItemSelectedPhotoBinding

class SelectedPhotoAdapter(
    private val selectedImages: MutableList<Uri>,
    private val onRemoveClick: (Int) -> Unit
) : RecyclerView.Adapter<SelectedPhotoAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemSelectedPhotoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(uri: Uri, position: Int) {
            Glide.with(binding.root.context)
                .load(uri)
                .placeholder(R.drawable.logo_wag)
                .error(R.drawable.logo_wag)
                .centerCrop()
                .into(binding.imageView)

            binding.btnRemove.setOnClickListener {
                onRemoveClick(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSelectedPhotoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(selectedImages[position], position)
    }

    override fun getItemCount(): Int = selectedImages.size
}