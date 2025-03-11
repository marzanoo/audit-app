package com.example.auditapp.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.auditapp.R
import com.example.auditapp.adapter.FormAnswerAdapter
import com.example.auditapp.databinding.FragmentIsiFormAuditorBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class IsiFormAuditorFragment : Fragment(), FormAnswerAdapter, SwipeRefreshLayout.OnRefreshListener {
    private var _binding: FragmentIsiFormAuditorBinding? = null
    private val binding get() = _binding!!
    private var auditAnswerId: Int = 0
    private lateinit var formAnswerAdapter: FormAnswerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIsiFormAuditorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onRefresh() {
        TODO("Not yet implemented")
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavView)
        bottomNav.visibility = View.GONE

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        val bottomNav = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavView)
        bottomNav.visibility = View.VISIBLE
    }

    companion object {
        private const val ARG_AUDITANSWER_ID = "auditAnswerId"
        fun newInstance(auditAnswerId: Int): Fragment {
            val fragment = IsiFormAuditorFragment()
            val args = Bundle()
            args.putInt(ARG_AUDITANSWER_ID, auditAnswerId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            auditAnswerId = it.getInt(ARG_AUDITANSWER_ID, 0)
        }
    }
}