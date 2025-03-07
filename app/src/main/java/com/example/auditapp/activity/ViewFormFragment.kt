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
import com.example.auditapp.adapter.FormAdapter
import com.example.auditapp.config.ApiServices
import com.example.auditapp.config.NetworkConfig
import com.example.auditapp.databinding.FragmentViewFormBinding
import com.example.auditapp.helper.SessionManager
import com.example.auditapp.model.Form
import com.example.auditapp.model.FormResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ViewFormFragment : Fragment(), FormAdapter.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    private var _binding: FragmentViewFormBinding? = null
    private val binding get() = _binding!!
    private lateinit var formAdapter: FormAdapter
    private val listForm = mutableListOf<Form>()
    private lateinit var apiServices: ApiServices
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewFormBinding.inflate(inflater, container, false)
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

        binding.btnTambahKategori.setOnClickListener {
            val fragment = TambahFormFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        loadDataFromApi()
        setupRecylerView()
    }

    private fun setupRecylerView() {
        formAdapter = FormAdapter(listForm, this)
        binding.rvForm.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = formAdapter
        }
    }

    private fun loadDataFromApi() {
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }
        apiServices.getForm("Bearer $token").enqueue(object : Callback<FormResponse> {
            override fun onResponse(call: Call<FormResponse>, response: Response<FormResponse>) {
                if (response.isSuccessful) {
                    val responseData = response.body()
                    Log.d("ViewFormFragment", "Response: $responseData")
                    response.body()?.let {
                        listForm.clear()
                        listForm.addAll(it.data?: emptyList())
                        formAdapter.notifyDataSetChanged()
                    }
                } else {
                    Toast.makeText(requireContext(), "Gagal Mengambil Data Form", Toast.LENGTH_SHORT).show()
                    Log.e("ViewFormFragment", "Gagal Mengambil Data Form: ${response.errorBody()?.toString()}")
                }
            }

            override fun onFailure(call: Call<FormResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("ViewFormFragment", "Network Error: ${t.message}")
            }
        })

    }

    override fun onViewClick(dataForm: Form) {
        val formId = dataForm.id
        val fragment = ViewTemaFormFragment.newInstance(formId)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDeleteClick(dataForm: Form) {
        val formId = dataForm.id
        if (formId != null){
            AlertDialog.Builder(requireContext())
                .setTitle("Hapus Form")
                .setMessage("Apakah Anda yakin ingin menghapus form ini?")
                .setPositiveButton("Hapus") { _, _ -> deleteForm(formId, dataForm)}
                .setNegativeButton("Batal", null)
                .show()
        } else {
            Toast.makeText(requireContext(), "ID form tidak valid", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteForm(id: Int, dataForm: Form) {
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }
        apiServices.deleteForm("Bearer $token", id).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    formAdapter.deleteForm(dataForm)
                    formAdapter.notifyDataSetChanged()
                    Toast.makeText(requireContext(), "Form Berhasil Dihapus", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Form Gagal Dihapus", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onRefresh() {
        loadDataFromApi()
        binding.swipeRefreshLayout.isRefreshing = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}