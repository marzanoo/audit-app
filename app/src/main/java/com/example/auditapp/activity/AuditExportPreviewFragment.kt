package com.example.auditapp.activity

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.auditapp.adapter.AuditExportPreviewAdapter
import com.example.auditapp.config.ApiServices
import com.example.auditapp.config.NetworkConfig
import com.example.auditapp.databinding.FragmentAuditExportPreviewBinding
import com.example.auditapp.helper.SessionManager
import com.example.auditapp.model.AuditAnswerResponseUpdate
import com.example.auditapp.model.AuditExcelResponse
import com.example.auditapp.model.DetailAuditAnswerResponseUpdate
import com.example.auditapp.model.DetailAuditAnswerUpdate
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AuditExportPreviewFragment: Fragment() {
    private var _binding: FragmentAuditExportPreviewBinding? = null
    private val binding get() = _binding!!
    private var auditAnswerId: Int = 0
    private lateinit var sessionManager: SessionManager
    private lateinit var apiServices: ApiServices
    private lateinit var auditExportPreviewAdapter: AuditExportPreviewAdapter
    private val listAuditData = mutableListOf<DetailAuditAnswerUpdate>()
    private var area: String = ""
    private var tanggal: String = ""
    private var totalScore: Int = 0
    private var grade: String = ""

    companion object {
        private const val ARG_AUDITANSWER_ID = "auditAnswerId"
        private const val ARG_AUDIT_DATA = "auditData"
        fun newInstance(
            auditAnswerId: Int?,
            data: DetailAuditAnswerResponseUpdate
        ): Fragment {
            val fragment = AuditExportPreviewFragment()
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
        _binding = FragmentAuditExportPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        apiServices = NetworkConfig().getServices()

        binding.backBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnDownloadExcel.setOnClickListener {
            downloadExcel()
        }

        setupRecyclerView()
        loadAuditData()
    }

    private fun downloadExcel() {
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak tersedia", Toast.LENGTH_SHORT).show()
            binding.progressDownload.visibility = View.GONE
            return
        }

        binding.progressDownload.visibility = View.VISIBLE

        apiServices.downloadAuditOfficeExcel("Bearer $token", auditAnswerId).enqueue(object :
            Callback<AuditExcelResponse> {  // Buat class untuk response JSON
            override fun onResponse(
                call: Call<AuditExcelResponse>,
                response: Response<AuditExcelResponse>
            ) {
                binding.progressDownload.visibility = View.GONE
                if (response.isSuccessful && response.body() != null) {
                    val fileUrl = response.body()?.fileUrl
                    if (!fileUrl.isNullOrEmpty()) {
                        downloadFileWithManager(fileUrl, response.body()?.fileName ?: "audit_excel.xlsx")
                    } else {
                        Toast.makeText(requireContext(), "URL file tidak tersedia", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Error: ${response.code()} - ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AuditExcelResponse>, t: Throwable) {
                binding.progressDownload.visibility = View.GONE
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun downloadFileWithManager(fileUrl: String, filename: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(fileUrl))
                .setTitle("Excel Export $area")
                .setDescription("Downloading audit report for $area")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename)
                .setMimeType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")

            val downloadManager = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)

            Toast.makeText(requireContext(), "Downloading file to Downloads/$filename", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error saat mendownload file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadAuditData() {
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
                    tanggal = auditAnswerResponse?.auditAnswer?.tanggal ?: ""
                    area = auditAnswerResponse?.auditAnswer?.area?.area ?: ""
                    totalScore = auditAnswerResponse?.auditAnswer?.totalScore ?: 0
                    grade = when (totalScore) {
                        in 0..2 -> "Diamond"
                        in 3..4 -> "Platinum"
                        in 5..6 -> "Gold"
                        in 7..8 -> "Silver"
                        else -> "Bronze"
                    }

                    binding.tvTitle.text = "Preview Excel Export"
                    binding.tvArea.text = "Area: $area"
                    binding.tvTanggal.text = "Tanggal: $tanggal"
                    binding.tvTotalScore.text = "Total Score: $totalScore"
                    binding.tvGrade.text = "Grade: $grade"

                    loadDetailAuditData(token)
                } else {
                    Toast.makeText(requireContext(), "Fetching Data Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AuditAnswerResponseUpdate>, t: Throwable) {
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadDetailAuditData(token: String) {
        apiServices.getDetailAuditAnswerShow("Bearer $token", auditAnswerId).enqueue(object :
            Callback<DetailAuditAnswerResponseUpdate> {
            override fun onResponse(
                call: Call<DetailAuditAnswerResponseUpdate>,
                response: Response<DetailAuditAnswerResponseUpdate>
            ) {
                if (response.isSuccessful) {
                    val detailAuditAnswerResponse = response.body()
                    detailAuditAnswerResponse?.data?.let { data ->
                        listAuditData.clear()
                        data.filterNotNull().forEach{
                            listAuditData.add(it)
                        }
                        auditExportPreviewAdapter.notifyDataSetChanged()
                    }
                } else {
                    Toast.makeText(requireContext(), "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DetailAuditAnswerResponseUpdate>, t: Throwable) {
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupRecyclerView() {
        auditExportPreviewAdapter = AuditExportPreviewAdapter(listAuditData)
        binding.rvExportPreview.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = auditExportPreviewAdapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}