package com.example.auditapp.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.auditapp.R
import com.example.auditapp.config.NetworkConfig
import com.example.auditapp.model.ResetPasswordRequest
import com.example.auditapp.model.ResetPasswordResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ResetPasswordActivity : AppCompatActivity() {
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var resetButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        passwordEditText = findViewById(R.id.passET)
        confirmPasswordEditText = findViewById(R.id.passConfET)
        resetButton = findViewById(R.id.resetBtn)

        resetButton.setOnClickListener {
            val apiServices = NetworkConfig().getServices()
            val email = intent.getStringExtra("email")
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (password != confirmPassword) {
                confirmPasswordEditText.error = "Password dan Konfirmasi Password tidak cocok"
            }

            if (password.isNotEmpty() && confirmPassword.isNotEmpty() && password == confirmPassword) {
                val request = ResetPasswordRequest(email, password, confirmPassword)
                apiServices.resetPassword(request).enqueue(object :
                    Callback<ResetPasswordResponse> {
                    override fun onResponse(
                        call: Call<ResetPasswordResponse>,
                        response: Response<ResetPasswordResponse>
                    ) {
                        if (response.isSuccessful) {
                            val resetPasswordResponse = response.body()
                            val message = resetPasswordResponse?.message
                            Toast.makeText(this@ResetPasswordActivity, message, Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@ResetPasswordActivity, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            val errorMessage = response.errorBody()?.string()
                            Toast.makeText(this@ResetPasswordActivity, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<ResetPasswordResponse>, t: Throwable) {
                        Toast.makeText(this@ResetPasswordActivity, "Reset Password gagal: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }
}