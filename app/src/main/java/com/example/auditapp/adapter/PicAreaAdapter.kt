package com.example.auditapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.auditapp.databinding.ListPicAreaAdapterBinding
import com.example.auditapp.model.PicArea

class PicAreaAdapter(
    private val listPicArea: MutableList<PicArea>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<PicAreaAdapter.ViewHolder>() {
    interface OnItemClickListener {
        fun onDeleteClick(dataPicArea: PicArea)
        fun onEditClick(dataPicArea: PicArea)
    }

    inner class ViewHolder(val binding: ListPicAreaAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBindItem(dataPicArea: PicArea?) {
            binding.picArea.text = dataPicArea?.karyawan?.emp_name ?: "Unknown"
            binding.dept.text = dataPicArea?.karyawan?.dept ?: "No Dept"
            binding.btnHapusArea.setOnClickListener {
                dataPicArea?.let { listener.onDeleteClick(it) }
            }
            binding.btnEditArea.setOnClickListener {
                dataPicArea?.let { listener.onEditClick(it) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ListPicAreaAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBindItem(listPicArea[position])
    }

    override fun getItemCount(): Int {
        return listPicArea.size
    }

    fun deletePicArea(deletedPicArea: PicArea) {
        val index = listPicArea.indexOfFirst { it.id == deletedPicArea.id }
        if (index != -1) {
            listPicArea.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}