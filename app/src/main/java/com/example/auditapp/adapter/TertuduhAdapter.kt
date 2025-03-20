package com.example.auditapp.adapter

import androidx.recyclerview.widget.RecyclerView
import com.example.auditapp.databinding.ListItemTertuduhAdapterBinding
import com.example.auditapp.model.TertuduhData

class TertuduhAdapter(
    private val listTertuduh: MutableList<TertuduhData>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<TertuduhAdapter.ViewHolder>() {
    interface OnItemClickListener {
        fun onDeleteClick(dataTertuduh: TertuduhData)
    }

    inner class ViewHolder(val binding: ListItemTertuduhAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {
            fun onBindItem(dataTertuduh: TertuduhData) {
                binding.tvTertuduh.text = dataTertuduh.name
                binding.tvTemuan.text = dataTertuduh.temuan.toString()

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