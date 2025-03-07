package com.example.auditapp.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.auditapp.R
import com.example.auditapp.config.NetworkConfig
import com.example.auditapp.databinding.FragmentTambahLantaiBinding
import com.example.auditapp.helper.SessionManager
import com.example.auditapp.model.Lantai
import com.example.auditapp.model.LantaiResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TambahLantaiFragment : Fragment() {
    private var _binding: FragmentTambahLantaiBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTambahLantaiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Tombol Kembali
        binding.backBtn.setOnClickListener {
            navigateToKonfigurasi()
        }

        // Tombol Batal
        binding.btnBatal.setOnClickListener {
            navigateToKonfigurasi()
        }

        // Tombol Tambah Lantai
        binding.btnTambah.setOnClickListener {
            createLantai()
        }
    }

    private fun createLantai() {
        val sessionManager = SessionManager(requireContext())
        val token = sessionManager.getAuthToken()
        val lantaiName = binding.etLantai.text.toString().trim()

        if (lantaiName.isEmpty()) {
            Toast.makeText(requireContext(), "Mohon untuk isi kolom lantai", Toast.LENGTH_SHORT).show()
            return
        }

        // Buat objek Lantai sesuai dengan model yang diharapkan
        val lantai = Lantai(
            id = null, // Biarkan null jika dibuat oleh server
            lantai = lantaiName.toIntOrNull(), // Pastikan ini sesuai tipe data
            created_at = null,
            updated_at = null
        )

        val apiServices = NetworkConfig().getServices()
        apiServices.createLantai("Bearer $token", lantai).enqueue(object : Callback<LantaiResponse> {
            override fun onResponse(call: Call<LantaiResponse>, response: Response<LantaiResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Lantai berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                    navigateToKonfigurasi()
                } else {
                    Toast.makeText(requireContext(), "Gagal menambahkan lantai!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LantaiResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun navigateToKonfigurasi() {
        val fragment = ViewLantaiFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
