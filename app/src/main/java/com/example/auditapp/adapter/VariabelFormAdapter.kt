package com.example.auditapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.auditapp.R
import com.example.auditapp.databinding.ListVariabelFormAdapterBinding
import com.example.auditapp.model.VariabelForm

class VariabelFormAdapter(
    private val listVariabelForm: MutableList<VariabelForm>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<VariabelFormAdapter.ViewHolder>() {
    interface OnItemClickListener {
        fun onEditClick(dataVariabelForm: VariabelForm)
        fun onDeleteClick(dataVariabelForm: VariabelForm)
    }

    inner class ViewHolder(val binding: ListVariabelFormAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBindItem(dataVariabelForm: VariabelForm?) {
            binding.variabel.text = dataVariabelForm?.variabel ?: "Tidak ada"
            binding.standarV.text = dataVariabelForm?.standar_variabel ?: "Tidak ada"
            val BASE_URL = "http://192.168.19.107:8000/storage/"
            val imageUrl = BASE_URL + dataVariabelForm?.standar_foto
            Glide.with(binding.root.context)
                .load(imageUrl)
                .placeholder(R.drawable.logo_wag)
                .error(R.drawable.logo_wag)
                .into(binding.standarF)

            binding.btnHapusVariabel.setOnClickListener {
                dataVariabelForm?.let { listener.onDeleteClick(it) }
            }

            binding.btnEditVariabel.setOnClickListener {
                dataVariabelForm?.let { listener.onEditClick(it) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ListVariabelFormAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBindItem(listVariabelForm[position])
    }

    override fun getItemCount(): Int {
        return listVariabelForm.size
    }

    fun deleteVariabelForm(deletedVariabelForm: VariabelForm) {
        val index = listVariabelForm.indexOfFirst { it.id == deletedVariabelForm.id }
        if (index != -1) {
            listVariabelForm.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}