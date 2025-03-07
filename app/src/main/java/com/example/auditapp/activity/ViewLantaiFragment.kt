package com.example.auditapp.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.auditapp.R
import com.example.auditapp.adapter.LantaiAdapter
import com.example.auditapp.config.ApiServices
import com.example.auditapp.config.NetworkConfig
import com.example.auditapp.databinding.FragmentViewLantaiBinding
import com.example.auditapp.helper.SessionManager
import com.example.auditapp.model.Lantai
import com.example.auditapp.model.LantaiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ViewLantaiFragment : Fragment(), LantaiAdapter.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    private var _binding: FragmentViewLantaiBinding? = null
    private val binding get() = _binding!!
    private lateinit var lantaiAdapter: LantaiAdapter
    private val listLantai = mutableListOf<Lantai>()
    private lateinit var apiServices: ApiServices
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewLantaiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        apiServices = NetworkConfig().getServices()
        val swipeRefresh = binding.swipeRefreshLayout
        swipeRefresh.setOnRefreshListener(this)

        binding.backBtn.setOnClickListener{
            val fragment = KonfigurasiFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        binding.btnTambahLantai.setOnClickListener {
            val fragment = TambahLantaiFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        setupRecylerView()
        loadDataFromApi()
    }

    override fun onRefresh() {
        loadDataFromApi()
        binding.swipeRefreshLayout.isRefreshing = false
    }

    private fun loadDataFromApi() {
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }
        apiServices.getLantai("Bearer $token").enqueue(object : Callback<LantaiResponse> {
            override fun onResponse(call: Call<LantaiResponse>, response: Response<LantaiResponse>) {
                if (response.isSuccessful) {
                    val responseData = response.body()
                    Log.d("ViewLantaiFragment", "Response: $responseData")
                    response.body()?.let {
                        listLantai.clear()
                        listLantai.addAll(it.lantaiList?: emptyList())
                        lantaiAdapter.notifyDataSetChanged()
                    }
                } else {
                    Toast.makeText(requireContext(), "Gagal mengambil data lantai", Toast.LENGTH_SHORT).show()
                    Log.e("ViewLantaiFragment", "Gagal mengambil data lantai: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<LantaiResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("ViewLantaiFragment", "Network Error: ${t.message}")
            }

        })
    }

    override fun onDeleteClick(dataLantai: Lantai) {
        val lantaiId = dataLantai.id
        if (lantaiId != null) {
            AlertDialog.Builder(this.requireContext())
                .setTitle("Hapus Lantai")
                .setMessage("Apakah Anda yakin ingin menghapus lantai ini?")
                .setPositiveButton("Hapus") { _, _ -> deleteLantai(lantaiId, dataLantai) }
                .setNegativeButton("Batal", null)
                .show()
        } else {
            Toast.makeText(requireContext(), "ID lantai tidak valid", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteLantai(id: Int, dataLantai: Lantai) {
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        apiServices.deleteLantai("Bearer $token", id).enqueue(object : Callback<Void> {
            override fun onResponse(
                call: Call<Void>,
                response: Response<Void>
            ) {
                if (response.isSuccessful) {
                    lantaiAdapter.deleteLantai(dataLantai)
                    lantaiAdapter.notifyDataSetChanged()
                    Toast.makeText(requireContext(), "Lantai berhasil dihapus", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Gagal menghapus lantai", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupRecylerView() {
        lantaiAdapter = LantaiAdapter(listLantai, this)
        binding.rvLantai.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = lantaiAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}