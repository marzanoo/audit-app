package com.example.auditapp.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.auditapp.R
import com.example.auditapp.config.ApiServices
import com.example.auditapp.config.NetworkConfig
import com.example.auditapp.databinding.FragmentKonfigurasiBinding
import com.example.auditapp.helper.SessionManager
import com.example.auditapp.model.AreaResponse
import com.example.auditapp.model.LantaiResponse
import com.example.auditapp.model.VariabelFormResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class KonfigurasiFragment : Fragment() {
    private var _binding: FragmentKonfigurasiBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKonfigurasiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getTotalLantai()
        getTotalArea()
        getTotalVariabel()

        binding.btnNextLantai.setOnClickListener {
            val fragment = ViewLantaiFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        binding.btnNextArea.setOnClickListener {
            val fragment = ViewAreaFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        binding.btnNextForm.setOnClickListener {
            val fragment = ViewFormFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun getTotalVariabel(): Int {
        val apiServices = NetworkConfig().getServices()
        val sessionManager = SessionManager(requireContext())
        val token = sessionManager.getAuthToken()
        apiServices.getTotalVariabel("Bearer $token").enqueue(object : Callback<VariabelFormResponse> {
            override fun onResponse(
                call: Call<VariabelFormResponse>,
                response: Response<VariabelFormResponse>
            ) {
                if (response.isSuccessful) {
                    val variabelResponse = response.body()
                    val totalVariabel = variabelResponse?.total ?: 0
                    binding.totalVariabel.text = totalVariabel.toString()
                }
            }

            override fun onFailure(call: Call<VariabelFormResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
                Log.e("KonfigurasiFragment", "Fetching Data Error: ${t.message}")
            }
        })
        return 0
    }

    private fun getTotalLantai(): Int {
        val apiServices = NetworkConfig().getServices()
        val sessionManager = SessionManager(requireContext())
        val token = sessionManager.getAuthToken()
        apiServices.getTotalLantai("Bearer $token").enqueue(object : Callback<LantaiResponse> {
            override fun onResponse(
                call: Call<LantaiResponse>,
                response: Response<LantaiResponse>
            ) {
                if (response.isSuccessful) {
                    val lantaiResponse = response.body()
                    val totalLantai = lantaiResponse?.total_lantai ?: 0
                    binding.totalLantai.text = totalLantai.toString()
                }
            }

            override fun onFailure(call: Call<LantaiResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
                Log.e("KonfigurasiFragment", "Fetching Data Error: ${t.message}")
            }
        })
        return 0
    }

    private fun getTotalArea(): Int {
        val apiServices = NetworkConfig().getServices()
        val sessionManager = SessionManager(requireContext())
        val token = sessionManager.getAuthToken()
        apiServices.getTotalArea("Bearer $token").enqueue(object : Callback<AreaResponse> {
            override fun onResponse(
                call: Call<AreaResponse>,
                response: Response<AreaResponse>
            ) {
                if (response.isSuccessful) {
                    val areaResponse = response.body()
                    val totalArea = areaResponse?.total ?: 0
                    binding.totalArea.text = totalArea.toString()
                }
            }

            override fun onFailure(call: Call<AreaResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
                Log.e("KonfigurasiFragment", "Fetching Data Error: ${t.message}")
            }
        })
        return 0
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}