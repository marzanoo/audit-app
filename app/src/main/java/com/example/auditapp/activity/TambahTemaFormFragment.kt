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
import com.example.auditapp.databinding.FragmentTambahTemaFormBinding
import com.example.auditapp.helper.SessionManager
import com.example.auditapp.model.TemaForm
import com.example.auditapp.model.UpdateTemaFormResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TambahTemaFormFragment : Fragment() {
    private var _binding: FragmentTambahTemaFormBinding? = null
    private val binding get() = _binding!!
    private var formId: Int = 0
    private lateinit var sessionManager: SessionManager
    private lateinit var apiServices: ApiServices

    companion object {
        private const val ARG_FORM_ID = "formId"
        fun newInstance(formId: Int): Fragment {
            val fragment = TambahTemaFormFragment()
            val args = Bundle()
            args.putInt(ARG_FORM_ID, formId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            formId = it.getInt(ARG_FORM_ID, 0)
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTambahTemaFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        apiServices = NetworkConfig().getServices()
        Log.d("TambahTemaFormFragment", "Form ID: $formId")

        binding.backBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnBatal.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnTambah.setOnClickListener {
            createTemaForm()
        }
    }

    private fun createTemaForm() {
        val token = sessionManager.getAuthToken()
        val temaForm = binding.etTema.text.toString()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }
        if (temaForm.isEmpty()) {
            Toast.makeText(requireContext(), "Mohon untuk isi semua kolom", Toast.LENGTH_SHORT).show()
            return
        }

        val temaFormRequest = TemaForm(
            id = null,
            form_id = formId,
            tema = temaForm,
            created_at = null,
            updated_at = null
        )
        apiServices.createTemaForm("Bearer $token", temaFormRequest).enqueue(object :
            Callback<UpdateTemaFormResponse> {
            override fun onResponse(
                call: Call<UpdateTemaFormResponse>,
                response: Response<UpdateTemaFormResponse>
            ) {
                if (response.isSuccessful) {
                    val responseData = response.body()
                    Toast.makeText(requireContext(), responseData?.message ?: "Tema form berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                } else {
                    val responseData = response.body()
                    Toast.makeText(requireContext(), responseData?.message ?: "Tema form gagal ditambahkan", Toast.LENGTH_SHORT).show()
                    Log.e("TambahTemaFormFragment", "Tema form gagal ditambahkan: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<UpdateTemaFormResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("TambahTemaFormFragment", "Network Error: ${t.message}")
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}