package com.example.auditapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.auditapp.R
import com.example.auditapp.databinding.ListItemFotoAdapterBinding
import com.example.auditapp.model.DetailFoto

class DetailFotoAdapter (
    private val listDetailFoto: MutableList<DetailFoto>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<DetailFotoAdapter.ViewHolder>() {
    interface OnItemClickListener {
        fun onDeleteClick(dataDetailFoto: DetailFoto)
    }

    inner class ViewHolder(val binding: ListItemFotoAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {
            fun onBindItem(dataDetailFoto: DetailFoto) {
                Glide.with(binding.root.context)
                    .load(dataDetailFoto.image_path)
                    .placeholder(R.drawable.logo_wag)
                    .into(binding.imgFoto)

                binding.btnDelete.setOnClickListener {
                    listener.onDeleteClick(dataDetailFoto)
                    deleteItem(dataDetailFoto)
                }
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ListItemFotoAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBindItem(listDetailFoto[position])
    }

    override fun getItemCount(): Int {
        return listDetailFoto.size
    }

    fun deleteItem(dataDetailFoto: DetailFoto) {
        val position = listDetailFoto.indexOf(dataDetailFoto)
        if (position != -1) {
            listDetailFoto.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}
