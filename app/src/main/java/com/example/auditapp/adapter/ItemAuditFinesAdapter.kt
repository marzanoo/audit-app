package com.example.auditapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.auditapp.databinding.ListItemAuditFinesAdapterBinding
import com.example.auditapp.model.Fine
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

class ItemAuditFinesAdapter(
    private val listAuditFines: MutableList<Fine>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<ItemAuditFinesAdapter.ViewHolder>() {
    interface OnItemClickListener {
        fun onEvidenceClick(dataAuditFines: Fine)
    }

    inner class ViewHolder(val binding: ListItemAuditFinesAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBindItem(dataAuditFines: Fine?) {
            dataAuditFines?.let {
                binding.tvType.text = if (it.type == "fine") "Denda" else "Pembayaran"
                binding.tvAmount.text = formatRupiah(it.amount ?: BigDecimal.ZERO)
                binding.tvDescription.text = it.description ?: "-"
                binding.tvDate.text = it.paidAt?.let { date ->
                    android.text.format.DateFormat.format("dd/MM/yyyy HH:mm", date).toString()
                } ?: "-"
                binding.tvEvidence.setOnClickListener {
                    listener.onEvidenceClick(dataAuditFines)
                }
                val status = it.status.orEmpty().uppercase()
                binding.tvStatus.text = status

                val context = binding.root.context
                val statusColor = when (it.status?.lowercase()) {
                    "paid" -> android.R.color.holo_green_dark
                    "unpaid" -> android.R.color.holo_red_dark
                    else -> android.R.color.darker_gray
                }
                binding.tvStatus.setTextColor(ContextCompat.getColor(context, statusColor))
            }
        }
    }

    private fun formatRupiah(amount: BigDecimal): String {
        val format = NumberFormat.getNumberInstance(Locale("id", "ID"))
        return "Rp ${format.format(amount)}"
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBindItem(listAuditFines[position])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemAuditFinesAdapterBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listAuditFines.size
    }
}