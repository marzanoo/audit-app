package com.example.auditapp.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.auditapp.R
import com.example.auditapp.config.NetworkConfig
import com.example.auditapp.model.OtpRequest
import com.example.auditapp.model.OtpResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotPasswordEmailActivity : AppCompatActivity() {
    private lateinit var emailEditText: EditText
    private lateinit var kirimButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password_email)

        emailEditText = findViewById(R.id.emailET)
        kirimButton = findViewById(R.id.kirimBtn)

        kirimButton.setOnClickListener {
            val email = emailEditText.text.toString()
            if (email.isEmpty()) {
                emailEditText.error = "Email harus diisi"
            }
            val request = OtpRequest(email)
            val apiServices = NetworkConfig().getServices()

            apiServices.sendResetLink(request).enqueue(object : Callback<OtpResponse> {
                override fun onResponse(call: Call<OtpResponse>, response: Response<OtpResponse>) {
                    if (response.isSuccessful) {
                        val otpResponse = response.body()
                        val otpMessage = otpResponse?.message
                        Toast.makeText(this@ForgotPasswordEmailActivity, otpMessage, Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@ForgotPasswordEmailActivity, VerifikasiOtpResetPasswordActivity::class.java)
                        intent.putExtra("email", email)
                        startActivity(intent)
                    } else {
                        val errorMessage = response.errorBody()?.string()
                        Toast.makeText(this@ForgotPasswordEmailActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<OtpResponse>, t: Throwable) {
                    Toast.makeText(this@ForgotPasswordEmailActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}