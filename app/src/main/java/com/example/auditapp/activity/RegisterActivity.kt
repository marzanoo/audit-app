package com.example.auditapp.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.auditapp.R
import com.example.auditapp.config.NetworkConfig
import com.example.auditapp.model.KaryawanResponse
import com.example.auditapp.model.RegisterRequest
import com.example.auditapp.model.RegisterResponse
import com.example.auditapp.model.SingleKaryawanResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {
    private lateinit var fullnameET: EditText
    private lateinit var nikET: EditText
    private lateinit var usnET: EditText
    private lateinit var emailET: EditText
    private lateinit var passwordET: EditText
    private lateinit var registerBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        fullnameET = findViewById(R.id.fullnameET)
        nikET = findViewById(R.id.nikET)
        usnET = findViewById(R.id.usnET)
        emailET = findViewById(R.id.emailET)
        passwordET = findViewById(R.id.passET)
        registerBtn = findViewById(R.id.registerBtn)

        // Always keep fullnameET disabled
        fullnameET.isEnabled = false

        nikET.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val nik = nikET.text.toString()
                if (nik.isNotEmpty()) {
                    fetchKaryawanName(nik)
                } else {
                    fullnameET.setText("")
                }
            }
        }

        registerBtn.setOnClickListener {
            registerUser()
        }

        findViewById<TextView>(R.id.loginTV).setOnClickListener {
            toLogin()
        }
    }

    private fun fetchKaryawanName(nik: String) {
        val apiServices = NetworkConfig().getServices()
        apiServices.getKaryawanByNik(nik).enqueue(object : Callback<SingleKaryawanResponse> {
            override fun onResponse(
                call: Call<SingleKaryawanResponse>,
                response: Response<SingleKaryawanResponse>
            ) {
                if (response.isSuccessful && response.body() != null && !response.body()!!.emp_name.isNullOrEmpty()) {
                    fullnameET.setText(response.body()!!.emp_name)
                } else {
                    fullnameET.setText("NIK tidak terdaftar, silahkan hubungi admin")
                }
            }

            override fun onFailure(call: Call<SingleKaryawanResponse>, t: Throwable) {
                fullnameET.setText("Terjadi kesalahan: ${t.message}")
            }
        })
    }

    private fun toLogin() {
        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun registerUser() {
        val nameValue = fullnameET.text.toString()
        val nikValue = nikET.text.toString()
        val usernameValue = usnET.text.toString()
        val emailValue = emailET.text.toString()
        val passwordValue = passwordET.text.toString()

        // Prevent registration if fullnameET contains the error message
        if (nikValue.isEmpty() || nameValue.isEmpty() || nameValue.contains("NIK tidak terdaftar") ||
            usernameValue.isEmpty() || emailValue.isEmpty() || passwordValue.isEmpty()
        ) {
            Toast.makeText(this, "Semua field harus diisi dengan benar", Toast.LENGTH_SHORT).show()
            return
        }

        val registerRequest = RegisterRequest(
            nik = nikValue,
            name = nameValue,
            username = usernameValue,
            email = emailValue,
            password = passwordValue
        )

        val apiServices = NetworkConfig().getServices()
        apiServices.register(registerRequest).enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(
                call: Call<RegisterResponse>,
                response: Response<RegisterResponse>
            ) {
                if (response.isSuccessful) {
                    val sharedPref = getSharedPreferences("RegisterPref", MODE_PRIVATE)
                    val registerResponse = response.body()
                    with(sharedPref.edit()) {
                        putString("email", registerResponse?.email)
                        apply()
                    }
                    Toast.makeText(
                        this@RegisterActivity,
                        response.body()?.message ?: "Registrasi Berhasil",
                        Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(this@RegisterActivity, VerifikasiAkunActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val errorMessage = response.errorBody()?.string()
                    if (errorMessage?.contains("NIK tidak ditemukan") == true) {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Anda tidak terdaftar dalam karyawan Wahana",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (errorMessage?.contains("Email atau username sudah digunakan") == true) {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Email atau username sudah digunakan!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Gagal: $errorMessage",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                Toast.makeText(
                    this@RegisterActivity,
                    "Terjadi Kesalahan: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}