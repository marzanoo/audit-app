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
import com.example.auditapp.adapter.PicAreaAdapter
import com.example.auditapp.config.ApiServices
import com.example.auditapp.config.NetworkConfig
import com.example.auditapp.databinding.FragmentPicAreaBinding
import com.example.auditapp.helper.SessionManager
import com.example.auditapp.model.PicArea
import com.example.auditapp.model.PicAreaResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PicAreaFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener,
    PicAreaAdapter.OnItemClickListener {
    private var _binding: FragmentPicAreaBinding? = null
    private val binding get() = _binding!!
    private lateinit var picAreaAdapter: PicAreaAdapter
    private val listPicArea = mutableListOf<PicArea>()
    private lateinit var apiServices: ApiServices
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPicAreaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        apiServices = NetworkConfig().getServices()
        val swipeRefresh = binding.swipeRefreshLayout
        swipeRefresh.setOnRefreshListener(this)

        binding.backBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnTambahPicArea.setOnClickListener {
            val fragment = TambahPicAreaFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        setupRecyclerView()
        loadDataFromApi()
    }

    override fun onRefresh() {
        loadDataFromApi()
        binding.swipeRefreshLayout.isRefreshing = false
    }

    private fun loadDataFromApi() {
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token is missing", Toast.LENGTH_SHORT).show()
            return
        }
        apiServices.getPicArea("Bearer $token").enqueue(object : Callback<PicAreaResponse> {
            override fun onResponse(
                call: Call<PicAreaResponse>,
                response: Response<PicAreaResponse>
            ) {
                if (response.isSuccessful) {
                    val responseData = response.body()
                    Log.d("PicAreaFragment", "Response: $responseData")
                    responseData?.let {
                        listPicArea.clear()
                        listPicArea.addAll(it.data ?: emptyList())
                        picAreaAdapter.notifyDataSetChanged()
                    }
                } else {
                    Log.e("PicAreaFragment", "Error: ${response.code()} - ${response.message()}")
                    Toast.makeText(
                        requireContext(),
                        "Failed to load data: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<PicAreaResponse>, t: Throwable) {
                Log.e("PicAreaFragment", "Network Error: ${t.message}", t)
                Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun rollingPic() {
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token is missing", Toast.LENGTH_SHORT).show()
            return
        }
        apiServices.rollingPic("Bearer $token").enqueue(object : Callback<PicAreaResponse> {
            override fun onResponse(
                call: Call<PicAreaResponse>,
                response: Response<PicAreaResponse>
            ) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "PIC Area Berhasil di Rolling",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Gagal Melakukan Rolling PIC Area",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<PicAreaResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    override fun onEditClick(dataPicArea: PicArea) {
        val picAreaId = dataPicArea.id
        val fragment = EditPicAreaFragment.newInstance(picAreaId)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDeleteClick(dataPicArea: PicArea) {
        val picAreaId = dataPicArea.id
        if (picAreaId != null) {
            AlertDialog.Builder(requireContext())
                .setTitle("Hapus PIC Area")
                .setMessage("Apakah Anda yakin ingin menghapus?")
                .setPositiveButton("Hapus") { _, _ -> deletePicArea(picAreaId, dataPicArea) }
                .setNegativeButton("Batal", null)
                .show()
        } else {
            Toast.makeText(requireContext(), "ID pic area tidak valid", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deletePicArea(id: Int, dataPicArea: PicArea) {
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }
        apiServices.deletePicArea("Bearer $token", id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "PIC Area deleted", Toast.LENGTH_SHORT).show()
                    picAreaAdapter.deletePicArea(dataPicArea)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Failed to delete PIC Area",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(
                        "PicAreaFragment",
                        "Failed to delete PIC Area: ${response.errorBody()?.string()}"
                    )
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
                Log.e("PicAreaFragment", "Network Error: ${t.message}", t)
            }
        })
    }

    private fun setupRecyclerView() {
        picAreaAdapter = PicAreaAdapter(listPicArea, this)
        binding.rvPicArea.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = picAreaAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}