package com.example.auditapp.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.auditapp.databinding.FragmentHomeSteercoBinding
import com.example.auditapp.helper.SessionManager

class HomeSteercoFragment : Fragment() {
    private var _binding: FragmentHomeSteercoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeSteercoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            val sessionManager = SessionManager(requireContext())
            val role = sessionManager.getUserRole()
            val name = sessionManager.getName()

            Log.d(name, "Nama Pengguna: $name")

            binding.textViewRole.text = when (role) {
                1 -> "Role: Admin"
                2 -> "Role: Steering Committee"
                3 -> "Role: Auditor"
                else -> "Role: Unknown"
            }

            binding.textViewFullname.text = name

        } catch (e: Exception) {
            Log.e("HomeSteercoFragment", "Error setting role text", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}