package com.example.auditapp.activity

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.auditapp.R
import com.example.auditapp.config.ApiServices
import com.example.auditapp.config.NetworkConfig
import com.example.auditapp.databinding.FragmentFormAuditorBinding
import com.example.auditapp.helper.SessionManager
import com.example.auditapp.model.Area
import com.example.auditapp.model.AreaResponse
import com.example.auditapp.model.AuditAnswerItem
import com.example.auditapp.model.AuditAnswerResponse
import com.example.auditapp.model.AuditAnswerResponseUpdate
import com.example.auditapp.model.Karyawan
import com.example.auditapp.model.PicArea
import com.example.auditapp.model.PicAreaResponse
import com.example.auditapp.model.UserResponseGetById
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class FormAuditorFragment : Fragment() {
    private var _binding: FragmentFormAuditorBinding? = null
    private val binding get() = _binding!!
    private var userId: Int = 0
    private lateinit var sessionManager: SessionManager
    private lateinit var apiServices: ApiServices

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFormAuditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        apiServices = NetworkConfig().getServices()
        getUserById()
        getArea()
        getPicArea()

        binding.backBtn.setOnClickListener {
            navigateToHomeAuditor()
        }

        binding.btnBatal.setOnClickListener {
            navigateToHomeAuditor()
        }

        binding.btnMulai.setOnClickListener {
            mulaiAudit()
        }

        binding.etTanggal.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog =
                DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                    val formattedDate = "${selectedYear}-${selectedMonth + 1}-${selectedDay}"
                    binding.etTanggal.setText(formattedDate)
                }, year, month, day)

            datePickerDialog.show()
        }
    }

    private fun mulaiAudit() {
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        val auditorName = userId
        val areaSelected = binding.spinArea.selectedItem as? Area
        val tanggal = binding.etTanggal.text.toString().trim()
        val picArea = binding.spinPicArea.selectedItem as? PicArea

        val areaId = areaSelected?.id ?: 0
        val picId = picArea?.id ?: 0
        if (auditorName == 0 || areaId == 0 || tanggal.isEmpty()) {
            Toast.makeText(requireContext(), "Mohon untuk isi semua kolom", Toast.LENGTH_SHORT)
                .show()
            return
        }

        val auditAnswer = AuditAnswerItem(
            tanggal = tanggal,
            auditorId = auditorName,
            areaId = areaId,
            pic_area = picId
        )

        apiServices.createAuditAnswer("Bearer $token", auditAnswer).enqueue(object :
            Callback<AuditAnswerResponseUpdate> {
            override fun onResponse(
                call: Call<AuditAnswerResponseUpdate>,
                response: Response<AuditAnswerResponseUpdate>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Toast.makeText(requireContext(), "Audit berhasil dimulai", Toast.LENGTH_SHORT)
                        .show()
                    navigateToIsiFormAuditor(
                        dataAuditAnswer = responseBody?.auditAnswer ?: AuditAnswerItem()
                    )
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Gagal mulai audit: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e(
                        "FormAuditorFragment",
                        "Gagal mulai audit: ${response.errorBody()?.string()}"
                    )
                }
            }

            override fun onFailure(call: Call<AuditAnswerResponseUpdate>, t: Throwable) {
                t.printStackTrace() // Cetak error di log
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun navigateToIsiFormAuditor(dataAuditAnswer: AuditAnswerItem) {
        Log.d("FormAuditorFragment", "Navigasi ke IsiFormAuditorFragment dipanggil")
        val fragment = IsiFormAuditorFragment.newInstance(dataAuditAnswer.id ?: 0)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun getPicArea() {
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        apiServices.getPicArea("Bearer $token").enqueue(object : Callback<PicAreaResponse> {
            override fun onResponse(
                call: Call<PicAreaResponse>,
                response: Response<PicAreaResponse>
            ) {
                if (response.isSuccessful) {
                    val picAreaList = response.body()?.data ?: emptyList()
                    // Create a new list with a placeholder item
                    val modifiedPicAreaList = mutableListOf<PicArea>().apply {
                        add(PicArea(id = null, karyawan = null)) // Placeholder item
                        addAll(picAreaList)
                    }
                    // Create custom adapter
                    val adapter = object : ArrayAdapter<PicArea>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        modifiedPicAreaList
                    ) {
                        override fun getView(
                            position: Int,
                            convertView: View?,
                            parent: ViewGroup
                        ): View {
                            val view = super.getView(position, convertView, parent)
                            if (position == 0) {
                                // Customize hint appearance (optional)
                                (view as TextView).setTextColor(requireContext().getColor(android.R.color.darker_gray))
                            }
                            return view
                        }

                        override fun getDropDownView(
                            position: Int,
                            convertView: View?,
                            parent: ViewGroup
                        ): View {
                            val view = super.getDropDownView(position, convertView, parent)
                            // Hide the hint in the dropdown
                            if (position == 0) {
                                view.visibility = View.GONE
                            }
                            return view
                        }

                        override fun isEnabled(position: Int): Boolean {
                            // Disable the hint item to prevent selection
                            return position != 0
                        }
                    }
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinPicArea.adapter = adapter
                    binding.spinPicArea.setSelection(0) // Set default to hint
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Gagal mengambil data PIC Area: ${response.errorBody()?.string()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<PicAreaResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun getArea() {
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }
        apiServices.getArea("Bearer $token").enqueue(object : Callback<AreaResponse> {
            override fun onResponse(call: Call<AreaResponse>, response: Response<AreaResponse>) {
                if (response.isSuccessful) {
                    val areaList = response.body()?.data ?: emptyList()
                    // Create a new list with a placeholder item
                    val modifiedAreaList = mutableListOf<Area>().apply {
                        add(Area(id = 0, area = "Pilih Area")) // Placeholder item
                        addAll(areaList)
                    }
                    // Create custom adapter
                    val adapter = object : ArrayAdapter<Area>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        modifiedAreaList
                    ) {
                        override fun getView(
                            position: Int,
                            convertView: View?,
                            parent: ViewGroup
                        ): View {
                            val view = super.getView(position, convertView, parent)
                            if (position == 0) {
                                // Customize hint appearance (optional)
                                (view as TextView).setTextColor(requireContext().getColor(android.R.color.darker_gray))
                                (view as TextView).text = "Pilih Area" // Override to match hint
                            }
                            return view
                        }

                        override fun getDropDownView(
                            position: Int,
                            convertView: View?,
                            parent: ViewGroup
                        ): View {
                            val view = super.getDropDownView(position, convertView, parent)
                            // Hide the hint in the dropdown
                            if (position == 0) {
                                view.visibility = View.GONE
                            }
                            return view
                        }

                        override fun isEnabled(position: Int): Boolean {
                            // Disable the hint item to prevent selection
                            return position != 0
                        }
                    }
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinArea.adapter = adapter
                    binding.spinArea.setSelection(0) // Set default to hint
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Gagal mengambil data Area: ${response.errorBody()?.string()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<AreaResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun navigateToHomeAuditor() {
        val homeAuditorFragment = HomeAuditorFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, homeAuditorFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun getUserById() {
        userId = sessionManager.getUserId()
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }
        apiServices.getUserById("Bearer $token", userId).enqueue(object :
            Callback<UserResponseGetById> {
            override fun onResponse(
                call: Call<UserResponseGetById>,
                response: Response<UserResponseGetById>
            ) {
                if (response.isSuccessful) {
                    val userResponse = response.body()
                    binding.edtNama.setText(userResponse?.data?.name)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Gagal mengambil data user",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }

            override fun onFailure(call: Call<UserResponseGetById>, t: Throwable) {
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}