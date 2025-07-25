package com.example.auditapp.adapter

import android.content.Context
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.auditapp.R
import com.example.auditapp.databinding.ListItemFormAuditorAdapterBinding
import com.example.auditapp.model.DetailAuditAnswer
import com.example.auditapp.model.DetailFoto
import com.example.auditapp.model.TertuduhData

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
            if (dataDetailAuditAnswer.listTertuduh == null) {
                dataDetailAuditAnswer.listTertuduh = mutableListOf()
            }

            // Setup tertuduh RecyclerView
            val tertuduhList =
                dataDetailAuditAnswer.listTertuduh ?: mutableListOf<TertuduhData>().also {
                    dataDetailAuditAnswer.listTertuduh = it
                }

            val tertuduhAdapter = TertuduhAdapter(
                tertuduhList,
                object : TertuduhAdapter.OnItemClickListener {
                    override fun onDeleteClick(dataTertuduh: TertuduhData) {
                        // Using the local variable which is guaranteed to be non-null
                        tertuduhList.remove(dataTertuduh)
                        // Notify adapter
                        binding.rvTertuduh.adapter?.notifyDataSetChanged()
                        updateTotalScore(tertuduhList, dataDetailAuditAnswer)
                    }
                }
            )

            binding.rvTertuduh.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = tertuduhAdapter
            }

            val standarFotoVariabelAdapter = FotoStandarVariabelAdapter(
                dataDetailAuditAnswer.listStandarFoto?.toMutableList() ?: mutableListOf()
            )
            binding.rvStandarFoto.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = standarFotoVariabelAdapter
            }

            // Add button for tertuduh
            binding.btnAddTertuduh.setOnClickListener {
                val tertuduhText = binding.etTertuduh.text.toString().trim()
                val temuanValue = binding.etTemuan.text.toString().toIntOrNull() ?: 0
                if (tertuduhText.isNotEmpty()) {
                    val newTertuduh = TertuduhData(tertuduhText, temuanValue)
                    tertuduhList.add(newTertuduh)
                    binding.etTertuduh.text.clear()
                    binding.etTemuan.text.clear()
                    binding.etTertuduh.clearFocus()
                    binding.etTemuan.clearFocus()
                    val imm =
                        binding.root.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.etTemuan.windowToken, 0)
                    // Notify adapter with specific item insertion
                    binding.rvTertuduh.adapter?.notifyItemInserted(tertuduhList.size - 1)
                    updateTotalScore(tertuduhList, dataDetailAuditAnswer)
                }
            }
            binding.tvKategoriForm.text = dataDetailAuditAnswer.kategori
            binding.tvTemaForm.text = dataDetailAuditAnswer.tema
            binding.tvStandarVariabel.text = dataDetailAuditAnswer.standarVariabel
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

        private fun updateTotalScore(
            tertuduhList: MutableList<TertuduhData>,
            dataDetailAuditAnswer: DetailAuditAnswer
        ) {
            val totalScore = tertuduhList.sumOf { it.temuan }
            dataDetailAuditAnswer.score = totalScore
            binding.etScore.setText(totalScore.toString())
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
        val binding = ListItemFormAuditorAdapterBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
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