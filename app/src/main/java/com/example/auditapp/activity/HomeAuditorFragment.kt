package com.example.auditapp.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.auditapp.R
import com.example.auditapp.config.NetworkConfig
import com.example.auditapp.databinding.FragmentHomeAuditorBinding
import com.example.auditapp.helper.SessionManager
import com.example.auditapp.model.AreaResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeAuditorFragment : Fragment() {
    private var _binding: FragmentHomeAuditorBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeAuditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            sessionManager = SessionManager(requireContext())
            val role = sessionManager.getUserRole()
            val name = sessionManager.getName()

            Log.d(name, "Nama Pengguna: $name")

            binding.textViewRole.text = when (role) {
                1 -> "Role: Admin"
                2 -> "Role: Steerco"
                3 -> "Role: Auditor"
                else -> "Role: Unknown"
            }

            binding.boxFormAudit.setOnClickListener {
                val fragment = ListFormAuditorFragment()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }

            binding.textViewFullname.text = name
            getTotalArea()
        } catch (e: Exception) {
            Log.e("HomeAuditorFragment", "Error setting role text", e)
        }
    }

    private fun getTotalArea(): Int {
        val apiServices = NetworkConfig().getServices()
        val token = sessionManager.getAuthToken()
        apiServices.getTotalArea("Bearer $token").enqueue(object : Callback<AreaResponse> {
            override fun onResponse(call: Call<AreaResponse>, response: Response<AreaResponse>) {
                if (response.isSuccessful) {
                    val areaResponse = response.body()
                    val totalArea = areaResponse?.total ?: 0
                    binding.textTotalArea.text = totalArea.toString()
                } else {
                    Toast.makeText(requireContext(), "Fetching Data Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AreaResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
                Log.e("HomeAuditorFragment", "Fetching Data Error: ${t.message}")
            }
        })
        return 0
    }
}