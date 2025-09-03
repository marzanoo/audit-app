package com.example.auditapp.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.auditapp.config.ApiServices
import com.example.auditapp.config.NetworkConfig
import com.example.auditapp.databinding.FragmentTambahPicAreaBinding
import com.example.auditapp.helper.SessionManager
import com.example.auditapp.model.Karyawan
import com.example.auditapp.model.KaryawanResponse
import com.example.auditapp.model.PicArea
import com.example.auditapp.model.UpdatePicAreaResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TambahPicAreaFragment : Fragment() {
    private var _binding: FragmentTambahPicAreaBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var apiServices: ApiServices
    private var karyawanList = mutableListOf<Karyawan>()
    private var selectedKaryawanId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTambahPicAreaBinding.inflate(inflater, container, false)
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

        binding.btnTambah.setOnClickListener {
            addPicArea()
        }

        fetchAllKaryawan()
    }

    private fun fetchAllKaryawan() {
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        apiServices.getCandidatePic("Bearer $token").enqueue(object : Callback<KaryawanResponse> {
            override fun onResponse(
                call: Call<KaryawanResponse>,
                response: Response<KaryawanResponse>
            ) {
                if (response.isSuccessful) {
                    val responseData = response.body()
                    responseData?.data?.let { list ->
                        karyawanList.clear()
                        karyawanList.addAll(list)

                        val namaList = list.map {
                            val name = it.emp_name ?: "Unknown"
                            val dept = it.dept ?: "-"
                            "$name - $dept"
                        }
                        val adapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            namaList
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.spinnerPicArea.adapter = adapter
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Gagal memuat data karyawan",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<KaryawanResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addPicArea() {
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedPosition = binding.spinnerPicArea.selectedItemPosition
        if (selectedPosition < 0 || selectedPosition >= karyawanList.size) {
            Toast.makeText(requireContext(), "Pilih Nama PIC Area", Toast.LENGTH_SHORT).show()
            return
        }

        selectedKaryawanId = karyawanList[selectedPosition].emp_id?.toInt()
        val namaTerpilih = karyawanList[selectedPosition].emp_name ?: "Unknown"
        if (selectedKaryawanId == null) {
            Toast.makeText(requireContext(), "ID Karyawan tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        val picArea = PicArea(pic_id = selectedKaryawanId.toString())

        apiServices.createPicArea("Bearer $token", picArea).enqueue(object :
            Callback<UpdatePicAreaResponse> {
            override fun onResponse(
                call: Call<UpdatePicAreaResponse>,
                response: Response<UpdatePicAreaResponse>
            ) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Berhasil menambahkan PIC Area: $namaTerpilih",
                        Toast.LENGTH_SHORT
                    ).show()
                    parentFragmentManager.popBackStack()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Gagal menambahkan PIC Area: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<UpdatePicAreaResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
