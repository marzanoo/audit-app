package com.example.auditapp.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.auditapp.R
import com.example.auditapp.databinding.FragmentHomeAdminBinding
import com.example.auditapp.helper.SessionManager

class HomeAdminFragment : Fragment() {
    private var _binding: FragmentHomeAdminBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            // Ambil role dari SharedPreferences
            val sessionManager = SessionManager(requireContext())
            val role = sessionManager.getUserRole()
            val name = sessionManager.getName()

            Log.d(name, "Nama Pengguna: $name")

            // Menampilkan role di TextView
            binding.textViewRole.text = when (role) {
                1 -> "Role: Admin"
                2 -> "Role: Steerco"
                3 -> "Role: Auditor"
                else -> "Role: Unknown"
            }

            binding.boxAuditOffice.setOnClickListener {
                val fragment = AuditOfficeAdminFragment()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }

            binding.boxKonfigurasi.setOnClickListener {
                val fragment = KonfigurasiFragment()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }

            binding.textViewFullname.text = name

        } catch (e: Exception) {
            Log.e("HomeAdminFragment", "Error setting role text", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
