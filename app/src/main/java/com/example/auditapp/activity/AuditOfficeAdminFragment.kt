package com.example.auditapp.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.auditapp.R
import com.example.auditapp.adapter.AreaAuditOfficeAdminAdapter
import com.example.auditapp.config.ApiServices
import com.example.auditapp.config.NetworkConfig
import com.example.auditapp.databinding.FragmentAuditOfficeAdminBinding
import com.example.auditapp.helper.SessionManager
import com.example.auditapp.model.Area
import com.example.auditapp.model.AreaResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuditOfficeAdminFragment : Fragment(), AreaAuditOfficeAdminAdapter.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    private var _binding: FragmentAuditOfficeAdminBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var apiServices: ApiServices
    private lateinit var areaAuditOfficeAdminAdapter: AreaAuditOfficeAdminAdapter
    private val listArea = mutableListOf<Area>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuditOfficeAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        apiServices = NetworkConfig().getServices()
        val swipeRefreshLayout = binding.swipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this)

        setupRecylerView()
        loadDataFromApi()
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
                    responseData?.let {
                        listArea.clear()
                        listArea.addAll(it.data ?: emptyList())
                        areaAuditOfficeAdminAdapter.notifyDataSetChanged()
                    }
                } else {
                    Toast.makeText(requireContext(), "Gagal mengambil data area", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AreaResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })

    }

    private fun setupRecylerView() {
        areaAuditOfficeAdminAdapter = AreaAuditOfficeAdminAdapter(listArea, this)
        binding.rvArea.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = areaAuditOfficeAdminAdapter
        }
    }

    override fun onRefresh() {
        loadDataFromApi()
        binding.swipeRefreshLayout.isRefreshing = false
    }

    override fun onViewClick(dataArea: Area) {
        val fragment = ListAuditOfficeAnswerAdminFragment.newInstance(dataArea.id)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}