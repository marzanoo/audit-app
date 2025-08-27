package com.example.auditapp.adapter

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.auditapp.R
import com.example.auditapp.databinding.ListItemFormAuditorAdapterBinding
import com.example.auditapp.model.DetailAuditAnswer
import com.example.auditapp.model.DetailFoto
import com.example.auditapp.model.TertuduhData
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import java.io.IOException

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

        private fun searchKaryawan(query: String, binding: ListItemFormAuditorAdapterBinding) {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("http://124.243.134.244/api/search-karyawan?q=$query")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    val bodu = response.body?.string()
                    if (!bodu.isNullOrEmpty()) {
                        val jsonArray = JSONArray(bodu)
                        val list = mutableListOf<String>()

                        for (i in 0 until jsonArray.length()) {
                            val obj = jsonArray.getJSONObject(i)
                            list.add(obj.getString("emp_name"))
                        }

                        (binding.etTertuduh.context as Activity).runOnUiThread {
                            val adapter = ArrayAdapter(
                                binding.etTertuduh.context,
                                android.R.layout.simple_dropdown_item_1line,
                                list
                            )
                            binding.etTertuduh.setAdapter(adapter)
                            binding.etTertuduh.showDropDown()
                        }
                    }
                }
            })
        }

        fun onBindItem(dataDetailAuditAnswer: DetailAuditAnswer) {
            binding.etScore.removeTextChangedListener(scoreTextWatcher)
            if (dataDetailAuditAnswer.listTertuduh == null) {
                dataDetailAuditAnswer.listTertuduh = mutableListOf()
            }

            binding.etTertuduh.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    val query = s.toString()
                    if (query.length >= 1) {
                        searchKaryawan(query, binding)
                    }
                }
            })

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