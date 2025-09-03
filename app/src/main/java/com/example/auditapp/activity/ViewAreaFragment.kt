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
import com.example.auditapp.adapter.AreaAdapter
import com.example.auditapp.config.ApiServices
import com.example.auditapp.config.NetworkConfig
import com.example.auditapp.databinding.FragmentViewAreaBinding
import com.example.auditapp.helper.SessionManager
import com.example.auditapp.model.Area
import com.example.auditapp.model.AreaResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ViewAreaFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener,
    AreaAdapter.OnItemClickListener {
    private var _binding: FragmentViewAreaBinding? = null
    private val binding get() = _binding!!
    private lateinit var areaAdapter: AreaAdapter
    private val listArea = mutableListOf<Area>()
    private lateinit var apiServices: ApiServices
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewAreaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        apiServices = NetworkConfig().getServices()
        val swipeRefresh = binding.swipeRefreshLayout
        swipeRefresh.setOnRefreshListener(this)

        binding.backBtn.setOnClickListener {
            val fragment = KonfigurasiFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        binding.btnTambahArea.setOnClickListener {
            val fragment = TambahAreaFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        binding.btnPICArea.setOnClickListener {
            val fragment = PicAreaFragment()
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
        apiServices.getArea("Bearer $token").enqueue(object : Callback<AreaResponse> {
            override fun onResponse(call: Call<AreaResponse>, response: Response<AreaResponse>) {
                if (response.isSuccessful) {
                    val responseData = response.body()
                    Log.d("ViewAreaFragment", "Response: $responseData")
                    response.body()?.let {
                        listArea.clear()
                        listArea.addAll(it.data ?: emptyList())
                        areaAdapter.notifyDataSetChanged()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Gagal mengambil data area",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(
                        "ViewAreaFragment",
                        "Gagal mengambil data area: ${response.errorBody()?.string()}"
                    )
                }
            }

            override fun onFailure(call: Call<AreaResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
                Log.e("ViewAreaFragment", "Network Error: ${t.message}")
            }
        })
    }

    override fun onEditClick(dataArea: Area) {
        val areaId = dataArea.id
        val fragment = EditAreaFragment.newInstance(areaId)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDeleteClick(dataArea: Area) {
        val areaId = dataArea.id
        if (areaId != null) {
            AlertDialog.Builder(requireContext())
                .setTitle("Hapus Area")
                .setMessage("Apakah Anda yakin ingin menghapus area ini?")
                .setPositiveButton("Hapus") { _, _ -> deleteArea(areaId, dataArea) }
                .setNegativeButton("Batal", null)
                .show()
        } else {
            Toast.makeText(requireContext(), "ID area tidak valid", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteArea(id: Int, dataArea: Area) {
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        apiServices.deleteArea("Bearer $token", id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    areaAdapter.deleteArea(dataArea)
                    areaAdapter.notifyDataSetChanged()
                    Toast.makeText(requireContext(), "Area berhasil dihapus", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(requireContext(), "Gagal menghapus area", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun setupRecylerView() {
        areaAdapter = AreaAdapter(listArea, this)
        binding.rvArea.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = areaAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}