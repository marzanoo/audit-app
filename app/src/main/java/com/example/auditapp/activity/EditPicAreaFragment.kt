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
import com.example.auditapp.databinding.FragmentEditPicAreaBinding
import com.example.auditapp.helper.SessionManager
import com.example.auditapp.model.Karyawan
import com.example.auditapp.model.KaryawanResponse
import com.example.auditapp.model.PicArea
import com.example.auditapp.model.UpdatePicAreaResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditPicAreaFragment : Fragment() {
    private var _binding: FragmentEditPicAreaBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var apiServices: ApiServices
    private var picAreaId: Int = 0
    private var selectedKaryawanId: Int? = null
    private var karyawanList = mutableListOf<Karyawan>()

    companion object {
        private const val ARG_PIC_AREA_ID = "picAreaId"
        fun newInstance(picAreaId: Int?): EditPicAreaFragment {
            val fragment = EditPicAreaFragment()
            val args = Bundle()
            args.putInt(ARG_PIC_AREA_ID, picAreaId ?: 0)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            picAreaId = it.getInt(ARG_PIC_AREA_ID, 0)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditPicAreaBinding.inflate(inflater, container, false)
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
            updatePicArea()
        }

        fetchAllKaryawan()
    }

    private fun fetchAllKaryawan() {
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token is missing", Toast.LENGTH_SHORT).show()
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
                        binding.spinnerPICArea.adapter = adapter

                        // Fetch current PIC Area data to set the selected item
                        fetchPicAreaData(picAreaId)
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to load data", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<KaryawanResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchPicAreaData(id: Int) {
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token is missing", Toast.LENGTH_SHORT).show()
            return
        }
        apiServices.getPicAreaById("Bearer $token", id).enqueue(object :
            Callback<UpdatePicAreaResponse> {
            override fun onResponse(
                call: Call<UpdatePicAreaResponse>,
                response: Response<UpdatePicAreaResponse>
            ) {
                if (response.isSuccessful) {
                    val responseData = response.body()
                    responseData?.let {
                        val currentName = it.data?.karyawan?.emp_name ?: "Unknown"

                        val position = karyawanList.indexOfFirst { karyawan ->
                            karyawan.emp_name == currentName
                        }
                        if (position >= 0) {
                            binding.spinnerPICArea.setSelection(position)
                            selectedKaryawanId = karyawanList[position].emp_id?.toInt()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to load data", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            override fun onFailure(call: Call<UpdatePicAreaResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updatePicArea() {
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token is missing", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedPosition = binding.spinnerPICArea.selectedItemPosition
        if (selectedPosition < 0 || selectedPosition >= karyawanList.size) {
            Toast.makeText(requireContext(), "Pilih nama PIC Area", Toast.LENGTH_SHORT)
                .show()
            return
        }
        selectedKaryawanId = karyawanList[selectedPosition].emp_id?.toInt()
        val namaTerpilih = karyawanList[selectedPosition].emp_name ?: "Unknown"

        if (selectedKaryawanId == null) {
            Toast.makeText(requireContext(), "Invalid Karyawan ID", Toast.LENGTH_SHORT).show()
            return
        }
        val picArea = PicArea(pic_id = selectedKaryawanId.toString())

        apiServices.updatePicArea("Bearer $token", picAreaId, picArea).enqueue(object :
            Callback<UpdatePicAreaResponse> {
            override fun onResponse(
                call: Call<UpdatePicAreaResponse>,
                response: Response<UpdatePicAreaResponse>
            ) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "PIC Area updated to $namaTerpilih",
                        Toast.LENGTH_SHORT
                    ).show()
                    parentFragmentManager.popBackStack()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Failed to update PIC Area",
                        Toast.LENGTH_SHORT
                    )
                        .show()
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
