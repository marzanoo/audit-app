package com.example.auditapp.activity

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.example.auditapp.R
import com.example.auditapp.adapter.ItemAuditFinesAdapter
import com.example.auditapp.config.ApiServices
import com.example.auditapp.config.NetworkConfig
import com.example.auditapp.databinding.FragmentAuditFinesBinding
import com.example.auditapp.helper.SessionManager
import com.example.auditapp.model.Fine
import com.example.auditapp.model.FinesResponse
import com.example.auditapp.model.PaymentResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.InputStream
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

class AuditFinesFragment : Fragment(), ItemAuditFinesAdapter.OnItemClickListener,
    SwipeRefreshLayout.OnRefreshListener {
    private var _binding: FragmentAuditFinesBinding? = null
    private val binding get() = _binding!!
    private var nik: String = ""
    private var imageUri: Uri? = null
    private lateinit var itemAuditFinesAdapter: ItemAuditFinesAdapter
    private val listAuditFines = mutableListOf<Fine>()
    private lateinit var sessionManager: SessionManager
    private lateinit var apiServices: ApiServices
    private lateinit var tvNotification: TextView
    private lateinit var tvEmptyPlaceholder: TextView  // Tambah placeholder untuk RV empty

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuditFinesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            nik = it.getString(ARG_NIK_ID, "")
        }
    }

//    private val pickImageLauncher = registerForActivityResult(
//        ActivityResultContracts.StartActivityForResult()
//    ) { result ->
//        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
//            imageUri = result.data?.data
//            binding.btnSubmit.text = "Bukti Terunggah"
//        }
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        apiServices = NetworkConfig().getServices()
        val empId = nik
        val swipeRefreshLayout = binding.swipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this)
        setupRecyclerView()

        binding.tvEmpName.text = sessionManager.getName() ?: "Nama Tidak Tersedia"

        tvNotification = view.findViewById(R.id.tv_notification)
        tvEmptyPlaceholder = TextView(requireContext()).apply {  // Placeholder untuk empty
            text = "Tidak ada riwayat transaksi"
            setTextColor(resources.getColor(android.R.color.darker_gray))
            visibility = View.GONE
        }
        (binding.rvFines.parent as ViewGroup).addView(tvEmptyPlaceholder)  // Tambah ke parent RV

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

//        binding.btnUploadEvidence.setOnClickListener {
//            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//            pickImageLauncher.launch(intent)
//        }
//
//        binding.btnSubmit.setOnClickListener {
//            val amount = binding.etAmount.text.toString().trim()
//            if (amount.isEmpty() || imageUri == null) {
//                Toast.makeText(requireContext(), "Mohon lengkapi semua field", Toast.LENGTH_SHORT)
//                    .show()
//                return@setOnClickListener
//            }
//            submitPayment(empId, amount)
//        }

        fetchAuditFines(empId)
    }

    private fun fetchAuditFines(empId: String) {
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(
                requireContext(),
                "Token tidak tersedia. Silakan login ulang.",
                Toast.LENGTH_SHORT
            ).show()
            Log.e("AuditFinesFragment", "Token null or empty")
            return
        }
        Log.d("AuditFinesFragment", "Fetching fines for empId: $empId with token: Bearer $token")
        apiServices.getFines("Bearer $token", empId).enqueue(object : Callback<FinesResponse> {
            override fun onResponse(call: Call<FinesResponse>, response: Response<FinesResponse>) {
                if (response.isSuccessful) {
                    val finesResponse = response.body()
                    Log.d("AuditFinesFragment", "Response success: $finesResponse")
                    binding.tvTotalFines.text =
                        formatRupiah(BigDecimal(finesResponse?.totalFines ?: 0.0))
                    binding.tvTotalDue.text =
                        formatRupiah(BigDecimal(finesResponse?.totalDue ?: 0.0))
                    binding.tvTotalPayments.text =
                        formatRupiah(BigDecimal(finesResponse?.totalPayments ?: 0.0))
//                    val cvPayment = view?.findViewById<CardView>(R.id.cv_payment_form)
//                    if ((finesResponse?.totalDue ?: 0.0) > 0) {
//                        cvPayment?.visibility = View.VISIBLE
//                    } else {
//                        cvPayment?.visibility = View.GONE
//                    }
                    finesResponse?.fines?.let { fines ->
                        listAuditFines.clear()
                        listAuditFines.addAll(fines)
                        itemAuditFinesAdapter.notifyDataSetChanged()
                        tvEmptyPlaceholder.visibility =
                            if (fines.isEmpty()) View.VISIBLE else View.GONE
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Gagal memuat data denda: ${response.code()} - ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(
                        "AuditFinesFragment",
                        "Response failed: ${response.code()} - ${response.errorBody()?.string()}"
                    )
                }
            }

            override fun onFailure(call: Call<FinesResponse>, t: Throwable) {
                Toast.makeText(
                    requireContext(),
                    "Kesalahan jaringan: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("AuditFinesFragment", "Network failure: ${t.stackTraceToString()}")
            }
        })
    }

    private fun submitPayment(empId: String, amount: String) {
        val inputStream: InputStream? =
            imageUri?.let { requireContext().contentResolver.openInputStream(it) }
        val file = inputStream?.let { createTempFileFromStream(it) }  // Method aman untuk URI
        if (file == null) {
            Toast.makeText(requireContext(), "Gagal membaca file bukti", Toast.LENGTH_SHORT).show()
            return
        }
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val evidencePart = MultipartBody.Part.createFormData("evidence", file.name, requestFile)
        val amountBody = amount.toRequestBody("text/plain".toMediaTypeOrNull())

        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }
        Log.d("AuditFinesFragment", "Submitting payment for empId: $empId, amount: $amount")
        apiServices.submitPayment("Bearer $token", empId, amountBody, evidencePart).enqueue(object :
            Callback<PaymentResponse> {
            override fun onResponse(
                call: Call<PaymentResponse>,
                response: Response<PaymentResponse>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    responseBody?.let {
                        tvNotification.text = it.message
                        tvNotification.setBackgroundColor(
                            if (it.status == "paid") resources.getColor(
                                R.color.green,
                                null
                            ) else resources.getColor(R.color.red, null)
                        )
                        tvNotification.visibility = View.VISIBLE
                        fetchAuditFines(empId)  // Refresh
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Pembayaran gagal: ${response.code()} - ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("AuditFinesFragment", "Submit failed: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<PaymentResponse>, t: Throwable) {
                Toast.makeText(
                    requireContext(),
                    "Kesalahan jaringan: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("AuditFinesFragment", "Submit network failure: ${t.stackTraceToString()}")
            }
        })
    }

    override fun onEvidenceClick(dataAuditFines: Fine) {
        val dialog = android.app.Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_evidence_image)
        val imageView = dialog.findViewById<android.widget.ImageView>(R.id.imagePreview)
        val BASE_URL = "http://124.243.134.244/storage/"
        val evidencePath = dataAuditFines.evidencePath ?: ""
        if (evidencePath.isNotEmpty()) {
            Glide.with(requireContext())
                .load(BASE_URL + evidencePath)
                .placeholder(R.drawable.logo_wag)
                .error(R.drawable.logo_wag)
                .into(imageView)
        } else {
            imageView.setImageResource(R.drawable.logo_wag)  // Default jika null
        }
        dialog.show()
    }

    private fun setupRecyclerView() {
        itemAuditFinesAdapter = ItemAuditFinesAdapter(listAuditFines, this)
        binding.rvFines.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = itemAuditFinesAdapter
        }
    }

    override fun onRefresh() {
        fetchAuditFines(nik)
        binding.swipeRefreshLayout.isRefreshing = false
    }

    private fun createTempFileFromStream(inputStream: InputStream): File? {
        return try {
            val tempFile = File.createTempFile("evidence", ".jpg", requireContext().cacheDir)
            inputStream.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            Log.e("AuditFinesFragment", "Failed to create temp file: ${e.message}")
            null
        }
    }

    private fun formatRupiah(amount: BigDecimal): String {
        val format = NumberFormat.getNumberInstance(Locale("id", "ID"))
        return "Rp ${format.format(amount)}"
    }

    companion object {
        private const val ARG_NIK_ID = "nik"

        fun newInstance(nik: String?): Fragment {
            val fragment = AuditFinesFragment()
            val args = Bundle()
            args.putString(ARG_NIK_ID, nik)
            fragment.arguments = args
            return fragment
        }
    }
}