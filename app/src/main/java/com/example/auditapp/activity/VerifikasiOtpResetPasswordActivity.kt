package com.example.auditapp.activity

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.auditapp.R
import com.example.auditapp.config.NetworkConfig
import com.example.auditapp.model.OtpRequest
import com.example.auditapp.model.OtpResponse
import com.example.auditapp.model.VerifikasiOtpRequest
import com.example.auditapp.model.VerifikasiOtpResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VerifikasiOtpResetPasswordActivity : AppCompatActivity() {
    private lateinit var resendButton: Button
    private lateinit var countdownText: TextView
    private lateinit var otpEditText: EditText
    private lateinit var verifikasiButton: Button
    private var countDownTimer: CountDownTimer? = null
    private var countdownTime = 60000L // 1 minutes in milliseconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verifikasi_otp_reset_password)

        resendButton = findViewById(R.id.resendButton)
        countdownText = findViewById(R.id.countdownText)
        otpEditText = findViewById(R.id.otpET)
        verifikasiButton = findViewById(R.id.verifikasiBtn)

        val email = intent.getStringExtra("email")
        verifikasiButton.setOnClickListener {
            val otp = otpEditText.text.toString()
            if (otp.isNotEmpty() || otp.length != 6) {
                val verifikasiRequest = VerifikasiOtpRequest(email.toString(), otp)
                val apiServices = NetworkConfig().getServices()
                apiServices.verifyResetOtp(verifikasiRequest).enqueue(object :
                    Callback<VerifikasiOtpResponse> {
                    override fun onResponse(
                        call: Call<VerifikasiOtpResponse>,
                        response: Response<VerifikasiOtpResponse>
                    ) {
                        if (response.isSuccessful) {
                            Toast.makeText(
                                this@VerifikasiOtpResetPasswordActivity,
                                "OTP Verifikasi Berhasil",
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(this@VerifikasiOtpResetPasswordActivity, ResetPasswordActivity::class.java)
                            intent.putExtra("email", email)
                            startActivity(intent)
                            finish()
                        } else {
                            val errorMessage = response.errorBody()?.string()
                            if (errorMessage?.contains("OTP salah atau kadaluarsa") == true) {
                                Toast.makeText(this@VerifikasiOtpResetPasswordActivity, "OTP salah atau kadaluarsa", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@VerifikasiOtpResetPasswordActivity, "Verifikasi gagal: $errorMessage", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    override fun onFailure(call: Call<VerifikasiOtpResponse>, t: Throwable) {
                        Toast.makeText(this@VerifikasiOtpResetPasswordActivity, "Verifikasi gagal: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }

        resendButton.setOnClickListener {
            email?.let {
                resendOtp(it)
                startCountdown()
            } ?: Toast.makeText(this, "Email tidak valid", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startCountdown() {
        resendButton.isEnabled = false
        countDownTimer = object : CountDownTimer(countdownTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                countdownText.text = "$secondsRemaining detik"
            }

            override fun onFinish() {
                resendButton.isEnabled = true
                countdownText.text = "Silahkan tekan tombol di atas"
            }
        }.start()
    }

    private fun resendOtp(email: String) {
        val apiServices = NetworkConfig().getServices()
        apiServices.resendOtpReset(OtpRequest(email)).enqueue(object : Callback<OtpResponse> {
            override fun onResponse(call: Call<OtpResponse>, response: Response<OtpResponse>) {
                if (response.isSuccessful) {
                    Log.d("RESEND_OTP", "Verifikasi berhasil dikirim ke $email")
                    Toast.makeText(this@VerifikasiOtpResetPasswordActivity, "Verifikasi berhasil dikirim ke $email", Toast.LENGTH_SHORT).show()
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("RESEND_OTP", "Gagal mengirim Email: $errorBody")
                    Toast.makeText(this@VerifikasiOtpResetPasswordActivity, "Gagal mengirim Email", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<OtpResponse>, t: Throwable) {
                Log.e("RESEND_OTP", "Terjadi kesalahan: ${t.message}", t)
                Toast.makeText(this@VerifikasiOtpResetPasswordActivity, "Terjadi kesalahan: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}