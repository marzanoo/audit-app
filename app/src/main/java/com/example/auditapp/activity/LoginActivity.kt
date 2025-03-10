package com.example.auditapp.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
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

        if (!sessionManager.isTokenExpired()) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        loginButton.setOnClickListener{
            val username = usernameEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val deviceId = DeviceUtils.getDeviceId(this)

            if (username.isNotEmpty() && password.isNotEmpty()) {
                val loginRequest = LoginRequest(username, password, deviceId)
                val apiservices = NetworkConfig().getServices()

                apiservices.login(loginRequest).enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                        if (response.isSuccessful) {
                            val loginResponse = response.body()

                            if(loginResponse != null && loginResponse.access_token != null) {
                                val role = loginResponse.role ?: 0
                                val expiresAt = loginResponse.expires_at ?: ""
                                sessionManager.saveAuthToken(
                                    loginResponse.id ?: 0,
                                    loginResponse.name ?: "",
                                    loginResponse.email ?: "",
                                    loginResponse.access_token,
                                    expiresAt,
                                    role
                                )
                                Toast.makeText(this@LoginActivity, "Login berhasil", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this@LoginActivity, "Login gagal: ${loginResponse?.message ?: "Kesalahan tidak diketahui"}", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@LoginActivity, "Login gagal: ${response.message()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        Toast.makeText(this@LoginActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                Toast.makeText(this@LoginActivity, "Tolong isi username dan password", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

object DeviceUtils {
    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }
}