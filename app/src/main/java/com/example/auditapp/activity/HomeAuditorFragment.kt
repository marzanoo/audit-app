package com.example.auditapp.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.auditapp.R
import com.example.auditapp.config.ApiServices
import com.example.auditapp.config.NetworkConfig
import com.example.auditapp.databinding.FragmentHomeAuditorBinding
import com.example.auditapp.helper.SessionManager
import com.example.auditapp.model.AreaResponse
import com.example.auditapp.model.AuditAnswerResponse
import com.example.auditapp.model.FinesResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeAuditorFragment : Fragment() {
    private var _binding: FragmentHomeAuditorBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var apiServices: ApiServices

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeAuditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun getTotalFines() {
        val empId = sessionManager.getNIK().toString()
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak valid", Toast.LENGTH_SHORT).show()
            Log.e("HomeAuditorFragment", "Token is null or empty")
            return
        }
        apiServices.getFines("Bearer $token", empId).enqueue(object : Callback<FinesResponse> {
            override fun onResponse(call: Call<FinesResponse>, response: Response<FinesResponse>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    binding.textViewTotalDenda.text = "Rp ${responseBody?.totalDue ?: 0}"
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Fetching Data Error: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(
                        "HomeAuditorFragment",
                        "Response failed: ${response.code()} - ${response.errorBody()?.string()}"
                    )
                }
            }

            override fun onFailure(call: Call<FinesResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
                Log.e("HomeAuditorFragment", "Network failure: ${t.stackTraceToString()}")
            }
        })

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            sessionManager = SessionManager(requireContext())
            apiServices = NetworkConfig().getServices()
            val role = sessionManager.getUserRole()
            val name = sessionManager.getName() ?: "Nama Tidak Tersedia"
            val nik = sessionManager.getNIK()

            Log.d("HomeAuditorFragment", "Nama Pengguna: $name, Role: $role, NIK: $nik")

            binding.textViewRole.text = when (role) {
                1 -> "Role: Admin"
                2 -> "Role: Steerco"
                3 -> "Role: Auditor"
                else -> "Role: Unknown"
            }

            val isBendahara = nik == "2011060104" // Adjust this condition as needed
            if (!isBendahara) {
                binding.boxFormDenda.visibility = View.GONE
                binding.spacer.visibility = View.GONE
                // Set boxTotalDenda width to match_parent to take full width
                val layoutParams = binding.boxTotalDenda.layoutParams as LinearLayout.LayoutParams
                layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT
                layoutParams.weight = 0f // Remove weight to prevent stretching
                binding.boxTotalDenda.layoutParams = layoutParams
            }

            binding.btnBayar.setOnClickListener {
                val fragment = BendaharaFinesPaymentFragment()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }
            getTotalFines()

            binding.boxFormAudit.setOnClickListener {
                val fragment = FormAuditorFragment()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }

            binding.textViewDetailDenda.setOnClickListener {
                onViewDetailDendaClick()
            }

            binding.textViewFullname.text = name
            getTotalArea()
            getTotalAudit()
        } catch (e: Exception) {
            Log.e("HomeAuditorFragment", "Error setting role text", e)
            Toast.makeText(
                requireContext(),
                "Terjadi kesalahan: ${e.message}",
                Toast.LENGTH_SHORT
            )
                .show()
        }
    }

    private fun onViewDetailDendaClick() {
        val nik = sessionManager.getNIK()
        Log.d("HomeAuditorFragment", "Retrieved NIK: $nik")
        if (nik.isNullOrEmpty()) {
            Toast.makeText(
                requireContext(),
                "NIK tidak tersedia. Silakan login ulang.",
                Toast.LENGTH_SHORT
            ).show()
            Log.e("HomeAuditorFragment", "NIK is null or empty, redirecting to LoginActivity")
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
            return
        }
        val fragment = AuditFinesFragment.newInstance(nik)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun getTotalAudit() {
        val token = sessionManager.getAuthToken()
        val userId = sessionManager.getUserId()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak valid", Toast.LENGTH_SHORT).show()
            Log.e("HomeAuditorFragment", "Token is null or empty")
            return
        }
        val apiServices = NetworkConfig().getServices()
        apiServices.getAuditAnswerAuditor("Bearer $token", userId).enqueue(object :
            Callback<AuditAnswerResponse> {
            override fun onResponse(
                call: Call<AuditAnswerResponse>,
                response: Response<AuditAnswerResponse>
            ) {
                if (response.isSuccessful) {
                    val auditAnswerResponse = response.body()
                    val totalAudit = auditAnswerResponse?.total ?: 0
                    binding.textTotalAudit.text = "$totalAudit Selesai"
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Fetching Data Error: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(
                        "HomeAuditorFragment",
                        "Response failed: ${response.code()} - ${response.errorBody()?.string()}"
                    )
                }
            }

            override fun onFailure(call: Call<AuditAnswerResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
                Log.e("HomeAuditorFragment", "Network failure: ${t.stackTraceToString()}")
            }
        })
    }

    private fun getTotalArea() {
        val apiServices = NetworkConfig().getServices()
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak valid", Toast.LENGTH_SHORT).show()
            Log.e("HomeAuditorFragment", "Token is null or empty")
            return
        }
        apiServices.getTotalArea("Bearer $token").enqueue(object : Callback<AreaResponse> {
            override fun onResponse(call: Call<AreaResponse>, response: Response<AreaResponse>) {
                if (response.isSuccessful) {
                    val areaResponse = response.body()
                    val totalArea = areaResponse?.total ?: 0
                    binding.textTotalArea.text = totalArea.toString()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Fetching Data Error: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(
                        "HomeAuditorFragment",
                        "Response failed: ${response.code()} - ${response.errorBody()?.string()}"
                    )
                }
            }

            override fun onFailure(call: Call<AreaResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
                Log.e("HomeAuditorFragment", "Network failure: ${t.stackTraceToString()}")
            }
        })
    }
}