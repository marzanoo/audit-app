package com.example.auditapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.auditapp.databinding.ListItemTemaAdapterBinding

import com.example.auditapp.model.TemaFormAnswer

class TemaAnswerAdapter(
    private val listTema: List<TemaFormAnswer>
) : RecyclerView.Adapter<TemaAnswerAdapter.ViewHolder>() {
    inner class ViewHolder(val binding: ListItemTemaAdapterBinding) :
            RecyclerView.ViewHolder(binding.root) {
                fun onBindItem(dataTemaForm: TemaFormAnswer) {
                    binding.tvTemaTitle.text = dataTemaForm.tema

                    val variabelAdapter = VariabelAnswerAdapter(dataTemaForm.listVariabel)
                    binding.recyclerViewVariabel.adapter = variabelAdapter
                }
            }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemTemaAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listTema.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBindItem(listTema[position])
    }
}