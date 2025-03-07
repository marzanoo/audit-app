package com.example.auditapp.activity

import android.R
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.auditapp.config.ApiServices
import com.example.auditapp.config.NetworkConfig
import com.example.auditapp.databinding.FragmentEditAreaBinding
import com.example.auditapp.helper.SessionManager
import com.example.auditapp.model.Area
import com.example.auditapp.model.AreaResponse
import com.example.auditapp.model.Karyawan
import com.example.auditapp.model.KaryawanResponse
import com.example.auditapp.model.Lantai
import com.example.auditapp.model.LantaiResponse
import com.example.auditapp.model.SingleAreaResponse
import com.example.auditapp.model.UpdateAreaResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditAreaFragment : Fragment() {
    private var _binding: FragmentEditAreaBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var apiServices: ApiServices

    private var areaId: Int = 0
    private var selectedLantaiId: Int? = null
    private var selectedPICId: String? = null

    private val lantaiList = mutableListOf<Lantai>()
    private val karyawanList = mutableListOf<Karyawan>()

    companion object {
        private const val ARG_AREA_ID = "areaId"
        fun newInstance(areaId: Int?): Fragment {
            val fragment = EditAreaFragment()
            val args = Bundle()
            args.putInt(ARG_AREA_ID, areaId?: 0)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            areaId = it.getInt(ARG_AREA_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditAreaBinding.inflate(inflater, container, false)
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
            updateArea()
        }

        loadLantaiData()
        loadPICData()
        getAreaById(areaId)
    }

    private fun getAreaById(id: Int) {
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }
        apiServices.getAreaById("Bearer $token", id).enqueue(object : Callback<SingleAreaResponse> {
            override fun onResponse(call: Call<SingleAreaResponse>, response: Response<SingleAreaResponse>) {
                if (response.isSuccessful) {
                    val areaResponse = response.body()
                    val areaData = areaResponse?.data
                    if (areaData != null) {
                        populateAreaData(areaData)
                        Log.d("EditAreaFragment", "Populating data: lantai_id=${areaData.lantai_id}, pic_area=${areaData.pic_area}")
                    } else {
                        Toast.makeText(requireContext(), "Data area tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Gagal mengambil data area", Toast.LENGTH_SHORT).show()
                    Log.e("EditAreaFragment", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<SingleAreaResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("EditAreaFragment", "Network Error: ${t.message}")
            }
        })
    }

    private fun populateAreaData(areaData: Area) {
        binding.etArea.setText(areaData.area)

        Log.d("EditAreaFragment", "Populating data: lantai_id=${areaData.lantai_id}, pic_area=${areaData.pic_area}")

        // Set selected Lantai in spinner
        areaData.lantai_id?.let { lantaiId ->
            selectedLantaiId = lantaiId
            Log.d("EditAreaFragment", "Lantai list size: ${lantaiList.size}")
            val lantaiPosition = lantaiList.indexOfFirst { it.id == lantaiId }
            Log.d("EditAreaFragment", "Lantai position: $lantaiPosition")
            if (lantaiPosition != -1) {
                binding.spinLantai.setSelection(lantaiPosition)
            }
        }

        // Set selected PIC in spinner
        areaData.pic_area?.let { picId ->
            selectedPICId = picId
            Log.d("EditAreaFragment", "PIC list size: ${karyawanList.size}")
            val picPosition = karyawanList.indexOfFirst { it.emp_id == picId }
            Log.d("EditAreaFragment", "PIC position: $picPosition")
            if (picPosition != -1) {
                binding.spinPIC.setSelection(picPosition)
            }
        }
    }

    private fun loadLantaiData() {
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }
        apiServices.getLantai("Bearer $token").enqueue(object : Callback<LantaiResponse> {
            override fun onResponse(
                call: Call<LantaiResponse>,
                response: Response<LantaiResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.lantaiList?.let {
                        lantaiList.clear()
                        lantaiList.addAll(it)
                        setupLantaiSpinner()

                        if (selectedLantaiId != null) {
                            val position = lantaiList.indexOfFirst { lantai -> lantai.id == selectedLantaiId }
                            if (position != -1) {
                                binding.spinLantai.setSelection(position)
                            }
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Gagal mengambil data lantai", Toast.LENGTH_SHORT).show()
                    Log.e("EditAreaFragment", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<LantaiResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("EditAreaFragment", "Network Error: ${t.message}")
            }
        })
    }

    private fun loadPICData() {
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }
        apiServices.getKaryawanPic("Bearer $token").enqueue(object : Callback<KaryawanResponse> {
            override fun onResponse(
                call: Call<KaryawanResponse>,
                response: Response<KaryawanResponse>
            ) {
                if (response.isSuccessful) {
                    response.body()?.data?.let {
                        karyawanList.clear()
                        karyawanList.addAll(it)
                        setupPICSpinner()

                        if (selectedPICId != null) {
                            val position = karyawanList.indexOfFirst { karyawan -> karyawan.emp_id == selectedPICId }
                            if (position != -1) {
                                binding.spinPIC.setSelection(position)
                            }
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Gagal mengambil data PIC", Toast.LENGTH_SHORT).show()
                    Log.e("EditAreaFragment", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<KaryawanResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("EditAreaFragment", "Network Error: ${t.message}")
            }
        })
    }

    private fun setupLantaiSpinner() {
        val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, lantaiList.map { it.lantai })
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spinLantai.adapter = adapter

        binding.spinLantai.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedLantaiId = lantaiList[position].id
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedLantaiId = null
            }
        }
    }

    private fun setupPICSpinner() {
        val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, karyawanList.map { it.emp_name })
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spinPIC.adapter = adapter

        binding.spinPIC.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedPICId = karyawanList[position].emp_id
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedPICId = null
            }
        }
    }

    private fun updateArea() {
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        val areanName = binding.etArea.text.toString().trim()
        if (areanName.isEmpty()) {
            Toast.makeText(requireContext(), "Mohon untuk isi semua kolom", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedLantaiId == null) {
            Toast.makeText(requireContext(), "Mohon untuk memilih lantai", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedPICId == null) {
            Toast.makeText(requireContext(), "Mohon untuk memilih PIC", Toast.LENGTH_SHORT).show()
            return
        }

        val area = Area(
            id = areaId,
            area = areanName,
            lantai_id = selectedLantaiId!!,
            pic_area = selectedPICId!!,
            created_at = null,
            updated_at = null
        )

        apiServices.updateArea("Bearer $token", areaId, area).enqueue(object :
            Callback<UpdateAreaResponse> {
            override fun onResponse(call: Call<UpdateAreaResponse>, response: Response<UpdateAreaResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Area berhasil diupdate", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                } else {
                    Toast.makeText(requireContext(), "Gagal memperbarui area", Toast.LENGTH_SHORT).show()
                    Log.e("EditAreaFragment", "Error: ${response.errorBody()?.string()}")
                }
            }
            override fun onFailure(call: Call<UpdateAreaResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("EditAreaFragment", "Network Error: ${t.message}")
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}