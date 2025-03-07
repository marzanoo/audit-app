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
import com.example.auditapp.model.RegisterRequest
import com.example.auditapp.model.RegisterResponse
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

        registerBtn.setOnClickListener {
            registerUser()
        }

        findViewById<TextView>(R.id.loginTV).setOnClickListener {
            toLogin()
        }
    }

    private fun toLogin() {
        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun registerUser() {
        val registerRequest = RegisterRequest(
            nik = nikET.text.toString(),
            name = fullnameET.text.toString(),
            username = usnET.text.toString(),
            email = emailET.text.toString(),
            password = passwordET.text.toString()
        )

        if (registerRequest.nik.isEmpty() || registerRequest.name.isEmpty() || registerRequest.username.isEmpty() || registerRequest.email.isEmpty() || registerRequest.password.isEmpty()) {
            // Tampilkan pesan kesalahan jika ada field yang kosong
            Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
            return
        }

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
                    Toast.makeText(this@RegisterActivity, response.body()?.message ?: "Registrasi Berhasil", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@RegisterActivity, VerifikasiAkunActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    val errorMessage = response.errorBody()?.string()

                    // Cek jika NIK tidak ditemukan
                    if (errorMessage?.contains("NIK tidak ditemukan") == true) {
                        Toast.makeText(this@RegisterActivity, "Anda tidak terdaftar dalam karyawan Wahana", Toast.LENGTH_SHORT).show()
                    } else if (errorMessage?.contains("Email atau username sudah digunakan") == true) {
                        Toast.makeText(this@RegisterActivity, "Email atau username sudah digunakan!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@RegisterActivity, "Gagal: $errorMessage", Toast.LENGTH_LONG).show()
                    }

                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                Toast.makeText(this@RegisterActivity, "Terjadi Kesalahan: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

}