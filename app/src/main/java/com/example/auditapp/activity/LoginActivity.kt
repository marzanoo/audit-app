package com.example.auditapp.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.auditapp.R
import com.example.auditapp.config.NetworkConfig
import com.example.auditapp.helper.SessionManager
import com.example.auditapp.model.LoginRequest
import com.example.auditapp.model.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val sessionManager = SessionManager(this)

        usernameEditText = findViewById(R.id.usnET)
        passwordEditText = findViewById(R.id.passET)
        loginButton = findViewById(R.id.loginBtn)

        findViewById<TextView>(R.id.registerTV).setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }

        findViewById<TextView>(R.id.forgotTV).setOnClickListener {
            val intent = Intent(this, ForgotPasswordEmailActivity::class.java)
            startActivity(intent)
            finish()
        }

        if (!sessionManager.isTokenExpired()) {
            Log.d("LoginActivity", "Token not expired, navigating to MainActivity")
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val deviceId = DeviceUtils.getDeviceId(this)

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Tolong isi username dan password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val loginRequest = LoginRequest(username, password, deviceId)
            val apiservices = NetworkConfig().getServices()

            apiservices.login(loginRequest).enqueue(object : Callback<LoginResponse> {
                override fun onResponse(
                    call: Call<LoginResponse>,
                    response: Response<LoginResponse>
                ) {
                    if (response.isSuccessful) {
                        val loginResponse = response.body()
                        Log.d("LoginActivity", "Login response: $loginResponse")
                        if (loginResponse?.success == true && loginResponse.access_token != null) {
                            if (loginResponse.nik.isNullOrEmpty()) {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Login gagal: NIK tidak tersedia di server",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.e("LoginActivity", "NIK is null or empty in response")
                                return
                            }
                            sessionManager.saveAuthToken(
                                id = loginResponse.id ?: 0,
                                name = loginResponse.name ?: "",
                                email = loginResponse.email ?: "",
                                token = loginResponse.access_token,
                                expiresAt = loginResponse.expires_at ?: "",
                                role = loginResponse.role ?: 0,
                                nik = loginResponse.nik ?: ""
                            )
                            Log.d("LoginActivity", "Saved NIK: ${sessionManager.getNIK()}")
                            Toast.makeText(this@LoginActivity, "Login berhasil", Toast.LENGTH_SHORT)
                                .show()
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(
                                this@LoginActivity,
                                "Login gagal: ${loginResponse?.message ?: "Kesalahan tidak diketahui"}",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.e("LoginActivity", "Login failed: ${loginResponse?.message}")
                        }
                    } else {
                        val errorMessage = when (response.code()) {
                            401 -> "Username atau password salah"
                            403 -> {
                                val errorBody = response.errorBody()?.string()
                                if (errorBody?.contains("Email belum diverifikasi") == true) {
                                    "Email belum diverifikasi"
                                } else if (errorBody?.contains("Akun ini hanya bisa digunakan di perangkat") == true) {
                                    "Perangkat tidak diizinkan. Reset perangkat diperlukan."
                                } else {
                                    "Akses ditolak: ${response.message()}"
                                }
                            }

                            else -> "Login gagal: ${response.message()}"
                        }
                        Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_SHORT).show()
                        Log.e(
                            "LoginActivity",
                            "Response failed: ${response.code()} - ${
                                response.errorBody()?.string()
                            }"
                        )
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Kesalahan jaringan: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("LoginActivity", "Network failure: ${t.stackTraceToString()}")
                }
            })
        }
    }
}

object DeviceUtils {
    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }
}