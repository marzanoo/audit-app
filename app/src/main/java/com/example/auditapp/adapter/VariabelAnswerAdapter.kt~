package com.example.auditapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.auditapp.R
import com.example.auditapp.databinding.ListItemVariabelAdapterBinding
import com.example.auditapp.model.DetailFoto

import com.example.auditapp.model.VariabelFormAnswer

class VariabelAnswerAdapter(
    private val listVariabel: List<VariabelFormAnswer>
) : RecyclerView.Adapter<VariabelAnswerAdapter.ViewHolder>(){
    inner class ViewHolder(val binding: ListItemVariabelAdapterBinding) :
            RecyclerView.ViewHolder(binding.root) {
                fun onBindItem(dataVariabelForm: VariabelFormAnswer){
                    binding.tvStandarVariabel.text = dataVariabelForm.standar_variabel
                    val BASE_URL = "http://192.168.19.107:8000/storage/"
                    val imageUrl = BASE_URL + dataVariabelForm.standar_foto
                    Glide.with(binding.root.context)
                        .load(imageUrl)
                        .placeholder(R.drawable.logo_wag)
                        .error(R.drawable.logo_wag)
                        .into(binding.imgVariabel)
                    binding.tvVariabelDesc.text = dataVariabelForm.variabel

                    val detailFotoAdapter = DetailFotoAdapter(dataVariabelForm.listDetailFoto.toMutableList(),
                        object : DetailFotoAdapter.OnItemClickListener {
                            override fun onDeleteClick(dataDetailFoto: DetailFoto) {

                            }
                        })
                    binding.recyclerViewFoto.adapter = detailFotoAdapter
                }
            }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemVariabelAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listVariabel.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBindItem(listVariabel[position])
    }
}