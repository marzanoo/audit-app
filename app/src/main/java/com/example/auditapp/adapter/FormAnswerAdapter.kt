package com.example.auditapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.auditapp.databinding.ListItemFormAdapterBinding
import com.example.auditapp.model.FormAnswer


class FormAnswerAdapter(
    private val listForm: List<FormAnswer>,
) : RecyclerView.Adapter<FormAnswerAdapter.ViewHolder>(){
    inner class ViewHolder(val binding: ListItemFormAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {
            fun onBindItem(dataForm: FormAnswer) {
                binding.tvFormTitle.text = dataForm.kategori

                val temaAdapter = TemaAnswerAdapter(dataForm.temaList)
                binding.recyclerViewTema.adapter = temaAdapter
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemFormAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listForm.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBindItem(listForm[position])
    }

}