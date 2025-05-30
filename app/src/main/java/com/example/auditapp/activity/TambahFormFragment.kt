package com.example.auditapp.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.auditapp.config.NetworkConfig
import com.example.auditapp.databinding.FragmentTambahFormBinding
import com.example.auditapp.helper.SessionManager
import com.example.auditapp.model.Form
import com.example.auditapp.model.UpdateFormResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TambahFormFragment : Fragment() {
    private var _binding: FragmentTambahFormBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTambahFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnBatal.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnTambah.setOnClickListener {
            createForm()
        }
    }

    private fun createForm() {
        val sessionManager = SessionManager(requireContext())
        val token = sessionManager.getAuthToken()
        val kategori = binding.etKategori.text.toString()

        if (kategori.isEmpty()) {
            Toast.makeText(requireContext(), "Mohon untuk isi semua kolom", Toast.LENGTH_SHORT).show()
            return
        }

        val form = Form(
            id = null,
            kategori = kategori,
            created_at = null,
            updated_at = null
        )
        val apiServices = NetworkConfig().getServices()
        apiServices.createForm("Bearer $token", form).enqueue(object :
            Callback<UpdateFormResponse> {
            override fun onResponse(
                call: Call<UpdateFormResponse>,
                response: Response<UpdateFormResponse>
            ) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Kategori Berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                } else {
                    Toast.makeText(requireContext(), "Gagal Menambahkan Kategori", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UpdateFormResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Network Error; ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}