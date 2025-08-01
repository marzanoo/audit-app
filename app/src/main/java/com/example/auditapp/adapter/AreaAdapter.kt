package com.example.auditapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.auditapp.databinding.ListAreaAdapterBinding
import com.example.auditapp.model.Area

class AreaAdapter (
    private val listArea: MutableList<Area>,
    private val listener: OnItemClickListener
) :RecyclerView.Adapter<AreaAdapter.ViewHolder>() {
    interface OnItemClickListener {
        fun onDeleteClick(dataArea: Area)
        fun onEditClick(dataArea: Area)
    }

    inner class ViewHolder(val binding: ListAreaAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBindItem(dataArea: Area?) {
            binding.area.text = dataArea?.area
//            binding.lantai.text = dataArea?.lantai?.lantai.toString()

            binding.btnHapusArea.setOnClickListener {
                dataArea?.let { listener.onDeleteClick(it) }
            }

            binding.btnEditArea.setOnClickListener {
                dataArea?.let { listener.onEditClick(it) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ListAreaAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBindItem(listArea[position])
    }

    override fun getItemCount(): Int {
        return listArea.size
    }

    fun deleteArea(deletedArea: Area) {
        val index = listArea.indexOfFirst { it.id == deletedArea.id }
        if (index != -1) {
            listArea.removeAt(index)
            notifyItemRemoved(index)
        }
    }

}