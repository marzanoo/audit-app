package com.example.auditapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.auditapp.databinding.ListTemaFormAdapterBinding
import com.example.auditapp.model.TemaForm

class TemaFormAdapter(
    private val listTemaForm: MutableList<TemaForm>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<TemaFormAdapter.ViewHolder>() {
    interface OnItemClickListener {
        fun onDeleteClick(dataTemaForm: TemaForm)
        fun onEditClick(dataTemaForm: TemaForm)
        fun onDetailClick(dataTemaForm: TemaForm)
    }

    inner class ViewHolder(val binding: ListTemaFormAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBindItem(dataTemaForm: TemaForm?) {
            binding.tema.text = dataTemaForm?.tema ?: "Tidak ada"

            binding.btnHapusTema.setOnClickListener {
                dataTemaForm?.let { listener.onDeleteClick(it) }
            }

            binding.btnEditTema.setOnClickListener {
                dataTemaForm?.let { listener.onEditClick(it) }
            }

            binding.btnDetailTema.setOnClickListener {
                dataTemaForm?.let { listener.onDetailClick(it) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ListTemaFormAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBindItem(listTemaForm[position])
    }

    override fun getItemCount(): Int {
        return listTemaForm.size
    }

    fun deleteTemaForm(deletedTemaForm: TemaForm) {
        val index = listTemaForm.indexOfFirst { it.id == deletedTemaForm.id }
        if (index != -1) {
            listTemaForm.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}