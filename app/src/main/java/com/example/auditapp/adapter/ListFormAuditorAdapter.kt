package com.example.auditapp.adapter

import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.auditapp.R
import com.example.auditapp.databinding.ListItemFormAuditorAdapterBinding
import com.example.auditapp.model.DetailAuditAnswer
import com.example.auditapp.model.DetailFoto

class ListFormAuditorAdapter(
    private val listAuditAnswer: MutableList<DetailAuditAnswer>,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<ListFormAuditorAdapter.ViewHolder>() {
    interface OnItemClickListener {
        fun onTakePictureClick(position: Int)
        fun onScoreChanged(position: Int, score: Int)
    }
    inner class ViewHolder(val binding: ListItemFormAuditorAdapterBinding) :
            RecyclerView.ViewHolder(binding.root) {
                private val scoreTextWatcher = object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {

                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {

                    }

                    override fun afterTextChanged(s: Editable?) {
                        val score = s.toString().toIntOrNull() ?: 0
                        listener.onScoreChanged(adapterPosition, score)
                    }
                }
                fun onBindItem(dataDetailAuditAnswer: DetailAuditAnswer) {
                    binding.etScore.removeTextChangedListener(scoreTextWatcher)
                    binding.tvKategoriForm.text = dataDetailAuditAnswer.kategori
                    binding.tvTemaForm.text = dataDetailAuditAnswer.tema
                    binding.tvStandarVariabel.text = dataDetailAuditAnswer.standarVariabel
                    val BASE_URL = "http://192.168.19.53:8000/storage/"
                    val imageUrl = BASE_URL + dataDetailAuditAnswer.standarFoto
                    Glide.with(binding.root.context)
                        .load(imageUrl)
                        .placeholder(R.drawable.logo_wag)
                        .error(R.drawable.logo_wag)
                        .into(binding.ivStandarFoto)
                    binding.tvVariabel.text = dataDetailAuditAnswer.variabel

                    val detailFotoAdapter = DetailFotoAdapter(
                        dataDetailAuditAnswer.listDetailFoto?.toMutableList() ?: mutableListOf(),
                        object : DetailFotoAdapter.OnItemClickListener {
                            override fun onDeleteClick(dataDetailFoto: DetailFoto) {
                                // Remove photo from the list
                                dataDetailAuditAnswer.listDetailFoto?.remove(dataDetailFoto)
                                // Notify adapter
                                notifyItemChanged(adapterPosition)
                            }
                        }
                    )
                    binding.rvFoto.apply {
                        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                        adapter = detailFotoAdapter
                    }
                    // Set take picture button click listener
                    binding.btnAmbilFoto.setOnClickListener {
                        listener.onTakePictureClick(adapterPosition)
                    }

                    // Set score
                    binding.etScore.setText(dataDetailAuditAnswer.score?.toString() ?: "0")

                    // Add text watcher
                    binding.etScore.addTextChangedListener(scoreTextWatcher)
                }
                fun unbind() {
                    // Remove text watcher when ViewHolder is recycled
                    binding.etScore.removeTextChangedListener(scoreTextWatcher)
                }
            }

    fun updateImage(position: Int, uri: Uri) {
        listAuditAnswer[position].imageUri = uri
        notifyItemChanged(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListItemFormAuditorAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBindItem(listAuditAnswer[position])
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        holder.unbind()
    }

    override fun getItemCount(): Int {
        return listAuditAnswer.size
    }
}