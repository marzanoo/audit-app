package com.example.auditapp.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.example.auditapp.R
import com.example.auditapp.config.ApiServices
import com.example.auditapp.config.NetworkConfig
import com.example.auditapp.databinding.FragmentBendaharaFinesPaymentBinding
import com.example.auditapp.helper.SessionManager
import com.example.auditapp.model.KaryawanFinesResponse
import com.example.auditapp.model.PaymentRequest
import com.example.auditapp.model.PaymentResponse

import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BendaharaFinesPaymentFragment : Fragment() {
    private var _binding: FragmentBendaharaFinesPaymentBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var apiServices: ApiServices
    private val client = OkHttpClient()

    private lateinit var namaAdapter: ArrayAdapter<String>
    private val namaList = mutableListOf<String>()

    private var karyawanMap = mutableMapOf<String, KaryawanFinesResponse>()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBendaharaFinesPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        apiServices = NetworkConfig().getServices()

        setupAutoComplete()
        binding.btnBayar.setOnClickListener {
            val nama = binding.edtNama.text.toString().trim()
            val amount = binding.edtAmount.text.toString().trim()

            if (nama.isEmpty()) {
                Toast.makeText(requireContext(), "Nama tidak boleh kosong", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            if (amount.isEmpty()) {
                Toast.makeText(requireContext(), "Jumlah tidak boleh kosong", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
            val paymentData = PaymentRequest(
                name = nama,
                amount = amount.toInt()
            )
            submitPayment(paymentData)
        }

        binding.backBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnBatal.setOnClickListener {
            binding.edtNama.text.clear()
            binding.edtAmount.text.clear()
            binding.tvSisaDenda.visibility = View.GONE
        }
    }

    private fun submitPayment(paymentData: PaymentRequest) {
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(
                requireContext(),
                "Token tidak tersedia. Silakan login ulang.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        apiServices.submitPaymentFines("Bearer $token", paymentData).enqueue(object :
            Callback<PaymentResponse> {
            override fun onResponse(
                call: Call<PaymentResponse>,
                response: Response<PaymentResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val msg = response.body()!!.message
                    binding.tvSisaDenda.setTextColor(
                        resources.getColor(android.R.color.holo_green_dark, null)
                    )
                    binding.tvSisaDenda.text = "✅ $msg"
                    binding.tvSisaDenda.visibility = View.VISIBLE
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Pembayaran gagal"
                    binding.tvSisaDenda.setTextColor(
                        resources.getColor(android.R.color.holo_red_dark, null)
                    )
                    binding.tvSisaDenda.text = "⚠ $errorMsg"
                    binding.tvSisaDenda.visibility = View.VISIBLE
                }
            }

            override fun onFailure(call: Call<PaymentResponse>, t: Throwable) {
                binding.tvSisaDenda.setTextColor(
                    resources.getColor(android.R.color.holo_red_dark, null)
                )
                binding.tvSisaDenda.text = "⚠ Gagal koneksi: ${t.message}"
                binding.tvSisaDenda.visibility = View.VISIBLE
            }
        })
    }

    private fun setupAutoComplete() {
        namaAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            namaList
        )
        binding.edtNama.setAdapter(namaAdapter)

        binding.edtNama.threshold = 1
        binding.edtNama.setOnItemClickListener { _, _, position, _ ->
            val selectedName = namaAdapter.getItem(position)
            val karyawan = karyawanMap[selectedName]
            if (karyawan != null) {
                binding.tvSisaDenda.text = "Sisa Denda: ${karyawan.total_due}"
                binding.tvSisaDenda.visibility = View.VISIBLE
            }
        }

        binding.edtNama.addTextChangedListener {
            val query = it.toString()
            if (query.length >= 1) {
                apiServices.searchKaryawan(query).enqueue(object :
                    Callback<List<KaryawanFinesResponse>> {
                    override fun onResponse(
                        call: Call<List<KaryawanFinesResponse>>,
                        response: Response<List<KaryawanFinesResponse>>
                    ) {
                        if (response.isSuccessful && response.body() != null) {
                            val result = response.body()!!

                            namaList.clear()
                            karyawanMap.clear()

                            result.forEach { k ->
                                namaList.add(k.emp_name)
                                karyawanMap[k.emp_name] = k
                            }

                            namaAdapter = ArrayAdapter(
                                requireContext(),
                                android.R.layout.simple_dropdown_item_1line,
                                namaList
                            )
                            binding.edtNama.setAdapter(namaAdapter)
                            namaAdapter.notifyDataSetChanged()

                            // tampilkan denda default (ambil yang pertama misalnya)
                            if (result.isNotEmpty()) {
                                binding.tvSisaDenda.text = "Sisa Denda: ${result[0].total_due}"
                                binding.tvSisaDenda.visibility = View.VISIBLE
                            }

                        } else {
                            // Handle the case where the response is not successful
                            binding.tvSisaDenda.text = "⚠ Error fetching data"
                            binding.tvSisaDenda.visibility = View.VISIBLE
                        }
                    }

                    override fun onFailure(call: Call<List<KaryawanFinesResponse>>, t: Throwable) {
                        // Handle the failure case
                        binding.tvSisaDenda.text = "⚠ Gagal memuat nama: ${t.message}"
                        binding.tvSisaDenda.visibility = View.VISIBLE
                    }
                })
            }
        }
    }
}