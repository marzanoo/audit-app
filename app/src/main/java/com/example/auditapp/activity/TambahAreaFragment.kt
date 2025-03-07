package com.example.auditapp.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.auditapp.R
import com.example.auditapp.config.NetworkConfig
import com.example.auditapp.databinding.FragmentTambahAreaBinding
import com.example.auditapp.helper.SessionManager
import com.example.auditapp.model.Area
import com.example.auditapp.model.AreaResponse
import com.example.auditapp.model.Karyawan
import com.example.auditapp.model.KaryawanResponse
import com.example.auditapp.model.Lantai
import com.example.auditapp.model.LantaiResponse
import com.example.auditapp.model.UpdateAreaResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TambahAreaFragment : Fragment() {
    private var _binding: FragmentTambahAreaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTambahAreaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.backBtn.setOnClickListener {
            navigateToViewArea()
        }

        binding.btnBatal.setOnClickListener {
            navigateToViewArea()
        }

        binding.btnTambah.setOnClickListener {
            createArea()
        }

        loadLantai()
        loadKaryawanPic()
    }

    private fun createArea() {
        val sessionManager = SessionManager(requireContext())
        val token = sessionManager.getAuthToken()
        val areaName = binding.etArea.text.toString()
        val lantaiSelected = binding.spinLantai.selectedItem as? Lantai
        val picSelected = binding.spinPIC.selectedItem as? Karyawan

        // Pastikan objek tidak null sebelum mengambil ID
        val lantaiId = lantaiSelected?.id ?: 0
        val picId = picSelected?.emp_id ?: ""

        if (areaName.isEmpty() || lantaiId == 0 || picId.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Mohon untuk isi semua kolom",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val area = Area(
            id = null,
            area = areaName,
            lantai_id = lantaiId,
            pic_area = picId,
            created_at = null,
            updated_at = null
        )

        val apiServices = NetworkConfig().getServices()
        apiServices.createArea("Bearer $token", area).enqueue(object : Callback<UpdateAreaResponse> {
            override fun onResponse(call: Call<UpdateAreaResponse>, response: Response<UpdateAreaResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Area berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                    navigateToViewArea()
                } else {
                    Toast.makeText(requireContext(), "Gagal menambahkan area", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UpdateAreaResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("TambahAreaFragment", "Fetching Data Error: ${t.message}")
            }
        })
    }

    private fun navigateToViewArea() {
        val fragment = ViewAreaFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun loadKaryawanPic() {
        val apiServices = NetworkConfig().getServices()
        val sessionManager = SessionManager(requireContext())
        val token = sessionManager.getAuthToken()
        apiServices.getKaryawanPic("Bearer $token").enqueue(object : Callback<KaryawanResponse> {
            override fun onResponse(
                call: Call<KaryawanResponse>,
                response: Response<KaryawanResponse>
            ) {
                if (response.isSuccessful) {
                    val karyawanList = response.body()?.data ?: emptyList()
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        karyawanList
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinPIC.adapter = adapter
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Gagal mengambil data Karyawan: ${response.errorBody()?.string()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<KaryawanResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
                Log.e("TambahAreaFragment", "Fetching Data Error: ${t.message}")
            }
        })
    }

    private fun loadLantai() {
        val apiServices = NetworkConfig().getServices()
        val sessionManager = SessionManager(requireContext())
        val token = sessionManager.getAuthToken()
        apiServices.getLantai("Bearer $token").enqueue(object : Callback<LantaiResponse> {
            override fun onResponse(
                call: Call<LantaiResponse>,
                response: Response<LantaiResponse>
            ) {
                if (response.isSuccessful) {
                    val lantaiList = response.body()?.lantaiList ?: emptyList()
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        lantaiList
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinLantai.adapter = adapter
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Gagal mengambil data Lantai: ${response.errorBody()?.string()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<LantaiResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
                Log.e("TambahAreaFragment", "Fetching Data Error: ${t.message}")
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}