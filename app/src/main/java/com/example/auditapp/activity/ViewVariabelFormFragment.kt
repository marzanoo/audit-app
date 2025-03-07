package com.example.auditapp.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.auditapp.R
import com.example.auditapp.adapter.VariabelFormAdapter
import com.example.auditapp.config.ApiServices
import com.example.auditapp.config.NetworkConfig
import com.example.auditapp.databinding.FragmentViewVariabelFormBinding
import com.example.auditapp.helper.SessionManager
import com.example.auditapp.model.VariabelForm
import com.example.auditapp.model.VariabelFormResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ViewVariabelFormFragment : Fragment(), VariabelFormAdapter.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    private var _binding: FragmentViewVariabelFormBinding? = null
    private val binding get() = _binding!!
    private var temaFormId: Int = 0
    private lateinit var sessionManager: SessionManager
    private lateinit var apiServices: ApiServices
    private val listVariabelForm = mutableListOf<VariabelForm>()
    private lateinit var variabelFormAdapter: VariabelFormAdapter
    companion object {
        private const val ARG_TEMAFORM_ID = "temaFormId"
        fun newInstance(temaFormId: Int): Fragment {
            val fragment = ViewVariabelFormFragment()
            val args = Bundle()
            args.putInt(ARG_TEMAFORM_ID, temaFormId)
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
        _binding = FragmentViewVariabelFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        apiServices = NetworkConfig().getServices()
        val swipeRefresh = binding.swipeRefreshLayout
        swipeRefresh.setOnRefreshListener(this)

        binding.backBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnTambahVariabel.setOnClickListener {
            val fragment = TambahVariabelFormFragment.newInstance(temaFormId)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        setupRecylerView()
        loadDataFromApi(temaFormId)
    }

    private fun loadDataFromApi(temaFormId: Int) {
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }
        apiServices.getVariabelForm("Bearer $token", temaFormId).enqueue(object :
            Callback<VariabelFormResponse> {
            override fun onResponse(
                call: Call<VariabelFormResponse>,
                response: Response<VariabelFormResponse>
            ) {
                if (response.isSuccessful) {
                    val responseData = response.body()
                    Log.d("ViewVariabelFormFragment", "Response: ${responseData}")
                    responseData?.data?.let { variabelForms ->
                        listVariabelForm.clear()
                        listVariabelForm.addAll(variabelForms)
                        variabelFormAdapter.notifyDataSetChanged()
                    }
                } else {
                    Toast.makeText(requireContext(), "Gagal mengambil data variabel form", Toast.LENGTH_SHORT).show()
                    Log.e("ViewVariabelFormFragment", "Gagal mengambil data variabel form: ${response.errorBody()?.string()}" )
                }
            }

            override fun onFailure(call: Call<VariabelFormResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("ViewTemaFormFragment", "Network Error: ${t.message}")
            }
        })
    }

    override fun onDeleteClick(dataVariabelForm: VariabelForm) {
        val variabelFormId = dataVariabelForm.id
        if (variabelFormId != null) {
            AlertDialog.Builder(requireContext())
                .setTitle("Hapus Variabel Form")
                .setMessage("Apakah Anda yakin ingin menghapus variabel form ini?")
                .setPositiveButton("Hapus") { _, _ -> deleteVariabelForm(variabelFormId, dataVariabelForm) }
                .setNegativeButton("Batal", null)
                .show()
        } else {
            Toast.makeText(requireContext(), "ID variabel form tidak valid", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteVariabelForm(variabelFormId: Int, dataVariabelForm: VariabelForm) {
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak valid", Toast.LENGTH_SHORT).show()
            return
        }
        apiServices.deleteVariabelForm("Bearer $token", variabelFormId).enqueue(object :
            Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    variabelFormAdapter.deleteVariabelForm(dataVariabelForm)
                    variabelFormAdapter.notifyDataSetChanged()
                    Toast.makeText(requireContext(), "Variabel form berhasil dihapus", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Gagal menghapus variabel form", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onEditClick(dataVariabelForm: VariabelForm) {
        TODO("Not yet implemented")
    }

    private fun setupRecylerView() {
        variabelFormAdapter = VariabelFormAdapter(listVariabelForm, this)
        binding.rvVariabel.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = variabelFormAdapter
        }
    }

    override fun onRefresh() {
        loadDataFromApi(temaFormId)
        binding.swipeRefreshLayout.isRefreshing = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}