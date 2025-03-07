package com.example.auditapp.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.auditapp.config.ApiServices
import com.example.auditapp.config.NetworkConfig
import com.example.auditapp.databinding.FragmentEditTemaFormBinding
import com.example.auditapp.helper.SessionManager
import com.example.auditapp.model.TemaForm
import com.example.auditapp.model.UpdateTemaFormResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditTemaFormFragment : Fragment() {
    private var _binding: FragmentEditTemaFormBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var apiServices: ApiServices

    private var temaFormId: Int = 0

    companion object {
        private const val ARG_TEMAFORM_ID = "temaFormId"
        fun newInstance(temaFormId: Int?): Fragment {
            val fragment = EditTemaFormFragment()
            val args = Bundle()
            args.putInt(ARG_TEMAFORM_ID, temaFormId?: 0)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            temaFormId = it.getInt(ARG_TEMAFORM_ID, 0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditTemaFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        apiServices = NetworkConfig().getServices()

        binding.backBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnBatal.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnUbah.setOnClickListener {
            updateTemaForm()
        }
        getTemaFormById(temaFormId)
    }

    private fun getTemaFormById(id: Int) {
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }
        apiServices.getTemaFormById("Bearer $token", id).enqueue(object :
            Callback<UpdateTemaFormResponse> {
            override fun onResponse(
                call: Call<UpdateTemaFormResponse>,
                response: Response<UpdateTemaFormResponse>
            ) {
                if (response.isSuccessful) {
                    val responseData = response.body()
                    binding.etTema.setText(responseData?.data?.tema ?: "")
                    Log.d("EditTemaFormFragment", "Response: ${responseData}")
                } else {
                    Toast.makeText(requireContext(), "Gagal mengambil data tema form", Toast.LENGTH_SHORT).show()
                    Log.e("EditTemaFormFragment", "Gagal mengambil data tema form: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<UpdateTemaFormResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("EditTemaFormFragment", "Network Error: ${t.message}")
            }
        })
    }

    private fun updateTemaForm() {
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }
        val tema = binding.etTema.text.toString().trim()
        if (tema.isEmpty()) {
            Toast.makeText(requireContext(), "Mohon untuk isi semua kolom", Toast.LENGTH_SHORT).show()
            return
        }

        apiServices.updateTemaForm("Bearer $token", temaFormId, TemaForm(tema = tema)).enqueue(object : Callback<UpdateTemaFormResponse> {
            override fun onResponse(call: Call<UpdateTemaFormResponse>, response: Response<UpdateTemaFormResponse>) {
                if (response.isSuccessful) {
                    val responseData = response.body()
                    Toast.makeText(requireContext(), responseData?.message ?: "Tema form berhasil diubah", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                } else {
                    Toast.makeText(requireContext(), "Gagal mengubah tema form", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UpdateTemaFormResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}