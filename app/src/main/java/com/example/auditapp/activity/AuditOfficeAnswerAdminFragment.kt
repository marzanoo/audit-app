package com.example.auditapp.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.example.auditapp.R
import com.example.auditapp.adapter.AuditOfficeAdminAdapter
import com.example.auditapp.config.ApiServices
import com.example.auditapp.config.NetworkConfig
import com.example.auditapp.databinding.FragmentAuditOfficeAnswerAdminBinding
import com.example.auditapp.helper.SessionManager
import com.example.auditapp.model.AuditAnswerResponseUpdate
import com.example.auditapp.model.DetailAuditAnswerResponseUpdate
import com.example.auditapp.model.DetailAuditAnswerUpdate
import com.example.auditapp.model.StandarFoto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AuditOfficeAnswerAdminFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {
    private var auditAnswerId: Int = 0
    private var _binding: FragmentAuditOfficeAnswerAdminBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var apiServices: ApiServices
    private lateinit var auditOfficeAdminAdapter: AuditOfficeAdminAdapter
    private val listAuditOfficeAdmin = mutableListOf<DetailAuditAnswerUpdate>()

    companion object {
        private const val ARG_AUDITANSWER_ID = "auditAnswerId"
        fun newInstance(auditAnswerId: Int?): Fragment {
            val fragment = AuditOfficeAnswerAdminFragment()
            val args = Bundle()
            args.putInt(ARG_AUDITANSWER_ID, auditAnswerId ?: 0)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            auditAnswerId = it.getInt(ARG_AUDITANSWER_ID, 0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuditOfficeAnswerAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        apiServices = NetworkConfig().getServices()

        val swipeRefreshLayout = binding.swipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this)
        binding.backBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        binding.btnSelesai.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnEkspor.setOnClickListener {
            showExportPreview()
        }
        setupRecylerView()
        loadDataFromApi()
        getAuditAnswerById()
    }

    private fun showExportPreview() {
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }
        apiServices.getDetailAuditOffice("Bearer $token", auditAnswerId).enqueue(object :
            Callback<DetailAuditAnswerResponseUpdate> {
            override fun onResponse(
                call: Call<DetailAuditAnswerResponseUpdate>,
                response: Response<DetailAuditAnswerResponseUpdate>
            ) {
                if (response.isSuccessful) {
                    val detailAuditAnswerResponse = response.body()
                    if (detailAuditAnswerResponse != null) {
                        val previewFragment = AuditExportPreviewFragment.newInstance(
                            auditAnswerId,
                            detailAuditAnswerResponse
                        )
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, previewFragment)
                            .addToBackStack(null)
                            .commit()
                    } else {
                        Toast.makeText(requireContext(), "Data tidak tersedia", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Error: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<DetailAuditAnswerResponseUpdate>, t: Throwable) {
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun getAuditAnswerById() {
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }
        apiServices.getAuditAnswerById("Bearer $token", auditAnswerId).enqueue(object :
            Callback<AuditAnswerResponseUpdate> {
            override fun onResponse(
                call: Call<AuditAnswerResponseUpdate>,
                response: Response<AuditAnswerResponseUpdate>
            ) {
                if (response.isSuccessful) {
                    val auditAnswerResponse = response.body()
                    val tanggal = auditAnswerResponse?.auditAnswer?.tanggal
                    binding.tvTanggal.text = tanggal
                    val area = auditAnswerResponse?.auditAnswer?.area?.area
                    binding.tvArea.text = area
                    val totalScore = auditAnswerResponse?.auditAnswer?.totalScore
                    binding.tvTotalScore.text = totalScore.toString()
                    val grade = auditAnswerResponse?.auditAnswer?.grade
                    val picName = auditAnswerResponse?.auditAnswer?.pic_name
                    binding.tvGrade.text = grade
                    binding.tvPicArea.text = picName.toString()

                } else {
                    Toast.makeText(
                        requireContext(),
                        "Fetching Data Error: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<AuditAnswerResponseUpdate>, t: Throwable) {
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun setupRecylerView() {
        auditOfficeAdminAdapter = AuditOfficeAdminAdapter(listAuditOfficeAdmin)
        binding.rvAuditAnswer.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = auditOfficeAdminAdapter
        }
    }

    private fun loadDataFromApi() {
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }
        apiServices.getDetailAuditAnswerShow("Bearer $token", auditAnswerId).enqueue(object :
            Callback<DetailAuditAnswerResponseUpdate> {
            override fun onResponse(
                call: Call<DetailAuditAnswerResponseUpdate>,
                response: Response<DetailAuditAnswerResponseUpdate>
            ) {
                if (response.isSuccessful) {
                    val detailAuditAnswerResponse = response.body()
                    detailAuditAnswerResponse?.data?.let { data ->
                        listAuditOfficeAdmin.clear()
                        data.filterNotNull().forEach { item ->
                            // Map single standarFoto to listStandarFoto if necessary
                            if (item.listStandarFoto == null && item.standarFoto != null) {
                                item.listStandarFoto = mutableListOf(
                                    StandarFoto(
                                        id = 0,
                                        image_path = item.standarFoto
                                    )
                                )
                            }
                            listAuditOfficeAdmin.add(item)
                        }
                        auditOfficeAdminAdapter.notifyDataSetChanged()
                    }

                    loadSignature(
                        detailAuditAnswerResponse?.auditor_signature,
                        detailAuditAnswerResponse?.auditee_signature,
                        detailAuditAnswerResponse?.facilitator_signature
                    )
                }
            }

            override fun onFailure(call: Call<DetailAuditAnswerResponseUpdate>, t: Throwable) {
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun loadSignature(
        auditorSignature: String?,
        auditeeSignature: String?,
        facilitatorSignature: String?
    ) {
        val BASE_URL = "http://124.243.134.244/storage/"
        auditorSignature?.let {
            Glide.with(requireContext())
                .load(BASE_URL + it)
                .placeholder(R.drawable.logo_wag)
                .error(R.drawable.logo_wag)
                .into(binding.ivSignAuditor)
        }

        auditeeSignature?.let {
            Glide.with(requireContext())
                .load(BASE_URL + it)
                .placeholder(R.drawable.logo_wag)
                .error(R.drawable.logo_wag)
                .into(binding.ivSignAuditee)
        }

        // Load facilitator signature
        facilitatorSignature?.let {
            Glide.with(requireContext())
                .load(BASE_URL + it)
                .placeholder(R.drawable.logo_wag)
                .error(R.drawable.logo_wag)
                .into(binding.ivSignFasilitator)
        }
    }

    override fun onRefresh() {
        loadDataFromApi()
        binding.swipeRefreshLayout.isRefreshing = false
    }
}