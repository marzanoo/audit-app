package com.example.auditapp.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.auditapp.R
import com.example.auditapp.config.NetworkConfig
import com.example.auditapp.databinding.ActivityMainBinding
import com.example.auditapp.helper.SessionManager
import com.example.auditapp.model.LogoutResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var role: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        findViewById<TextView>(R.id.logoutTextView).setOnClickListener {
            logout()
        }

//        val sharedPref = getSharedPreferences("USER_PREF", Context.MODE_PRIVATE)
//        role = sharedPref.getInt("ROLE", 0)

        val sessionManager = SessionManager(this)
        role = sessionManager.getUserRole()
        Log.d("MainActivity", "User role: $role")
        setupBottomNavigation(role)
    }

    private fun setupBottomNavigation(role: Int) {
        binding.bottomNavView.menu.clear()

        val defaultFragment: Fragment = when (role) {
            1 -> {
                menuInflater.inflate(R.menu.bottom_nav_menu_admin, binding.bottomNavView.menu)
                HomeAdminFragment()
            }
            2 -> {
                menuInflater.inflate(R.menu.bottom_nav_menu_steerco, binding.bottomNavView.menu)
                HomeSteercoFragment()
            }
            3 -> {
                menuInflater.inflate(R.menu.bottom_nav_menu_auditor, binding.bottomNavView.menu)
                HomeAuditorFragment()
            }
            else -> {
                Toast.makeText(this, "Role tidak dikenali", Toast.LENGTH_SHORT).show()
                return
            }
        }

        loadFragment(defaultFragment)

        binding.bottomNavView.setOnItemSelectedListener { menuItem ->
            val fragment: Fragment? = when (role) {
                1 -> when (menuItem.itemId) {
                    R.id.navigation_home -> HomeAdminFragment()
                    R.id.navigation_konfigurasi -> KonfigurasiFragment()
                    R.id.navigation_office -> AuditOfficeAdminFragment()
                    else -> null
                }
                2 -> when (menuItem.itemId) {
                    R.id.navigation_home -> HomeSteercoFragment()
                    R.id.navigation_office -> AuditOfficeSteercoFragment()
                    else -> null
                }
                3 -> when (menuItem.itemId) {
                    R.id.navigation_home -> HomeAuditorFragment()
                    R.id.navigation_form -> FormAuditorFragment()
                    else -> null
                }
                else -> null
            }

            if (fragment != null) {
                loadFragment(fragment)
                true
            } else {
                false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        Log.d("MainActivity", "Fragment ${fragment::class.java.simpleName} dipanggil")
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun logout() {
        val sessionManager = SessionManager(this)
        val token = sessionManager.getAuthToken()

        if (token.isNullOrEmpty()) {
            Toast.makeText(this, "Token tidak tersedia", Toast.LENGTH_SHORT).show()
            return
        }

        val apiServices = NetworkConfig().getServices()
        apiServices.logout("Bearer $token").enqueue(object : Callback<LogoutResponse> {
            override fun onResponse(
                call: Call<LogoutResponse>,
                response: Response<LogoutResponse>
            ) {
                if (response.isSuccessful) {
                    Toast.makeText(this@MainActivity, "Logout Berhasil", Toast.LENGTH_SHORT).show()
                    // Hapus semua data user dari SharedPreferences
                    sessionManager.clearAuthToken()

                    val intent = Intent(this@MainActivity, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Logout Gagal: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<LogoutResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }
}
