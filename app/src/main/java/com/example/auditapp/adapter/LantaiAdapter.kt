package com.example.auditapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import androidx.recyclerview.widget.RecyclerView
import com.example.auditapp.databinding.ListLantaiAdapterBinding
import com.example.auditapp.model.Lantai

class LantaiAdapter (
    private val listLantai: MutableList<Lantai>,
    private val listener: OnItemClickListener
) :RecyclerView.Adapter<LantaiAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onDeleteClick(dataLantai: Lantai)
    }

    inner class ViewHolder(val binding: ListLantaiAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun onBindItem(dataLantai: Lantai?) {
            binding.lantai.text = dataLantai?.lantai.toString()

            binding.btnHapusLantai.setOnClickListener {
                dataLantai?.let { listener.onDeleteClick(it) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ListLantaiAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBindItem(listLantai[position])
    }

    override fun getItemCount(): Int {
        return listLantai.size
    }

    fun deleteLantai(deletedLantai: Lantai) {
        val index = listLantai.indexOfFirst { it.id == deletedLantai.id }
        if (index != -1) {
            listLantai.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}