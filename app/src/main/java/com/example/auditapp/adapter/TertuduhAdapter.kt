package com.example.auditapp.adapter

import androidx.recyclerview.widget.RecyclerView
import com.example.auditapp.databinding.ListItemTertuduhAdapterBinding

class TertuduhAdapter(
    private val listTertuduh: MutableList<String>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<TertuduhAdapter.ViewHolder>() {
    interface OnItemClickListener {
        fun onDeleteClick(dataTertuduh: String)
    }

    inner class ViewHolder(val binding: ListItemTertuduhAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {
            fun onBindItem(dataTertuduh: String) {
                binding.tvTertuduh.text = dataTertuduh

                binding.btnDeleteTertuduh.setOnClickListener {
                    listener.onDeleteClick(dataTertuduh)
                }
            }
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemTertuduhAdapterBinding.inflate(
            android.view.LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBindItem(listTertuduh[position])
    }

    override fun getItemCount(): Int {
        return listTertuduh.size
    }
}