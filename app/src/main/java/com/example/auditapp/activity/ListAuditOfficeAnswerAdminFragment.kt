package com.example.auditapp.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.auditapp.R
import com.example.auditapp.adapter.ListAuditOfficeAdminAdapter
import com.example.auditapp.config.ApiServices
import com.example.auditapp.config.NetworkConfig
import com.example.auditapp.databinding.FragmentListAuditOfficeAnswerAdminBinding
import com.example.auditapp.helper.SessionManager
import com.example.auditapp.model.AuditAnswerItem
import com.example.auditapp.model.AuditAnswerResponse
import com.example.auditapp.model.AuditAnswerResponseUpdate
import com.example.auditapp.model.SingleAreaResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ListAuditOfficeAnswerAdminFragment : Fragment(),
    ListAuditOfficeAdminAdapter.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    private var _binding: FragmentListAuditOfficeAnswerAdminBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var apiServices: ApiServices
    private var areaId: Int = 0
    private val listAuditOfficeAdmin = mutableListOf<AuditAnswerItem>()
    private lateinit var listAuditOfficeAdminAdapter: ListAuditOfficeAdminAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            areaId = it.getInt(ARG_AREA_ID, 0)
        }
    }

    companion object {
        private const val ARG_AREA_ID = "areaId"
        fun newInstance(areaId: Int?): Fragment {
            val fragment = ListAuditOfficeAnswerAdminFragment()
            val args = Bundle()
            args.putInt(ARG_AREA_ID, areaId ?: 0)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListAuditOfficeAnswerAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        apiServices = NetworkConfig().getServices()
        val swipeRefreshLayout = binding.swipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this)

        getAreaName()

        binding.backBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        setupRecylerView()
        loadDataFromApi()
    }

    private fun getAreaName() {
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        apiServices.getAreaById("Bearer $token", areaId).enqueue(object :
            Callback<SingleAreaResponse> {
            override fun onResponse(
                call: Call<SingleAreaResponse>,
                response: Response<SingleAreaResponse>
            ) {
                if (response.isSuccessful) {
                    val areaData = response.body()?.data
                    val areaName = areaData?.area ?: "Data tidak tersedia"
                    binding.tvAreas.text = areaName
                } else {
                    binding.tvAreas.text = ""
                    Toast.makeText(
                        requireContext(),
                        "Gagal mengambil data area",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<SingleAreaResponse>, t: Throwable) {
                binding.tvAreas.text = ""
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })

    }

    override fun onViewClick(dataAuditOfficeAdmin: AuditAnswerItem) {
        val fragment = AuditOfficeAnswerAdminFragment.newInstance(dataAuditOfficeAdmin.id)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onApproveClick(dataAuditOfficeAdmin: AuditAnswerItem) {
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }
        apiServices.approveAuditAnswer("Bearer $token", dataAuditOfficeAdmin.id ?: 0)
            .enqueue(object :
                Callback<AuditAnswerResponseUpdate> {
                override fun onResponse(
                    call: Call<AuditAnswerResponseUpdate>,
                    response: Response<AuditAnswerResponseUpdate>
                ) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        responseBody?.let {
                            Toast.makeText(
                                requireContext(),
                                "Audit Answer Approved",
                                Toast.LENGTH_SHORT
                            ).show()
                            loadDataFromApi() // Refresh the list after approval
                        } ?: run {
                            Toast.makeText(
                                requireContext(),
                                "Response body is null",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Error: ${response.code()} ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<AuditAnswerResponseUpdate>, t: Throwable) {
                    Toast.makeText(
                        requireContext(),
                        "Network Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    override fun onRefresh() {
        loadDataFromApi()
        binding.swipeRefreshLayout.isRefreshing = false
    }

    private fun setupRecylerView() {
        listAuditOfficeAdminAdapter = ListAuditOfficeAdminAdapter(listAuditOfficeAdmin, this)
        binding.rvFormAudit.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = listAuditOfficeAdminAdapter
        }
    }

    private fun loadDataFromApi() {
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }
        apiServices.getAuditAnswerByArea("Bearer $token", areaId).enqueue(object :
            Callback<AuditAnswerResponse> {
            override fun onResponse(
                call: Call<AuditAnswerResponse>,
                response: Response<AuditAnswerResponse>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    responseBody?.let {
                        listAuditOfficeAdmin.clear()
                        listAuditOfficeAdmin.addAll(it.auditAnswer ?: emptyList())
                        listAuditOfficeAdminAdapter.notifyDataSetChanged()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Error: ${response.code()} ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<AuditAnswerResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}