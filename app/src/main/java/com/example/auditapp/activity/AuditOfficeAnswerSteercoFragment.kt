package com.example.auditapp.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.auditapp.config.ApiServices
import com.example.auditapp.databinding.FragmentAuditOfficeAnswerSreercoBinding
import com.example.auditapp.helper.SessionManager

class AuditOfficeAnswerSteercoFragment : Fragment(){
    private var _binding: FragmentAuditOfficeAnswerSreercoBinding? = null
    private val binding get() = _binding!!
    private var auditAnswerId: Int = 0
    private lateinit var sessionManager: SessionManager
    private lateinit var apiServices: ApiServices

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            auditAnswerId = it.getInt(ARG_AUDITANSWER_ID, 0)
        }
    }

    companion object {
        private const val ARG_AUDITANSWER_ID = "auditAnswerId"
        fun newInstance(auditAnswerId: Int?): Fragment {
            val fragment = AuditOfficeAnswerSteercoFragment()
            val args = Bundle()
            args.putInt(ARG_AUDITANSWER_ID, auditAnswerId?: 0)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuditOfficeAnswerSreercoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}