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
import com.example.auditapp.adapter.TemaFormAdapter
import com.example.auditapp.config.ApiServices
import com.example.auditapp.config.NetworkConfig
import com.example.auditapp.databinding.FragmentViewTemaFormBinding
import com.example.auditapp.helper.SessionManager
import com.example.auditapp.model.TemaForm
import com.example.auditapp.model.TemaFormResponse
import com.example.auditapp.model.UpdateTemaFormResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ViewTemaFormFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener, TemaFormAdapter.OnItemClickListener {
    private var _binding: FragmentViewTemaFormBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var apiServices: ApiServices
    private val listTemaForm = mutableListOf<TemaForm>()
    private lateinit var temaFormAdapter: TemaFormAdapter
    private var formId: Int? = null
    companion object {
        private const val ARG_FORM_ID = "formId"
        fun newInstance(formId: Int?): Fragment {
            val fragment = ViewTemaFormFragment()
            val args = Bundle()
            args.putInt(ARG_FORM_ID, formId?: 0)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            formId = it.getInt(ARG_FORM_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewTemaFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())
        apiServices = NetworkConfig().getServices()
        val swipeRefresh = binding.swipeRefreshLayout
        swipeRefresh.setOnRefreshListener(this)

        setupRecylerView()
        loadDataFromApi(formId?: 0)

        binding.backBtn.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnTambahTema.setOnClickListener {
            val fragment = TambahTemaFormFragment.newInstance(formId?: 0)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDetailClick(dataTemaForm: TemaForm) {
        val temaFormId = dataTemaForm.id
        if (temaFormId != null) {
            val fragment = ViewVariabelFormFragment.newInstance(temaFormId)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onEditClick(dataTemaForm: TemaForm) {
        val fragment = EditTemaFormFragment.newInstance(dataTemaForm.id)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun setupRecylerView() {
        temaFormAdapter = TemaFormAdapter(listTemaForm, this)
        binding.rvTema.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = temaFormAdapter
        }
    }

    override fun onRefresh() {
        loadDataFromApi(formId?: 0)
        binding.swipeRefreshLayout.isRefreshing = false
    }

    private fun loadDataFromApi(formId: Int) {
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }
        apiServices.getTemaForm("Bearer $token", formId).enqueue(object :
            Callback<TemaFormResponse> {
            override fun onResponse(
                call: Call<TemaFormResponse>,
                response: Response<TemaFormResponse>
            ) {
                if (response.isSuccessful) {
                    val responseData = response.body()
                    Log.d("ViewTemaFormFragment", "Response: $responseData")
                    responseData?.data?.let { temaForms ->
                        listTemaForm.clear()
                        listTemaForm.addAll(temaForms)
                        temaFormAdapter.notifyDataSetChanged()
                    }
                } else {
                    Toast.makeText(requireContext(), "Gagal mengambil data tema form", Toast.LENGTH_SHORT).show()
                    Log.e("ViewTemaFormFragment", "Gagal mengambil data tema form: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<TemaFormResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("ViewTemaFormFragment", "Network Error: ${t.message}")
            }
        })
    }

    override fun onDeleteClick(dataTemaForm: TemaForm) {
        val temaFormId = dataTemaForm.id
        if (temaFormId != null) {
            AlertDialog.Builder(requireContext())
                .setTitle("Hapus Tema Form")
                .setMessage("Apakah Anda yakin ingin menghapus tema form ini?")
                .setPositiveButton("Hapus") { _, _ -> deleteTemaForm(temaFormId, dataTemaForm) }
                .setNegativeButton("Batal", null)
                .show()
        } else {
            Toast.makeText(requireContext(), "ID tema form tidak valid", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteTemaForm(temaFormId: Int, dataTemaForm: TemaForm) {
        val token = sessionManager.getAuthToken()
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }
        apiServices.deleteTemaForm("Bearer $token", temaFormId).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    temaFormAdapter.deleteTemaForm(dataTemaForm)
                    temaFormAdapter.notifyDataSetChanged()
                    Toast.makeText(requireContext(), "Tema form berhasil dihapus", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Gagal menghapus tema form", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(requireContext(), "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}